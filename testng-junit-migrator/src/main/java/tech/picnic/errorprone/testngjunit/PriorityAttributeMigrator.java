package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.testngjunit.TestNgMetadata.AnnotationMetadata;
import tech.picnic.errorprone.util.SourceCode;

/**
 * A {@link AttributeMigrator} that migrates the {@code org.testng.annotations.Test#priority}
 * attribute.
 */
@Immutable
final class PriorityAttributeMigrator implements AttributeMigrator {
  @Override
  public Optional<SuggestedFix> migrate(
      TestNgMetadata metadata,
      AnnotationMetadata annotation,
      MethodTree methodTree,
      boolean minimalChangesMode,
      VisitorState state) {
    return Optional.ofNullable(annotation.getAttributes().get("priority"))
        .map(
            priority ->
                SuggestedFix.builder()
                    .addImport("org.junit.jupiter.api.Order")
                    .addImport("org.junit.jupiter.api.TestMethodOrder")
                    .addImport("org.junit.jupiter.api.MethodOrderer")
                    .prefixWith(
                        methodTree,
                        String.format("@Order(%s)%n", SourceCode.treeToString(priority, state)))
                    .prefixWith(
                        metadata.getClassTree(),
                        "@TestMethodOrder(MethodOrderer.OrderAnnotation.class)\n")
                    .build());
  }
}
