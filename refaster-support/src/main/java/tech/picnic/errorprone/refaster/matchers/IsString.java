package tech.picnic.errorprone.refaster.matchers;

import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.tools.javac.code.Type;

/** A matcher of {@link String}-typed expressions. */
public final class IsString implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;

  /** Instantiates a new {@link IsString} instance. */
  public IsString() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    Type type = ASTHelpers.getType(tree);
    return type != null && state.getTypes().isSameType(type, state.getSymtab().stringType);
  }
}
