package tech.picnic.errorprone.plugin;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TaskEvent;
import tech.picnic.errorprone.plugin.objects.RefasterTemplateTestData;

public class RefasterTestExtractor
    implements DocExtractor<ImmutableList<RefasterTemplateTestData>> {
  @Override
  public ImmutableList<RefasterTemplateTestData> extractData(
      ClassTree tree, TaskEvent taskEvent, VisitorState state) {

    String templateCollectionName = tree.getSimpleName().toString().replace("Test", "");

    return tree.getMembers().stream()
        .filter(MethodTree.class::isInstance)
        .map(MethodTree.class::cast)
        .filter(m -> m.getName().toString().startsWith("test"))
        .map(
            m ->
                RefasterTemplateTestData.create(
                    templateCollectionName,
                    m.getName().toString().replace("test", ""),
                    m.toString()))
        .collect(toImmutableList());
  }
}
