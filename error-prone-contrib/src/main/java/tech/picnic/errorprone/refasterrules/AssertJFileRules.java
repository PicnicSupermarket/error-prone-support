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

/** Refaster rules related to AssertJ assertions over {@link File}s. */
@OnlineDocumentation
final class AssertJFileRules {
  private AssertJFileRules() {}

  /** Prefer {@link AbstractFileAssert#exists()} over more verbose alternatives. */
  static final class AssertThatExists {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(File actual) {
      return assertThat(actual.exists()).isTrue();
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual) {
      return assertThat(actual).exists();
    }
  }

  /** Prefer {@link AbstractFileAssert#doesNotExist()} over more verbose alternatives. */
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

  /** Prefer {@link AbstractFileAssert#isFile()} over more verbose alternatives. */
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

  /** Prefer {@link AbstractFileAssert#isDirectory()} over more verbose alternatives. */
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

  /** Prefer {@link AbstractFileAssert#isAbsolute()} over more verbose alternatives. */
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

  /** Prefer {@link AbstractFileAssert#isRelative()} over more verbose alternatives. */
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

  /** Prefer {@link AbstractFileAssert#isReadable()} over more verbose alternatives. */
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

  /** Prefer {@link AbstractFileAssert#isWritable()} over more verbose alternatives. */
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

  /** Prefer {@link AbstractFileAssert#isExecutable()} over more verbose alternatives. */
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

  /** Prefer {@link AbstractFileAssert#hasFileName(String)} over more verbose alternatives. */
  static final class AssertThatHasFileName {
    @BeforeTemplate
    AbstractStringAssert<?> before(File actual, String expected) {
      return assertThat(actual.getName()).isEqualTo(expected);
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual, String expected) {
      return assertThat(actual).hasFileName(expected);
    }
  }

  /** Prefer {@link AbstractFileAssert#hasParent(File)} over more verbose alternatives. */
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

  /** Prefer {@link AbstractFileAssert#hasParent(String)} over more verbose alternatives. */
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

  /** Prefer {@link AbstractFileAssert#hasNoParent()} over more verbose alternatives. */
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
   * Prefer {@link AbstractFileAssert#hasExtension(String)} over more verbose or less explicit
   * alternatives.
   */
  static final class AssertThatHasExtension {
    @BeforeTemplate
    AbstractStringAssert<?> before(File actual, String expected) {
      return assertThat(Refaster.anyOf(actual.getName(), actual.toString()))
          .endsWith(Refaster.anyOf('.' + expected, "." + expected));
    }

    @AfterTemplate
    AbstractFileAssert<?> after(File actual, String expected) {
      return assertThat(actual).hasExtension(expected);
    }
  }
}
