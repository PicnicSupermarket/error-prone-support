package tech.picnic.errorprone.plugin;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.errorprone.BugPattern;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** XXX: Fill in. */
final class DocgenTaskListener implements TaskListener {
  private final Context context;

  private final String basePath;

  private final ObjectMapper mapper =
      new ObjectMapper()
          .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
          .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

  DocgenTaskListener(Context context, String path) {
    this.context = context;
    this.basePath = path.substring(path.indexOf('=') + 1);
  }

  @Override
  @SuppressWarnings("SystemOut")
  public void finished(TaskEvent taskEvent) {
    if (taskEvent.getKind() != Kind.ANALYZE || JavaCompiler.instance(context).errorCount() > 0) {
      return;
    }

    ClassTree tree = JavacTrees.instance(context).getTree(taskEvent.getTypeElement());
    if (tree == null || !isBugPattern(tree)) {
      return;
    }

    BugPattern annotation = taskEvent.getTypeElement().getAnnotation(BugPattern.class);
    BugPatternData bugPatternData =
        BugPatternData.create(annotation, taskEvent.getTypeElement().getSimpleName().toString());

    System.out.println("Analysing: " + taskEvent.getTypeElement().getSimpleName());
    File file = new File(basePath + "/bugpattern-data.jsonl");

    try (FileWriter fileWriter = new FileWriter(file, true)) {
      mapper.writeValue(fileWriter, bugPatternData);
      fileWriter.write("\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static boolean isBugPattern(ClassTree tree) {
    return ASTHelpers.hasDirectAnnotationWithSimpleName(tree, BugPattern.class.getSimpleName());
  }
}
