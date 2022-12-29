package tech.picnic.errorprone.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.errorprone.FileManagers;
import com.google.errorprone.FileObjects;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import java.io.Writer;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;

abstract class DocumentationGeneratorCompilerBasedTest {
  public void compile(String outputDirectory, String... lines) {
    compile(outputDirectory, FileObjects.forSourceLines("CompilerBasedTestInput.java", lines));
  }

  private static void compile(String outputDirectory, JavaFileObject javaFileObject) {
    JavaCompiler compiler = JavacTool.create();
    JavacTaskImpl task =
        (JavacTaskImpl)
            compiler.getTask(
                Writer.nullWriter(),
                FileManagers.testFileManager(),
                new DiagnosticCollector<>(),
                ImmutableList.of(
                    "-Xplugin:DocumentationGenerator -XdocsOutputDirectory=" + outputDirectory),
                ImmutableList.of(),
                ImmutableList.of(javaFileObject));

    CompilationUnitTree compilationUnitTree = Iterables.getOnlyElement(task.parse());
    task.analyze();

    if (compilationUnitTree.getTypeDecls().isEmpty()) {
      return;
    }
    Tree classTree = Iterables.getOnlyElement(compilationUnitTree.getTypeDecls());
    ClassSymbol classSymbol = (ClassSymbol) ASTHelpers.getSymbol(classTree);

    TaskEvent taskEvent = new TaskEvent(Kind.ANALYZE, compilationUnitTree, classSymbol);

    for (TaskListener tl : task.getTaskListeners()) {
      tl.finished(taskEvent);
    }
  }
}
