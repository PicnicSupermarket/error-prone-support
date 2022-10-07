package tech.picnic.errorprone.plugin;

import static tech.picnic.errorprone.plugin.DocType.BUG_PATTERN;
import static tech.picnic.errorprone.plugin.DocType.BUG_PATTERN_TEST;
import static tech.picnic.errorprone.plugin.DocType.REFASTER_TEMPLATE_TEST_INPUT;
import static tech.picnic.errorprone.plugin.DocType.REFASTER_TEMPLATE_TEST_OUTPUT;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.util.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import javax.tools.JavaFileObject;

/** XXX: Write this. */
final class DocgenTaskListener implements TaskListener {
  private final Context context;

  private final String basePath;

  private final VisitorState state;

  private final ObjectMapper mapper =
      new ObjectMapper()
          .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
          .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
          .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

  DocgenTaskListener(Context context, String path) {
    this.context = context;
    this.basePath = path.substring(path.indexOf('=') + 1) + "/docs";
    this.state = VisitorState.createForUtilityPurposes(context);

    // XXX: Move this somewhere else?
    try {
      Files.createDirectories(Paths.get(basePath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @SuppressWarnings("SystemOut")
  public void finished(TaskEvent taskEvent) {
    ClassTree tree = JavacTrees.instance(context).getTree(taskEvent.getTypeElement());
    JavaFileObject sourceFile = taskEvent.getSourceFile();
    if (tree == null || sourceFile == null || taskEvent.getKind() != TaskEvent.Kind.ANALYZE) {
      return;
    }

    getDocType(tree, sourceFile)
        .ifPresent(
            docType ->
                writeToFile(
                    docType.getDocExtractor().extractData(tree, taskEvent, state),
                    docType.getOutputFileNamePrefix(),
                    getSimpleClassName(sourceFile.getName())));
  }

  private static Optional<DocType> getDocType(ClassTree tree, JavaFileObject sourceFile) {
    if (isBugPattern(tree)) {
      return Optional.of(BUG_PATTERN);
    } else if (isBugPatternTest(tree)) {
      return Optional.of(BUG_PATTERN_TEST);
    } else if (sourceFile.getName().contains("TestInput")) {
      return Optional.of(REFASTER_TEMPLATE_TEST_INPUT);
    } else if (sourceFile.getName().contains("TestOutput")) {
      return Optional.of(REFASTER_TEMPLATE_TEST_OUTPUT);
    }
    return Optional.empty();
  }

  private <T> void writeToFile(T data, String fileName, String name) {
    File file = new File(basePath + "/" + fileName + "-" + name + ".json");
    // XXX: Use Path instead of File.

    try (FileWriter fileWriter = new FileWriter(file, true)) {
      mapper.writeValue(fileWriter, data);
      fileWriter.write("\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static boolean isBugPattern(ClassTree tree) {
    return ASTHelpers.hasDirectAnnotationWithSimpleName(tree, BugPattern.class.getSimpleName());
  }

  private static boolean isBugPatternTest(ClassTree tree) {
    return tree.getSimpleName().toString().endsWith("Test")
        && tree.getMembers().stream()
            .filter(VariableTree.class::isInstance)
            .map(VariableTree.class::cast)
            .anyMatch(vt -> vt.getType().toString().equals("BugCheckerRefactoringTestHelper"));
  }

  private static String getSimpleClassName(String path) {
    int index = path.lastIndexOf('/');
    String fileName = path.substring(index + 1);
    return fileName.replace(".java", "");
  }
}
