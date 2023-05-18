package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.fixes.SuggestedFixes.addSuppressWarnings;
import static com.google.errorprone.util.ASTHelpers.getStartPosition;
import static com.google.errorprone.util.ASTHelpers.getSymbol;
import static com.google.errorprone.util.ASTHelpers.isGeneratedConstructor;
import static com.sun.tools.javac.parser.Tokens.TokenKind.LBRACE;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
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
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

/** A {@link BugChecker} that flags classes with non-standard member ordering. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Members should be ordered in a standard way.",
    explanation =
        "Members should be ordered in a standard way, which is: "
            + "static member variables, non-static member variables, constructors and methods.",
    link = BUG_PATTERNS_BASE_URL + "MemberOrdering",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
public final class MemberOrdering extends BugChecker implements BugChecker.ClassTreeMatcher {
  private static final long serialVersionUID = 1L;
  /** A comparator that sorts variable and method (incl. constructors) in a standard order. */
  private static final Comparator<Tree> COMPARATOR =
      comparing(
          (Tree memberTree) -> {
            switch (memberTree.getKind()) {
              case VARIABLE:
                return isStatic((JCVariableDecl) memberTree) ? 1 : 2;
              case METHOD:
                return isConstructor((JCMethodDecl) memberTree) ? 3 : 4;
              default:
                throw new IllegalStateException("Unexpected kind: " + memberTree.getKind());
            }
          });

  /** Instantiates a new {@link MemberOrdering} instance. */
  public MemberOrdering() {}

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
        || (tree instanceof JCMethodDecl && !isGeneratedConstructor((JCMethodDecl) tree));
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
      // xxx: Technically not necessary, but avoids redundant replacements.
      if (!original.equals(correct)) {
        var replacement =
            Stream.concat(
                    getComments(classTree, correct, state).map(Tokens.Comment::getText),
                    Stream.of(state.getSourceForNode(correct)))
                .collect(joining("\n"));
        fix.merge(replaceIncludingComments(classTree, original, replacement, state));
      }
    }
    return fix.build();
  }

  // xxx: From this point the code is just a fancy copy of `SuggestFixes.replaceIncludingComments` -
  // if we cannot use existing solutions for this functionality, this one needs a big refactor.

  private static Stream<Tokens.Comment> getComments(
      ClassTree classTree, Tree member, VisitorState state) {
    return getTokensBeforeMember(classTree, member, state).findFirst().stream()
        .map(ErrorProneToken::comments)
        // xxx: Original impl sorts comments, but that seems unnecessary.
        .flatMap(List::stream);
  }

  private static boolean isStatic(JCVariableDecl memberTree) {
    Set<Modifier> modifiers = memberTree.getModifiers().getFlags();
    return modifiers.contains(Modifier.STATIC);
  }

  private static boolean isConstructor(JCMethodDecl methodDecl) {
    return getSymbol(methodDecl).isConstructor();
  }

  private static SuggestedFix replaceIncludingComments(
      ClassTree classTree, Tree member, String replacement, VisitorState state) {
    Optional<Tree> previousMember = getPreviousMember(member, classTree);
    ImmutableList<ErrorProneToken> tokens =
        getTokensBeforeMember(classTree, member, state).collect(toImmutableList());

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
    @Var int startPos = getStartPosition(member);
    // This can happen for desugared expressions like `int a, b;`.
    if (startPos < getStartTokenization(classTree, state, previousMember)) {
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
    @Var Tree previousMember = null;
    for (Tree member : classTree.getMembers()) {
      if (member instanceof MethodTree && isGeneratedConstructor((MethodTree) member)) {
        continue;
      }
      if (member.equals(tree)) {
        break;
      }
      previousMember = member;
    }
    return Optional.ofNullable(previousMember);
  }

  private static Stream<ErrorProneToken> getTokensBeforeMember(
      ClassTree classTree, Tree member, VisitorState state) {
    Optional<Tree> previousMember = getPreviousMember(member, classTree);
    var startTokenization = getStartTokenization(classTree, state, previousMember);

    Stream<ErrorProneToken> tokens =
        state.getOffsetTokens(startTokenization, state.getEndPosition(member)).stream();

    if (previousMember.isEmpty()) {
      return tokens.dropWhile(token -> token.kind() != LBRACE).skip(1);
    } else {
      return tokens;
    }
  }

  // xxx: rename / remove method - it gets the start position of tokens that *might* be related to
  // member.
  // - afaik it includes a Class declaration if the member is the first member in the
  // class and does not have comments.
  private static Integer getStartTokenization(
      ClassTree classTree, VisitorState state, Optional<Tree> previousMember) {
    return previousMember
        .map(state::getEndPosition)
        .orElseGet(
            () ->
                // xxx: could return the position of the character next to the opening brace of the
                // class - `... Clazz ... {`
                // this could make this method more defined, worthy of existence, but it may also
                // require additional parameters and changes in `replaceIncludingComments` method.
                state.getEndPosition(classTree.getModifiers()) == Position.NOPOS
                    ? getStartPosition(classTree)
                    : state.getEndPosition(classTree.getModifiers()));
  }
}
