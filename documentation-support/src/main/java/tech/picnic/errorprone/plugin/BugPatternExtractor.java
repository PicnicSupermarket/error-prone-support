package tech.picnic.errorprone.plugin;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TaskEvent;
import tech.picnic.errorprone.plugin.models.BugPatternData;

public final class BugPatternExtractor implements DocExtractor<BugPatternData> {
  @Override
  public BugPatternData extractData(ClassTree tree, TaskEvent taskEvent, VisitorState state) {
    BugPattern annotation = taskEvent.getTypeElement().getAnnotation(BugPattern.class);
    return BugPatternData.create(
        taskEvent.getTypeElement().getQualifiedName().toString(),
        taskEvent.getTypeElement().getSimpleName().toString(),
        ImmutableList.copyOf(annotation.altNames()),
        annotation.linkType(),
        annotation.link(),
        ImmutableList.copyOf(annotation.tags()),
        annotation.summary(),
        annotation.explanation(),
        annotation.severity(),
        annotation.disableable());
  }
}
