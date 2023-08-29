package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.BugPattern.StandardTags.PERFORMANCE;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.method.MethodMatchers.instanceMethod;
import static com.google.errorprone.matchers.method.MethodMatchers.staticMethod;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.base.VerifyException;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
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
import java.util.stream.Stream;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags {@code Comparator#comparing*} invocations that can be replaced
 * with an equivalent alternative so as to avoid unnecessary (un)boxing.
 */
// XXX: Add more documentation. Explain how this is useful in the face of refactoring to more
// specific types.
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        """
        Ensure invocations of `Comparator#comparing{,Double,Int,Long}` match the return type of \
        the provided function""",
    link = BUG_PATTERNS_BASE_URL + "PrimitiveComparison",
    linkType = CUSTOM,
    severity = WARNING,
    tags = PERFORMANCE)
@SuppressWarnings("java:S1192" /* Factoring out repeated method names impacts readability. */)
public final class PrimitiveComparison extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> STATIC_COMPARISON_METHOD =
      anyOf(
          staticMethod()
              .onClass(Comparator.class.getCanonicalName())
              .namedAnyOf("comparingInt", "comparingLong", "comparingDouble"),
          staticMethod()
              .onClass(Comparator.class.getCanonicalName())
              .named("comparing")
              .withParameters(Function.class.getCanonicalName()));
  private static final Matcher<ExpressionTree> INSTANCE_COMPARISON_METHOD =
      anyOf(
          instanceMethod()
              .onDescendantOf(Comparator.class.getCanonicalName())
              .namedAnyOf("thenComparingInt", "thenComparingLong", "thenComparingDouble"),
          instanceMethod()
              .onDescendantOf(Comparator.class.getCanonicalName())
              .named("thenComparing")
              .withParameters(Function.class.getCanonicalName()));

  /** Instantiates a new {@link PrimitiveComparison} instance. */
  public PrimitiveComparison() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    boolean isStatic = STATIC_COMPARISON_METHOD.matches(tree, state);
    if (!isStatic && !INSTANCE_COMPARISON_METHOD.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return getPotentiallyBoxedReturnType(tree.getArguments().get(0))
        .flatMap(cmpType -> attemptMethodInvocationReplacement(tree, cmpType, isStatic, state))
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static Optional<Fix> attemptMethodInvocationReplacement(
      MethodInvocationTree tree, Type cmpType, boolean isStatic, VisitorState state) {
    String actualMethodName = ASTHelpers.getSymbol(tree).getSimpleName().toString();
    String preferredMethodName = getPreferredMethod(cmpType, isStatic, state);
    if (actualMethodName.equals(preferredMethodName)) {
      return Optional.empty();
    }

    return Optional.of(
        suggestFix(
            tree, prefixTypeArgumentsIfRelevant(preferredMethodName, tree, cmpType, state), state));
  }

  /**
   * Prefixes the given method name with generic type parameters if it replaces a {@code
   * Comparator#comparing{,Double,Long,Int}} method which also has generic type parameters.
   *
   * <p>Such type parameters are retained as they are likely required.
   *
   * <p>Note that any type parameter to {@code Comparator#thenComparing} is likely redundant, and in
   * any case becomes obsolete once that method is replaced with {@code
   * Comparator#thenComparing{Double,Long,Int}}. Conversion in the opposite direction does not
   * require the introduction of a generic type parameter.
   */
  private static String prefixTypeArgumentsIfRelevant(
      String preferredMethodName, MethodInvocationTree tree, Type cmpType, VisitorState state) {
    if (tree.getTypeArguments().isEmpty() || preferredMethodName.startsWith("then")) {
      return preferredMethodName;
    }

    String typeArguments =
        Stream.concat(
                Stream.of(SourceCode.treeToString(tree.getTypeArguments().get(0), state)),
                Stream.of(cmpType.tsym.getSimpleName())
                    .filter(u -> "comparing".equals(preferredMethodName)))
            .collect(joining(", ", "<", ">"));

    return typeArguments + preferredMethodName;
  }

  private static String getPreferredMethod(Type cmpType, boolean isStatic, VisitorState state) {
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

  // XXX: Use switch pattern matching once the targeted JDK supports this.
  private static Optional<Type> getPotentiallyBoxedReturnType(ExpressionTree tree) {
    if (tree instanceof LambdaExpressionTree lambdaExpression) {
      /* Return the lambda expression's actual return type. */
      return Optional.ofNullable(ASTHelpers.getType(lambdaExpression.getBody()));
    }

    // XXX: The match against a concrete type and reference to one of its fields is fragile. Do
    // better.
    if (tree instanceof JCMemberReference memberReference) {
      /* Return the method's declared return type. */
      Type subType = memberReference.referentType;
      return Optional.of(subType.getReturnType());
    }

    /* This appears to be a genuine `{,ToInt,ToLong,ToDouble}Function`. */
    return Optional.empty();
  }

  // XXX: Use switch pattern matching once the targeted JDK supports this.
  private static Fix suggestFix(
      MethodInvocationTree tree, String preferredMethodName, VisitorState state) {
    ExpressionTree expr = tree.getMethodSelect();

    if (expr instanceof IdentifierTree) {
      SuggestedFix.Builder fix = SuggestedFix.builder();
      String replacement =
          SuggestedFixes.qualifyStaticImport(
              Comparator.class.getCanonicalName() + '.' + preferredMethodName, fix, state);
      return fix.replace(expr, replacement).build();
    }

    if (expr instanceof MemberSelectTree memberSelect) {
      return SuggestedFix.replace(
          memberSelect,
          SourceCode.treeToString(memberSelect.getExpression(), state) + '.' + preferredMethodName);
    }

    throw new VerifyException("Unexpected type of expression: " + expr.getKind());
  }
}
