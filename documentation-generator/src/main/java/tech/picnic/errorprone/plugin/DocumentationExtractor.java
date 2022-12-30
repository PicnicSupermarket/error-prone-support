package tech.picnic.errorprone.plugin;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TaskEvent;

/**
 * Interface implemented by a class that defines how to extract {@link T} from a given {@link
 * ClassTree}.
 *
 * @param <T> The resulting type of the data that is extracted.
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
  T extract(ClassTree tree, TaskEvent taskEvent);

  /**
   * Tells whether this {@link DocumentationExtractor extractor} can extract documentation content
   * from the given {@link ClassTree tree}.
   *
   * @param tree The {@link ClassTree} to check whether documentation can be extracted or not.
   * @return {@code true} iff documentation can be extracted
   */
  // XXX: `JavaFileObject` will most likely be added as parameter to help identify other `DocType`s.
  boolean canExtract(ClassTree tree);
}
