package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

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
 * <p>These rules simplify and improve the readability of Path assertions by using the more specific
 * AssertJ Path assertion methods instead of generic assertions.
 */
@OnlineDocumentation
final class AssertJPathRules {
  private AssertJPathRules() {}

  static final class AbstractPathAssertExists {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path) {
      return assertThat(Files.exists(path)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path) {
      return assertThat(path).exists();
    }
  }

  static final class AbstractPathAssertDoesNotExist {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path) {
      return assertThat(Files.exists(path)).isFalse();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path) {
      return assertThat(path).doesNotExist();
    }
  }

  static final class AbstractPathAssertIsRegularFile {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path) {
      return assertThat(Files.isRegularFile(path)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path) {
      return assertThat(path).isRegularFile();
    }
  }

  static final class AbstractPathAssertIsDirectory {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path) {
      return assertThat(Files.isDirectory(path)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path) {
      return assertThat(path).isDirectory();
    }
  }

  static final class AbstractPathAssertIsSymbolicLink {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path) {
      return assertThat(Files.isSymbolicLink(path)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path) {
      return assertThat(path).isSymbolicLink();
    }
  }

  static final class AbstractPathAssertIsAbsolute {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path) {
      return assertThat(path.isAbsolute()).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path) {
      return assertThat(path).isAbsolute();
    }
  }

  static final class AbstractPathAssertIsRelative {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path) {
      return assertThat(path.isAbsolute()).isFalse();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path) {
      return assertThat(path).isRelative();
    }
  }

  static final class AbstractPathAssertIsReadable {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path) {
      return assertThat(Files.isReadable(path)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path) {
      return assertThat(path).isReadable();
    }
  }

  static final class AbstractPathAssertIsWritable {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path) {
      return assertThat(Files.isWritable(path)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path) {
      return assertThat(path).isWritable();
    }
  }

  static final class AbstractPathAssertIsExecutable {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path) {
      return assertThat(Files.isExecutable(path)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path) {
      return assertThat(path).isExecutable();
    }
  }

  static final class AbstractPathAssertHasFileName {
    @BeforeTemplate
    @SuppressWarnings("AssertThatHasToString")
    AbstractStringAssert<?> before(Path path, String fileName) {
      return assertThat(path.getFileName().toString()).isEqualTo(fileName);
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path, String fileName) {
      return assertThat(path).hasFileName(fileName);
    }
  }

  static final class AbstractPathAssertHasParent {
    @BeforeTemplate
    AbstractPathAssert<?> before(Path path, Path parent) {
      return assertThat(path.getParent()).isEqualTo(parent);
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path, Path parent) {
      return assertThat(path).hasParent(parent);
    }
  }

  static final class AbstractPathAssertHasNoParent {
    @BeforeTemplate
    void before(Path path) {
      assertThat(path.getParent()).isNull();
    }

    @AfterTemplate
    void after(Path path) {
      assertThat(path).hasNoParent();
    }
  }

  static final class AbstractPathAssertStartsWith {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path, Path other) {
      return assertThat(path.startsWith(other)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path, Path other) {
      return assertThat(path).startsWith(other);
    }
  }

  static final class AbstractPathAssertEndsWith {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path, Path other) {
      return assertThat(path.endsWith(other)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path, Path other) {
      return assertThat(path).endsWith(other);
    }
  }

  static final class AbstractPathAssertHasExtension {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(Path path, String extension) {
      return assertThat(path.toString().endsWith("." + extension)).isTrue();
    }

    @AfterTemplate
    AbstractPathAssert<?> after(Path path, String extension) {
      return assertThat(path).hasExtension(extension);
    }
  }
}
