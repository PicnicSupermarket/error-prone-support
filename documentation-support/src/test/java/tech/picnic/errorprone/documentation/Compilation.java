package tech.picnic.errorprone.documentation;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.FileManagers;
import com.google.errorprone.FileObjects;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.file.JavacFileManager;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;

// XXX: Generalize and move this class so that it can also be used by `refaster-compiler`.
// XXX: This class is supported by the `ErrorProneTestHelperSourceFormat` check, but until that
// support is covered by unit tests, make sure to update that logic if this class or its methods are
// moved/renamed.
public final class Compilation {
  private Compilation() {}

  public static void compileWithDocumentationGenerator(
      Path outputDirectory, String path, String... lines) {
    compileWithDocumentationGenerator(outputDirectory.toAbsolutePath().toString(), path, lines);
  }

  public static void compileWithDocumentationGenerator(
      String outputDirectory, String path, String... lines) {
    compile(
        ImmutableList.of(
            "-proc:none",
            "-Werror",
            "-Xlint:all,-processing,-serial",
            "-Xplugin:DocumentationGenerator -XoutputDirectory=" + outputDirectory),
        FileObjects.forSourceLines(path, lines));
  }

  private static void compile(ImmutableList<String> options, JavaFileObject javaFileObject) {
    JavacFileManager javacFileManager = FileManagers.testFileManager();
    JavaCompiler compiler = JavacTool.create();

    List<Diagnostic<?>> diagnostics = new ArrayList<>();
    JavacTaskImpl task =
        (JavacTaskImpl)
            compiler.getTask(
                null,
                javacFileManager,
                diagnostics::add,
                options,
                ImmutableList.of(),
                ImmutableList.of(javaFileObject));

    Boolean result = task.call();
    assertThat(diagnostics).isEmpty();
    assertThat(result).isTrue();
  }
}
