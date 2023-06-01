package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.TestNGMetadata.AnnotationMetadata;

/**
 * A {@link Migrator} that migrates the {@code org.testng.annotations.Test#dataProvider} attributes.
 */
@Immutable
final class DataProviderAttributeMigrator implements Migrator {
  @Override
  public boolean canFix(
      TestNGMetadata metadata,
      AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    ExpressionTree dataProviderNameExpressionTree = annotation.getAttributes().get("dataProvider");
    if (dataProviderNameExpressionTree == null) {
      return false;
    }

    String dataProviderName = ASTHelpers.constValue(dataProviderNameExpressionTree, String.class);
    return metadata.getDataProviderMetadata().containsKey(dataProviderName);
  }

  @Override
  public Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, ExpressionTree dataValue, VisitorState state) {
    String dataProviderName = ASTHelpers.constValue(dataValue, String.class);

    return Optional.of(
        SuggestedFix.builder()
            .addImport("org.junit.jupiter.params.ParameterizedTest")
            .addImport("org.junit.jupiter.params.provider.MethodSource")
            .prefixWith(methodTree, "@ParameterizedTest\n")
            .prefixWith(methodTree, String.format("@MethodSource(\"%s\")%n", dataProviderName))
            .build());
  }
}
