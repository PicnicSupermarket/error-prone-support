package tech.picnic.errorprone.bugpatterns.testmigrator.migrators;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import tech.picnic.errorprone.bugpatterns.TestNGMetadata;
import tech.picnic.errorprone.bugpatterns.testmigrator.ArgumentMigrator;
import tech.picnic.errorprone.bugpatterns.testmigrator.TestNGMigrationContext;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

public class DescriptionArgumentMigrator implements ArgumentMigrator {
  @Override
  public SuggestedFix createFix(
      TestNGMigrationContext context,
      MethodTree methodTree,
      ExpressionTree content,
      VisitorState state) {
    return SuggestedFix.prefixWith(
        methodTree,
        String.format(
            "@org.junit.jupiter.api.DisplayName(%s)\n", SourceCode.treeToString(content, state)));
  }

  @Override
  public boolean canFix(
      TestNGMigrationContext context, TestNGMetadata.TestNGAnnotation annotation) {
    return annotation.getArguments().containsKey("description");
  }
}
