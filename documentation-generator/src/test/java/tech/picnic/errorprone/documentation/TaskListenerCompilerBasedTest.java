package tech.picnic.errorprone.documentation;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.FileObjects;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import java.nio.file.Path;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;

// XXX: Convert this to a utility class.
// XXX: Compiled classes should be written to an in-memory file-system such as `jimfs` instead.
// XXX: Generalize and move this class so that it can also be used by `refaster-compiler`.
// XXX: Add support for this class to the `ErrorProneTestHelperSourceFormat` check.
abstract class TaskListenerCompilerBasedTest {
  public void compile(Path path, String fileName, String... lines) {
    performCompilationForFile(
        path.toAbsolutePath().toString(), FileObjects.forSourceLines(fileName, lines));
  }

  public void compile(String outputDirectory, String fileName, String... lines) {
    performCompilationForFile(outputDirectory, FileObjects.forSourceLines(fileName, lines));
  }

  private static void performCompilationForFile(
      String outputDirectory, JavaFileObject javaFileObject) {
    JavaCompiler compiler = JavacTool.create();
    JavacTaskImpl task =
        (JavacTaskImpl)
            compiler.getTask(
                null,
                null,
                null,
                ImmutableList.of(
                    "-Xplugin:DocumentationGenerator -XdocsOutputDirectory=" + outputDirectory),
                ImmutableList.of(),
                ImmutableList.of(javaFileObject));

    task.call();
  }
}
