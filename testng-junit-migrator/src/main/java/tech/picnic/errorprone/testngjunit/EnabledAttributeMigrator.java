package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.TestNGMetadata.AnnotationMetadata;

/** A {@link Migrator} that migrates the {@code enabled} attribute. */
@Immutable
final class EnabledAttributeMigrator implements Migrator {
  @Override
  public Optional<SuggestedFix> createFix(
      ClassTree classTree, MethodTree methodTree, ExpressionTree dataValue, VisitorState state) {
    return Optional.ofNullable(((LiteralTree) dataValue).getValue())
        .filter(Boolean.FALSE::equals)
        .map(
            unused ->
                SuggestedFix.builder()
                    .addImport("org.junit.jupiter.api.Disabled")
                    .prefixWith(methodTree, "@Disabled\n")
                    .build());
  }

  @Override
  public boolean canFix(
      TestNGMetadata metadata,
      AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    return annotation.getAttributes().containsKey("enabled");
  }
}
