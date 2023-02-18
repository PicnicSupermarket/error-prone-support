package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.sun.source.tree.MemberReferenceTree.ReferenceMode.INVOKE;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.Tree;

/** A matcher of lambda expressions or method references. */
public final class IsLambdaExpressionOrMethodReference implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;

  private static final Matcher<ExpressionTree> DELEGATE =
      anyOf(isLambdaExpression(), isMethodReference());

  /** Instantiates a new {@link IsLambdaExpressionOrMethodReference} instance. */
  public IsLambdaExpressionOrMethodReference() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return DELEGATE.matches(tree, state);
  }

  /** Returns a matcher that matches lambda expressions. */
  public static <T extends Tree> Matcher<T> isLambdaExpression() {
    return (tree, state) -> tree instanceof LambdaExpressionTree;
  }

  /** Returns a matcher that matches method references. */
  public static <T extends Tree> Matcher<T> isMethodReference() {
    return (tree, state) ->
        tree instanceof MemberReferenceTree
            && ((MemberReferenceTree) tree).getMode().equals(INVOKE);
  }
}
