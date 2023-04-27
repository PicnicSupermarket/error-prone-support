package tech.picnic.errorprone.refaster.matchers;

import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.CompilationUnitTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
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
    new TreeScanner<@Nullable Void, @Nullable Void>() {
      @Override
      public @Nullable Void scan(Tree tree, @Nullable Void unused) {
        if (tree instanceof ExpressionTree expression && delegate.matches(expression, state)) {
          state.reportMatch(describeMatch(tree));
        }

        return super.scan(tree, unused);
      }

      @Override
      public @Nullable Void visitImport(ImportTree node, @Nullable Void unused) {
        /*
         * We're not interested in matching import statements. While components of these
         * can be `ExpressionTree`s, they will never be matched by Refaster.
         */
        return null;
      }

      @Override
      public @Nullable Void visitMethod(MethodTree node, @Nullable Void unused) {
        /*
         * We're not interested in matching e.g. parameter and return type declarations. While these
         * can be `ExpressionTree`s, they will never be matched by Refaster.
         */
        return scan(node.getBody(), unused);
      }
    }.scan(compilationUnit, null);

    return Description.NO_MATCH;
  }
}
