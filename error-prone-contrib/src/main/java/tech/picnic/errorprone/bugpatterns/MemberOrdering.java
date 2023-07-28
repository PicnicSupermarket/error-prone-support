package tech.picnic.errorprone.bugpatterns;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
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
import com.sun.source.util.TreePath;
import com.sun.tools.javac.parser.Tokens;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

/** A {@link BugChecker} that flags classes with non-standard member ordering. */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Members should be ordered in a standard way",
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
          (Tree tree) -> {
            switch (tree.getKind()) {
              case VARIABLE:
                return isStatic((VariableTree) tree) ? 1 : 2;
              case METHOD:
                return isConstructor((MethodTree) tree) ? 3 : 4;
              default:
                throw new IllegalStateException("Unexpected kind: " + tree.getKind());
            }
          });

  /** Instantiates a new {@link MemberOrdering} instance. */
  public MemberOrdering() {}

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    ImmutableList<ClassMemberWithComments> membersWithComments =
        getMembersWithComments(tree, state).stream()
            .filter(classMemberWithComments -> shouldBeSorted(classMemberWithComments.member()))
            .collect(toImmutableList());

    ImmutableList<ClassMemberWithComments> sortedMembersWithComments =
        ImmutableList.sortedCopyOf(
            (a, b) -> MEMBER_SORTING.compare(a.member(), b.member()), membersWithComments);

    if (membersWithComments.equals(sortedMembersWithComments)) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .addFix(swapMembersWithComments(membersWithComments, sortedMembersWithComments, state))
        .setMessage(
            "Members, constructors and methods should follow standard ordering. "
                + "The standard ordering is: static variables, non-static variables, "
                + "constructors and methods.")
        .build();
  }

  private static boolean isStatic(VariableTree memberTree) {
    Set<Modifier> modifiers = memberTree.getModifiers().getFlags();
    return modifiers.contains(Modifier.STATIC);
  }

  private static boolean isConstructor(MethodTree methodDecl) {
    return ASTHelpers.getSymbol(methodDecl).isConstructor();
  }

  private static boolean shouldBeSorted(Tree tree) {
    return tree instanceof VariableTree
        || (tree instanceof MethodTree && !ASTHelpers.isGeneratedConstructor((MethodTree) tree));
  }

  private static SuggestedFix swapMembersWithComments(
      ImmutableList<ClassMemberWithComments> memberWithComments,
      ImmutableList<ClassMemberWithComments> sortedMembersWithComments,
      VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();
    for (int i = 0; i < memberWithComments.size(); i++) {
      Tree originalMember = memberWithComments.get(i).member();
      ClassMemberWithComments correct = sortedMembersWithComments.get(i);
      /* Technically this check is not necessary, but it avoids redundant replacements. */
      if (!originalMember.equals(correct.member())) {
        String replacement =
            Stream.concat(
                    correct.comments().stream().map(Tokens.Comment::getText),
                    Stream.of(state.getSourceForNode(correct.member())))
                .collect(joining("\n"));
        fix.merge(
            SuggestedFixes.replaceIncludingComments(
                TreePath.getPath(state.getPath(), originalMember), replacement, state));
      }
    }
    return fix.build();
  }

  /** Returns the class' members with their comments. */
  private static ImmutableList<ClassMemberWithComments> getMembersWithComments(
      ClassTree classTree, VisitorState state) {
    return classTree.getMembers().stream()
        .map(
            member ->
                new ClassMemberWithComments(member, getMemberComments(state, classTree, member)))
        .collect(toImmutableList());
  }

  private static ImmutableList<Tokens.Comment> getMemberComments(
      VisitorState state, ClassTree classTree, Tree member) {
    if (member.getKind() == Tree.Kind.METHOD
        && ASTHelpers.isGeneratedConstructor((MethodTree) member)) {
      return ImmutableList.of();
    }

    checkState(
        state.getEndPosition(member) != -1,
        "Member's end position is not available (-1).\n - member=[%s]\n - source=[%s]",
        member,
        state.getSourceForNode(member));

    ImmutableList<ErrorProneToken> tokens =
        ImmutableList.copyOf(
            state.getOffsetTokens(
                ASTHelpers.getStartPosition(classTree), state.getEndPosition(classTree)));

    int memberStartPos = ASTHelpers.getStartPosition(member);
    Optional<Integer> previousMemberEndPos =
        tokens.stream()
            .map(ErrorProneToken::endPos)
            .takeWhile(endPos -> endPos < memberStartPos)
            .reduce((earlierPos, laterPos) -> laterPos);

    ImmutableList<ErrorProneToken> memberTokens =
        ImmutableList.copyOf(
            state.getOffsetTokens(
                previousMemberEndPos.orElse(memberStartPos), state.getEndPosition(member)));

    return ImmutableList.copyOf(memberTokens.get(0).comments());
  }

  static final class ClassMemberWithComments {
    private final Tree member;
    private final ImmutableList<Tokens.Comment> comments;

    ClassMemberWithComments(Tree member, ImmutableList<Tokens.Comment> comments) {
      this.member = requireNonNull(member);
      this.comments = requireNonNull(comments);
    }

    public Tree member() {
      return member;
    }

    public ImmutableList<Tokens.Comment> comments() {
      return comments;
    }
  }
}
