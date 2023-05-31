package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.TestNGMetadata.AnnotationMetadata;
import tech.picnic.errorprone.util.SourceCode;

/** A {@link Migrator} that migrates the {@code dataProvider} argument. */
@Immutable
final class DataProviderAttributeMigrator implements Migrator {
  @Override
  public Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, ExpressionTree dataValue, VisitorState state) {
    String dataProviderName = getDataProviderName(dataValue, state);

    return Optional.of(
        SuggestedFix.builder()
            .addImport("org.junit.jupiter.params.ParameterizedTest")
            .addImport("org.junit.jupiter.params.provider.MethodSource")
            .prefixWith(methodTree, "@ParameterizedTest\n")
            .merge(
                SuggestedFix.prefixWith(
                    methodTree, String.format("@MethodSource(\"%s\")%n", dataProviderName)))
            .build());
  }

  @Override
  public boolean canFix(
      TestNGMetadata metadata,
      AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    ExpressionTree dataProviderNameExpression = annotation.getAttributes().get("dataProvider");
    if (dataProviderNameExpression == null) {
      return false;
    }

    String dataProviderName = getDataProviderName(dataProviderNameExpression, state);
    return metadata.getDataProviderMetadata().containsKey(dataProviderName);
  }

  private static String getDataProviderName(ExpressionTree tree, VisitorState state) {
    return tree.getKind() == Kind.STRING_LITERAL
        ? ASTHelpers.constValue(tree, String.class)
        : SourceCode.treeToString(tree, state);
  }
}
