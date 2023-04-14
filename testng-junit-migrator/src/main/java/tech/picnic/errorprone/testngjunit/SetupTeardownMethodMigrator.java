package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import javax.lang.model.element.Modifier;
import tech.picnic.errorprone.testngjunit.TestNGMetadata.SetupTeardownType;

/**
 * A helper class that migrates TestNG setup and teardown methods to their JUnit Jupiter equivalent.
 */
final class SetupTeardownMethodMigrator {
  /**
   * Create the {@link SuggestedFix} required to migrate a TestNG setup/teardown methods to the
   * JUnit Jupiter variant.
   *
   * @param tree The setup/teardown method tree.
   * @param type The setup/teardown type.
   * @param state The visitor state.
   * @return An {@link Optional} containing the created fix.
   */
  public Optional<SuggestedFix> createFix(
      MethodTree tree, SetupTeardownType type, VisitorState state) {
    return getSetupTeardownAnnotationTree(tree, type, state)
        .map(
            annotation -> {
              SuggestedFix.Builder fix =
                  SuggestedFix.builder()
                      .merge(
                          SuggestedFix.replace(
                              annotation, String.format("@%s", type.getJunitAnnotationClass())));
              if (type.requiresStaticMethod()
                  && !tree.getModifiers().getFlags().contains(Modifier.STATIC)) {
                SuggestedFixes.addModifiers(tree, state, Modifier.STATIC).ifPresent(fix::merge);
              }

              return fix.build();
            });
  }

  private static Optional<? extends AnnotationTree> getSetupTeardownAnnotationTree(
      MethodTree tree, SetupTeardownType type, VisitorState state) {
    return ASTHelpers.getAnnotations(tree).stream()
        .filter(annotation -> type.getAnnotationMatcher().matches(annotation, state))
        .findFirst();
  }
}
