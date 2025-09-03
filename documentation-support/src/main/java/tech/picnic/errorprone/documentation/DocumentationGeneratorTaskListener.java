package tech.picnic.errorprone.documentation;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;
import javax.tools.JavaFileObject;

/**
 * A {@link TaskListener} that identifies and extracts relevant content for documentation generation
 * and writes it to disk.
 */
// XXX: Find a better name for this class; it doesn't generate documentation per se.
record DocumentationGeneratorTaskListener(Context context, Path docsPath) implements TaskListener {
  @SuppressWarnings({"rawtypes", "unchecked"} /* Unbounded wildcard type introduction is safe. */)
  private static final ImmutableList<Extractor<?>> EXTRACTORS =
      (ImmutableList)
          ImmutableList.copyOf(
              ServiceLoader.load(
                  Extractor.class, DocumentationGeneratorTaskListener.class.getClassLoader()));

  @Override
  public void started(TaskEvent taskEvent) {
    if (taskEvent.getKind() == Kind.ANALYZE) {
      createDocsDirectory();
    }
  }

  @Override
  public void finished(TaskEvent taskEvent) {
    if (taskEvent.getKind() != Kind.ANALYZE || JavaCompiler.instance(context()).errorCount() > 0) {
      return;
    }

    ClassTree classTree = JavacTrees.instance(context()).getTree(taskEvent.getTypeElement());
    if (classTree == null) {
      return;
    }

    CompilationUnitTree compilationUnit =
        requireNonNull(taskEvent.getCompilationUnit(), "No compilation unit");
    VisitorState state =
        VisitorState.createForUtilityPurposes(context())
            .withPath(new TreePath(new TreePath(compilationUnit), classTree));

    JavaFileObject sourceFile = requireNonNull(taskEvent.getSourceFile(), "No source file");
    for (Extractor<?> extractor : EXTRACTORS) {
      extractor
          .tryExtract(classTree, state)
          .ifPresent(
              data ->
                  writeToFile(
                      extractor.identifier(), getSimpleClassName(sourceFile.toUri()), data));
    }
  }

  private void createDocsDirectory() {
    try {
      Files.createDirectories(docsPath());
    } catch (IOException e) {
      throw new IllegalStateException(
          "Error while creating directory with path '%s'".formatted(docsPath()), e);
    }
  }

  private <T> void writeToFile(String identifier, String className, T data) {
    Json.write(docsPath().resolve("%s-%s.json".formatted(identifier, className)), data);
  }

  private static String getSimpleClassName(URI path) {
    return Path.of(path).getFileName().toString().replace(".java", "");
  }
}
