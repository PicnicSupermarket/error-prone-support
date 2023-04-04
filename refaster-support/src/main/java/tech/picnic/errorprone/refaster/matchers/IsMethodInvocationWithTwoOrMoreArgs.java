package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.matchers.Matchers.anyMethod;
import static com.sun.source.tree.Tree.Kind.METHOD_INVOCATION;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

/** A matcher for method invocations with two or more arguments. */
public final class IsMethodInvocationWithTwoOrMoreArgs implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;

  @Override
  public boolean matches(ExpressionTree expressionTree, VisitorState state) {
    if (expressionTree.getKind() == METHOD_INVOCATION) {
      MethodInvocationTree methodInvocationTree = (MethodInvocationTree) expressionTree;
      return anyMethod().matches(methodInvocationTree.getMethodSelect(), state)
          && methodInvocationTree.getArguments().size() > 1;
    }
    return false;
  }
}
