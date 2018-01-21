package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.VisitorState;
import com.sun.source.tree.Tree;

// XXX: Can we locate this code in a better place? Maybe contribute it upstream?
final class Util {
  private Util() {}

  /**
   * Returns a string representation of the given {@link Tree}, preferring the original source code
   * (if available) over its prettified representation.
   *
   * @return A non-{@code null} string.
   */
  static String treeToString(Tree tree, VisitorState state) {
    String src = state.getSourceForNode(tree);
    return src != null ? src : tree.toString();
  }
}
