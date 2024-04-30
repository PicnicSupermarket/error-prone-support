package tech.picnic.errorprone.refaster.benchmark;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.util.Context;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.JavaFileObject;
import org.jspecify.annotations.Nullable;

// XXX: Document.
final class RefasterRuleBenchmarkGeneratorTaskListener implements TaskListener {
  private static final Matcher<Tree> IS_TEMPLATE =
      anyOf(hasAnnotation(BeforeTemplate.class), hasAnnotation(AfterTemplate.class));
  private static final Matcher<Tree> IS_BENCHMARKED =
      Matchers.hasAnnotation("tech.picnic.errorprone.refaster.annotation.Benchmarked");

  private final Context context;
  private final Path outputPath;

  RefasterRuleBenchmarkGeneratorTaskListener(Context context, Path outputPath) {
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

    new TreePathScanner<@Nullable Void, Boolean>() {
      @Override
      public @Nullable Void visitClass(ClassTree classTree, Boolean doBenchmark) {
        // XXX: Validate that `@Benchmarked` is only placed in contexts with at least one Refaster
        // rule.
        boolean inspectClass = doBenchmark || IS_BENCHMARKED.matches(classTree, state);

        if (inspectClass) {
          System.out.println(handle(classTree, state));
          // XXX: If this class has a `@BeforeTemplate` method, generate a benchmark for it.
        }

        return super.visitClass(classTree, inspectClass);
      }
    }.scan(compilationUnit, false);
  }

  // XXX: Name? Scope?
  private static Rule handle(ClassTree classTree, VisitorState state) {
    ImmutableList<Rule.Method> methods =
        classTree.getMembers().stream()
            .filter(m -> IS_TEMPLATE.matches(m, state))
            .map(m -> process((MethodTree) m, state))
            .collect(toImmutableList());

    Rule rule = new Rule(classTree, methods);
    return rule;
  }

  private static Rule.Method process(MethodTree methodTree, VisitorState state) {
    // XXX: Initially, disallow `Refaster.x` usages.
    // XXX: Initially, disallow references to `@Placeholder` methods.
    // XXX: Disallow `void` methods. (Can't be black-holed.)
    return new Rule.Method(methodTree);
  }

  // XXX: Move types down.
  record Rule(ClassTree tree, ImmutableList<Rule.Method> methods) {
    record Method(MethodTree tree) {}
  }

  private void createOutputDirectory() {
    try {
      Files.createDirectories(outputPath);
    } catch (IOException e) {
      throw new IllegalStateException(
          String.format("Error while creating directory with path '%s'", outputPath), e);
    }
  }
}
