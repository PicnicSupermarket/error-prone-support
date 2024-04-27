package tech.picnic.errorprone.refaster.benchmark;

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

// XXX: This code is a near-duplicate of the identically named class in `documentation-support`.
public final class Compilation {
  private Compilation() {}

  public static void compileWithRefasterRuleBenchmarkGenerator(
      Path outputDirectory, String path, String... lines) {
    compileWithRefasterRuleBenchmarkGenerator(
        outputDirectory.toAbsolutePath().toString(), path, lines);
  }

  public static void compileWithRefasterRuleBenchmarkGenerator(
      String outputDirectory, String path, String... lines) {
    /*
     * The compiler options specified here largely match those used by Error Prone's
     * `CompilationTestHelper`. A key difference is the stricter linting configuration. When
     * compiling using JDK 21+, these lint options also require that certain JDK modules are
     * explicitly exported.
     */
    compile(
        ImmutableList.of(
            "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
            "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
            "-encoding",
            "UTF-8",
            "-parameters",
            "-proc:none",
            "-Werror",
            "-Xlint:all,-serial",
            "-Xplugin:RefasterRuleBenchmarkGenerator -XoutputDirectory=" + outputDirectory,
            "-XDdev",
            "-XDcompilePolicy=simple"),
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
