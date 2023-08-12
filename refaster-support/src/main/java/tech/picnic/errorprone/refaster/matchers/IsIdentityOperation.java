package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.toType;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LambdaExpressionTree.BodyKind;
import com.sun.source.tree.Tree.Kind;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;

/** A matcher of expressions that represent identity operations. */
// XXX: In selected contexts many other method invocations can be considered identity operations;
// see the `IdentityConversion` check. Review whether those can/should be captured by this matcher
// as well.
public final class IsIdentityOperation implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> DELEGATE =
      anyOf(
          staticMethod()
              .onDescendantOfAny(
                  DoubleUnaryOperator.class.getName(),
                  Function.class.getName(),
                  IntUnaryOperator.class.getName(),
                  LongUnaryOperator.class.getName())
              .named("identity"),
          isIdentityLambdaExpression());

  /** Instantiates a new {@link IsIdentityOperation} instance. */
  public IsIdentityOperation() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return DELEGATE.matches(tree, state);
  }

  // XXX: Also support selected block expressions, including ones that perform a vacuous parameter
  // transformation such as those identified by the `IdentityConversion` check.
  private static Matcher<ExpressionTree> isIdentityLambdaExpression() {
    return toType(
        LambdaExpressionTree.class,
        (tree, state) ->
            tree.getBodyKind() == BodyKind.EXPRESSION
                && tree.getParameters().size() == 1
                && tree.getBody().getKind() == Kind.IDENTIFIER
                && ASTHelpers.getSymbol(tree.getParameters().get(0))
                    .equals(ASTHelpers.getSymbol(tree.getBody())));
  }
}
