package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractPathAssert;
import org.assertj.core.api.AbstractStringAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/**
 * Refaster rules related to AssertJ assertions over {@link Path}s.
 *
 * <p>These rules simplify and improve the readability of tests by using {@link Path}-specific
 * AssertJ assertion methods instead of generic assertions.
 */
@OnlineDocumentation
final class AssertJPathRules {
  private AssertJPathRules() {}

  static final class AssertThatExists {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path actual) {
      return assertThat(Files.exists(actual)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual) {
      return assertThat(actual).exists();
    }
  }

  static final class AssertThatDoesNotExist {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path actual) {
      return assertThat(Files.exists(actual)).isFalse();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual) {
      return assertThat(actual).doesNotExist();
    }
  }

  static final class AssertThatIsRegularFile {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path actual) {
      return assertThat(Files.isRegularFile(actual)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual) {
      return assertThat(actual).isRegularFile();
    }
  }

  static final class AssertThatIsDirectory {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path actual) {
      return assertThat(Files.isDirectory(actual)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual) {
      return assertThat(actual).isDirectory();
    }
  }

  static final class AssertThatIsSymbolicLink {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path actual) {
      return assertThat(Files.isSymbolicLink(actual)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual) {
      return assertThat(actual).isSymbolicLink();
    }
  }

  static final class AssertThatIsAbsolute {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path actual) {
      return assertThat(actual.isAbsolute()).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual) {
      return assertThat(actual).isAbsolute();
    }
  }

  static final class AssertThatIsRelative {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path actual) {
      return assertThat(actual.isAbsolute()).isFalse();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual) {
      return assertThat(actual).isRelative();
    }
  }

  static final class AssertThatIsReadable {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path actual) {
      return assertThat(Files.isReadable(actual)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual) {
      return assertThat(actual).isReadable();
    }
  }

  static final class AssertThatIsWritable {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path actual) {
      return assertThat(Files.isWritable(actual)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual) {
      return assertThat(actual).isWritable();
    }
  }

  static final class AssertThatIsExecutable {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path actual) {
      return assertThat(Files.isExecutable(actual)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual) {
      return assertThat(actual).isExecutable();
    }
  }

  // XXX: This rule changes the `Path` against which subsequent assertions are made.
  static final class AssertThatHasFileName {
    @BeforeTemplate
    AbstractPathAssert<?> before(Path actual, String fileName) {
      return assertThat(actual.getFileName()).hasToString(fileName);
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual, String fileName) {
      return assertThat(actual).hasFileName(fileName);
    }
  }

  static final class AssertThatHasParentRaw {
    @BeforeTemplate
    AbstractPathAssert<?> before(Path actual, Path expected) {
      return assertThat(actual.getParent()).isEqualTo(expected);
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual, Path expected) {
      return assertThat(actual).hasParentRaw(expected);
    }
  }

  static final class AssertThatHasNoParent {
    @BeforeTemplate
    void before(Path actual) {
      assertThat(actual.getParent()).isNull();
    }

    @AfterTemplate
    void after(Path actual) {
      assertThat(actual).hasNoParent();
    }
  }

  static final class AssertThatStartsWithRaw {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path actual, Path other) {
      return assertThat(actual.startsWith(other)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual, Path other) {
      return assertThat(actual).startsWithRaw(other);
    }
  }

  static final class AssertThatEndsWithRaw {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path actual, Path other) {
      return assertThat(actual.endsWith(other)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual, Path other) {
      return assertThat(actual).endsWithRaw(other);
    }
  }

  /**
   * Prefer using {@link AbstractPathAssert#hasExtension(String)} over more verbose and less
   * accurate alternatives.
   */
  static final class AssertThatHasExtension {
    @BeforeTemplate
    AbstractStringAssert<?> before(Path actual, String expectedExtension) {
      return assertThat(Refaster.anyOf(actual.getFileName().toString(), actual.toString()))
          .endsWith(Refaster.anyOf('.' + expectedExtension, "." + expectedExtension));
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path actual, String expectedExtension) {
      return assertThat(actual).hasExtension(expectedExtension);
    }
  }
}
