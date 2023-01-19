package tech.picnic.errorprone.bugpatterns.testmigrator;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import tech.picnic.errorprone.bugpatterns.TestNGMetadata;

public interface ArgumentMigrator {
  SuggestedFix createFix(
      TestNGMigrationContext context,
      MethodTree methodTree,
      ExpressionTree content,
      VisitorState state);

  boolean canFix(TestNGMigrationContext context, TestNGMetadata.TestNGAnnotation annotation);
}
