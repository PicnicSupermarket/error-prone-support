package tech.picnic.errorprone.documentation;

import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Immutable;
import com.sun.source.tree.ClassTree;
import com.sun.tools.javac.util.Context;

/**
 * Interface implemented by classes that define how to extract data of some type {@link T} from a
 * given {@link ClassTree}.
 *
 * @param <T> The type of data that is extracted.
 */
@Immutable
interface Extractor<T> {
  /**
   * Extracts and returns an instance of {@link T} using the provided arguments.
   *
   * @param tree The {@link ClassTree} to analyze and from which to extract instances of {@link T}.
   * @param context The {@link Context} in which the current compilation takes place.
   * @return A non-null instance of {@link T}.
   */
  // XXX: Drop `Context` parameter unless used.
  T extract(ClassTree tree, Context context);

  /**
   * Tells whether this {@link Extractor} can extract documentation content from the given {@link
   * ClassTree}.
   *
   * @param tree The {@link ClassTree} of interest.
   * @param state A {@link VisitorState} describes the context in which the given {@link ClassTree}
   *     is found.
   * @return {@code true} iff data extraction is supported.
   */
  boolean canExtract(ClassTree tree, VisitorState state);
}
