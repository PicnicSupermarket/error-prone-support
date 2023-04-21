package tech.picnic.errorprone.refaster.matchers;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

/** A matcher for method invocations with two or more arguments. */
public final class IsMethodInvocationWithTwoOrMoreArgs implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link IsMethodInvocationWithTwoOrMoreArgs} instance. */
  public IsMethodInvocationWithTwoOrMoreArgs() {}

  @Override
  public boolean matches(ExpressionTree expressionTree, VisitorState state) {
    return expressionTree instanceof MethodInvocationTree
        && ((MethodInvocationTree) expressionTree).getArguments().size() > 1;
  }
}
