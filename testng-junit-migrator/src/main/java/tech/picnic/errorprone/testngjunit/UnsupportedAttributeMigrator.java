package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import tech.picnic.errorprone.util.SourceCode;

/**
 * A {@link AttributeMigrator} that leaves a comment for attributes that aren't supported in the
 * migration.
 */
@Immutable
final class UnsupportedAttributeMigrator {
  private UnsupportedAttributeMigrator() {}

  static Optional<SuggestedFix> migrate(
      TestNGMetadata.AnnotationMetadata annotation,
      MethodTree methodTree,
      String attributeName,
      VisitorState state) {
    return Optional.ofNullable(annotation.getAttributes().get(attributeName))
        .map(
            value ->
                SuggestedFix.prefixWith(
                    methodTree,
                    String.format(
                        "// XXX: Attribute `%s` is not supported, value: `%s`%n",
                        attributeName, SourceCode.treeToString(value, state))));
  }
}
