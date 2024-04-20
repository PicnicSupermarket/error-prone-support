package tech.picnic.errorprone.documentation;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.util.Context;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.tools.JavaFileObject;
import org.jspecify.annotations.Nullable;

// XXX: Document.
final class RefasterBenchmarkGeneratorTaskListener implements TaskListener {
  private final Context context;
  private final Path outputPath;

  RefasterBenchmarkGeneratorTaskListener(Context context, Path outputPath) {
    this.context = context;
    this.outputPath = outputPath;
  }

  @Override
  public void started(TaskEvent taskEvent) {
    if (taskEvent.getKind() == Kind.ANALYZE) {
      createOutputDirectory();
    }
  }

  @Override
  public void finished(TaskEvent taskEvent) {
    if (taskEvent.getKind() != Kind.ANALYZE) {
      return;
    }

    JavaFileObject sourceFile = taskEvent.getSourceFile();
    CompilationUnitTree compilationUnit = taskEvent.getCompilationUnit();
    ClassTree classTree = JavacTrees.instance(context).getTree(taskEvent.getTypeElement());
    if (sourceFile == null || compilationUnit == null || classTree == null) {
      return;
    }

    VisitorState state =
        VisitorState.createForUtilityPurposes(context)
            .withPath(new TreePath(new TreePath(compilationUnit), classTree));

    // XXX: Make static.
    Matcher<Tree> isBenchmarked =
        Matchers.hasAnnotation("tech.picnic.errorprone.refaster.annotation.Benchmarked");

    new TreePathScanner<@Nullable Void, Boolean>() {
      @Override
      public @Nullable Void visitClass(ClassTree classTree, Boolean doBenchmark) {
        // XXX: Validate that `@Benchmarked` is only placed in contexts with at least one Refaster
        // rule.
        boolean inspectClass = doBenchmark || isBenchmarked.matches(classTree, state);

        if (inspectClass) {
          // XXX: If this class has a `@BeforeTemplate` method, generate a benchmark for it.
        }

        return super.visitClass(classTree, inspectClass);
      }
    }.scan(compilationUnit, false);
  }

  private void createOutputDirectory() {
    try {
      Files.createDirectories(outputPath);
    } catch (IOException e) {
      throw new IllegalStateException(
          String.format("Error while creating directory with path '%s'", outputPath), e);
    }
  }

  private <T> void writeToFile(String identifier, String className, T data) {
    Json.write(outputPath.resolve(String.format("%s-%s.json", identifier, className)), data);
  }

  private static String getSimpleClassName(URI path) {
    return Paths.get(path).getFileName().toString().replace(".java", "");
  }
}
