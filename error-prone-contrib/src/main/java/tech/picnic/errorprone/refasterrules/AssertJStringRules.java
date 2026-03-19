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
  static final class AbstractStringAssertIsEmpty {
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
  static final class AbstractStringAssertIsNotEmpty {
    @BeforeTemplate
    AbstractStringAssert<?> before(AbstractStringAssert<?> stringAssert) {
      return stringAssert.isNotEqualTo("");
    }

    @AfterTemplate
    AbstractStringAssert<?> after(AbstractStringAssert<?> stringAssert) {
      return stringAssert.isNotEmpty();
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#startsWith(CharSequence)} over more contrived alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatStartsWith {
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
   * Prefer {@link AbstractStringAssert#doesNotStartWith(CharSequence)} over more contrived
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatDoesNotStartWith {
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

  /**
   * Prefer {@link AbstractStringAssert#endsWith(CharSequence)} over more contrived alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatEndsWith {
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
   * Prefer {@link AbstractStringAssert#doesNotEndWith(CharSequence)} over more contrived
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatDoesNotEndWith {
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
  static final class AssertThatContains {
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
  static final class AssertThatDoesNotContain {
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

  /** Prefer {@link AbstractStringAssert#matches(CharSequence)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
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

  /**
   * Prefer {@link AbstractStringAssert#doesNotMatch(CharSequence)} over more contrived
   * alternatives.
   */
  @PossibleSourceIncompatibility
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
  static final class AssertThatContent {
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
  static final class AssertThatContentUtf8 {
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
