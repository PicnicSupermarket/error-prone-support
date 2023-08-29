package tech.picnic.errorprone.refaster.matchers;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types.FunctionDescriptorLookupError;
import java.util.Collection;

/**
 * A matcher of functional interface expressions for which execution of the functional interface
 * method may throw a checked exception.
 */
@SuppressWarnings("java:S2166" /* This type's name is suitable for a `Matcher`. */)
public final class ThrowsCheckedException implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link ThrowsCheckedException} instance. */
  public ThrowsCheckedException() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return containsCheckedException(getThrownTypes(tree, state), state);
  }

  private static Collection<Type> getThrownTypes(ExpressionTree tree, VisitorState state) {
    if (tree instanceof LambdaExpressionTree lambdaExpression) {
      return ASTHelpers.getThrownExceptions(lambdaExpression.getBody(), state);
    }

    if (tree instanceof MemberReferenceTree memberReference) {
      return ASTHelpers.getSymbol(memberReference).type.getThrownTypes();
    }

    Type type = ASTHelpers.getType(tree);
    if (type == null) {
      return ImmutableSet.of();
    }

    try {
      return state.getTypes().findDescriptorType(type).getThrownTypes();
    } catch (
        @SuppressWarnings("java:S1166" /* Not exceptional. */)
        FunctionDescriptorLookupError e) {
      return ImmutableSet.of();
    }
  }

  private static boolean containsCheckedException(Collection<Type> types, VisitorState state) {
    return !types.stream()
        .allMatch(
            t ->
                ASTHelpers.isSubtype(t, state.getSymtab().runtimeExceptionType, state)
                    || ASTHelpers.isSubtype(t, state.getSymtab().errorType, state));
  }
}
