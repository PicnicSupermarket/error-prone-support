package tech.picnic.errorprone.bugpatterns;

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
    summary = "Class members should be ordered in a standard way",
    explanation =
        "Class members should be ordered in a standard way, which is: "
            + "static fields, non-static fields, constructors and methods.",
    link = BUG_PATTERNS_BASE_URL + "ClassMemberOrdering",
    linkType = CUSTOM,
    severity = WARNING,
    tags = STYLE)
public final class ClassMemberOrdering extends BugChecker implements BugChecker.ClassTreeMatcher {
  private static final long serialVersionUID = 1L;

  /** A comparator that sorts class members (including constructors) in a standard order. */
  private static final Comparator<Tree> CLASS_MEMBER_SORTER =
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

  /** Instantiates a new {@link ClassMemberOrdering} instance. */
  public ClassMemberOrdering() {}

  @Override
  public Description matchClass(ClassTree classTree, VisitorState state) {
    ImmutableList<ClassMemberWithComments> classMembers =
        getClassMembersWithComments(classTree, state).stream()
            .filter(classMember -> shouldBeSorted(classMember.tree()))
            .collect(toImmutableList());

    ImmutableList<ClassMemberWithComments> sortedClassMembers =
        ImmutableList.sortedCopyOf(
            (a, b) -> CLASS_MEMBER_SORTER.compare(a.tree(), b.tree()), classMembers);

    if (classMembers.equals(sortedClassMembers)) {
      return Description.NO_MATCH;
    }

    return buildDescription(classTree)
        .addFix(replaceClassMembers(classMembers, sortedClassMembers, state))
        .setMessage(
            "Fields, constructors and methods should follow standard ordering. "
                + "The standard ordering is: static fields, non-static fields, "
                + "constructors and methods.")
        .build();
  }

  private static boolean isStatic(VariableTree variableTree) {
    Set<Modifier> modifiers = variableTree.getModifiers().getFlags();
    return modifiers.contains(Modifier.STATIC);
  }

  private static boolean isConstructor(MethodTree methodTree) {
    return ASTHelpers.getSymbol(methodTree).isConstructor();
  }

  private static boolean shouldBeSorted(Tree tree) {
    return tree instanceof VariableTree
        || (tree instanceof MethodTree && !ASTHelpers.isGeneratedConstructor((MethodTree) tree));
  }

  private static SuggestedFix replaceClassMembers(
      ImmutableList<ClassMemberWithComments> classMembers,
      ImmutableList<ClassMemberWithComments> replacementClassMembers,
      VisitorState state) {
    SuggestedFix.Builder fix = SuggestedFix.builder();
    for (int i = 0; i < classMembers.size(); i++) {
      ClassMemberWithComments original = classMembers.get(i);
      ClassMemberWithComments replacement = replacementClassMembers.get(i);
      fix.merge(replaceClassMember(state, original, replacement));
    }
    return fix.build();
  }

  private static SuggestedFix replaceClassMember(
      VisitorState state, ClassMemberWithComments original, ClassMemberWithComments replacement) {
    /* Technically this check is not necessary, but it avoids redundant replacements. */
    if (original.equals(replacement)) {
      return SuggestedFix.emptyFix();
    }
    String replacementSource =
        Stream.concat(
                replacement.comments().stream().map(Tokens.Comment::getText),
                Stream.of(state.getSourceForNode(replacement.tree())))
            .collect(joining("\n"));
    return SuggestedFixes.replaceIncludingComments(
        TreePath.getPath(state.getPath(), original.tree()), replacementSource, state);
  }

  /** Returns the class' members with their comments. */
  private static ImmutableList<ClassMemberWithComments> getClassMembersWithComments(
      ClassTree classTree, VisitorState state) {
    return classTree.getMembers().stream()
        .map(
            classMember ->
                new ClassMemberWithComments(
                    classMember, getClassMemberComments(state, classTree, classMember)))
        .collect(toImmutableList());
  }

  private static ImmutableList<Tokens.Comment> getClassMemberComments(
      VisitorState state, ClassTree classTree, Tree classMember) {
    if (state.getEndPosition(classMember) == -1) {
      // Member is probably generated, according to `VisitorState` its end position is "not
      // available".
      return ImmutableList.of();
    }

    ImmutableList<ErrorProneToken> classTokens =
        ImmutableList.copyOf(
            state.getOffsetTokens(
                ASTHelpers.getStartPosition(classTree), state.getEndPosition(classTree)));

    int classMemberStartPos = ASTHelpers.getStartPosition(classMember);
    Optional<Integer> previousClassTokenEndPos =
        classTokens.stream()
            .map(ErrorProneToken::endPos)
            .takeWhile(endPos -> endPos < classMemberStartPos)
            .reduce((earlierPos, laterPos) -> laterPos);

    ImmutableList<ErrorProneToken> classMemberTokens =
        ImmutableList.copyOf(
            state.getOffsetTokens(
                previousClassTokenEndPos.orElse(classMemberStartPos),
                state.getEndPosition(classMember)));

    return ImmutableList.copyOf(classMemberTokens.get(0).comments());
  }

  private static final class ClassMemberWithComments {
    private final Tree tree;
    private final ImmutableList<Tokens.Comment> comments;

    ClassMemberWithComments(Tree tree, ImmutableList<Tokens.Comment> comments) {
      this.tree = requireNonNull(tree);
      this.comments = requireNonNull(comments);
    }

    public Tree tree() {
      return tree;
    }

    public ImmutableList<Tokens.Comment> comments() {
      return comments;
    }
  }
}
