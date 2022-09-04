package tech.picnic.errorprone.refaster.util;

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
import javax.annotation.Nullable;

abstract class AbstractTestChecker extends BugChecker implements CompilationUnitTreeMatcher {
  private static final long serialVersionUID = 1L;

  private final Matcher<ExpressionTree> delegate;

  AbstractTestChecker(Matcher<ExpressionTree> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Description matchCompilationUnit(CompilationUnitTree compilationUnit, VisitorState state) {
    new TreeScanner<Void, Void>() {
      @Nullable
      @Override
      public Void scan(Tree tree, @Nullable Void p) {
        if (tree instanceof ExpressionTree && delegate.matches((ExpressionTree) tree, state)) {
          state.reportMatch(
              Description.builder(tree, canonicalName(), null, defaultSeverity(), message())
                  .build());
        }

        return super.scan(tree, p);
      }

      @Nullable
      @Override
      public Void visitImport(ImportTree node, @Nullable Void unused) {
        /*
         * We're not interested in matching import statements. While components of these
         * can be `ExpressionTree`s.
         */
        return null;
      }

      @Nullable
      @Override
      public Void visitMethod(MethodTree node, @Nullable Void p) {
        /*
         * We're not interested in matching e.g. parameter and return type declarations. While these
         * can be `ExpressionTree`s, they will never be matched by Refaster.
         */
        return scan(node.getBody(), p);
      }
    }.scan(compilationUnit, null);

    return Description.NO_MATCH;
  }
}
