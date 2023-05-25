package tech.picnic.errorprone.refaster.matchers;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree.Kind;

/** A matcher of expressions with type arguments. */
public final class HasTypeArguments implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link HasTypeArguments} instance. */
  public HasTypeArguments() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    switch (tree.getKind()) {
      case METHOD_INVOCATION:
        return !((MethodInvocationTree) tree).getTypeArguments().isEmpty();
      case NEW_CLASS:
        NewClassTree classTree = (NewClassTree) tree;
        if (!classTree.getTypeArguments().isEmpty()) {
          return true;
        }

        ExpressionTree identifier = classTree.getIdentifier();
        if (identifier.getKind() != Kind.PARAMETERIZED_TYPE) {
          return false;
        }

        return !((ParameterizedTypeTree) identifier).getTypeArguments().isEmpty();
      default:
        return false;
    }
  }
}
