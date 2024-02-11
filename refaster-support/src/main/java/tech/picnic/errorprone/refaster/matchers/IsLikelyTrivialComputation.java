package tech.picnic.errorprone.refaster.matchers;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;

/** A matcher of expressions that likely require little to no computation. */
public final class IsLikelyTrivialComputation implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link IsLikelyTrivialComputation} instance. */
  public IsLikelyTrivialComputation() {}

  @Override
  public boolean matches(ExpressionTree expressionTree, VisitorState state) {
    if (expressionTree instanceof MethodInvocationTree methodInvocation) {
      // XXX: Method invocations are generally *not* trivial computations, but we make an exception
      // for nullary method invocations on the result of a trivial computation. This exception
      // allows this `Matcher` to by the `OptionalOrElseGet` Refaster rule, such that it does not
      // suggest the introduction of lambda expressions that are better expressed as method
      // references. Once the `MethodReferenceUsage` bug checker is production-ready, this exception
      // should be removed. (But at that point, instead defining a `RequiresComputation` matcher may
      // be more appropriate.)
      if (methodInvocation.getArguments().isEmpty()
          && matches(methodInvocation.getMethodSelect())) {
        return true;
      }
    }

    return matches(expressionTree);
  }

  // XXX: Some `BinaryTree`s may represent what could be considered "trivial computations".
  // Depending on feedback such trees may be matched in the future.
  private static boolean matches(ExpressionTree expressionTree) {
    if (expressionTree instanceof ArrayAccessTree arrayAccess) {
      return matches(arrayAccess.getExpression()) && matches(arrayAccess.getIndex());
    }

    if (expressionTree instanceof LiteralTree) {
      return true;
    }

    if (expressionTree instanceof LambdaExpressionTree) {
      /*
       * Lambda expressions encapsulate computations, but their definition does not involve
       * significant computation.
       */
      return true;
    }

    if (expressionTree instanceof IdentifierTree) {
      return true;
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
      // XXX: Arguably side-effectful options such as pre- and post-increment and -decrement are not
      // trivial.
      return matches(unary.getExpression());
    }

    return false;
  }
}
