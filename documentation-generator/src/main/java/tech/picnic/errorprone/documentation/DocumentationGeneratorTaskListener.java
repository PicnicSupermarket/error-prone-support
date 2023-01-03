package tech.picnic.errorprone.documentation;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.util.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javax.tools.JavaFileObject;

/**
 * A {@link TaskListener} that identifies and extracts relevant content for documentation and writes
 * it to disk.
 */
final class DocumentationGeneratorTaskListener implements TaskListener {
  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
  private final Context context;
  private final Path docsPath;

  DocumentationGeneratorTaskListener(Context context, Path path) {
    this.context = context;
    this.docsPath = path;
  }

  @Override
  public void started(TaskEvent taskEvent) {
    if (taskEvent.getKind() == Kind.ANALYZE) {
      createDocsDirectory();
    }
  }

  @Override
  public void finished(TaskEvent taskEvent) {
    if (taskEvent.getKind() != Kind.ANALYZE) {
      return;
    }

    ClassTree classTree = JavacTrees.instance(context).getTree(taskEvent.getTypeElement());
    JavaFileObject sourceFile = taskEvent.getSourceFile();
    if (classTree == null || sourceFile == null) {
      return;
    }

    findDocumentationType(classTree)
        .ifPresent(
            documentationType ->
                writeToFile(
                    documentationType.getIdentifier(),
                    getSimpleClassName(sourceFile.toUri()),
                    documentationType.getDocumentationExtractor().extract(classTree, taskEvent)));
  }

  private void createDocsDirectory() {
    try {
      Files.createDirectories(docsPath);
    } catch (IOException e) {
      throw new IllegalStateException(
          String.format("Error while creating directory with path '%s'", docsPath), e);
    }
  }

  // XXX: `JavaFileObject` will most likely be added as parameter to help identify other `DocType`s.
  private static Optional<DocumentationType> findDocumentationType(ClassTree tree) {
    return stream(DocumentationType.values())
        .filter(type -> type.getDocumentationExtractor().canExtract(tree))
        .findFirst();
  }

  private <T> void writeToFile(String identifier, String className, T data) {
    File file = docsPath.resolve(String.format("%s-%s.json", identifier, className)).toFile();

    try (FileWriter fileWriter = new FileWriter(file, UTF_8, /* append= */ true)) {
      OBJECT_MAPPER.writeValue(fileWriter, data);
    } catch (IOException e) {
      throw new IllegalStateException(
          String.format("Could not write to file '%s'", file.getPath()), e);
    }
  }

  private static String getSimpleClassName(URI path) {
    return Paths.get(path).getFileName().toString().replace(".java", "");
  }
}
