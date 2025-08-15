package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.matchers.Matchers.typePredicateMatcher;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;

/** A matcher of expressions that represent a multidimensional array type. */
public final class IsMultidimensionalArray implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> DELEGATE =
      typePredicateMatcher((type, state) -> state.getTypes().dimensions(type) > 1);

  /** Instantiates a new {@link IsMultidimensionalArray} instance. */
  public IsMultidimensionalArray() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return DELEGATE.matches(tree, state);
  }
}
