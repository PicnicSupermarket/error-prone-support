package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.TestNGMetadata.AnnotationMetadata;

/**
 * Interface implemented by classes that define how to migrate a specific argument from a TestNG
 * {@code Test} annotation to JUnit.
 */
@Immutable
interface Migrator {
  /**
   * Tells whether the specified annotation can be migrated.
   *
   * @param metadata The metadata that will be fixed.
   * @param annotation The metadata for the annotation that will be fixed.
   * @param methodTree The test {@link MethodTree}.
   * @param state The visitor state.
   * @return {@code true} if the annotation or an annotation attribute can be migrated.
   */
  boolean canFix(
      TestNGMetadata metadata,
      AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state);

  /**
   * Attempts to create a {@link SuggestedFix}.
   *
   * @param classTree The class tree containing the test.
   * @param methodTree The method tree the annotation is on.
   * @param dataValue The value of annotation argument.
   * @param state The visitor state.
   * @return an {@link Optional} containing the created fix.
   */
  Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, ExpressionTree dataValue, VisitorState state);
}
