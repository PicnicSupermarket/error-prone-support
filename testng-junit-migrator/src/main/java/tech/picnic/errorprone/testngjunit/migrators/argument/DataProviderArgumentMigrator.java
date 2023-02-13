package tech.picnic.errorprone.testngjunit.migrators.argument;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.util.Optional;
import org.testng.annotations.Test;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;
import tech.picnic.errorprone.testngjunit.ArgumentMigrator;
import tech.picnic.errorprone.testngjunit.Migrator;
import tech.picnic.errorprone.testngjunit.TestNGMetadata;

/** An {@link Migrator} that migrates the {@link Test#dataProvider()} argument. */
@Immutable
public class DataProviderArgumentMigrator implements ArgumentMigrator {

  @Override
  public Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, ExpressionTree dataValue, VisitorState state) {
    String dataProviderName = getDataProviderName(dataValue, state);

    return Optional.of(
        SuggestedFix.builder()
            .addImport("org.junit.jupiter.params.ParameterizedTest")
            .addImport("org.junit.jupiter.params.provider.MethodSource")
            .merge(SuggestedFix.prefixWith(methodTree, "@ParameterizedTest\n"))
            .merge(
                SuggestedFix.prefixWith(
                    methodTree, String.format("@MethodSource(\"%s\")\n", dataProviderName)))
            .build());
  }

  @Override
  public boolean canFix(
      TestNGMetadata metadata,
      TestNGMetadata.AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    ExpressionTree dataProviderNameExpression = annotation.getArguments().get("dataProvider");
    if (dataProviderNameExpression == null) {
      return false;
    }

    String dataProviderName = getDataProviderName(dataProviderNameExpression, state);
    return metadata.getDataProviderMetadata().containsKey(dataProviderName);
  }

  private static String getDataProviderName(ExpressionTree tree, VisitorState state) {
    return tree.getKind() == Tree.Kind.STRING_LITERAL
        ? (String) ((LiteralTree) tree).getValue()
        : SourceCode.treeToString(tree, state);
  }
}
