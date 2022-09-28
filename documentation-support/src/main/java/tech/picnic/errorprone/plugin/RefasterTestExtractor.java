package tech.picnic.errorprone.plugin;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TreePath;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;
import tech.picnic.errorprone.plugin.models.RefasterTemplateCollectionTestData;
import tech.picnic.errorprone.plugin.models.RefasterTemplateTestData;

public final class RefasterTestExtractor
    implements DocExtractor<RefasterTemplateCollectionTestData> {
  @Override
  public RefasterTemplateCollectionTestData extractData(
      ClassTree tree, TaskEvent taskEvent, VisitorState state) {
    String templateCollectionName = tree.getSimpleName().toString().replace("Test", "");
    boolean isInput = taskEvent.getSourceFile().getName().contains("Input");
    VisitorState stateWithPath =
        state.withPath(TreePath.getPath(taskEvent.getCompilationUnit(), tree));

    ImmutableList<RefasterTemplateTestData> templateTests =
        tree.getMembers().stream()
            .filter(MethodTree.class::isInstance)
            .map(MethodTree.class::cast)
            .filter(m -> m.getName().toString().startsWith("test"))
            .map(
                m ->
                    RefasterTemplateTestData.create(
                        m.getName().toString().replace("test", ""),
                        SourceCode.treeToString(m, stateWithPath)))
            .collect(toImmutableList());

    return RefasterTemplateCollectionTestData.create(
        templateCollectionName, isInput, templateTests);
  }
}
