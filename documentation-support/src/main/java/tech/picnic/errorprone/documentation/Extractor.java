package tech.picnic.errorprone.documentation;

import com.google.errorprone.VisitorState;
import com.sun.source.tree.ClassTree;
import java.util.Optional;

/**
 * Interface implemented by classes that define how to extract some type {@link ProjectInfo} from a
 * given {@link ClassTree}.
 *
 * @param <T> The type of data that is extracted.
 */
interface Extractor<T extends ProjectInfo> {
  /**
   * Returns the unique identifier of this extractor.
   *
   * @return A non-{@code null} string.
   */
  String identifier();

  /**
   * Attempts to extract an instance of type {@link T} using the provided arguments.
   *
   * @param tree The {@link ClassTree} to analyze and from which to extract an instance of type
   *     {@link T}.
   * @param state A {@link VisitorState} describing the context in which the given {@link ClassTree}
   *     is found.
   * @return An instance of type {@link T}, if possible.
   */
  Optional<T> tryExtract(ClassTree tree, VisitorState state);
}
