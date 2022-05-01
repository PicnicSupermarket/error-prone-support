package tech.picnic.errorprone.bugpatterns.util;

import com.google.errorprone.VisitorState;
import com.sun.source.tree.Tree;

/**
 * A collection of Error Prone utility methods for dealing with the source code representation of
 * AST nodes.
 */
// XXX: Can we locate this code in a better place? Maybe contribute it upstream?
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

  /**
   * Returns a string representation of the given {@link Tree.Kind}.
   *
   * @return A string representation of the operator or else throws an exception.
   */
  // XXX: List needs to be extended, as it currently only supports `BinaryTree`s and `UnaryTree`s.
  static String treeKindToString(Tree.Kind kind) {
    switch (kind) {
      case POSTFIX_INCREMENT:
      case PREFIX_INCREMENT:
        return "++";
      case POSTFIX_DECREMENT:
      case PREFIX_DECREMENT:
        return "--";
      case UNARY_PLUS:
      case PLUS:
        return "+";
      case UNARY_MINUS:
      case MINUS:
        return "-";
      case BITWISE_COMPLEMENT:
        return "~";
      case LOGICAL_COMPLEMENT:
        return "!";
      case MULTIPLY:
        return "*";
      case DIVIDE:
        return "/";
      case REMAINDER:
        return "%";
      case LEFT_SHIFT:
        return "<<";
      case RIGHT_SHIFT:
        return ">>";
      case UNSIGNED_RIGHT_SHIFT:
        return ">>>";
      case LESS_THAN:
        return "<";
      case GREATER_THAN:
        return ">";
      case LESS_THAN_EQUAL:
        return "<=";
      case GREATER_THAN_EQUAL:
        return ">=";
      case EQUAL_TO:
        return "==";
      case NOT_EQUAL_TO:
        return "!=";
      case AND:
        return "&";
      case XOR:
        return "^";
      case OR:
        return "|";
      case CONDITIONAL_AND:
        return "&&";
      case CONDITIONAL_OR:
        return "||";
      default:
        throw new IllegalStateException("Cannot convert Tree.Kind to a String: " + kind);
    }
  }
}
