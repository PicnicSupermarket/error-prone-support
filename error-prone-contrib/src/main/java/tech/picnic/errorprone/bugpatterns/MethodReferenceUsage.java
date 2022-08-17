package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;

import com.google.auto.service.AutoService;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.LambdaExpressionTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import java.util.List;
import java.util.Optional;
import javax.lang.model.element.Name;

/**
 * A {@link BugChecker} which flags lambda expressions that can be replaced with method references.
 */
// XXX: Other custom expressions we could rewrite:
// - `a -> "str" + a` to `"str"::concat`. But only if `str` is provably non-null.
// - `(a, b) -> a + b` to `String::concat` or `{Integer,Long,Float,Double}::sum`. Also requires null
//   checking.
// - `i -> new int[i]` to `int[]::new`.
// - `() -> new Foo()` to `Foo::new` (and variations).
// XXX: Link to Effective Java, Third Edition, Item 43. In there the suggested approach is not so
// black-and-white. Maybe we can more closely approximate it?
// XXX: With Java 9's introduction of `Predicate.not`, we could write many lambda expressions to
// `not(some::reference)`.
// XXX: This check is extremely inefficient due to its reliance on `SuggestedFixes.compilesWithFix`.
// Palantir's `LambdaMethodReference` check seems to suffer a similar issue at this time.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer method references over lambda expressions",
    linkType = NONE,
    severity = SUGGESTION,
    tags = STYLE)
public final class MethodReferenceUsage extends BugChecker implements LambdaExpressionTreeMatcher {
  private static final long serialVersionUID = 1L;

