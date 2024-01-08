package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.ChildMultiMatcher.MatchType.AT_LEAST_ONE;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasArguments;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.toType;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.generic;
import static tech.picnic.errorprone.bugpatterns.util.MoreTypes.type;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import java.util.Optional;
import reactor.core.publisher.Mono;

/**
 * A {@link BugChecker} that flags usages of {@link Mono#zip(Mono, Mono)} and {@link
 * Mono#zipWith(Mono)} with {@link Mono#empty()} parameters.
 *
 * <p>{@link Mono#zip(Mono, Mono)} and {@link Mono#zipWith(Mono)} perform incorrectly upon retrieval
 * of the empty publisher and prematurely terminates the reactive chain from the execution. In most
 * cases this is not the desired behaviour.
 *
 * @apiNote {@code Mono<?>.zipWith(Mono<Void>)} is allowed by the Reactor API, but it is an
 *     incorrect usage of the API. It will be flagged by ErrorProne but the fix won't be supplied.
 *     The problem with the original code should be revisited and fixed in a structural manner by
 *     the developer.
 */
@AutoService(BugChecker.class)
@BugPattern(
    summary =
        "`Mono#zip` and `Mono#zipWith` should not be executed against `Mono#empty` or `Mono<Void>` parameter; "
            + "please revisit the parameters used and make sure to supply correct publishers instead",
    link = BUG_PATTERNS_BASE_URL + "MonoZipOfMonoVoidUsage",
    linkType = CUSTOM,
    severity = ERROR,
    tags = LIKELY_ERROR)
public final class MonoZipOfMonoVoidUsage extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;

  /**
   * In fact, we use {@code Mono<Void>} everywhere in codebases instead of {@code Mono<Object>}
   * (actual return type of {@link Mono#empty()}) to represent empty publisher.
   */
  private static final String MONO_ZIP_WITH_METHOD = "zipWith";

  private static final String MONO_ZIP_METHOD = "zip";
  private static final String MONO_EMPTY_METHOD = "empty";
  private static final Supplier<Type> MONO_VOID_TYPE_SUPPLIER =
      VisitorState.memoize(
          generic(type("reactor.core.publisher.Mono"), type(Void.class.getCanonicalName())));
  private static final Matcher<ExpressionTree> MONO_ZIP_AND_MONO_ZIP_WITH =
      anyOf(
          instanceMethod().onDescendantOf(MONO_VOID_TYPE_SUPPLIER).named(MONO_ZIP_WITH_METHOD),
          staticMethod().onClass(MONO_VOID_TYPE_SUPPLIER).named(MONO_ZIP_METHOD));
  private static final Matcher<ExpressionTree> GENERIC_ARGUMENT_DERIVED_FROM_MONO_TYPE =
      toType(MethodInvocationTree.class, hasGenericArgumentOfExactType(MONO_VOID_TYPE_SUPPLIER));
  private static final Matcher<ExpressionTree> HAS_MONO_EMPTY_AS_ARGUMENT =
      toType(
          MethodInvocationTree.class,
          hasArguments(
              AT_LEAST_ONE,
              staticMethod().onClass(MONO_VOID_TYPE_SUPPLIER).named(MONO_EMPTY_METHOD)));
  private static final Matcher<ExpressionTree> OPERATORS_WITH_MONO_VOID_GENERIC_ARGUMENT =
      allOf(
          MONO_ZIP_AND_MONO_ZIP_WITH,
          anyOf(GENERIC_ARGUMENT_DERIVED_FROM_MONO_TYPE, HAS_MONO_EMPTY_AS_ARGUMENT));
  private static final Matcher<ExpressionTree> ANY_MONO_VOID_IN_PUBLISHERS =
      anyOf(
          OPERATORS_WITH_MONO_VOID_GENERIC_ARGUMENT,
          onClassWithMethodName(MONO_VOID_TYPE_SUPPLIER, MONO_ZIP_WITH_METHOD));

  /** Instantiates a new {@link MonoZipOfMonoVoidUsage} instance. */
  public MonoZipOfMonoVoidUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (!ANY_MONO_VOID_IN_PUBLISHERS.matches(tree, state)) {
      return Description.NO_MATCH;
    }

    return describeMatch(tree, SuggestedFixes.addSuppressWarnings(state, canonicalName()));
  }

  private static Matcher<ExpressionTree> onClassWithMethodName(
      Supplier<Type> genericDesiredType, String methodName) {
    return (tree, state) ->
        getMethodExecuted(tree)
            .filter(
                methodExecuted -> {
                  Type invokedType = ASTHelpers.getType(methodExecuted.getExpression());
                  String invokedMethodName = methodExecuted.getIdentifier().toString();
                  return invokedMethodName.equals(methodName)
                      && isOfSameGenericType(invokedType, genericDesiredType.get(state), state);
                })
            .isPresent();
  }

  private static Optional<MemberSelectTree> getMethodExecuted(ExpressionTree expressionTree) {
    return Optional.of((MethodInvocationTree) expressionTree)
        .map(MethodInvocationTree::getMethodSelect)
        .filter(MemberSelectTree.class::isInstance)
        .map(MemberSelectTree.class::cast);
  }

  private static Matcher<MethodInvocationTree> hasGenericArgumentOfExactType(
      Supplier<Type> genericDesiredType) {
    return (tree, state) ->
        tree.getArguments().stream()
            .anyMatch(
                arg ->
                    isOfSameGenericType(
                        ASTHelpers.getType(arg), genericDesiredType.get(state), state));
  }

  /**
   * We need to extract real types from the generics because {@link ASTHelpers} cannot distinguish
   * {@code Mono<Integer>} and {@code Mono<Void>} and reports those being the same.
   *
   * <p>In case of {@code Mono}, we can infer the real type out of the parameters of the invocation
   * ({@link MethodInvocationTree#getArguments()}):
   *
   * <ul>
   *   <li>either we have explicit variable declared and the provided type which will be inferred,
   *   <li>or we have a method invocation, like {@link Mono#just(Object)} or {@link Mono#empty()},
   *       for which we can also infer type.
   * </ul>
   *
   * <p>Similarly, we can infer the matching type.
   */
  private static boolean isOfSameGenericType(
      Type genericArgumentType, Type genericDesiredType, VisitorState state) {
    Type argumentType = Iterables.getFirst(genericArgumentType.allparams(), Type.noType);
    Type requiredType = Iterables.getOnlyElement(genericDesiredType.allparams());
    return ASTHelpers.isSameType(argumentType, requiredType, state);
  }
}
