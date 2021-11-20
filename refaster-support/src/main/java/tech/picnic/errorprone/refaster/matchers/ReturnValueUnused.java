package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.kindIs;
import static com.google.errorprone.matchers.Matchers.parentNode;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.Tree;
import javax.lang.model.type.TypeKind;

/**
 * A matcher of expressions of which the result (if any) is unused, for use with Refaster's
 * {@code @Matches} annotation.
 */
// XXX: Review whether other parts of Error Prone's `AbstractReturnValueIgnored` should be ported to
// this class.
public final class ReturnValueUnused implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> DELEGATE =
      parentNode(
          anyOf(
              ReturnValueUnused::isVoidReturningLambdaExpression,
              kindIs(Tree.Kind.EXPRESSION_STATEMENT)));

  /** Instantiates a new {@link ReturnValueUnused} instance. */
  public ReturnValueUnused() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return DELEGATE.matches(tree, state);
  }

  private static boolean isVoidReturningLambdaExpression(Tree tree, VisitorState state) {
    return tree instanceof LambdaExpressionTree
        && state.getTypes().findDescriptorType(ASTHelpers.getType(tree)).getReturnType().getKind()
            == TypeKind.VOID;
  }
}
