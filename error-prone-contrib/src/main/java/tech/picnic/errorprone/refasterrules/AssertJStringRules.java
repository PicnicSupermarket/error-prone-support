package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractStringAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/** Refaster rules related to AssertJ assertions over {@link String}s. */
@OnlineDocumentation
final class AssertJStringRules {
  private AssertJStringRules() {}

  /** Prefer {@link AbstractStringAssert#isEmpty()} over less explicit alternatives. */
  static final class AbstractStringAssertStringIsEmpty {
    @BeforeTemplate
    void before(AbstractStringAssert<?> stringAssert) {
      stringAssert.isEqualTo("");
    }

    @AfterTemplate
    void after(AbstractStringAssert<?> stringAssert) {
      stringAssert.isEmpty();
    }
  }

  /** Prefer {@link AbstractStringAssert#isNotEmpty()} over less explicit alternatives. */
  static final class AbstractStringAssertStringIsNotEmpty {
    @BeforeTemplate
    AbstractStringAssert<?> before(AbstractStringAssert<?> stringAssert) {
      return stringAssert.isNotEqualTo("");
    }

    @AfterTemplate
    AbstractStringAssert<?> after(AbstractStringAssert<?> stringAssert) {
      return stringAssert.isNotEmpty();
    }
  }

  /** Prefer {@link AbstractStringAssert#startsWith(String)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatStringStartsWith {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String string, String prefix) {
      return assertThat(string.startsWith(prefix)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String string, String prefix) {
      return assertThat(string).startsWith(prefix);
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#doesNotStartWith(String)} over more contrived alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatStringDoesNotStartWith {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String string, String prefix) {
      return assertThat(string.startsWith(prefix)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String string, String prefix) {
      return assertThat(string).doesNotStartWith(prefix);
    }
  }

  /** Prefer {@link AbstractStringAssert#endsWith(String)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatStringEndsWith {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String string, String suffix) {
      return assertThat(string.endsWith(suffix)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String string, String suffix) {
      return assertThat(string).endsWith(suffix);
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#doesNotEndWith(String)} over more contrived alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatStringDoesNotEndWith {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String string, String suffix) {
      return assertThat(string.endsWith(suffix)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String string, String suffix) {
      return assertThat(string).doesNotEndWith(suffix);
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#contains(CharSequence...)} over more contrived alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatStringContains {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String string, CharSequence substring) {
      return assertThat(string.contains(substring)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String string, CharSequence substring) {
      return assertThat(string).contains(substring);
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#doesNotContain(CharSequence...)} over more contrived
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatStringDoesNotContain {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String string, CharSequence substring) {
      return assertThat(string.contains(substring)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String string, CharSequence substring) {
      return assertThat(string).doesNotContain(substring);
    }
  }

  /** Prefer {@link AbstractStringAssert#matches(String)} over more contrived alternatives. */
  static final class AssertThatMatches {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String string, String regex) {
      return assertThat(string.matches(regex)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String string, String regex) {
      return assertThat(string).matches(regex);
    }
  }

  /** Prefer {@link AbstractStringAssert#doesNotMatch(String)} over more contrived alternatives. */
  static final class AssertThatDoesNotMatch {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String string, String regex) {
      return assertThat(string.matches(regex)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String string, String regex) {
      return assertThat(string).doesNotMatch(regex);
    }
  }

  /** Prefer {@code assertThat(path).content(charset)} over more contrived alternatives. */
  static final class AssertThatPathContent {
    @BeforeTemplate
    AbstractStringAssert<?> before(Path path, Charset charset) throws IOException {
      return assertThat(Files.readString(path, charset));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(Path path, Charset charset) {
      return assertThat(path).content(charset);
    }
  }

  /** Prefer {@code assertThat(path).content(UTF_8)} over more contrived alternatives. */
  static final class AssertThatPathContentUtf8 {
    @BeforeTemplate
    AbstractStringAssert<?> before(Path path) throws IOException {
      return assertThat(Files.readString(path));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(Path path) {
      return assertThat(path).content(UTF_8);
    }
  }
}
