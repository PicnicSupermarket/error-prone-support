package tech.picnic.errorprone.testngjunit.migrators.argument;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import org.testng.annotations.Test;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;
import tech.picnic.errorprone.testngjunit.ArgumentMigrator;
import tech.picnic.errorprone.testngjunit.Migrator;
import tech.picnic.errorprone.testngjunit.TestNGMetadata;

/** An {@link Migrator} that migrates the {@link Test#priority()} argument. */
@Immutable
public class PriorityArgumentMigrator implements ArgumentMigrator {

  @Override
  public Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, ExpressionTree dataValue, VisitorState state) {
    return Optional.of(
        SuggestedFix.builder()
            .addImport("org.junit.jupiter.api.Order")
            .addImport("org.junit.jupiter.api.TestMethodOrder")
            .addImport("org.junit.jupiter.api.MethodOrderer")
            .merge(
                SuggestedFix.prefixWith(
                    methodTree,
                    String.format("@Order(%s)\n", SourceCode.treeToString(dataValue, state))))
            .merge(
                SuggestedFix.prefixWith(
                    classTree, "@TestMethodOrder(MethodOrderer.OrderAnnotation.class)\n"))
            .build());
  }

  @Override
  public boolean canFix(
      TestNGMetadata metadata,
      TestNGMetadata.AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    return annotation.getArguments().containsKey("priority");
  }
}
