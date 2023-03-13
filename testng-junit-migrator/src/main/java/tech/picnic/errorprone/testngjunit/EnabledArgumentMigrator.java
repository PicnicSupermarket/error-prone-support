package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;

/**
 * A {@link tech.picnic.errorprone.testngjunit.Migrator} that migrates the {@code enabled} argument.
 */
@Immutable
final class EnabledArgumentMigrator implements Migrator {
  @Override
  public Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, ExpressionTree dataValue, VisitorState state) {
    if (!(boolean) ((LiteralTree) dataValue).getValue()) {
      return Optional.of(
          SuggestedFix.builder()
              .addImport("org.junit.jupiter.api.Disabled")
              .merge(SuggestedFix.prefixWith(methodTree, "@Disabled\n"))
              .build());
    }

    return Optional.empty();
  }

  @Override
  public boolean canFix(
      TestNGMetadata metadata,
      TestNGMetadata.AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    return annotation.getArguments().containsKey("enabled");
  }
}
