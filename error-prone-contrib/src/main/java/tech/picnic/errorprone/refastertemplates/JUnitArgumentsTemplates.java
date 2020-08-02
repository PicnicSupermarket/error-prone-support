package tech.picnic.errorprone.refastertemplates;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Refaster templates which replaces argument creation for parametrized JUnit tests using {@link
 * Arguments#of} with statically imported {@link Arguments#arguments} calls.
 */
final class JUnitArgumentsTemplates {
  private JUnitArgumentsTemplates() {}

  /** Prefer statically imported {@link Arguments#arguments} over {@link Arguments#of} calls. */
  static final class ArgumentsReplace<T> {
    @BeforeTemplate
    Arguments before(@Repeated T objs) {
      return Arguments.of(objs);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    Arguments after(@Repeated T objs) {
      return arguments(objs);
    }
  }
}
