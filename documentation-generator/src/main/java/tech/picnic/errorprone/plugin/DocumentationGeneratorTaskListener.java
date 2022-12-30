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
 * A {@link TaskListener} that identifies files that contain content relevant for in the
 * documentation.
 */
final class DocumentationGeneratorTaskListener implements TaskListener {
  private final Context context;
  private final Path basePath;
  private final ObjectMapper mapper =
      new ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

  DocumentationGeneratorTaskListener(Context context, String path) {
    this.context = context;

    // XXX: Should we extract this method?
    String docsPath = path.substring(path.indexOf('=') + 1) + File.separator + "docs";
    try {
      this.basePath = Files.createDirectories(Paths.get(docsPath));
    } catch (IOException | InvalidPathException e) {
      throw new IllegalStateException(
          String.format("Error while creating directory with path '%s'", docsPath), e);
    }
  }

  @Override
  public void finished(TaskEvent taskEvent) {
    ClassTree classTree = JavacTrees.instance(context).getTree(taskEvent.getTypeElement());
    JavaFileObject sourceFile = taskEvent.getSourceFile();
    if (classTree == null || sourceFile == null || taskEvent.getKind() != Kind.ANALYZE) {
      return;
    }

    getDocumentationType(classTree)
        .ifPresent(
            documentationType ->
                writeToFile(
                    documentationType.getDocumentationExtractor().extract(classTree, taskEvent),
                    documentationType.getOutputFileNamePrefix(),
                    getSimpleClassName(sourceFile.toUri())));
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
      mapper.writeValue(fileWriter, data);
    } catch (IOException e) {
      throw new IllegalStateException(
          String.format("Could not write to file '%s'", file.getPath()), e);
    }
  }

  private static String getSimpleClassName(URI path) {
    return Paths.get(path).getFileName().toString().replace(".java", "");
  }
}
