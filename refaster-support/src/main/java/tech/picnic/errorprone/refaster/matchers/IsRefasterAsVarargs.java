package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.matchers.Matchers.staticMethod;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.Refaster;
import com.sun.source.tree.ExpressionTree;

/** A matcher of {@link Refaster#asVarargs} method invocations. */
public final class IsRefasterAsVarargs implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> DELEGATE =
      staticMethod().onClass(Refaster.class.getCanonicalName()).named("asVarargs");

  /** Instantiates a new {@link IsRefasterAsVarargs} instance. */
  public IsRefasterAsVarargs() {}

  @Override
  public boolean matches(ExpressionTree expressionTree, VisitorState state) {
    return DELEGATE.matches(expressionTree, state);
  }
}
