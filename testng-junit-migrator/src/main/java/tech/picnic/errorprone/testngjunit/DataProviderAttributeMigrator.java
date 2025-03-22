package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.TestNgMetadata.AnnotationMetadata;

/**
 * A {@link AttributeMigrator} that migrates the {@code org.testng.annotations.Test#dataProvider}
 * attributes.
 */
@Immutable
final class DataProviderAttributeMigrator implements AttributeMigrator {
  @Override
  public Optional<SuggestedFix> migrate(
      TestNgMetadata metadata,
      AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    ExpressionTree dataProviderNameExpressionTree = annotation.getAttributes().get("dataProvider");
    if (dataProviderNameExpressionTree == null) {
      return Optional.empty();
    }

    String dataProviderName = ASTHelpers.constValue(dataProviderNameExpressionTree, String.class);
    if (!metadata.getDataProviderMetadata().containsKey(dataProviderName)) {
      return Optional.empty();
    }

    return Optional.of(
        SuggestedFix.builder()
            .addImport("org.junit.jupiter.params.ParameterizedTest")
            .addImport("org.junit.jupiter.params.provider.MethodSource")
            .prefixWith(methodTree, "@ParameterizedTest\n")
            .prefixWith(methodTree, String.format("@MethodSource(\"%s\")%n", dataProviderName))
            .build());
  }
}
