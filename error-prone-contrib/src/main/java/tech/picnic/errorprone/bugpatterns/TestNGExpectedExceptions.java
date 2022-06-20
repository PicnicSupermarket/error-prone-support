package tech.picnic.errorprone.bugpatterns;

import static com.google.auto.common.MoreStreams.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.REFACTORING;
import static com.google.errorprone.matchers.Matchers.isType;
import static com.sun.source.tree.Tree.Kind.MEMBER_SELECT;
import static com.sun.source.tree.Tree.Kind.NEW_ARRAY;
import static java.util.stream.Collectors.joining;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import java.util.Optional;
import org.junit.jupiter.api.function.Executable;
import org.testng.annotations.Test;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} which flags {@link Test#expectedExceptions()} and suggests a JUnit
 * equivalent replacement.
 *
 * <p>The method body is wrapped in a {@link org.junit.jupiter.api.Assertions#assertThrows(Class,
 * Executable)} statement.
 *
 * <p>This {@link BugChecker} does not support migrating more than one exception and will therefore
 * omit extra {@code expectedExceptions}. As this is not behavior preserving, a note with
 * explanation is added in a comment.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Migrate TestNG expected exceptions to JUnit",
    linkType = NONE,
    tags = REFACTORING,
    severity = ERROR)
public final class TestNGExpectedExceptions extends BugChecker implements MethodTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<AnnotationTree> TESTNG_ANNOTATION =
      isType("org.testng.annotations.Test");

  @Override
  public Description matchMethod(MethodTree tree, VisitorState state) {
    Optional<? extends AnnotationTree> testAnnotation =
        ASTHelpers.getAnnotations(tree).stream()
            .filter(annotation -> TESTNG_ANNOTATION.matches(annotation, state))
            .findFirst();
    if (testAnnotation.isEmpty()) {
      return Description.NO_MATCH;
    }
    Optional<AssignmentTree> assignmentTree =
        testAnnotation.get().getArguments().stream()
            .filter(AssignmentTree.class::isInstance)
            .map(AssignmentTree.class::cast)
            .filter(
                assignment ->
                    SourceCode.treeToString(assignment.getVariable(), state)
                        .equals("expectedExceptions"))
            .findFirst();
    if (assignmentTree.isEmpty()) {
      return Description.NO_MATCH;
    }

    ExpressionTree argumentExpression = assignmentTree.orElseThrow().getExpression();
    if (argumentExpression == null) {
      return Description.NO_MATCH;
    }

    Optional<String> expectedException = getExpectedException(argumentExpression, state);
    if (expectedException.isEmpty()) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder fix =
        SuggestedFix.builder()
            .replace(
                tree.getBody(),
                buildWrappedBody(tree.getBody(), expectedException.orElseThrow(), state))
            .replace(
                testAnnotation.get(),
                buildAnnotationReplacementSource(
                    testAnnotation.get(), assignmentTree.orElseThrow(), state));

    ImmutableList<String> removedExceptions = getRemovedExceptions(argumentExpression, state);
    if (!removedExceptions.isEmpty()) {
      fix.prefixWith(
          testAnnotation.get(),
          String.format(
              "// XXX: Removed handling of `%s` because this migration doesn't support it.\n",
              String.join(", ", removedExceptions)));
    }

    return describeMatch(testAnnotation.get(), fix.build());
  }

  private static String buildAnnotationReplacementSource(
      AnnotationTree annotationTree, AssignmentTree argumentToRemove, VisitorState state) {
    StringBuilder replacement = new StringBuilder();
    replacement.append(
        String.format("@%s", SourceCode.treeToString(annotationTree.getAnnotationType(), state)));
    String arguments =
        annotationTree.getArguments().stream()
            .filter(argument -> !argument.equals(argumentToRemove))
            .map(argument -> SourceCode.treeToString(argument, state))
            .collect(joining(", "));

    if (!arguments.isEmpty()) {
      replacement.append(String.format("(%s)", arguments));
    }

    return replacement.toString();
  }

  private static Optional<String> getExpectedException(
      ExpressionTree expectedExceptions, VisitorState state) {
    if (expectedExceptions.getKind() == NEW_ARRAY) {
      NewArrayTree arrayTree = (NewArrayTree) expectedExceptions;
      if (arrayTree.getInitializers().isEmpty()) {
        return Optional.empty();
      }

      return Optional.of(SourceCode.treeToString(arrayTree.getInitializers().get(0), state));
    } else if (expectedExceptions.getKind() == MEMBER_SELECT) {
      return Optional.of(SourceCode.treeToString(expectedExceptions, state));
    }

    return Optional.empty();
  }

  private static ImmutableList<String> getRemovedExceptions(
      ExpressionTree expectedExceptions, VisitorState state) {
    if (expectedExceptions.getKind() != NEW_ARRAY) {
      return ImmutableList.of();
    }

    NewArrayTree arrayTree = (NewArrayTree) expectedExceptions;
    if (arrayTree.getInitializers().size() <= 1) {
      return ImmutableList.of();
    }

    return arrayTree.getInitializers().subList(1, arrayTree.getInitializers().size()).stream()
        .map(initializer -> SourceCode.treeToString(initializer, state))
        .collect(toImmutableList());
  }

  private static String buildWrappedBody(BlockTree tree, String exception, VisitorState state) {
    return String.format(
        "{\norg.junit.jupiter.api.Assertions.assertThrows(%s, () -> %s);\n}",
        exception, SourceCode.treeToString(tree, state));
  }
}
