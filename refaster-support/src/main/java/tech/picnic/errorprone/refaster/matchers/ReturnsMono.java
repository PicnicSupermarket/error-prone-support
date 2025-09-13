package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static tech.picnic.errorprone.utils.MoreMatchers.isSubTypeOf;
import static tech.picnic.errorprone.utils.MoreTypes.generic;
import static tech.picnic.errorprone.utils.MoreTypes.subOf;
import static tech.picnic.errorprone.utils.MoreTypes.unbound;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.suppliers.Suppliers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import java.util.function.Function;
import tech.picnic.errorprone.utils.MoreTypePredicates;

/**
 * A matcher of {@link Function}s that return a {@code reactor.core.publisher.Mono}.
 *
 * <p>Refaster template method parameter types enable one to constrain the type of expressions
 * matched by the "free variables" represented by the associated method parameters. For this,
 * Refaster relies on the Java compiler to determine the type of candidate expressions. For lambda
 * expressions and method references, the inferred type is one that is compatible with the
 * associated assignment, invocation or casting context. It is important to note that this inferred
 * type is not necessarily the most specific compatible type.
 *
 * <p>This matcher addresses that issue for the specific case in which one wishes to e.g. match
 * expressions that are assignment/invocation/cast-compatible with {@code Function<String, Mono<?
 * extends String>>}, but are in fact typed {@code Function<String, Publisher<? extends String>>}.
 */
// XXX: Generalize this matcher to also support other functional types such as `BiFunction` and
// `Supplier`, and then update the Javadoc.
public final class ReturnsMono implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Supplier<Type> MONO =
      generic(Suppliers.typeFromString("reactor.core.publisher.Mono"), unbound());
  private static final Supplier<Type> MONO_RETURNING_FUNCTION =
      generic(Suppliers.typeFromString(Function.class.getCanonicalName()), unbound(), subOf(MONO));
  private static final TypePredicate IS_MONO_TYPE = MoreTypePredicates.isSubTypeOf(MONO);
  private static final Matcher<Tree> IS_MONO_TREE = isSubTypeOf(MONO);
  private static final Matcher<Tree> IS_FUNCTION_RETURNING_MONO_TREE =
      isSubTypeOf(MONO_RETURNING_FUNCTION);

  /**
   * A matcher of any {@link Function}-typed expression.
   *
   * <p>Due to type erasure, this matcher does not consider the return type of the function.
   */
  private static final Matcher<Tree> IS_FUNCTION_TREE = isSubtypeOf(MONO_RETURNING_FUNCTION);

  /** Instantiates a new {@link ReturnsMono} instance. */
  public ReturnsMono() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    if (!IS_FUNCTION_TREE.matches(tree, state)) {
      return false;
    }

    if (tree instanceof LambdaExpressionTree lambdaExpression) {
      return IS_MONO_TREE.matches(lambdaExpression.getBody(), state);
    }

    if (tree instanceof MemberReferenceTree memberReference) {
      return IS_MONO_TYPE.apply(
          ASTHelpers.getSymbol(memberReference).type.asMethodType().getReturnType(), state);
    }

    return IS_FUNCTION_RETURNING_MONO_TREE.matches(tree, state);
  }
}
