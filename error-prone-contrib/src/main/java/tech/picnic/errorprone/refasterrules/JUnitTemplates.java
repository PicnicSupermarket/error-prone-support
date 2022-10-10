package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.junit.jupiter.params.provider.Arguments;

/** Refaster rules related to JUnit expressions and statements. */
final class JUnitTemplates {
  private JUnitTemplates() {}

  /** Prefer statically imported {@link Arguments#arguments} over {@link Arguments#of} calls. */
  static final class ArgumentsEnumeration<T> {
    @BeforeTemplate
    Arguments before(@Repeated T objects) {
      return Arguments.of(objects);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Arguments after(@Repeated T objects) {
      return arguments(objects);
    }
  }
}
