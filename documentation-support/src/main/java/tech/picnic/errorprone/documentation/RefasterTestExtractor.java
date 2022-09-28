package tech.picnic.errorprone.documentation;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Context;
import java.util.List;

public final class RefasterTestExtractor
    implements Extractor<RefasterTestExtractor.RefasterTemplateCollectionTestData> {
  @Override
  public RefasterTemplateCollectionTestData extract(
      ClassTree tree, Context context, TaskEvent taskEvent) {
    String templateCollectionName = tree.getSimpleName().toString().replace("Test", "");
    boolean isInput = taskEvent.getSourceFile().getName().contains("Input");
    VisitorState stateWithPath =
        VisitorState.createForUtilityPurposes(context)
            .withPath(TreePath.getPath(taskEvent.getCompilationUnit(), tree));

    ImmutableList<RefasterTemplateTestData> templateTests =
        tree.getMembers().stream()
            .filter(MethodTree.class::isInstance)
            .map(MethodTree.class::cast)
            .filter(m -> m.getName().toString().startsWith("test"))
            .map(
                m -> {
                  String src = stateWithPath.getSourceForNode(tree);
                  return new AutoValue_RefasterTestExtractor_RefasterTemplateTestData(
                      m.getName().toString().replace("test", ""),
                      src != null ? src : tree.toString());
                })
            .collect(toImmutableList());

    return new AutoValue_RefasterTestExtractor_RefasterTemplateCollectionTestData(
        templateCollectionName, isInput, templateTests);
  }

  @Override
  public boolean canExtract(ClassTree tree, VisitorState state) {
    String className = tree.getSimpleName().toString();
    return className.endsWith("TestInput") || className.endsWith("TestOutput");
  }

  @AutoValue
  abstract static class RefasterTemplateTestData {

    abstract String templateName();

    abstract String templateTestContent();
  }

  @AutoValue
  abstract static class RefasterTemplateCollectionTestData {
    abstract String templateCollection();

    abstract boolean isInput();

    abstract List<RefasterTemplateTestData> templateTests();
  }
}
