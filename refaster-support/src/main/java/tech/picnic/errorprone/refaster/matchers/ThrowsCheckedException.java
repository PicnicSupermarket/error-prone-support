package tech.picnic.errorprone.refaster.matchers;

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
@SuppressWarnings({
  "java:S2166",
  "NonExceptionNameEndsWithException"
} /* This type's name is suitable for a `Matcher`. */)
public final class ThrowsCheckedException implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link ThrowsCheckedException} instance. */
  public ThrowsCheckedException() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    if (tree instanceof LambdaExpressionTree) {
      return throwsCheckedException((LambdaExpressionTree) tree, state);
    }

    if (tree instanceof MemberReferenceTree) {
      return throwsCheckedException((MemberReferenceTree) tree, state);
    }

    Type type = ASTHelpers.getType(tree);
    return type != null && throwsCheckedException(type, state);
  }

  private static boolean throwsCheckedException(LambdaExpressionTree tree, VisitorState state) {
    return containsCheckedException(ASTHelpers.getThrownExceptions(tree.getBody(), state), state);
  }

  private static boolean throwsCheckedException(MemberReferenceTree tree, VisitorState state) {
    return containsCheckedException(ASTHelpers.getSymbol(tree).type.getThrownTypes(), state);
  }

  private static boolean throwsCheckedException(Type type, VisitorState state) {
    try {
      return containsCheckedException(
          state.getTypes().findDescriptorType(type).getThrownTypes(), state);
    } catch (
        @SuppressWarnings("java:S1166" /* Not exceptional. */)
        FunctionDescriptorLookupError e) {
      /* This isn't a functional interface: check its supertypes. */
      return state.getTypes().directSupertypes(type).stream()
          .anyMatch(t -> throwsCheckedException(t, state));
    }
  }

  private static boolean containsCheckedException(Collection<Type> types, VisitorState state) {
    return types.stream().anyMatch(type -> isCheckedException(type, state));
  }

  private static boolean isCheckedException(Type type, VisitorState state) {
    return !ASTHelpers.isSubtype(type, state.getSymtab().runtimeExceptionType, state)
        && !ASTHelpers.isSubtype(type, state.getSymtab().errorType, state);
  }
}
