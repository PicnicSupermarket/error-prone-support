package tech.picnic.errorprone.bugpatterns.testngtojunit.migrators;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.bugpatterns.testngtojunit.ArgumentMigrator;
import tech.picnic.errorprone.bugpatterns.testngtojunit.TestNGMetadata;
import tech.picnic.errorprone.bugpatterns.testngtojunit.TestNGMigrationContext;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

public class PriorityArgumentMigrator implements ArgumentMigrator {
  @Override
  public Optional<SuggestedFix> createFix(
      TestNGMigrationContext context,
      MethodTree methodTree,
      ExpressionTree content,
      VisitorState state) {
    return Optional.of(
        SuggestedFix.builder()
            .addImport("org.junit.jupiter.api.Order")
            .addImport("org.junit.jupiter.api.TestMethodOrder")
            .addImport("org.junit.jupiter.api.MethodOrderer")
            .merge(
                SuggestedFix.prefixWith(
                    methodTree,
                    String.format("@Order(%s)\n", SourceCode.treeToString(content, state))))
            .merge(
                SuggestedFix.prefixWith(
                    context.getClassTree(),
                    "@TestMethodOrder(MethodOrderer.OrderAnnotation.class)\n"))
            .build());
  }

  @Override
  public boolean canFix(TestNGMigrationContext context, TestNGMetadata.Annotation annotation) {
    return annotation.getArguments().containsKey("priority");
  }
}
