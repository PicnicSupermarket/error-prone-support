package tech.picnic.errorprone.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.FileManagers;
import com.google.errorprone.FileObjects;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import java.io.File;
import java.io.Writer;
import java.nio.file.Path;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
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
  void noClassNoOutput(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    compile(outputPath.toString(), "package pkg;");

    Path docsPath = outputPath.resolve("docs").toAbsolutePath();
    assertThat(docsPath).isEmptyDirectory();
  }

  @Test
  void twoArgumentsFailsInitPlugin() {
    JavaFileObject javaFileObject =
        FileObjects.forSourceLines("TaskListenerTestInput.java", "package pkg;");
    JavacTaskImpl task =
        (JavacTaskImpl)
            JavacTool.create()
                .getTask(
                    Writer.nullWriter(),
                    FileManagers.testFileManager(),
                    new DiagnosticCollector<>(),
                    ImmutableList.of(
                        "-Xplugin:DocumentationGenerator -XdocsOutputDirectory=arg1 -XdocsOutputDirectory=arg2"),
                    ImmutableList.of(),
                    ImmutableList.of(javaFileObject));

    assertThatThrownBy(task::call)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Specify only one output path");
  }
}
