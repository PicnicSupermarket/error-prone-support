package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
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
import com.google.errorprone.util.ErrorProneTokens;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.parser.Tokens;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
    ImmutableList<MemberWithComments> membersWithComments =
        getMembersWithComments(tree, state).stream()
            .filter(memberWithComments -> shouldBeSorted(memberWithComments.member()))
            .collect(toImmutableList());

    ImmutableList<MemberWithComments> sortedMembersWithComments =
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
      ImmutableList<MemberWithComments> memberWithComments,
      ImmutableList<MemberWithComments> sortedMembersWithComments,
      VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();
    for (int i = 0; i < memberWithComments.size(); i++) {
      Tree originalMember = memberWithComments.get(i).member();
      MemberWithComments correct = sortedMembersWithComments.get(i);
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

  // XXX: Work around that `ErrorProneTokens.getTokens(memberSrc, ctx)` returns tokens not
  //  containing the member's comments.
  /** Returns the class' members with their comments. */
  private static ImmutableList<MemberWithComments> getMembersWithComments(
      ClassTree classTree, VisitorState state) {
    List<ErrorProneToken> tokens =
        new ArrayList<>(
            ErrorProneTokens.getTokens(state.getSourceForNode(classTree), state.context));

    ImmutableList.Builder<MemberWithComments> membersWithComments = ImmutableList.builder();
    for (Tree member : classTree.getMembers()) {
      ImmutableList<ErrorProneToken> memberTokens =
          ErrorProneTokens.getTokens(state.getSourceForNode(member), state.context);
      if (memberTokens.isEmpty() || memberTokens.get(0).kind() == Tokens.TokenKind.EOF) {
        continue;
      }

      @Var
      ImmutableList<ErrorProneToken> maybeCommentedMemberTokens =
          ImmutableList.copyOf(tokens.subList(0, memberTokens.size()));
      while (!areTokenListsMatching(memberTokens, maybeCommentedMemberTokens)) {
        tokens.remove(0);
        maybeCommentedMemberTokens = ImmutableList.copyOf(tokens.subList(0, memberTokens.size()));
      }

      membersWithComments.add(
          new MemberWithComments(
              member, ImmutableList.copyOf(maybeCommentedMemberTokens.get(0).comments())));
    }
    return membersWithComments.build();
  }

  /**
   * Checks whether two lists of error-prone tokens are 'equal' without considering their comments.
   */
  private static boolean areTokenListsMatching(
      ImmutableList<ErrorProneToken> tokens, ImmutableList<ErrorProneToken> memberTokens) {
    if (tokens.size() != memberTokens.size()) {
      return false;
    }
    for (int i = 0; i < tokens.size() - 1 /* EOF */; i++) {
      if (tokens.get(i).kind() != memberTokens.get(i).kind()
          || tokens.get(i).hasName() != memberTokens.get(i).hasName()
          || (tokens.get(i).hasName()
              && !tokens.get(i).name().equals(memberTokens.get(i).name()))) {
        return false;
      }
    }
    return true;
  }

  private static final class MemberWithComments {
    final Tree member;
    final ImmutableList<Tokens.Comment> comments;

    MemberWithComments(Tree member, ImmutableList<Tokens.Comment> comments) {
      this.member = member;
      this.comments = comments;
    }

    public Tree member() {
      return member;
    }

    public ImmutableList<Tokens.Comment> comments() {
      return comments;
    }
  }
}
