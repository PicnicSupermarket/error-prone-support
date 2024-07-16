package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.TestNgMetadata.AnnotationMetadata;

/**
 * Interface implemented by classes that define how to migrate a specific attribute from a TestNG
 * {@code Test} annotation to JUnit.
 */
@Immutable
interface AttributeMigrator {
  /**
   * Attempts to create a {@link SuggestedFix}.
   *
   * @param methodTree The method tree the annotation is on.
   * @param minimalChangesMode Whether the migration should introduce the minimal changes required
   *     to migrate.
   * @param state The visitor state.
   * @return an {@link Optional} containing the created fix. This returns an {@link
   *     Optional#empty()} if the {@link AttributeMigrator} is not able to migrate the attribute.
   */
  Optional<SuggestedFix> migrate(
      TestNgMetadata metadata,
      AnnotationMetadata annotation,
      MethodTree methodTree,
      boolean minimalChangesMode,
      VisitorState state);
}
