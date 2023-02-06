package tech.picnic.errorprone.bugpatterns.testngtojunit.migrators;

import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.bugpatterns.testngtojunit.Migrator;
import tech.picnic.errorprone.bugpatterns.testngtojunit.TestNGMetadata;
import tech.picnic.errorprone.bugpatterns.testngtojunit.TestNGMigrationContext;

public class AnnotationMigrator implements Migrator<TestNGMetadata.AnnotationMetadata> {

  @Override
  public Optional<SuggestedFix> createFix(ClassTree classTree, MethodTree methodTree, TestNGMetadata.AnnotationMetadata dataValue, VisitorState state) {
    return Optional.empty();
  }

  @Override
  public boolean canFix(TestNGMetadata metadata, TestNGMetadata.AnnotationMetadata annotation, MethodTree methodTree, VisitorState state) {
    return false;
  }
}
