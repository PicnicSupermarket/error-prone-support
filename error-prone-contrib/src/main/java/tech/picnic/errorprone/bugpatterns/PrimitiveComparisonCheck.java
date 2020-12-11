package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static java.util.function.Predicate.not;

import com.google.auto.service.AutoService;
import com.google.common.base.VerifyException;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

/**
 * A {@link BugChecker} which flags {@code Comparator#comparing*} invocations that can be replaced
 * with an equivalent alternative so as to avoid unnecessary (un)boxing.
 */
// XXX: Add more documentation. Explain how this is useful in the face of refactoring to more
// specific types.
// XXX: Change this checker's name?
// XXX: Introduce a companion checker (or Refaster template?) for
// https://youtrack.jetbrains.com/issue/IDEA-185548.
@AutoService(BugChecker.class)
@BugPattern(
    name = "PrimitiveComparison",
    summary =
        "Ensure invocations of `Comparator#comparing{,Double,Int,Long}` match the return type"
            + " of the provided function",
    linkType = LinkType.NONE,
    severity = SeverityLevel.WARNING,
    tags = StandardTags.PERFORMANCE,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class PrimitiveComparisonCheck extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> STATIC_COMPARISION_METHOD =
      anyOf(
          staticMethod()
              .onClass(Comparator.class.getName())
              .namedAnyOf("comparingInt", "comparingLong", "comparingDouble"),
          staticMethod()
              .onClass(Comparator.class.getName())
              .named("comparing")
              .withParameters(Function.class.getName()));
  private static final Matcher<ExpressionTree> INSTANCE_COMPARISION_METHOD =
      anyOf(
          instanceMethod()
              .onDescendantOf(Comparator.class.getName())
              .namedAnyOf("thenComparingInt", "thenComparingLong", "thenComparingDouble"),
          instanceMethod()
              .onDescendantOf(Comparator.class.getName())
              .named("thenComparing")
              .withParameters(Function.class.getName()));

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    boolean isStatic = STATIC_COMPARISION_METHOD.matches(tree, state);
    if (!isStatic && !INSTANCE_COMPARISION_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return getPotentiallyBoxedReturnType(tree.getArguments().get(0))
        .flatMap(cmpType -> tryFix(tree, state, cmpType, isStatic))
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static Optional<Fix> tryFix(
      MethodInvocationTree tree, VisitorState state, Type cmpType, boolean isStatic) {
    return Optional.ofNullable(ASTHelpers.getSymbol(tree))
        .map(methodSymbol -> methodSymbol.getSimpleName().toString())
        .flatMap(
            actualMethodName ->
                Optional.of(getPreferredMethod(state, cmpType, isStatic))
                    .filter(not(actualMethodName::equals)))
        .map(preferredMethodName -> suggestFix(tree, preferredMethodName, state));
  }

  private static String getPreferredMethod(VisitorState state, Type cmpType, boolean isStatic) {
    Types types = state.getTypes();
    Symtab symtab = state.getSymtab();

    if (types.isSubtype(cmpType, symtab.intType)) {
      return isStatic ? "comparingInt" : "thenComparingInt";
    }

    if (types.isSubtype(cmpType, symtab.longType)) {
      return isStatic ? "comparingLong" : "thenComparingLong";
    }

    if (types.isSubtype(cmpType, symtab.doubleType)) {
      return isStatic ? "comparingDouble" : "thenComparingDouble";
    }

    return isStatic ? "comparing" : "thenComparing";
  }

  private static Optional<Type> getPotentiallyBoxedReturnType(ExpressionTree tree) {
    switch (tree.getKind()) {
      case LAMBDA_EXPRESSION:
        /* Return the lambda expression's actual return type. */
        return Optional.ofNullable(ASTHelpers.getType(((LambdaExpressionTree) tree).getBody()));
      case MEMBER_REFERENCE:
        /* Return the method's declared return type. */
        // XXX: Very fragile. Do better.
        Type subType2 = ((JCMemberReference) tree).referentType;
        return Optional.of(subType2.getReturnType());
      default:
        /* This appears to be a genuine `{,ToInt,ToLong,ToDouble}Function`. */
        return Optional.empty();
    }
  }

  // XXX: We drop explicitly specified generic type information. In case the number of type
  // arguments before and after doesn't match, that's for the better. But if we e.g. replace
  // `comparingLong` with `comparingInt`, then we should retain it.
  private static Fix suggestFix(
      MethodInvocationTree tree, String preferredMethodName, VisitorState state) {
    ExpressionTree expr = tree.getMethodSelect();
    switch (expr.getKind()) {
      case IDENTIFIER:
        return SuggestedFix.builder()
            .addStaticImport(Comparator.class.getName() + '.' + preferredMethodName)
            .replace(expr, preferredMethodName)
            .build();
      case MEMBER_SELECT:
        MemberSelectTree ms = (MemberSelectTree) tree.getMethodSelect();
        return SuggestedFix.replace(
            ms, Util.treeToString(ms.getExpression(), state) + '.' + preferredMethodName);
      default:
        throw new VerifyException("Unexpected type of expression: " + expr.getKind());
    }
  }
}
