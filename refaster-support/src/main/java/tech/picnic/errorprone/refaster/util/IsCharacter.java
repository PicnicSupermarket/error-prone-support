package tech.picnic.errorprone.refaster.util;

import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isSameType;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;

/** A matcher of {@link Character}-typed expressions. */
public final class IsCharacter implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> DELEGATE =
      anyOf(isSameType(Character.class), isSameType(s -> s.getSymtab().charType));

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return DELEGATE.matches(tree, state);
  }
}
