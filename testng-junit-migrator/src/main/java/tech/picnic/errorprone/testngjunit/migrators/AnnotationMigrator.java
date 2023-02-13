package tech.picnic.errorprone.testngjunit.migrators;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.Migrator;
import tech.picnic.errorprone.testngjunit.TestNGMetadata;

public class AnnotationMigrator implements Migrator<TestNGMetadata.AnnotationMetadata> {

  @Override
  public Optional<SuggestedFix> createFix(
      ClassTree classTree,
      MethodTree methodTree,
      TestNGMetadata.AnnotationMetadata dataValue,
      VisitorState state) {
    SuggestedFix.Builder fixBuilder =
        SuggestedFix.builder()
            .addImport("org.junit.jupiter.api.Test")
            .removeImport("org.testng.annotations.Test")
            .merge(SuggestedFix.delete(dataValue.getAnnotationTree()));
    if (!dataValue.getArguments().containsKey("dataProvider")) {
      fixBuilder.merge(SuggestedFix.prefixWith(methodTree, "@Test\n"));
    }

    return Optional.of(fixBuilder.build());
  }
}
