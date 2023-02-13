package tech.picnic.errorprone.bugpatterns.testngtojunit;

import com.google.errorprone.VisitorState;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;

public interface ArgumentMigrator extends Migrator<ExpressionTree> {

  /**
   * Get whether the specified annotation can be migrated.
   *
   * @param state the visitor state
   * @return {@code true} if the annotation argument can be migrated or else {@code false}
   */
  boolean canFix(
      TestNGMetadata metadata,
      TestNGMetadata.AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state);
}
