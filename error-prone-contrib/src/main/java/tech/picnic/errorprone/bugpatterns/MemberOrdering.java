package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.fixes.SuggestedFixes.addSuppressWarnings;
import static com.google.errorprone.util.ASTHelpers.getStartPosition;
import static com.sun.source.tree.Tree.Kind.METHOD;
import static com.sun.source.tree.Tree.Kind.VARIABLE;
import static java.util.Comparator.comparing;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Position;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

@AutoService(BugChecker.class)
@BugPattern(
    summary = "Members should be ordered in a standard way.",
    link = BUG_PATTERNS_BASE_URL + "MemberOrdering",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
public class MemberOrdering extends BugChecker implements BugChecker.ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  /** A comparator that sorts members, constructors and methods in a standard order. */
  private static final Comparator<Tree> COMPARATOR =
      comparing(
              (Tree memberTree) -> {
                switch (memberTree.getKind()) {
                  case VARIABLE:
                    return 0;
                  case METHOD:
                    return 1;
                }
                throw new IllegalStateException("Unexpected kind: " + memberTree.getKind());
              })
          .thenComparing(
              (Tree memberTree) -> {
                switch (memberTree.getKind()) {
                  case VARIABLE:
                    return isStatic((JCVariableDecl) memberTree) ? 0 : 1;
                  case METHOD:
                    return isConstructor((JCMethodDecl) memberTree) ? 0 : 1;
                }
                throw new IllegalStateException("Unexpected kind: " + memberTree.getKind());
              });
  // XXX: Evaluate alternative implementation.
  /** A comparator that sorts members, constructors and methods in a standard order. */
  private static final Comparator<Tree> SQUASHED_COMPARATOR =
      Comparator.comparing(
          (Tree memberTree) -> {
            if (memberTree.getKind() == VARIABLE) {
              if (isStatic((JCVariableDecl) memberTree)) {
                // 1. static variables.
                return 1;
              } else {
                // 2. non-static variables.
                return 2;
              }
            }
            if (memberTree.getKind() == METHOD) {
              if (isConstructor((JCMethodDecl) memberTree)) {
                // 3. constructors.
                return 3;
              } else {
                // 4. methods.
                return 4;
              }
            }
            throw new IllegalStateException("Unexpected kind: " + memberTree.getKind());
          });

  /** Instantiates a new {@link MemberOrdering} instance. */
  public MemberOrdering() {
    super();
  }

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    var members =
        tree.getMembers().stream().filter(MemberOrdering::isHandled).collect(toImmutableList());
    var sortedMembers = members.stream().sorted(COMPARATOR).collect(toImmutableList());

    if (members.equals(sortedMembers)) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .addFix(addSuppressWarnings(state, canonicalName()))
        .addFix(swapMembersIncludingComments(members, sortedMembers, tree, state))
        .setMessage(
            "Members, constructors and methods should follow standard ordering. "
                + "The standard ordering is: static variables, non-static variables, "
                + "constructors and methods.")
        .build();
  }

  private static boolean isHandled(Tree tree) {
    return tree instanceof JCVariableDecl
        || (tree instanceof JCMethodDecl
            && !ASTHelpers.isGeneratedConstructor((JCMethodDecl) tree));
  }

  private static SuggestedFix swapMembersIncludingComments(
      ImmutableList<? extends Tree> members,
      ImmutableList<? extends Tree> sortedMembers,
      ClassTree classTree,
      VisitorState state) {
    var fix = SuggestedFix.builder();
    for (int i = 0; i < members.size(); i++) {
      Tree original = members.get(i);
      Tree correct = sortedMembers.get(i);
      // XXX: Technically not necessary, but avoids redundant replacements.
      if (!original.equals(correct)) {
        var replacement =
            Streams.concat(
                    getComments(classTree, correct, state),
                    Stream.of(state.getSourceForNode(correct)))
                .collect(Collectors.joining("\n"));
        fix.merge(replaceIncludingComments(classTree, original, replacement, state));
      }
    }
    return fix.build();
  }

  private static boolean isStatic(JCVariableDecl memberTree) {
    Set<Modifier> modifiers = memberTree.getModifiers().getFlags();
    return modifiers.contains(Modifier.STATIC);
  }

  private static boolean isConstructor(JCMethodDecl methodDecl) {
    // XXX: Using state.getName(...) would be better, but than we'd need to introduce `state` as a
    // parameter and that would mean we have to instantiate a COMPARATOR for every matchClass(...)
    // call, which seems excessive for a simple check like this.
    // That may also violate some kind of contract about comparators being stateless.
    return methodDecl.getName().toString().equals("<init>");
  }

  private static Stream<String> getComments(ClassTree classTree, Tree member, VisitorState state) {
    var previousMember = getPreviousMember(member, classTree).orElse(null);
    int startTokenization;
    if (previousMember != null) {
      startTokenization = state.getEndPosition(previousMember);
    } else if (state.getEndPosition(classTree.getModifiers()) == Position.NOPOS) {
      startTokenization = getStartPosition(classTree);
    } else {
      startTokenization = state.getEndPosition(classTree.getModifiers());
    }
    List<ErrorProneToken> tokens =
        state.getOffsetTokens(startTokenization, state.getEndPosition(member));
    if (previousMember == null) {
      // todo: check if this is redundant.
      tokens = getTokensAfterOpeningBrace(tokens);
    }
    if (tokens.isEmpty()) {
      return Stream.empty();
    }
    return tokens.get(0).comments().stream().map(c -> c.getText());
  }

  private static List<ErrorProneToken> getTokensAfterOpeningBrace(List<ErrorProneToken> tokens) {
    for (int i = 0; i < tokens.size() - 1; ++i) {
      if (tokens.get(i).kind() == Tokens.TokenKind.LBRACE) {
        return tokens.subList(i + 1, tokens.size());
      }
    }
    return ImmutableList.of();
  }

  public static SuggestedFix replaceIncludingComments(
      ClassTree classTree, Tree member, String replacement, VisitorState state) {
    Tree previousMember = getPreviousMember(member, classTree).orElse(null);
    int startTokenization;
    // from here copy of `SuggestedFixes#replaceIncludingComments`.
    if (previousMember != null) {
      startTokenization = state.getEndPosition(previousMember);
    } else if (state.getEndPosition(classTree.getModifiers()) == Position.NOPOS) {
      startTokenization = getStartPosition(classTree);
    } else {
      startTokenization = state.getEndPosition(classTree.getModifiers());
    }
    List<ErrorProneToken> tokens =
        state.getOffsetTokens(startTokenization, state.getEndPosition(member));
    if (previousMember == null) {
      tokens = getTokensAfterOpeningBrace(tokens);
    }
    if (tokens.isEmpty()) {
      return SuggestedFix.replace(member, replacement);
    }
    if (tokens.get(0).comments().isEmpty()) {
      return SuggestedFix.replace(tokens.get(0).pos(), state.getEndPosition(member), replacement);
    }
    ImmutableList<Tokens.Comment> comments =
        ImmutableList.sortedCopyOf(
            Comparator.<Tokens.Comment>comparingInt(c -> c.getSourcePos(0)).reversed(),
            tokens.get(0).comments());
    int startPos = getStartPosition(member);
    // This can happen for desugared expressions like `int a, b;`.
    if (startPos < startTokenization) {
      return SuggestedFix.emptyFix();
    }
    // Delete backwards for comments which are not separated from our target by a blank line.
    CharSequence sourceCode = state.getSourceCode();
    for (Tokens.Comment comment : comments) {
      int endOfCommentPos = comment.getSourcePos(comment.getText().length() - 1);
      CharSequence stringBetweenComments = sourceCode.subSequence(endOfCommentPos, startPos);
      if (stringBetweenComments.chars().filter(c -> c == '\n').count() > 1) {
        break;
      }
      startPos = comment.getSourcePos(0);
    }
    return SuggestedFix.replace(startPos, state.getEndPosition(member), replacement);
  }

  private static Optional<Tree> getPreviousMember(Tree tree, ClassTree classTree) {
    Tree previousMember = null;
    for (Tree member : classTree.getMembers()) {
      if (member instanceof MethodTree && ASTHelpers.isGeneratedConstructor((MethodTree) member)) {
        continue;
      }
      if (member.equals(tree)) {
        break;
      }
      previousMember = member;
    }
    return Optional.ofNullable(previousMember);
  }
}
