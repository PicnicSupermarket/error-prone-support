package tech.picnic.errorprone.refaster.matchers;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;

/** A matcher of expressions that may a non-trivial amount of computation. */
public final class RequiresComputation implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link RequiresComputation} instance. */
  public RequiresComputation() {}

  @Override
  public boolean matches(ExpressionTree expressionTree, VisitorState state) {
    return matches(expressionTree);
  }

  // XXX: Some `BinaryTree`s may represent what could be considered "trivial computations".
  // Depending on feedback such trees may be matched in the future.
  private static boolean matches(ExpressionTree expressionTree) {
    return switch (expressionTree) {
      case ArrayAccessTree arrayAccess ->
          matches(arrayAccess.getExpression()) || matches(arrayAccess.getIndex());
      case LiteralTree literal -> false;
      case LambdaExpressionTree lambdaExpression ->
          /*
           * Lambda expressions encapsulate computations, but their definition does not involve
           * significant computation.
           */
          false;
      case IdentifierTree identifier ->
          // XXX: Generally identifiers don't by themselves represent a computation, though they may
          // be a stand-in for one if they are a Refaster template method argument. Can we identify
          // such cases, also when the `Matcher` is invoked by Refaster?
          false;
      case MemberReferenceTree memberReference -> matches(memberReference.getQualifierExpression());
      case MemberSelectTree memberSelect -> matches(memberSelect.getExpression());
      case ParenthesizedTree parenthesized -> matches(parenthesized.getExpression());
      case TypeCastTree typeCast -> matches(typeCast.getExpression());
      case UnaryTree unary ->
          // XXX: Arguably side-effectful options such as pre- and post-increment and -decrement
          // represent non-trivial computations.
          matches(unary.getExpression());
      default -> ASTHelpers.constValue(expressionTree) == null;
    };
  }
}
