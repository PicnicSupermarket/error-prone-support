package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.TestNgMetadata.AnnotationMetadata;

/** A {@link AttributeMigrator} that migrates the {@code enabled} attribute. */
@Immutable
final class EnabledAttributeMigrator implements AttributeMigrator {
  @Override
  public Optional<SuggestedFix> migrate(
      TestNgMetadata metadata,
      AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    return Optional.ofNullable(annotation.getAttributes().get("enabled"))
        .map(enabled -> ((LiteralTree) enabled).getValue())
        .filter(Boolean.FALSE::equals)
        .map(
            unused ->
                SuggestedFix.builder()
                    .addImport("org.junit.jupiter.api.Disabled")
                    .prefixWith(methodTree, "@Disabled\n    ")
                    .build())
        .or(() -> Optional.of(SuggestedFix.emptyFix()));
  }
}
