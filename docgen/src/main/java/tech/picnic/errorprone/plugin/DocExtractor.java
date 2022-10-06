package tech.picnic.errorprone.plugin;

import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;

public interface DocExtractor<T> {
  T extractData(ClassTree tree, VisitorState state);
}
