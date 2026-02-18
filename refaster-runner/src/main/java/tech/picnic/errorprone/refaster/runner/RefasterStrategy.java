package tech.picnic.errorprone.refaster.runner;

import com.google.errorprone.DescriptionListener;
import com.google.errorprone.SubContext;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.CompilationUnitTree;
import java.io.Serializable;

/** A strategy for applying Refaster transformers to a compilation unit. */
interface RefasterStrategy extends Serializable {
  /**
   * Applies {@link CodeTransformers transformers} to the given compilation unit.
   *
   * @param tree The compilation unit to apply transformers on.
   * @param context The sub-context to use for applying transformers.
   * @param listener The listener to receive descriptions of any matches found.
   * @param state The visitor state for the current compilation.
   */
  void applyTransformers(
      CompilationUnitTree tree,
      SubContext context,
      DescriptionListener listener,
      VisitorState state);
}
