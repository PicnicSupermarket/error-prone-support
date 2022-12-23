package tech.picnic.errorprone.plugin;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TaskEvent;

/**
 * Interface implemented by a class that defines how to extract {@link T} from a given {@link
 * ClassTree}.
 */
public interface DocumentationExtractor<T> {
  /**
   * Extracts and returns an instance of {@link T} using the provided arguments.
   *
   * @param tree The {@link ClassTree} to analyse and extract {@link T} from.
   * @param taskEvent The {@link TaskEvent} containing information about the current state of the
   *     compilation.
   * @return A non-null instance of {@link T}.
   */
  T extractData(ClassTree tree, TaskEvent taskEvent);
}
