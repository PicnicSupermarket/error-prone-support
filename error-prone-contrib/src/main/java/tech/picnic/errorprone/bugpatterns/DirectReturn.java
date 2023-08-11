package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.argument;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.isVariable;
import static com.google.errorprone.matchers.Matchers.not;
import static com.google.errorprone.matchers.Matchers.returnStatement;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.toType;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.MoreMatchers.HAS_LOMBOK_DATA;

import com.google.auto.service.AutoService;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.BlockTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.bugpatterns.util.MoreASTHelpers;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

/**
 * A {@link BugChecker} that flags unnecessary local variable assignments preceding a return
 * statement.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Variable assignment is redundant; value can be returned directly",
    link = BUG_PATTERNS_BASE_URL + "DirectReturn",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = SIMPLIFICATION)
public final class DirectReturn extends BugChecker implements BlockTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<StatementTree> VARIABLE_RETURN = returnStatement(isVariable());
  private static final Matcher<ExpressionTree> MOCKITO_MOCK_OR_SPY_WITH_IMPLICIT_TYPE =
      allOf(
          not(toType(MethodInvocationTree.class, argument(0, isSameType(Class.class.getName())))),
          staticMethod().onClass("org.mockito.Mockito").namedAnyOf("mock", "spy"));

  /** Instantiates a new {@link DirectReturn} instance. */
  public DirectReturn() {}

  @Override
  public Description matchBlock(BlockTree tree, VisitorState state) {
    List<? extends StatementTree> statements = tree.getStatements();
    ClassTree enclosingClass = ASTHelpers.findEnclosingNode(state.getPath(), ClassTree.class);
    if (statements.size() < 2 || HAS_LOMBOK_DATA.matches(enclosingClass, state)) {
      return Description.NO_MATCH;
    }

    StatementTree finalStatement = statements.get(statements.size() - 1);
    if (!VARIABLE_RETURN.matches(finalStatement, state)) {
      return Description.NO_MATCH;
    }

    Symbol variableSymbol = ASTHelpers.getSymbol(((ReturnTree) finalStatement).getExpression());
    StatementTree precedingStatement = statements.get(statements.size() - 2);

    return tryMatchAssignment(variableSymbol, precedingStatement)
        .filter(
            resultExpr ->
                canInlineToReturnStatement(resultExpr, state)
                    && !isIdentifierSymbolReferencedInAssociatedFinallyBlock(variableSymbol, state))
        .map(
            resultExpr ->
                describeMatch(
                    precedingStatement,
                    SuggestedFix.builder()
                        .replace(
                            precedingStatement,
                            String.format("return %s;", SourceCode.treeToString(resultExpr, state)))
                        .delete(finalStatement)
                        .build()))
        .orElse(Description.NO_MATCH);
  }

  private static Optional<ExpressionTree> tryMatchAssignment(Symbol targetSymbol, Tree tree) {
    if (tree instanceof ExpressionStatementTree) {
      return tryMatchAssignment(targetSymbol, ((ExpressionStatementTree) tree).getExpression());
    }

    if (tree instanceof AssignmentTree) {
      AssignmentTree assignment = (AssignmentTree) tree;
      return targetSymbol.equals(ASTHelpers.getSymbol(assignment.getVariable()))
          ? Optional.of(assignment.getExpression())
          : Optional.empty();
    }

    if (tree instanceof VariableTree) {
      VariableTree declaration = (VariableTree) tree;
      return declaration.getModifiers().getAnnotations().isEmpty()
              && targetSymbol.equals(ASTHelpers.getSymbol(declaration))
          ? Optional.ofNullable(declaration.getInitializer())
          : Optional.empty();
    }

    return Optional.empty();
  }

  /**
   * Tells whether inlining the given expression to the associated return statement can likely be
   * done without changing the expression's return type.
   *
   * <p>Inlining an expression generally does not change its return type, but in rare cases the
   * operation may have a functional impact. The sole case considered here is the inlining of a
   * Mockito mock or spy construction without an explicit type. In such a case the type created
   * depends on context, such as the method's return type.
   */
  private static boolean canInlineToReturnStatement(
      ExpressionTree expressionTree, VisitorState state) {
    return !MOCKITO_MOCK_OR_SPY_WITH_IMPLICIT_TYPE.matches(expressionTree, state)
        || MoreASTHelpers.findMethodExitedOnReturn(state)
            .filter(m -> MoreASTHelpers.areSameType(expressionTree, m.getReturnType(), state))
            .isPresent();
  }

  /**
   * Tells whether the given identifier {@link Symbol} is referenced in a {@code finally} block that
   * is executed <em>after</em> control flow returns from the {@link VisitorState#getPath() current
   * location}.
   */
  private static boolean isIdentifierSymbolReferencedInAssociatedFinallyBlock(
      Symbol symbol, VisitorState state) {
    return Streams.zip(
            Streams.stream(state.getPath()).skip(1),
            Streams.stream(state.getPath()),
            (tree, child) -> {
              if (!(tree instanceof TryTree)) {
                return null;
              }

              BlockTree finallyBlock = ((TryTree) tree).getFinallyBlock();
              return !child.equals(finallyBlock) ? finallyBlock : null;
            })
        .anyMatch(finallyBlock -> referencesIdentifierSymbol(symbol, finallyBlock));
  }

  private static boolean referencesIdentifierSymbol(Symbol symbol, @Nullable BlockTree tree) {
    return Boolean.TRUE.equals(
        new TreeScanner<Boolean, @Nullable Void>() {
          @Override
          public Boolean visitIdentifier(IdentifierTree node, @Nullable Void unused) {
            return symbol.equals(ASTHelpers.getSymbol(node));
          }

          @Override
          public Boolean reduce(Boolean r1, Boolean r2) {
            return Boolean.TRUE.equals(r1) || Boolean.TRUE.equals(r2);
          }
        }.scan(tree, null));
  }
}
