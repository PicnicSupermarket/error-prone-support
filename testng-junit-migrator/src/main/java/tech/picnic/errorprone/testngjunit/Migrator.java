package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.TestNGMetadata.AnnotationMetadata;

// XXX: General feedback not specific to this class.
// Make sure to:
// - We have kind of "integration tests" for the `TestNGScanner` which indirectly tests the
// `ArgumentMigrator`s, so we should probably add tests for the separate `ArgumentMigrator`s as
// well.

/**
 * Interface implemented by classes that define how to migrate a specific argument from a TestNG
 * {@code Test} annotation to JUnit.
 */
@Immutable
interface Migrator {
  /**
   * Attempt to create a {@link SuggestedFix}.
   *
   * @param classTree The class tree containing the test.
   * @param methodTree The method tree the annotation is on.
   * @param dataValue The value of annotation argument.
   * @param state The visitor state.
   * @return an {@link Optional} containing the created fix.
   */
  Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, ExpressionTree dataValue, VisitorState state);

  /**
   * Get whether the specified annotation can be migrated.
   *
   * @param metadata The metadata that will be fixed.
   * @param annotation The metadata for the annotation that will be fixed.
   * @param methodTree The test {@link MethodTree}.
   * @param state The visitor state.
   * @return {@code true} if the annotation argument can be migrated or else {@code false}
   */
  boolean canFix(
      TestNGMetadata metadata,
      AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state);
}
