package tech.picnic.errorprone.refaster.matchers;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;

/** A matcher of lambda expressions or method references. */
public final class IsLambdaExpressionOrMethodReference implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link IsLambdaExpressionOrMethodReference} instance. */
  public IsLambdaExpressionOrMethodReference() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return isLambdaExpression(tree) || isMethodReference(tree);
  }

  private static boolean isLambdaExpression(ExpressionTree tree) {
    return tree instanceof LambdaExpressionTree;
  }

  private static boolean isMethodReference(ExpressionTree tree) {
    return tree instanceof MemberReferenceTree
        && ((MemberReferenceTree) tree).getMode() == MemberReferenceTree.ReferenceMode.INVOKE;
  }
}
