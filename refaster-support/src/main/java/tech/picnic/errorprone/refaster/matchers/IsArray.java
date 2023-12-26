package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.matchers.Matchers.isArrayType;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;

/** A matcher of array-typed expressions. */
public final class IsArray implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> DELEGATE = isArrayType();

  /** Instantiates a new {@link IsArray} instance. */
  public IsArray() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return DELEGATE.matches(tree, state);
  }
}
