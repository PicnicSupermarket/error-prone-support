package tech.picnic.errorprone.util;

import com.google.errorprone.VisitorState;
import com.sun.source.tree.Tree;

/**
 * A collection of Error Prone utility methods for dealing with the source code representation of
 * AST nodes.
 */
// XXX: This is a duplicate of `error-prone-contrib`s `SourceCode`, improve this.
public final class SourceCode {
  private SourceCode() {}

  /**
   * Returns a string representation of the given {@link Tree}, preferring the original source code
   * (if available) over its prettified representation.
   *
   * @param tree The AST node of interest.
   * @param state A {@link VisitorState} describing the context in which the given {@link Tree} is
   *     found.
   * @return A non-{@code null} string.
   */
  public static String treeToString(Tree tree, VisitorState state) {
    String src = state.getSourceForNode(tree);
    return src != null ? src : tree.toString();
  }
}
