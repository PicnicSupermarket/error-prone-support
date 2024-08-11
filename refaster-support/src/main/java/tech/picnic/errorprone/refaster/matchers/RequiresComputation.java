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
    if (expressionTree instanceof ArrayAccessTree arrayAccess) {
      return matches(arrayAccess.getExpression()) || matches(arrayAccess.getIndex());
    }

    if (expressionTree instanceof LiteralTree) {
      return false;
    }

    if (expressionTree instanceof LambdaExpressionTree) {
      /*
       * Lambda expressions encapsulate computations, but their definition does not involve
       * significant computation.
       */
      return false;
    }

    if (expressionTree instanceof IdentifierTree) {
      // XXX: Generally identifiers don't by themselves represent a computation, though they may be
      // a stand-in for one if they are a Refaster template method argument. Can we identify such
      // cases, also when the `Matcher` is invoked by Refaster?
      return false;
    }

    if (expressionTree instanceof MemberReferenceTree memberReference) {
      return matches(memberReference.getQualifierExpression());
    }

    if (expressionTree instanceof MemberSelectTree memberSelect) {
      return matches(memberSelect.getExpression());
    }

    if (expressionTree instanceof ParenthesizedTree parenthesized) {
      return matches(parenthesized.getExpression());
    }

    if (expressionTree instanceof TypeCastTree typeCast) {
      return matches(typeCast.getExpression());
    }

    if (expressionTree instanceof UnaryTree unary) {
      // XXX: Arguably side-effectful options such as pre- and post-increment and -decrement
      // represent non-trivial computations.
      return matches(unary.getExpression());
    }

    if (ASTHelpers.constValue(expressionTree) != null) {
      return false;
    }

    return true;
  }
}
