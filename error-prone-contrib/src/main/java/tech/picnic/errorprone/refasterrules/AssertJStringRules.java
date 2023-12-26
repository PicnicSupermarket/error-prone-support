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
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJStringRules {
  private AssertJStringRules() {}

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

  static final class AssertThatStringIsEmpty {
    @BeforeTemplate
    void before(String string) {
      assertThat(string.isEmpty()).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(String string) {
      assertThat(string).isEmpty();
    }
  }

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

  static final class AssertThatStringIsNotEmpty {
    @BeforeTemplate
    AbstractAssert<?, ?> before(String string) {
      return assertThat(string.isEmpty()).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractAssert<?, ?> after(String string) {
      return assertThat(string).isNotEmpty();
    }
  }

  static final class AssertThatMatches {
    @BeforeTemplate
    AbstractAssert<?, ?> before(String string, String regex) {
      return assertThat(string.matches(regex)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractAssert<?, ?> after(String string, String regex) {
      return assertThat(string).matches(regex);
    }
  }

  static final class AssertThatDoesNotMatch {
    @BeforeTemplate
    AbstractAssert<?, ?> before(String string, String regex) {
      return assertThat(string.matches(regex)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractAssert<?, ?> after(String string, String regex) {
      return assertThat(string).doesNotMatch(regex);
    }
  }

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
