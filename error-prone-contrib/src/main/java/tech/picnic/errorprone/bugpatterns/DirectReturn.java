package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.isVariable;
import static com.google.errorprone.matchers.Matchers.returnStatement;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
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
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import java.util.List;
import java.util.Optional;
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

  /** Instantiates a new {@link DirectReturn} instance. */
  public DirectReturn() {}

  @Override
  public Description matchBlock(BlockTree tree, VisitorState state) {
    List<? extends StatementTree> statements = tree.getStatements();
    if (statements.size() < 2) {
      return Description.NO_MATCH;
    }

    StatementTree finalStatement = statements.get(statements.size() - 1);
    if (!VARIABLE_RETURN.matches(finalStatement, state)) {
      return Description.NO_MATCH;
    }

    Symbol variableSymbol = ASTHelpers.getSymbol(((ReturnTree) finalStatement).getExpression());
    StatementTree precedingStatement = statements.get(statements.size() - 2);

    return tryMatchAssignment(variableSymbol, precedingStatement)
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
}
