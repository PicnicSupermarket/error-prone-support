package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.util.SourceCode;

/** A {@link Migrator} that migrates the {@code priority} argument. */
@Immutable
final class PriorityAttributeMigrator implements Migrator {
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
