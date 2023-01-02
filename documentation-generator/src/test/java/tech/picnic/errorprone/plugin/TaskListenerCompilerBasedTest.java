package tech.picnic.errorprone.plugin;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.FileObjects;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;

abstract class TaskListenerCompilerBasedTest {
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

    task.parse();
    task.analyze();
  }
}
