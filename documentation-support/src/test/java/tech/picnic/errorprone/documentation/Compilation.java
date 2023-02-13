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
public final class Compilation {
  private Compilation() {}

  public static void compileWithDocumentationGenerator(
      Path outputDirectory, String fileName, String... lines) {
    compileWithDocumentationGenerator(outputDirectory.toAbsolutePath().toString(), fileName, lines);
  }

  public static void compileWithDocumentationGenerator(
      String outputDirectory, String fileName, String... lines) {
    compile(
        ImmutableList.of("-Xplugin:DocumentationGenerator -XoutputDirectory=" + outputDirectory),
        FileObjects.forSourceLines(fileName, lines));
  }

  private static void compile(ImmutableList<String> options, JavaFileObject javaFileObject) {
    JavacFileManager javacFileManager = FileManagers.testFileManager();
    JavaCompiler compiler = JavacTool.create();
    JavacTaskImpl task =
        (JavacTaskImpl)
            compiler.getTask(
                null,
                javacFileManager,
                null,
                options,
                ImmutableList.of(),
                ImmutableList.of(javaFileObject));

    task.call();
  }
}
