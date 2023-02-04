package tech.picnic.errorprone.documentation;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.nio.file.attribute.AclEntryPermission.ADD_SUBDIRECTORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.errorprone.FileObjects;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;

final class DocumentationGeneratorTaskListenerTest {
  @EnabledOnOs(WINDOWS)
  @Test
  void readOnlyFileSystemWindows(@TempDir Path directory) throws IOException {
    AclFileAttributeView view = Files.getFileAttributeView(directory, AclFileAttributeView.class);
    view.setAcl(
        view.getAcl().stream()
            .map(
                entry ->
                    AclEntry.newBuilder(entry)
                        .setPermissions(
                            Sets.difference(entry.permissions(), ImmutableSet.of(ADD_SUBDIRECTORY)))
                        .build())
            .collect(toImmutableList()));

    readOnlyFileSystemFailsToWrite(directory.resolve("nonexistent"));
  }

  @DisabledOnOs(WINDOWS)
  @Test
  void readOnlyFileSystemOtherOperatingSystems(@TempDir Path directory) {
    assertThat(directory.toFile().setWritable(false))
        .describedAs("Failed to make test directory unwritable")
        .isTrue();

    readOnlyFileSystemFailsToWrite(directory.resolve("nonexistent"));
  }

  private static void readOnlyFileSystemFailsToWrite(Path testPath) {
    assertThatThrownBy(() -> JavacTaskCompilation.compile(testPath, "A.java", "public class A {}"))
        .hasRootCauseInstanceOf(FileSystemException.class)
        .hasCauseInstanceOf(IllegalStateException.class)
        .hasMessageEndingWith("Error while creating directory with path '%s'", testPath);
  }

  @Test
  void emptyDirectoryWhenNotStartingKindAnalyze(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg");
    JavacTaskCompilation.compile(outputPath, "A.java", "package pkg;");

    assertThat(directory).isEmptyDirectory();
  }

  @Test
  void noClassNoOutput(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg");
    JavacTaskCompilation.compile(outputPath, "A.java", "package pkg;");

    assertThat(directory).isEmptyDirectory();
  }

  @Test
  void twoArgumentsFailsInitPlugin(@TempDir Path directory) {
    Path outputPath = directory.resolve("pkg").toAbsolutePath();

    assertThatThrownBy(
            () ->
                JavacTaskCompilation.compile(
                    outputPath + " -XoutputDirectory=arg2", "A.java", "package pkg;"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Precisely one path must be provided");
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
                ImmutableList.of("-Xplugin:DocumentationGenerator -XoutputDirectory=" + outputPath),
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
