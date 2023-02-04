package tech.picnic.errorprone.documentation;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.FileManagers;
import com.google.errorprone.FileObjects;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.file.JavacFileManager;
import java.nio.file.Path;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;

// XXX: Generalize and move this class so that it can also be used by `refaster-compiler`.
// XXX: Add support for this class to the `ErrorProneTestHelperSourceFormat` check.
public final class JavacTaskCompilation {
  private JavacTaskCompilation() {}

  public static void compile(Path outputDirectory, String fileName, String... lines) {
    performCompilationForFile(
        outputDirectory.toAbsolutePath().toString(), FileObjects.forSourceLines(fileName, lines));
  }

  public static void compile(String outputDirectory, String fileName, String... lines) {
    performCompilationForFile(outputDirectory, FileObjects.forSourceLines(fileName, lines));
  }

  private static void performCompilationForFile(
      String outputDirectory, JavaFileObject javaFileObject) {
    JavacFileManager javacFileManager = FileManagers.testFileManager();
    JavaCompiler compiler = JavacTool.create();
    JavacTaskImpl task =
        (JavacTaskImpl)
            compiler.getTask(
                null,
                javacFileManager,
                null,
                ImmutableList.of(
                    "-Xplugin:DocumentationGenerator -XoutputDirectory=" + outputDirectory),
                ImmutableList.of(),
                ImmutableList.of(javaFileObject));

    task.call();
  }
}
