package tech.picnic.errorprone.refaster.matchers;

import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import org.jspecify.annotations.Nullable;

/**
 * An abstract {@link BugChecker} that reports a match for each expression matched by the given
 * {@link Matcher}.
 *
 * <p>Only {@link ExpressionTree}s that represent proper Java expressions (i.e. {@link
 * ExpressionTree}s that may be matched by Refaster) are considered.
 */
abstract class AbstractMatcherTestChecker extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;

  private final Matcher<ExpressionTree> delegate;

  AbstractMatcherTestChecker(Matcher<ExpressionTree> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Description matchCompilationUnit(CompilationUnitTree compilationUnit, VisitorState state) {
    new TreeScanner<@Nullable Void, TreePath>() {
      @Override
      public @Nullable Void scan(@Nullable Tree tree, TreePath treePath) {
        if (tree == null) {
          return null;
        }

        TreePath path = new TreePath(treePath, tree);
        if (tree instanceof ExpressionTree) {
          ExpressionTree expressionTree = (ExpressionTree) tree;
          if (!isMethodSelect(expressionTree, path)
              && delegate.matches(expressionTree, state.withPath(path))) {
            state.reportMatch(describeMatch(tree));
          }
        }

        return super.scan(tree, path);
      }

      @Override
      public @Nullable Void visitImport(ImportTree tree, TreePath path) {
        /*
         * We're not interested in matching import statements. While components of these
         * can be `ExpressionTree`s, they will never be matched by Refaster.
         */
        return null;
      }

      @Override
      public @Nullable Void visitMethod(MethodTree tree, TreePath path) {
        /*
         * We're not interested in matching e.g. parameter and return type declarations. While these
         * can be `ExpressionTree`s, they will never be matched by Refaster.
         */
        return scan(tree.getBody(), new TreePath(path, tree));
      }

      @Override
      public @Nullable Void visitTypeCast(TypeCastTree tree, TreePath path) {
        /*
         * We're not interested in matching the parenthesized type subtree that is part of a type
         * cast expression. While such trees can be `ExpressionTree`s, they will never be matched by
         * Refaster.
         */
        return scan(tree.getExpression(), new TreePath(path, tree));
      }
    }.scan(compilationUnit, state.getPath());

    return Description.NO_MATCH;
  }

  /**
   * Tells whether the given {@link ExpressionTree} is the {@link
   * MethodInvocationTree#getMethodSelect() method select} portion of a method invocation.
   *
   * <p>Such {@link ExpressionTree}s will never be matched by Refaster.
   */
  private static boolean isMethodSelect(ExpressionTree tree, TreePath path) {
    TreePath parentPath = path.getParentPath();
    if (parentPath == null) {
      return false;
    }

    Tree parentTree = parentPath.getLeaf();
    return parentTree instanceof MethodInvocationTree
        && ((MethodInvocationTree) parentTree).getMethodSelect().equals(tree);
  }
}
