package tech.picnic.errorprone.plugin;

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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javax.tools.JavaFileObject;

/**
 * A {@link TaskListener} that identifies and extracts relevant content for documentation and writes
 * it to disk.
 */
final class DocumentationGeneratorTaskListener implements TaskListener {
  private static final ObjectMapper MAPPER =
      new ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
  private final Context context;
  private final String path;
  private Path basePath;

  DocumentationGeneratorTaskListener(Context context, String path) {
    this.context = context;
    this.path = path;
    this.basePath = Paths.get(path);
  }

  @Override
  public void started(TaskEvent taskEvent) {
    createDirectoriesForPath();
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

    getDocumentationType(classTree)
        .ifPresent(
            documentationType ->
                writeToFile(
                    documentationType.getDocumentationExtractor().extract(classTree, taskEvent),
                    documentationType.getIdentifier(),
                    getSimpleClassName(sourceFile.toUri())));
  }

  private void createDirectoriesForPath() {
    String docsPath = path.substring(path.indexOf('=') + 1) + File.separator + "docs";
    try {
      basePath = Files.createDirectories(Paths.get(docsPath));
    } catch (IOException | InvalidPathException e) {
      throw new IllegalStateException(
          String.format("Error while creating directory with path '%s'", docsPath), e);
    }
  }

  // XXX: `JavaFileObject` will most likely be added as parameter to help identify other `DocType`s.
  private static Optional<DocumentationType> getDocumentationType(ClassTree tree) {
    return stream(DocumentationType.values())
        .filter(type -> type.getDocumentationExtractor().canExtract(tree))
        .findFirst();
  }

  private <T> void writeToFile(T data, String fileName, String name) {
    File file = basePath.resolve(String.format("%s-%s.json", fileName, name)).toFile();

    try (FileWriter fileWriter = new FileWriter(file, UTF_8, /* append= */ true)) {
      MAPPER.writeValue(fileWriter, data);
    } catch (IOException e) {
      throw new IllegalStateException(
          String.format("Could not write to file '%s'", file.getPath()), e);
    }
  }

  private static String getSimpleClassName(URI path) {
    return Paths.get(path).getFileName().toString().replace(".java", "");
  }
}
