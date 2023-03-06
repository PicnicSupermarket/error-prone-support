package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.TestNGMetadata.SetupTeardownType;

/** A helper class that migrates TestNG setup/teardown methods to their JUnit Jupiter variant. */
final class SetupTeardownMethodMigrator {
  /**
   * Create the {@link SuggestedFix} required to migrate a TestNG setup/teardown methods to the
   * JUnit Jupiter varian t.
   *
   * @param tree the setup/teardown method tree.
   * @param type the setup/teardown type.
   * @param state the visitor state.
   * @return An {@link Optional} containing the created fix.
   */
  public Optional<SuggestedFix> createFix(
      MethodTree tree, SetupTeardownType type, VisitorState state) {
    return getSetupTeardownAnnotationTree(tree, type, state)
        .map(
            annotation ->
                SuggestedFix.replace(
                    annotation, String.format("@%s", type.getJunitAnnotationClass())));
  }

  private static Optional<? extends AnnotationTree> getSetupTeardownAnnotationTree(
      MethodTree tree, SetupTeardownType type, VisitorState state) {
    return ASTHelpers.getAnnotations(tree).stream()
        .filter(annotation -> type.getAnnotationMatcher().matches(annotation, state))
        .findFirst();
  }
}
