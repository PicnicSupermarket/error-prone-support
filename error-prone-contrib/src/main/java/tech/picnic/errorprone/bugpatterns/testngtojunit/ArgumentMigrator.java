package tech.picnic.errorprone.bugpatterns.testngtojunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;

public interface ArgumentMigrator {
  Optional<SuggestedFix> createFix(
      TestNGMigrationContext context,
      MethodTree methodTree,
      ExpressionTree content,
      VisitorState state);

  boolean canFix(TestNGMigrationContext context, TestNGMetadata.Annotation annotation);
}
