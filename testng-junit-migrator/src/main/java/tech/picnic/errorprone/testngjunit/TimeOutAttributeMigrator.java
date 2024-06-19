package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.fixes.SuggestedFix;
import com.sun.source.tree.MethodTree;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import tech.picnic.errorprone.util.SourceCode;

/**
 * A {@link AttributeMigrator} that migrates the {@code org.testng.annotations.Test#timeOut}
 * attribute. The TestNG {@code org.testng.annotations.Test#timeOut} attribute is always in
 * milliseconds, the JUnit variant {@code @Timeout} takes a value in seconds by default, hence we
 * add the {@link java.util.concurrent.TimeUnit#MILLISECONDS} attribute.
 */
@Immutable
final class TimeOutAttributeMigrator implements AttributeMigrator {
  @Override
  public Optional<SuggestedFix> migrate(
      TestNgMetadata metadata,
      TestNgMetadata.AnnotationMetadata annotation,
      MethodTree methodTree,
      VisitorState state) {
    return Optional.ofNullable(annotation.getAttributes().get("timeOut"))
        .map(
            timeOut ->
                SuggestedFix.builder()
                    .addImport("org.junit.jupiter.api.Timeout")
                    .addStaticImport(TimeUnit.class.getCanonicalName() + ".MILLISECONDS")
                    .prefixWith(
                        methodTree,
                        String.format(
                            "@Timeout(value = %s, unit = MILLISECONDS)%n",
                            SourceCode.treeToString(timeOut, state)))
                    .build());
  }
}
