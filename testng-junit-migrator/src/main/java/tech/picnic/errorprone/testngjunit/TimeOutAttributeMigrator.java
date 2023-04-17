package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.util.SourceCode;

/**
 * A {@link Migrator} that migrates the {@code timeOut} argument. The TestNG {@code timeOut}
 * attribute is always in milliseconds, the JUnit variant {@code @Timeout} takes a value in seconds
 * by default, hence we add the {@link java.util.concurrent.TimeUnit#MILLISECONDS} unit.
 */
@Immutable
final class TimeOutAttributeMigrator implements Migrator {
  @Override
  public Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, ExpressionTree dataValue, VisitorState state) {
    return Optional.of(
        SuggestedFix.builder()
            .addImport("org.junit.jupiter.api.Timeout")
            .addStaticImport("java.util.concurrent.TimeUnit.MILLISECONDS")
            .merge(
                SuggestedFix.prefixWith(
                    methodTree,
                    String.format(
                        "@Timeout(value = %s, unit = MILLISECONDS)\n",
                        SourceCode.treeToString(dataValue, state))))
            .build());
  }

  @Override
  public boolean canFix(
      TestNGMetadata metadata,
      TestNGMetadata.AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    return annotation.getAttributes().containsKey("timeOut");
  }
}
