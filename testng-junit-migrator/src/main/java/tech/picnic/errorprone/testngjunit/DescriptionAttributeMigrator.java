package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.TestNgMetadata.AnnotationMetadata;
import tech.picnic.errorprone.util.SourceCode;

/** A {@link AttributeMigrator} that migrates the {@code description} attribute. */
@Immutable
final class DescriptionAttributeMigrator implements AttributeMigrator {
  @Override
  public Optional<SuggestedFix> migrate(
      TestNgMetadata metadata,
      AnnotationMetadata annotation,
      MethodTree methodTree,
      boolean minimalChangesMode,
      VisitorState state) {
    return Optional.ofNullable(annotation.getAttributes().get("description"))
        .map(
            description ->
                SuggestedFix.builder()
                    .addImport("org.junit.jupiter.api.DisplayName")
                    .prefixWith(
                        methodTree,
                        String.format(
                            "@DisplayName(%s)%n    ", SourceCode.treeToString(description, state)))
                    .build());
  }
}
