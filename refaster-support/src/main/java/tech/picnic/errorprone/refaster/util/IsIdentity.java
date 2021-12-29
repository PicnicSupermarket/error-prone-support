package tech.picnic.errorprone.refaster.util;

import static com.google.errorprone.matchers.Matchers.staticMethod;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;

/**
 * A matcher for `identity` operations, for use with Refaster's {@code {Not,}Matches} annotation.
 */
public final class IsIdentity implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> IDENTITY =
      staticMethod()
          .onClass((type, state) -> type.toString().contains("Function"))
          .named("identity");

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return IDENTITY.matches(tree, state);
  }
}
