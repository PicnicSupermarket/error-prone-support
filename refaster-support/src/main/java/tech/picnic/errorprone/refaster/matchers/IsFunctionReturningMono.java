package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.matchers.Matchers.isSameType;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Suppliers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;

/**
 * A matcher of lambda expressions that are of type {@code java.util.function.Function} that returns
 * a {@code reactor.core.publisher.Mono}.
 */
public final class IsFunctionReturningMono implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> MONO_TYPE =
      isSameType(Suppliers.typeFromString("reactor.core.publisher.Mono"));
  private static final Matcher<Tree> FUNCTION_TYPE =
      isSameType(Suppliers.typeFromString("java.util.function.Function"));

  /** Instantiates a new {@link IsFunctionReturningMono} instance. */
  public IsFunctionReturningMono() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    if (tree.getKind() != Kind.LAMBDA_EXPRESSION) {
      return false;
    }

    LambdaExpressionTree lambdaExpressionTree = (LambdaExpressionTree) tree;
    return MONO_TYPE.matches(lambdaExpressionTree.getBody(), state)
        && FUNCTION_TYPE.matches(lambdaExpressionTree, state);
  }
}
