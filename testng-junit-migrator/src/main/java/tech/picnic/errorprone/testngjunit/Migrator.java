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
// - Check if everything needs to be `public`. I suspect most things can actually be package
// private.
// That makes it easier for yourself because public things should be documented with Javadoc :).
// - Make sure there is a completely green build, now `mvn clean install` still gives problems.
// - For some classes it would still be nice to have some Javadoc nonetheless.
// - Maybe we should start working on a README where we document a few things.
//   1. What does is and how it works.
//   2. How to use this.
//   3. The script used to perform the migration?
//   4. Possibly we can think of something else? Things not supported?
// - We have kind of "integration tests" for the `TestNGScanner` which indirectly tests the
// `ArgumentMigrator`s, so we should probably add tests for the separate `ArgumentMigrator`s as
// well.

/**
 * Interface implemented by classes that define how to migrate a specific argument from a TestNG
 * {@code Test} annotation to JUnit.
 */
@Immutable
public interface Migrator {
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
