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
  static final class AssertThatStartsWith {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String actual, String prefix) {
      return assertThat(actual.startsWith(prefix)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String actual, String prefix) {
      return assertThat(actual).startsWith(prefix);
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#doesNotStartWith(CharSequence)} over more contrived
   * alternatives.
   */
  static final class AssertThatDoesNotStartWith {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String actual, String prefix) {
      return assertThat(actual.startsWith(prefix)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String actual, String prefix) {
      return assertThat(actual).doesNotStartWith(prefix);
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#endsWith(CharSequence)} over more contrived alternatives.
   */
  static final class AssertThatEndsWith {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String actual, String suffix) {
      return assertThat(actual.endsWith(suffix)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String actual, String suffix) {
      return assertThat(actual).endsWith(suffix);
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#doesNotEndWith(CharSequence)} over more contrived
   * alternatives.
   */
  static final class AssertThatDoesNotEndWith {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String actual, String suffix) {
      return assertThat(actual.endsWith(suffix)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String actual, String suffix) {
      return assertThat(actual).doesNotEndWith(suffix);
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#contains(CharSequence...)} over more contrived alternatives.
   */
  static final class AssertThatContains {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String actual, CharSequence s) {
      return assertThat(actual.contains(s)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String actual, CharSequence s) {
      return assertThat(actual).contains(s);
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#doesNotContain(CharSequence...)} over more contrived
   * alternatives.
   */
  static final class AssertThatDoesNotContain {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String actual, CharSequence s) {
      return assertThat(actual.contains(s)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String actual, CharSequence s) {
      return assertThat(actual).doesNotContain(s);
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#isEqualToIgnoringCase(CharSequence)} over less explicit
   * alternatives.
   */
  static final class AssertThatIsEqualToIgnoringCase {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String actual, String expected) {
      return assertThat(actual.equalsIgnoreCase(expected)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String actual, String expected) {
      return assertThat(actual).isEqualToIgnoringCase(expected);
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#isNotEqualToIgnoringCase(CharSequence)} over less explicit
   * alternatives.
   */
  static final class AssertThatIsNotEqualToIgnoringCase {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String actual, String expected) {
      return assertThat(actual.equalsIgnoreCase(expected)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String actual, String expected) {
      return assertThat(actual).isNotEqualToIgnoringCase(expected);
    }
  }

  /** Prefer {@link AbstractStringAssert#isBlank()} over less explicit alternatives. */
  static final class AssertThatIsBlank {
    @BeforeTemplate
    void before(String actual) {
      assertThat(actual.isBlank()).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(String actual) {
      assertThat(actual).isBlank();
    }
  }

  /** Prefer {@link AbstractStringAssert#isNotBlank()} over less explicit alternatives. */
  static final class AssertThatIsNotBlank {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String actual) {
      return assertThat(actual.isBlank()).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String actual) {
      return assertThat(actual).isNotBlank();
    }
  }

  /** Prefer {@link AbstractStringAssert#matches(CharSequence)} over more contrived alternatives. */
  static final class AssertThatMatches {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String actual, String regex) {
      return assertThat(actual.matches(regex)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String actual, String regex) {
      return assertThat(actual).matches(regex);
    }
  }

  /**
   * Prefer {@link AbstractStringAssert#doesNotMatch(CharSequence)} over more contrived
   * alternatives.
   */
  static final class AssertThatDoesNotMatch {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(String actual, String regex) {
      return assertThat(actual.matches(regex)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(String actual, String regex) {
      return assertThat(actual).doesNotMatch(regex);
    }
  }

  /** Prefer {@code assertThat(path).content(charset)} over more contrived alternatives. */
  static final class AssertThatContent {
    @BeforeTemplate
    AbstractStringAssert<?> before(Path actual, Charset charset) throws IOException {
      return assertThat(Files.readString(actual, charset));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(Path actual, Charset charset) {
      return assertThat(actual).content(charset);
    }
  }

  /** Prefer {@code assertThat(path).content(UTF_8)} over more contrived alternatives. */
  static final class AssertThatContentUtf8 {
    @BeforeTemplate
    AbstractStringAssert<?> before(Path actual) throws IOException {
      return assertThat(Files.readString(actual));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractStringAssert<?> after(Path actual) {
      return assertThat(actual).content(UTF_8);
    }
  }
}
