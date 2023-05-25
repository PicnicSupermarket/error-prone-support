package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
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
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.ErrorProneToken;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.parser.Tokens;
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
  private static final Comparator<Tree> MEMBER_SORTING =
      comparing(
          (Tree memberTree) -> {
            switch (memberTree.getKind()) {
              case VARIABLE:
                return isStatic((VariableTree) memberTree) ? 1 : 2;
              case METHOD:
                return isConstructor((MethodTree) memberTree) ? 3 : 4;
              default:
                throw new IllegalStateException("Unexpected kind: " + memberTree.getKind());
            }
          });

  /** Instantiates a new {@link MemberOrdering} instance. */
  public MemberOrdering() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    ImmutableList<? extends Tree> members =
        tree.getMembers().stream()
            .filter(MemberOrdering::shouldBeSorted)
            .collect(toImmutableList());
    ImmutableList<? extends Tree> sortedMembers =
        members.stream().sorted(MEMBER_SORTING).collect(toImmutableList());

    if (members.equals(sortedMembers)) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .addFix(SuggestedFixes.addSuppressWarnings(state, canonicalName()))
        .addFix(swapMembersIncludingComments(members, sortedMembers, tree, state))
        .setMessage(
            "Members, constructors and methods should follow standard ordering. "
                + "The standard ordering is: static variables, non-static variables, "
                + "constructors and methods.")
        .build();
  }

  private static boolean shouldBeSorted(Tree tree) {
    return tree instanceof VariableTree
        || (tree instanceof MethodTree && !ASTHelpers.isGeneratedConstructor((MethodTree) tree));
  }

  private static SuggestedFix swapMembersIncludingComments(
      ImmutableList<? extends Tree> members,
      ImmutableList<? extends Tree> sortedMembers,
      ClassTree classTree,
      VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();
    for (int i = 0; i < members.size(); i++) {
      Tree original = members.get(i);
      Tree correct = sortedMembers.get(i);
      /* Technically not necessary, but avoids redundant replacements. */
      if (!original.equals(correct)) {
        String replacement =
            Stream.concat(
                    getComments(classTree, correct, state).map(Tokens.Comment::getText),
                    Stream.of(state.getSourceForNode(correct)))
                .collect(joining("\n"));
        fix.merge(SuggestedFixes.replaceIncludingComments(state.getPath(), replacement, state));
      }
    }
    return fix.build();
  }

  private static boolean isStatic(VariableTree memberTree) {
    Set<Modifier> modifiers = memberTree.getModifiers().getFlags();
    return modifiers.contains(Modifier.STATIC);
  }

  private static boolean isConstructor(MethodTree methodDecl) {
    return ASTHelpers.getSymbol(methodDecl).isConstructor();
  }

  private static Stream<Tokens.Comment> getComments(
      ClassTree classTree, Tree member, VisitorState state) {
    return getTokensBeforeMember(classTree, member, state).findFirst().stream()
        .map(ErrorProneToken::comments)
        // xxx: Original impl sorts comments, but that seems unnecessary.
        .flatMap(List::stream);
  }

  private static Optional<Tree> getPreviousMember(Tree tree, ClassTree classTree) {
    @Var Tree previousMember = null;
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

  private static Stream<ErrorProneToken> getTokensBeforeMember(
      ClassTree classTree, Tree member, VisitorState state) {
    Optional<Tree> previousMember = getPreviousMember(member, classTree);
    Integer startTokenization = getStartTokenization(classTree, state, previousMember);

    Stream<ErrorProneToken> tokens =
        state.getOffsetTokens(startTokenization, state.getEndPosition(member)).stream();

    if (previousMember.isEmpty()) {
      return tokens.dropWhile(token -> token.kind() != LBRACE).skip(1);
    }
    return tokens;
  }

  // XXX: rename / remove method - it gets the start position of tokens that *might* be related to
  // member.
  // - afaik it includes a Class declaration if the member is the first member in the
  // class and does not have comments.
  private static Integer getStartTokenization(
      ClassTree classTree, VisitorState state, Optional<Tree> previousMember) {
    return previousMember
        .map(state::getEndPosition)
        .orElseGet(
            () ->
                // XXX: Could return the position of the character next to the opening brace of the
                // class - `... Clazz ... {`
                // this could make this method more defined, worthy of existence, but it may also
                // require additional parameters and changes in `replaceIncludingComments` method.
                state.getEndPosition(classTree.getModifiers()) == Position.NOPOS
                    ? ASTHelpers.getStartPosition(classTree)
                    : state.getEndPosition(classTree.getModifiers()));
  }
}
