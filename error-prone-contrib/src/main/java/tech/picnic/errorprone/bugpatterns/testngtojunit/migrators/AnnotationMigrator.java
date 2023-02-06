package tech.picnic.errorprone.bugpatterns.testngtojunit.migrators;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.bugpatterns.testngtojunit.Migrator;
import tech.picnic.errorprone.bugpatterns.testngtojunit.TestNGMetadata;
import tech.picnic.errorprone.bugpatterns.testngtojunit.TestNGMigrationContext;

public class AnnotationMigrator implements Migrator<TestNGMetadata.AnnotationMetadata> {

  @Override
  public Optional<SuggestedFix> createFix(
      MethodTree methodTree,
      TestNGMetadata.AnnotationMetadata dataValue,
      VisitorState state) {

    return Optional.empty();
  }
}
