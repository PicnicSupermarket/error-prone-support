package tech.picnic.errorprone.plugin;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TaskEvent;
import tech.picnic.errorprone.plugin.objects.BugPatternData;

public final class BugPatternExtractor implements DocExtractor<BugPatternData> {
  @Override
  public BugPatternData extractData(ClassTree tree, TaskEvent taskEvent, VisitorState state) {
    BugPattern annotation = taskEvent.getTypeElement().getAnnotation(BugPattern.class);
    return BugPatternData.create(annotation, taskEvent.getTypeElement().getSimpleName().toString());
  }
}