  @Override
  public Description matchLambdaExpression(LambdaExpressionTree tree, VisitorState state) {
    /*
     * Lambda expressions can be used in several places where method references cannot, either
     * because the latter are not syntactically valid or ambiguous. Rather than encoding all these
     * edge cases we try to compile the code with the suggested fix, to see whether this works.
     */
    return constructMethodRef(tree, tree.getBody())
        .map(SuggestedFix.Builder::build)
        .filter(
            fix ->
                SuggestedFixes.compilesWithFix(
                    fix, state, ImmutableList.of(), /* onlyInSameCompilationUnit= */ true))
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static Optional<SuggestedFix.Builder> constructMethodRef(
      LambdaExpressionTree lambdaExpr, Tree subTree) {
    switch (subTree.getKind()) {
      case BLOCK:
        return constructMethodRef(lambdaExpr, (BlockTree) subTree);
      case EXPRESSION_STATEMENT:
        return constructMethodRef(lambdaExpr, ((ExpressionStatementTree) subTree).getExpression());
      case METHOD_INVOCATION:
        return constructMethodRef(lambdaExpr, (MethodInvocationTree) subTree);
      case PARENTHESIZED:
        return constructMethodRef(lambdaExpr, ((ParenthesizedTree) subTree).getExpression());
      case RETURN:
        return constructMethodRef(lambdaExpr, ((ReturnTree) subTree).getExpression());
      default:
        return Optional.empty();
    }
  }

  private static Optional<SuggestedFix.Builder> constructMethodRef(
      LambdaExpressionTree lambdaExpr, BlockTree subTree) {
    return Optional.of(subTree.getStatements())
        .filter(statements -> statements.size() == 1)
        .flatMap(statements -> constructMethodRef(lambdaExpr, statements.get(0)));
  }

  @SuppressWarnings("NestedOptionals")
  private static Optional<SuggestedFix.Builder> constructMethodRef(
      LambdaExpressionTree lambdaExpr, MethodInvocationTree subTree) {
    return matchArguments(lambdaExpr, subTree)
        .flatMap(expectedInstance -> constructMethodRef(lambdaExpr, subTree, expectedInstance));
  }

  private static Optional<SuggestedFix.Builder> constructMethodRef(
      LambdaExpressionTree lambdaExpr,
      MethodInvocationTree subTree,
      Optional<Name> expectedInstance) {
    ExpressionTree methodSelect = subTree.getMethodSelect();
    switch (methodSelect.getKind()) {
      case IDENTIFIER:
        if (expectedInstance.isPresent()) {
          /* Direct method call; there is no matching "implicit parameter". */
          return Optional.empty();
        }
        Symbol sym = ASTHelpers.getSymbol(methodSelect);
        if (!sym.isStatic()) {
          return constructFix(lambdaExpr, "this", methodSelect);
        }
        return constructFix(lambdaExpr, sym.owner, methodSelect);
      case MEMBER_SELECT:
        return constructMethodRef(lambdaExpr, (MemberSelectTree) methodSelect, expectedInstance);
      default:
        throw new VerifyException("Unexpected type of expression: " + methodSelect.getKind());
    }
  }

  private static Optional<SuggestedFix.Builder> constructMethodRef(
      LambdaExpressionTree lambdaExpr, MemberSelectTree subTree, Optional<Name> expectedInstance) {
    if (subTree.getExpression().getKind() != Kind.IDENTIFIER) {
      // XXX: Could be parenthesized. Handle. Also in other classes.
      /*
       * Only suggest a replacement if the method select's expression provably doesn't have
       * side-effects. Otherwise the replacement may not be behavior preserving.
       */
      return Optional.empty();
    }

    Name lhs = ((IdentifierTree) subTree.getExpression()).getName();
    if (expectedInstance.isEmpty()) {
      return constructFix(lambdaExpr, lhs, subTree.getIdentifier());
    }

    Type lhsType = ASTHelpers.getType(subTree.getExpression());
    if (lhsType == null || !expectedInstance.orElseThrow().equals(lhs)) {
      return Optional.empty();
    }

    // XXX: Dropping generic type information is in most cases fine or even more likely to yield a
    // valid expression, but in some cases it's necessary to keep them. Maybe return multiple
    // variants?
    return constructFix(lambdaExpr, lhsType.tsym, subTree.getIdentifier());
  }

  @SuppressWarnings("NestedOptionals")
  private static Optional<Optional<Name>> matchArguments(
      LambdaExpressionTree lambdaExpr, MethodInvocationTree subTree) {
    ImmutableList<Name> expectedArguments = getVariables(lambdaExpr);
    List<? extends ExpressionTree> args = subTree.getArguments();
    int diff = expectedArguments.size() - args.size();

    if (diff < 0 || diff > 1) {
      return Optional.empty();
    }

    for (int i = 0; i < args.size(); i++) {
      ExpressionTree arg = args.get(i);
      if (arg.getKind() != Kind.IDENTIFIER
          || !((IdentifierTree) arg).getName().equals(expectedArguments.get(i + diff))) {
        return Optional.empty();
      }
    }

    return Optional.of(diff == 0 ? Optional.empty() : Optional.of(expectedArguments.get(0)));
  }

  private static ImmutableList<Name> getVariables(LambdaExpressionTree tree) {
    return tree.getParameters().stream().map(VariableTree::getName).collect(toImmutableList());
  }

  private static Optional<SuggestedFix.Builder> constructFix(
      LambdaExpressionTree lambdaExpr, Symbol target, Object methodName) {
    Name sName = target.getSimpleName();
    Optional<SuggestedFix.Builder> fix = constructFix(lambdaExpr, sName, methodName);

    if (!"java.lang".equals(target.packge().toString())) {
      Name fqName = target.getQualifiedName();
      if (!sName.equals(fqName)) {
        return fix.map(b -> b.addImport(fqName.toString()));
      }
    }

    return fix;
  }

  private static Optional<SuggestedFix.Builder> constructFix(
      LambdaExpressionTree lambdaExpr, Object target, Object methodName) {
    return Optional.of(SuggestedFix.builder().replace(lambdaExpr, target + "::" + methodName));
  }
}
