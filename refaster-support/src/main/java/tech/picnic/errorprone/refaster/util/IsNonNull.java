package tech.picnic.errorprone.refaster.util;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.sun.source.tree.ExpressionTree;

/** A matcher of non-null expressions, for use with Refaster's {@code @Matches} annotation. */
public final class IsNonNull implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> NONNULL = Matchers.isNonNullUsingDataflow();

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return NONNULL.matches(tree, state);
  }
}
