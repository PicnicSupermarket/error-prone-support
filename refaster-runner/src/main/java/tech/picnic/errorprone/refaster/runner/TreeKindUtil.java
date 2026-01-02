package tech.picnic.errorprone.refaster.runner;

import com.sun.source.tree.Tree;

// XXX: Consider a better name, without `Util`.
final class TreeKindUtil {
  private TreeKindUtil() {}

  /**
   * Returns a unique string representation of the given {@link Tree.Kind}.
   *
   * @return A string representation of the operator, if known
   * @throws IllegalStateException If the given input is not supported.
   */
  // XXX: Extend list to cover remaining cases; at least for any `Kind` that may appear in a
  // Refaster template. (E.g. keywords such as `if`, `instanceof`, `new`, ...)
  static String treeKindToString(Tree.Kind kind) {
    return switch (kind) {
      case ASSIGNMENT -> "=";
      case POSTFIX_INCREMENT -> "x++";
      case PREFIX_INCREMENT -> "++x";
      case POSTFIX_DECREMENT -> "x--";
      case PREFIX_DECREMENT -> "--x";
      case UNARY_PLUS -> "+x";
      case UNARY_MINUS -> "-x";
      case BITWISE_COMPLEMENT -> "~";
      case LOGICAL_COMPLEMENT -> "!";
      case MULTIPLY -> "*";
      case DIVIDE -> "/";
      case REMAINDER -> "%";
      case PLUS -> "+";
      case MINUS -> "-";
      case LEFT_SHIFT -> "<<";
      case RIGHT_SHIFT -> ">>";
      case UNSIGNED_RIGHT_SHIFT -> ">>>";
      case LESS_THAN -> "<";
      case GREATER_THAN -> ">";
      case LESS_THAN_EQUAL -> "<=";
      case GREATER_THAN_EQUAL -> ">=";
      case EQUAL_TO -> "==";
      case NOT_EQUAL_TO -> "!=";
      case AND -> "&";
      case XOR -> "^";
      case OR -> "|";
      case CONDITIONAL_AND -> "&&";
      case CONDITIONAL_OR -> "||";
      case MULTIPLY_ASSIGNMENT -> "*=";
      case DIVIDE_ASSIGNMENT -> "/=";
      case REMAINDER_ASSIGNMENT -> "%=";
      case PLUS_ASSIGNMENT -> "+=";
      case MINUS_ASSIGNMENT -> "-=";
      case LEFT_SHIFT_ASSIGNMENT -> "<<=";
      case RIGHT_SHIFT_ASSIGNMENT -> ">>=";
      case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT -> ">>>=";
      case AND_ASSIGNMENT -> "&=";
      case XOR_ASSIGNMENT -> "^=";
      case OR_ASSIGNMENT -> "|=";
      default -> throw new IllegalStateException("Cannot convert Tree.Kind to a String: " + kind);
    };
  }
}
