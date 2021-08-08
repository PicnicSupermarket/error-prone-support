package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.STYLE;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static java.util.function.Predicate.not;

import com.google.auto.service.AutoService;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.LambdaExpressionTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
// XXX: Does this check also do `() -> something.someConst` -> `something::someConst`?
// XXX: Don't rewrite `() -> field.m()` to `field::m` for non-final fields.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer method references over lambda expressions",
    linkType = NONE,
    severity = SUGGESTION,
    tags = STYLE)
public final class MethodReferenceUsage extends BugChecker implements LambdaExpressionTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<Tree> REFASTER_TEMPLATE_METHOD =
      anyOf(
          hasAnnotation(AfterTemplate.class.getName()),
          hasAnnotation(BeforeTemplate.class.getName()));
  private static final String VERIFY_SUGGESTIONS_FLAG = "MethodReferenceUsage:VerifySuggestions";
  /**
   * Tells whether this class is loaded in inside a {@link SuggestedFixes#compilesWithFix(Fix,
   * VisitorState, ImmutableList, boolean)} invocation. This allows {@link
   * #tryCompileWithFix(VisitorState, SuggestedFix)} to prevent a very expensive chain of nested
   * compilations.
   */
  // XXX: Review whether, given the most recent improvements, we need the `compilesWithFix` fallback
  // at all. (Likely at least one open point remains: handling of generic types.)
  private static final boolean IS_NESTED_INVOCATION =
      StackWalker.getInstance()
          .walk(
              frames -> {
                String className = MethodReferenceUsage.class.getCanonicalName();
                return frames.filter(f -> f.getClassName().equals(className)).limit(2).count() > 1;
              });

  private final boolean verifySuggestions;

  /** Instantiates the default {@link MethodReferenceUsage}. */
  public MethodReferenceUsage() {
    this(ErrorProneFlags.empty());
  }

  /**
   * Instantiates a customized {@link MethodReferenceUsage}.
   *
   * @param flags Any provided command line flags.
   */
  public MethodReferenceUsage(ErrorProneFlags flags) {
    verifySuggestions = flags.getBoolean(VERIFY_SUGGESTIONS_FLAG).orElse(Boolean.FALSE);
  }

  @Override
  public Description matchLambdaExpression(LambdaExpressionTree tree, VisitorState state) {
    MethodTree enclosingMethod = state.findEnclosing(MethodTree.class);
    if (enclosingMethod != null && REFASTER_TEMPLATE_METHOD.matches(enclosingMethod, state)) {
      /*
       * Within Refaster template methods variable references may stand in for complex expressions.
       * Additionally, `@Placeholder` methods cannot be used as method handles. For both these
       * reasons it is not safe to replace lambda expressions with method references inside Refaster
       * templates.
       */
      // XXX: This is too strict; we should more explicitly handle `@Placeholder` and method
      // parameters. For example, `() -> Optional.empty()` _should_ be flagged.
      return Description.NO_MATCH;
    }

    if (isPassedToSameArgCountMethodOverload(state)) {
      // XXX: When a lambda expression is passed to an overloaded method, replacing it with a method
      // reference may introduce an ambiguity about the method that is intended to be invoked. An
      // example is a pair of overloads accepting a `Runnable` and `Supplier<T>` respectively, where
      // the lambda expression in question returns a value: in this case the first overload is
      // selected, but when converted to a method reference the intended target is no longer clear.
      // Right now any lambda expression passed to an method with an overload accepting the same
      // number of arguments is ignored. Improve this detection logic.
      return Description.NO_MATCH;
    }

    /*
     * Lambda expressions can be used in several places where method references cannot, either
     * because the latter are not syntactically valid or ambiguous. Rather than encoding all these
     * edge cases we try to compile the code with the suggested fix, to see whether this works.
     */
    // XXX: Update the comment to reflect actual `tryCompileWithFix` usage.
    return constructMethodRef(tree, state, tree.getBody())
        .map(SuggestedFix.Builder::build)
        .filter(fix -> !verifySuggestions || tryCompileWithFix(state, fix))
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static boolean isPassedToSameArgCountMethodOverload(VisitorState state) {
    TreePath parent = state.getPath().getParentPath();
    if (parent == null) {
      return false;
    }

    Symbol symbol = ASTHelpers.getSymbol(parent.getLeaf());
    if (!(symbol instanceof MethodSymbol)) {
      return false;
    }

    MethodSymbol method = (MethodSymbol) symbol;
    return getOverloads(method).anyMatch(m -> m.params().size() == method.params.size());
  }

  private static Optional<SuggestedFix.Builder> constructMethodRef(
      LambdaExpressionTree lambdaExpr, VisitorState state, Tree subTree) {
    switch (subTree.getKind()) {
      case BLOCK:
        return constructMethodRef(lambdaExpr, state, (BlockTree) subTree);
      case EXPRESSION_STATEMENT:
        return constructMethodRef(
            lambdaExpr, state, ((ExpressionStatementTree) subTree).getExpression());
      case METHOD_INVOCATION:
        return constructMethodRef(lambdaExpr, state, (MethodInvocationTree) subTree);
      case PARENTHESIZED:
        // XXX: Add test!
        return constructMethodRef(lambdaExpr, state, ((ParenthesizedTree) subTree).getExpression());
      case RETURN:
        // XXX: This case isn't tested. Reachable?
        // XXX: Should be possible with `{ return x; }`.
        return constructMethodRef(lambdaExpr, state, ((ReturnTree) subTree).getExpression());
      default:
        // XXX: Explicitly handle known cases and throw an exception otherwise?
        return Optional.empty();
    }
  }

  private static Optional<SuggestedFix.Builder> constructMethodRef(
      LambdaExpressionTree lambdaExpr, VisitorState state, BlockTree subTree) {
    // XXX: Add test with >1 statement.
    return Optional.of(subTree.getStatements())
        .filter(statements -> statements.size() == 1)
        .flatMap(statements -> constructMethodRef(lambdaExpr, state, statements.get(0)));
  }

  // XXX: Replace nested `Optional` usage.
  @SuppressWarnings("NestedOptionals")
  private static Optional<SuggestedFix.Builder> constructMethodRef(
      LambdaExpressionTree lambdaExpr, VisitorState state, MethodInvocationTree subTree) {
    return matchParameters(lambdaExpr, subTree)
        .flatMap(
            expectedInstance -> constructMethodRef(lambdaExpr, state, subTree, expectedInstance));
  }

  private static Optional<SuggestedFix.Builder> constructMethodRef(
      LambdaExpressionTree lambdaExpr,
      VisitorState state,
      MethodInvocationTree subTree,
      Optional<Name> expectedInstance) {
    ExpressionTree methodSelect = subTree.getMethodSelect();
    switch (methodSelect.getKind()) {
      case IDENTIFIER:
        if (expectedInstance.isPresent()) {
          /* Direct method call; there is no matching "implicit parameter". */
          return Optional.empty();
        }
        // XXX: Here too test for ambiguous method references.
        Symbol sym = ASTHelpers.getSymbol(methodSelect);
        if (!sym.isStatic()) {
          return constructFix(lambdaExpr, "this", methodSelect);
        }
        return constructFix(lambdaExpr, sym.owner, methodSelect);
      case MEMBER_SELECT:
        return constructMethodRef(
            lambdaExpr, state, (MemberSelectTree) methodSelect, expectedInstance);
      default:
        throw new VerifyException("Unexpected type of expression: " + methodSelect.getKind());
    }
  }

  private static Optional<SuggestedFix.Builder> constructMethodRef(
      LambdaExpressionTree lambdaExpr,
      VisitorState state,
      MemberSelectTree subTree,
      Optional<Name> expectedInstance) {
    if (subTree.getExpression().getKind() != Kind.IDENTIFIER) {
      // XXX: Could be parenthesized. Handle. Also in other classes. Maybe consult
      // `SideEffectAnalysis`?
      // XXX: This branch isn't tested. Fix. Maybe something like `foo.bar().baz()`.
      /*
       * Only suggest a replacement if the method select's expression provably doesn't have
       * side-effects. Otherwise the replacement may not be behavior preserving.
       */
      // XXX: So do this ^.
      return Optional.empty();
    }

    // XXX: Check whether this cast is safe in all cases.
    MethodSymbol method = (MethodSymbol) ASTHelpers.getSymbol(subTree);
    Symbol lhsSymbol = ASTHelpers.getSymbol(subTree.getExpression());
    if (method.isStatic() && lhsSymbol instanceof VarSymbol) {
      return Optional.empty();
    }

    if (hasAmbiguousMethodReference(method, state)) {
      return Optional.empty();
    }

    Name lhs = lhsSymbol.name;
    if (expectedInstance.isEmpty()) {
      return constructFix(lambdaExpr, lhs, subTree.getIdentifier());
    }

    Type lhsType = lhsSymbol.type;
    if (lhsType == null || !expectedInstance.orElseThrow().equals(lhs)) {
      return Optional.empty();
    }

    // XXX: Dropping generic type information is in most cases fine or even more likely to yield a
    // valid expression, but in some cases it's necessary to keep them. Maybe return multiple
    // variants?
    return constructFix(lambdaExpr, lhsType.tsym, subTree.getIdentifier());
  }

  /**
   * Tells whether the given method has an overload that would lead to an ambiguous method
   * reference.
   */
  private static boolean hasAmbiguousMethodReference(MethodSymbol method, VisitorState state) {
    return getOverloads(method)
        .anyMatch(m -> haveAmbiguousMethodReferences(m, method, state.getTypes()));
  }

  /** Returns any overloads of the given method defined on the same class. */
  // XXX: This probably doesn't return overloads defined by supertypes. Review and extend if
  // necessary.
  private static Stream<MethodSymbol> getOverloads(MethodSymbol method) {
    return Streams.stream(ASTHelpers.enclosingClass(method).members().getSymbolsByName(method.name))
        .filter(MethodSymbol.class::isInstance)
        .map(MethodSymbol.class::cast)
        .filter(not(method::equals));
  }

  /** Tells whether method references to the given methods would be mutually ambiguous. */
  // XXX: This doesn't necessarily identify all ambiguous cases; carefully read the JLS and update
  // this logic if necessary.
  private static boolean haveAmbiguousMethodReferences(
      MethodSymbol method1, MethodSymbol method2, Types types) {
    if (method1.isStatic() == method2.isStatic()) {
      return false;
    }

    if (method1.isStatic()) {
      return haveAmbiguousMethodReferences(method2, method1, types);
    }

    com.sun.tools.javac.util.List<VarSymbol> params1 = method1.params();
    com.sun.tools.javac.util.List<VarSymbol> params2 = method2.params();
    if (params1.size() != params2.size() - 1) {
      return false;
    }

    // XXX: Here and below: perhaps `isAssignable` is more appropriate than `isConvertible`.
    if (!types.isConvertible(method1.owner.asType(), params2.get(0).asType())) {
      return false;
    }

    for (int i = 0; i < params1.size(); i++) {
      if (!types.isConvertible(params1.get(0).asType(), params2.get(i + 1).asType())) {
        return false;
      }
    }

    return true;
  }

  /**
   * Attempts to match the given method invocation's arguments against the rightmost parameters of
   * the provided lambda expression, in order; if successful with zero or one lambda parameter
   * unaccounted for, then said parameter is returned.
   */
  // XXX: Refactor or replace inner `Optional` with a custom type.
  @SuppressWarnings("NestedOptionals")
  private static Optional<Optional<Name>> matchParameters(
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

    if (!"java.lang".equals(target.packge().getQualifiedName().toString())) {
      // XXX: Check whether the type can be imported. If not, skip the suggestion. (In other words:
      // if another type with simple name `sName` is already imported, then this suggested fix would
      // introduce a compilation failure.)
      // XXX: Make sure `SuggestedFixes.qualifyType` handles `java.lang` and the double-import case,
      // then use that method.
      Name fqName = target.getQualifiedName();
      if (!sName.equals(fqName)) {
        return fix.map(b -> b.addImport(fqName.toString()));
      }
    }

    return fix;
  }

  // XXX: As-is this method shouldn't return an `Optional`.
  private static Optional<SuggestedFix.Builder> constructFix(
      LambdaExpressionTree lambdaExpr, Object target, Object methodName) {
    return Optional.of(SuggestedFix.builder().replace(lambdaExpr, target + "::" + methodName));
  }

  private static boolean tryCompileWithFix(VisitorState state, SuggestedFix fix) {
    return !IS_NESTED_INVOCATION
        && SuggestedFixes.compilesWithFix(
            fix, state, ImmutableList.of(), /* onlyInSameCompilationUnit= */ true);
  }
}
