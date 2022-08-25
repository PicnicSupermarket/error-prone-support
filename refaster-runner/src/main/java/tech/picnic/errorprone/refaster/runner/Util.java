package tech.picnic.errorprone.refaster.runner;

import com.sun.source.tree.Tree;

/** Fix a comment here later. */
public final class Util {
  private Util() {}

  /**
   * Returns a unique string representation of the given {@link Tree.Kind}.
   *
   * @return A string representation of the operator, if known
   * @throws IllegalArgumentException If the given input is not supported.
   */
  // XXX: Extend list to cover remaining cases; at least for any `Kind` that may appear in a
  // Refaster template.
  static String treeKindToString(Tree.Kind kind) {
    switch (kind) {
      case ASSIGNMENT:
        return "=";
      case POSTFIX_INCREMENT:
        return "x++";
      case PREFIX_INCREMENT:
        return "++x";
      case POSTFIX_DECREMENT:
        return "x--";
      case PREFIX_DECREMENT:
        return "--x";
      case UNARY_PLUS:
        return "+x";
      case UNARY_MINUS:
        return "-x";
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
      case PLUS:
        return "+";
      case MINUS:
        return "-";
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
      case MULTIPLY_ASSIGNMENT:
        return "*=";
      case DIVIDE_ASSIGNMENT:
        return "/=";
      case REMAINDER_ASSIGNMENT:
        return "%=";
      case PLUS_ASSIGNMENT:
        return "+=";
      case MINUS_ASSIGNMENT:
        return "-=";
      case LEFT_SHIFT_ASSIGNMENT:
        return "<<=";
      case RIGHT_SHIFT_ASSIGNMENT:
        return ">>=";
      case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
        return ">>>=";
      case AND_ASSIGNMENT:
        return "&=";
      case XOR_ASSIGNMENT:
        return "^=";
      case OR_ASSIGNMENT:
        return "|=";
      default:
        throw new IllegalStateException("Cannot convert Tree.Kind to a String: " + kind);
    }
  }
}
