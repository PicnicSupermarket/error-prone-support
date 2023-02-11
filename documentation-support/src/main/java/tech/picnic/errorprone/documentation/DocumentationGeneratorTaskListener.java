package tech.picnic.errorprone.documentation;

import static java.nio.charset.StandardCharsets.UTF_8;

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
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.tools.JavaFileObject;

/**
 * A {@link TaskListener} that identifies and extracts relevant content for documentation generation
 * and writes it to disk.
 */
// XXX: Find a better name for this class; it doesn't generate documentation per se.
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

    ExtractorType.findMatchingType(classTree)
        .ifPresent(
            extractorType ->
                writeToFile(
                    extractorType.getIdentifier(),
                    getSimpleClassName(sourceFile.toUri()),
                    extractorType.getExtractor().extract(classTree, context)));
  }

  private void createDocsDirectory() {
    try {
      Files.createDirectories(docsPath);
    } catch (IOException e) {
      throw new IllegalStateException(
          String.format("Error while creating directory with path '%s'", docsPath), e);
    }
  }

  private <T> void writeToFile(String identifier, String className, T data) {
    File file = docsPath.resolve(String.format("%s-%s.json", identifier, className)).toFile();

    try (FileWriter fileWriter = new FileWriter(file, UTF_8)) {
      OBJECT_MAPPER.writeValue(fileWriter, data);
    } catch (IOException e) {
      throw new UncheckedIOException(String.format("Cannot write to file '%s'", file.getPath()), e);
    }
  }

  private static String getSimpleClassName(URI path) {
    return Paths.get(path).getFileName().toString().replace(".java", "");
  }
}
