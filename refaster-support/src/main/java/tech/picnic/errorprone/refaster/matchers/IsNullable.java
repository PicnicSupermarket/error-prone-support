package tech.picnic.errorprone.refaster.matchers;

import com.google.errorprone.VisitorState;
import com.google.errorprone.dataflow.nullnesspropagation.Nullness;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.NullnessMatcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;

/** A matcher of nullable expressions. */
public final class IsNullable implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> DELEGATE = new NullnessMatcher(Nullness.NONNULL);

  /** Instantiates a new {@link IsNullable} instance. */
  public IsNullable() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return !DELEGATE.matches(tree, state) && !isSuper(tree);
  }

  private static boolean isSuper(Tree tree) {
    return ASTHelpers.isSuper(tree)
        || (tree instanceof MethodInvocationTree
            && ASTHelpers.isSuper(((MethodInvocationTree) tree).getMethodSelect()));
  }
}
