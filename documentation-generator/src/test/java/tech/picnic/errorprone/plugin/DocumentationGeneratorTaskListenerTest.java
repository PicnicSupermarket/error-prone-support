package tech.picnic.errorprone.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import com.sun.source.util.TaskEvent.Kind;
import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;

final class DocumentationGeneratorTaskListenerTest extends TaskListenerCompilerBasedTest {
  @EnabledOnOs(WINDOWS)
  @Test
  void wrongPathFailsWindows() {
    wrongPathFails('?');
  }

  @DisabledOnOs(WINDOWS)
  @Test
  void wrongPathFailsOtherOperatingSystems() {
    // Strictly speaking we are validating here that we cannot write to a Read-only file system.
    wrongPathFails('/');
  }

  private void wrongPathFails(char invalidCharacter) {
    String invalidPath = invalidCharacter + "wrong-path";
    assertThatThrownBy(() -> compile(invalidPath))
        .hasCauseInstanceOf(IllegalStateException.class)
        .hasMessageEndingWith(
            "Error while creating directory with path '%s'", invalidPath + File.separator + "docs");
  }

  @Test
  void noDirectoryForTaskEventKindOtherThenAnalyze(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    compile(outputPath.toString(), Kind.ANALYZE, "package pkg;");

    assertThat(directory).isEmptyDirectory();
  }

  @Test
  void noClassNoOutput(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    compile(outputPath.toString(), "package pkg;");

    Path docsPath = outputPath.resolve("docs").toAbsolutePath();
    assertThat(docsPath).isEmptyDirectory();
  }

  @Test
  void twoArgumentsFailsInitPlugin(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();

    assertThatThrownBy(() -> compile(outputPath + " -XdocsOutputDirectory=arg2", "package pkg;"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Specify only one output path");
  }
}
