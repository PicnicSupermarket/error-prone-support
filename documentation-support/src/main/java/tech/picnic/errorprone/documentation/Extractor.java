package tech.picnic.errorprone.documentation;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.sun.source.tree.ClassTree;

/**
 * Interface implemented by classes that define how to extract data of some type {@link T} from a
 * given {@link ClassTree}.
 *
 * @param <T> The type of data that is extracted.
 */
// XXX: Here and in the implementations, either:
// 1. Swap `canExtract` and `extract`.
// 2. Combine the methods into a single `Optional<T> tryExtract`.
@Immutable
interface Extractor<T> {
  /**
   * Extracts and returns an instance of {@link T} using the provided arguments.
   *
   * @param tree The {@link ClassTree} to analyze and from which to extract instances of {@link T}.
   * @param state A {@link VisitorState} describing the context in which the given {@link ClassTree}
   *     is found.
   * @return A non-null instance of {@link T}.
   */
  T extract(ClassTree tree, VisitorState state);

  /**
   * Tells whether this {@link Extractor} can extract documentation content from the given {@link
   * ClassTree}.
   *
   * @param tree The {@link ClassTree} of interest.
   * @param state A {@link VisitorState} describing the context in which the given {@link ClassTree}
   *     is found.
   * @return {@code true} iff data extraction is supported.
   */
  boolean canExtract(ClassTree tree, VisitorState state);
}
