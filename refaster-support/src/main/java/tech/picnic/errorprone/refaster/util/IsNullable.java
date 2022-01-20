package tech.picnic.errorprone.refaster.util;

import com.google.errorprone.VisitorState;
import com.google.errorprone.dataflow.nullnesspropagation.Nullness;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.NullnessMatcher;
import com.sun.source.tree.ExpressionTree;

/** A matcher of nullable expressions, for use with Refaster's {@code @Matches} annotation. */
public final class IsNullable implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    NullnessMatcher nullnessMatcher = new NullnessMatcher(Nullness.NULLABLE);
    return nullnessMatcher.matches(tree, state);
  }
}
