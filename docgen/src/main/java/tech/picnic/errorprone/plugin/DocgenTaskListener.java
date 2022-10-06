package tech.picnic.errorprone.plugin;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import tech.picnic.errorprone.plugin.objects.BugPatternData;
import tech.picnic.errorprone.plugin.objects.BugPatternTestData;

/** XXX: Write this. */
final class DocgenTaskListener implements TaskListener {
  private final Context context;

  private final String basePath;

  private final VisitorState state;

  private final ObjectMapper mapper =
      new ObjectMapper()
          .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
          .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

  DocgenTaskListener(Context context, String path) {
    this.context = context;
    this.basePath = path.substring(path.indexOf('=') + 1);
    this.state = VisitorState.createForUtilityPurposes(context);
  }

  @Override
  @SuppressWarnings("SystemOut")
  public void finished(TaskEvent taskEvent) {
    if (taskEvent.getKind() != Kind.ANALYZE || JavaCompiler.instance(context).errorCount() > 0) {
      return;
    }

    ClassTree tree = JavacTrees.instance(context).getTree(taskEvent.getTypeElement());
    if (tree == null || (!isBugPattern(tree) && !isBugPatternTest(tree))) {
      return;
    }

    if (isBugPatternTest(tree)) {
      BugPatternTestData testData =
          new BugPatternTestsExtractor().extractData(tree, taskEvent, state);
      System.out.println(testData);
      writeToFile(testData, "bug-pattern-test-data.jsonl");
    } else if(isBugPattern(tree)) {

      BugPatternData data = new BugPatternExtractor().extractData(tree, taskEvent, state);

      System.out.println("Analysing: " + taskEvent.getTypeElement().getSimpleName());
      writeToFile(data, "bug-pattern-data.jsonl");
    } else {
      System.out.println("~~~~~~~~~!!!!~~~~~~~NOW ANALYSING: " + tree.getSimpleName().toString());
    }
  }

  private <T> void writeToFile(T data, String fileName) {
    File file = new File(basePath + "/" + fileName);

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
            .anyMatch(
                member -> member.getType().toString().equals("BugCheckerRefactoringTestHelper"));
  }
}
