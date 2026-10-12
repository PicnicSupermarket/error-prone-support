package tech.picnic.errorprone.refaster.matchers;

import static com.google.errorprone.matchers.Matchers.nullLiteral;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;

/** A matcher of the {@code null} literal. */
public final class IsNullLiteral implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> DELEGATE = nullLiteral();

  /** Instantiates a new {@link IsNullLiteral} instance. */
  public IsNullLiteral() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return DELEGATE.matches(tree, state);
  }
}
