package tech.picnic.errorprone.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.FileObjects;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import java.io.File;
import java.nio.file.Path;
import javax.tools.JavaCompiler;
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
    assertThatThrownBy(() -> compile(invalidPath, "A.java", "public class A {}"))
        .hasCauseInstanceOf(IllegalStateException.class)
        .hasMessageEndingWith(
            "Error while creating directory with path '%s'", invalidPath + File.separator + "docs");
  }

  @Test
  void emptyDirectoryWhenNotStartingKindAnalyze(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    compile(outputPath.toString(), "A.java", "package pkg;");

    assertThat(directory).isEmptyDirectory();
  }

  @Test
  void noClassNoOutput(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    compile(outputPath.toString(), "A.java", "package pkg;");

    assertThat(directory).isEmptyDirectory();
  }

  @Test
  void twoArgumentsFailsInitPlugin(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();

    assertThatThrownBy(
            () -> compile(outputPath + " -XdocsOutputDirectory=arg2", "A.java", "package pkg;"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Specify only one output path");
  }

  @Test
  void skipTaskListenerStartedCreatesNoDirectories(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();
    JavaFileObject javaFileObject =
        FileObjects.forSourceLines(
            "A.java",
            "package pkg;",
            "",
            "import com.google.errorprone.bugpatterns.BugChecker;",
            "",
            "public final class A extends BugChecker {}");
    JavaCompiler compiler = JavacTool.create();
    JavacTaskImpl task =
        (JavacTaskImpl)
            compiler.getTask(
                null,
                null,
                null,
                ImmutableList.of(
                    "-Xplugin:DocumentationGenerator -XdocsOutputDirectory=" + outputPath),
                ImmutableList.of(),
                ImmutableList.of(javaFileObject));

    task.parse();
    TaskEvent taskEvent = new TaskEvent(Kind.ANALYZE, javaFileObject);

    for (TaskListener tl : task.getTaskListeners()) {
      tl.finished(taskEvent);
    }

    assertThat(directory).isEmptyDirectory();
  }
}
