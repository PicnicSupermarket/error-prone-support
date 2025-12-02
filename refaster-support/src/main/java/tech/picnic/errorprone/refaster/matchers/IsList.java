package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.matchers.Matchers.isSubtypeOf;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import java.util.List;

/** A matcher of {@link List}-typed expressions. */
public final class IsList implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> DELEGATE = isSubtypeOf(List.class);

  /** Instantiates a new {@link IsList} instance. */
  public IsList() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return DELEGATE.matches(tree, state);
  }
}
