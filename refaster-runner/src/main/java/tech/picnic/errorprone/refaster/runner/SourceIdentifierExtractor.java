package tech.picnic.errorprone.refaster.runner;

import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import java.util.HashSet;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Extracts all identifiers from a {@link Tree}.
 *
 * <p>This class scans a Javac tree and collects all identifiers that are relevant for matching
 * Refaster rules. It extracts identifiers from expressions, member references, member selects, and
 * operators.
 */
final class SourceIdentifierExtractor extends TreeScanner<@Nullable Void, Set<String>> {
  private static final SourceIdentifierExtractor INSTANCE = new SourceIdentifierExtractor();

  private SourceIdentifierExtractor() {}

  /**
   * Extracts all identifiers from the given {@link Tree}.
   *
   * @param tree The tree to extract identifiers from.
   * @return A set of all identifiers found in the tree.
   */
  static Set<String> extractIdentifiers(Tree tree) {
    Set<String> identifiers = new HashSet<>();
    INSTANCE.scan(tree, identifiers);
    return identifiers;
  }

  @Override
  public @Nullable Void visitPackage(PackageTree node, Set<String> identifiers) {
    /* Refaster rules never match package declarations. */
    return null;
  }

  @Override
  public @Nullable Void visitClass(ClassTree node, Set<String> identifiers) {
    /*
     * Syntactic details of a class declaration other than the definition of its members do not
     * need to be reflected in a Refaster rule for it to apply to the class's code.
     */
    return scan(node.getMembers(), identifiers);
  }

  @Override
  public @Nullable Void visitMethod(MethodTree node, Set<String> identifiers) {
    if (ASTHelpers.isGeneratedConstructor(node)) {
      return null;
    }

    /*
     * Syntactic details of a method declaration other than its body do not need to be reflected
     * in a Refaster rule for it to apply to the method's code.
     */
    return scan(node.getBody(), identifiers);
  }

  @Override
  public @Nullable Void visitVariable(VariableTree node, Set<String> identifiers) {
    /* A variable's modifiers and name do not influence where a Refaster rule matches. */
    return reduce(scan(node.getInitializer(), identifiers), scan(node.getType(), identifiers));
  }

  @Override
  public @Nullable Void visitIdentifier(IdentifierTree node, Set<String> identifiers) {
    identifiers.add(node.getName().toString());
    return null;
  }

  @Override
  public @Nullable Void visitMemberReference(MemberReferenceTree node, Set<String> identifiers) {
    super.visitMemberReference(node, identifiers);
    identifiers.add(node.getName().toString());
    return null;
  }

  @Override
  public @Nullable Void visitMemberSelect(MemberSelectTree node, Set<String> identifiers) {
    super.visitMemberSelect(node, identifiers);
    identifiers.add(node.getIdentifier().toString());
    return null;
  }

  @Override
  public @Nullable Void visitAssignment(AssignmentTree node, Set<String> identifiers) {
    registerOperator(node, identifiers);
    return super.visitAssignment(node, identifiers);
  }

  @Override
  public @Nullable Void visitCompoundAssignment(
      CompoundAssignmentTree node, Set<String> identifiers) {
    registerOperator(node, identifiers);
    return super.visitCompoundAssignment(node, identifiers);
  }

  @Override
  public @Nullable Void visitUnary(UnaryTree node, Set<String> identifiers) {
    registerOperator(node, identifiers);
    return super.visitUnary(node, identifiers);
  }

  @Override
  public @Nullable Void visitBinary(BinaryTree node, Set<String> identifiers) {
    registerOperator(node, identifiers);
    return super.visitBinary(node, identifiers);
  }

  private static void registerOperator(ExpressionTree node, Set<String> identifiers) {
    identifiers.add(treeKindToString(node.getKind()));
  }

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
