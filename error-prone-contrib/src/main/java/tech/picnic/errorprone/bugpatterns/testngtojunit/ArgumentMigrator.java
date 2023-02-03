package tech.picnic.errorprone.bugpatterns.testngtojunit;

import com.sun.source.tree.ExpressionTree;

public interface ArgumentMigrator extends Migrator<ExpressionTree> {
  /**
   * Get whether the specified annotation can be migrated.
   *
   * @param context the context of the current migration
   * @param annotationMetadata the annotation to check
   * @return {@code true} if the annotation argument can be migrated or else {@code false}
   */
  boolean canFix(
      TestNGMigrationContext context, TestNGMetadata.AnnotationMetadata annotationMetadata);
}
