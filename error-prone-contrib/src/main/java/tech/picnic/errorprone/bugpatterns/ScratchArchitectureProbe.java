package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.sun.source.tree.Tree;
import tech.picnic.errorprone.utils.Flags;
import tech.picnic.errorprone.utils.SourceCode;

/** Throwaway class used to give CodeBoarding a Java change to render. Do not merge. */
final class ScratchArchitectureProbe {
  private ScratchArchitectureProbe() {}

  static String describe(ErrorProneFlags flags, Tree tree, VisitorState state) {
    return Flags.getList(flags, "Scratch") + SourceCode.treeToString(tree, state);
  }
}
