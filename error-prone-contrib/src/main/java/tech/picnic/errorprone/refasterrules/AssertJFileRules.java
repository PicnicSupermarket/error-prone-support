package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.io.File;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractFileAssert;
import org.assertj.core.api.AbstractStringAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/**
 * Refaster rules related to AssertJ assertions over {@link File}s.
 *
 * <p>These rules simplify and improve the readability of tests by using {@link File}-specific
 * AssertJ assertion methods instead of generic assertions.
 */
@OnlineDocumentation
final class AssertJFileRules {
  private AssertJFileRules() {}

  static final class AssertThatDoesNotExist {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(File actual) {
      return assertThat(actual.exists()).isFalse();
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual) {
      return assertThat(actual).doesNotExist();
    }
  }

  static final class AssertThatIsFile {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(File actual) {
      return assertThat(actual.isFile()).isTrue();
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual) {
      return assertThat(actual).isFile();
    }
  }

  static final class AssertThatIsDirectory {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(File actual) {
      return assertThat(actual.isDirectory()).isTrue();
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual) {
      return assertThat(actual).isDirectory();
    }
  }

  static final class AssertThatIsAbsolute {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(File actual) {
      return assertThat(actual.isAbsolute()).isTrue();
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual) {
      return assertThat(actual).isAbsolute();
    }
  }

  static final class AssertThatIsRelative {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(File actual) {
      return assertThat(actual.isAbsolute()).isFalse();
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual) {
      return assertThat(actual).isRelative();
    }
  }

  static final class AssertThatIsReadable {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(File actual) {
      return assertThat(actual.canRead()).isTrue();
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual) {
      return assertThat(actual).isReadable();
    }
  }

  static final class AssertThatIsWritable {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(File actual) {
      return assertThat(actual.canWrite()).isTrue();
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual) {
      return assertThat(actual).isWritable();
    }
  }

  static final class AssertThatIsExecutable {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(File actual) {
      return assertThat(actual.canExecute()).isTrue();
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual) {
      return assertThat(actual).isExecutable();
    }
  }

  static final class AssertThatHasFileName {
    @BeforeTemplate
    AbstractStringAssert<?> before(File actual, String fileName) {
      return assertThat(actual.getName()).isEqualTo(fileName);
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual, String fileName) {
      return assertThat(actual).hasFileName(fileName);
    }
  }

  // XXX: This rule changes the `File` against which subsequent assertions are made.
  static final class AssertThatHasParentFile {
    @BeforeTemplate
    AbstractFileAssert<?> before(File actual, File expected) {
      return assertThat(actual.getParentFile()).isEqualTo(expected);
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual, File expected) {
      return assertThat(actual).hasParent(expected);
    }
  }

  // XXX: This rule changes the `File` against which subsequent assertions are made.
  static final class AssertThatHasParentString {
    @BeforeTemplate
    AbstractFileAssert<?> before(File actual, String expected) {
      return assertThat(actual.getParentFile()).hasFileName(expected);
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual, String expected) {
      return assertThat(actual).hasParent(expected);
    }
  }

  static final class AssertThatHasNoParent {
    @BeforeTemplate
    void before(File actual) {
      assertThat(actual.getParent()).isNull();
    }

    @AfterTemplate
    void after(File actual) {
      assertThat(actual).hasNoParent();
    }
  }

  /**
   * Prefer using {@link AbstractFileAssert#hasExtension(String)} over more verbose and less
   * accurate alternatives.
   */
  static final class AssertThatHasExtension {
    @BeforeTemplate
    AbstractStringAssert<?> before(File actual, String expectedExtension) {
      return assertThat(Refaster.anyOf(actual.getName(), actual.toString()))
          .endsWith(Refaster.anyOf('.' + expectedExtension, "." + expectedExtension));
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual, String expectedExtension) {
      return assertThat(actual).hasExtension(expectedExtension);
    }
  }
}
