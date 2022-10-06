package tech.picnic.errorprone.plugin;

import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TaskEvent;

public interface DocExtractor<T> {
  T extractData(ClassTree tree, TaskEvent taskEvent, VisitorState state);
}
