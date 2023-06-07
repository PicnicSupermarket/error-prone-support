package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.util.SourceCode;

/**
 * A {@link Migrator} that leaves a comment for attributes that aren't supported in the migration.
 */
@Immutable
final class UnsupportedAttributeMigrator implements Migrator {
  @Override
  public boolean canFix(
      TestNGMetadata metadata,
      TestNGMetadata.AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    return true;
  }

  @Override
  public Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, ExpressionTree dataValue, VisitorState state) {
    return getAttributeName(methodTree, dataValue, state)
        .map(
            attributeName ->
                SuggestedFix.prefixWith(
                    methodTree,
                    String.format(
                        "// XXX: Attribute `%s` is not supported, value: `%s`%n",
                        attributeName, SourceCode.treeToString(dataValue, state))));
  }

  private static Optional<String> getAttributeName(
      MethodTree method, ExpressionTree expression, VisitorState state) {
    return ASTHelpers.getAnnotations(method).stream()
        .filter(tree -> TestNGMatchers.TESTNG_TEST_ANNOTATION.matches(tree, state))
        .findFirst()
        .flatMap(
            annotationTree ->
                annotationTree.getArguments().stream()
                    .filter(AssignmentTree.class::isInstance)
                    .map(AssignmentTree.class::cast)
                    .filter(assignment -> assignment.getExpression().equals(expression))
                    .findFirst())
        .map(assignment -> ((IdentifierTree) assignment.getVariable()).getName().toString());
  }
}
