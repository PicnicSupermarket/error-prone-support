package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.BugPattern.StandardTags.LIKELY_ERROR;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.toType;
import static com.google.errorprone.util.ASTHelpers.isSameType;
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
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;

/**
 * A {@link BugChecker} that flags usages of {@code Mono.zip(Mono, Mono)} and {@code
 * Mono.zipWith(Mono)} with {@code Mono.empty()} parameters.
 *
 * <p>{@code Mono.zip(Mono, Mono)} and {@code Mono.zipWith(Mono)} perform incorrectly upon retrieval
 * of the empty publisher and prematurely terminates the reactive chain from the execution. In most
 * cases this is not the desired behaviour.
 *
 * <p>NB: {@code Mono<?>.zipWith(Mono<Void>)} is allowed be the Reactor API, but it is an incorrect
 * usage of the API. It will be flagged by ErrorProne but the fix won't be supplied. The problem
 * with the original code should be revisited and fixed in a structural manner by the developer.
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
  private static final Supplier<Type> MONO = type("reactor.core.publisher.Mono");
  // In fact, we use `Mono<Void>` everywhere in codebases instead of `Mono<Object>` to represent
  // empty publisher
  private static final Supplier<Type> MONO_VOID_TYPE =
      VisitorState.memoize(generic(MONO, type("java.lang.Void")));

  // On instance mono.zipWith, at least one element should match empty in order to proceed.
  private static final Matcher<ExpressionTree> MONO_ZIP_AND_WITH =
      anyOf(
          allOf(
              instanceMethod().onDescendantOf(MONO).named("zipWith"),
              toType(MethodInvocationTree.class, hasGenericArgumentOfExactType(MONO_VOID_TYPE))),
          allOf(
              instanceMethod().onDescendantOf(MONO).named("zipWith"),
              toType(MethodInvocationTree.class, staticMethod().onClass(MONO).named("empty"))),
          allOf(
              onClassWithMethodName(MONO_VOID_TYPE, "zipWith"),
              toType(MethodInvocationTree.class, hasGenericArgumentOfType(MONO))));

  // On class Mono.zip, at least one element should match empty in order to proceed.
  private static final Matcher<ExpressionTree> STATIC_MONO_ZIP =
      allOf(
          staticMethod().onClass(MONO).named("zip"),
          toType(MethodInvocationTree.class, hasGenericArgumentOfExactType(MONO_VOID_TYPE)));

  /** Instantiates a new {@link MonoZipOfMonoVoidUsage} instance. */
  public MonoZipOfMonoVoidUsage() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    boolean dynamicMono = MONO_ZIP_AND_WITH.matches(tree, state);
    boolean staticMono = STATIC_MONO_ZIP.matches(tree, state);
    if (!dynamicMono && !staticMono) {
      return Description.NO_MATCH;
    }

    return buildDescription(tree)
        .setMessage(
            "`Mono#zip` and `Mono#zipWith` should not be executed against empty publisher; "
                + "remove it or suppress this warning and add a comment explaining its purpose")
        .addFix(SuggestedFixes.addSuppressWarnings(state, canonicalName()))
        .build();
  }

  private static Matcher<ExpressionTree> onClassWithMethodName(
      Supplier<Type> genericDesiredType, String methodName) {
    return (tree, state) -> {
      JCTree.JCExpression methodSelect = ((JCTree.JCMethodInvocation) tree).getMethodSelect();
      if (!(methodSelect instanceof JCTree.JCFieldAccess)) {
        return false;
      }
      JCTree.JCFieldAccess methodExecuted = (JCTree.JCFieldAccess) methodSelect;
      Type invokedType = methodExecuted.selected.type;
      String invokedMethodName = methodExecuted.getIdentifier().toString();
      return invokedMethodName.equals(methodName)
          && hasSameGenericType(invokedType, genericDesiredType.get(state), state);
    };
  }

  private static Matcher<MethodInvocationTree> hasGenericArgumentOfType(
      Supplier<Type> genericDesiredType) {
    return (tree, state) ->
        tree.getArguments().stream()
            .anyMatch(
                arg -> isSameType(ASTHelpers.getType(arg), genericDesiredType.get(state), state));
  }

  private static Matcher<MethodInvocationTree> hasGenericArgumentOfExactType(
      Supplier<Type> genericDesiredType) {
    return (tree, state) ->
        tree.getArguments().stream()
            .anyMatch(
                arg ->
                    hasSameGenericType(
                        ASTHelpers.getType(arg), genericDesiredType.get(state), state));
  }

  /**
   * We need to extract real types from the generics because {@link ASTHelpers} cannot distinguish
   * {@code Mono<Integer>} and {@code Mono<Void>} and reports those being the same.
   *
   * <p>In case of {@code Mono}, we can infer the real type out of the parameters of the invocation
   * ({@link MethodInvocationTree#getArguments()}):
   *
   * <p>- either we have explicit variable declared and the provided type which will be inferred,
   *
   * <p>- or we have a method invocation, like {@code Mono.just(Object)} or {@code Mono.empty()},
   * for which we can also infer type.
   *
   * <p>Similarly, we can infer the matching type
   */
  private static boolean hasSameGenericType(
      Type genericArgumentType, Type genericDesiredType, VisitorState state) {
    Type argumentType = Iterables.getFirst(genericArgumentType.allparams(), Type.noType);
    Type requiredType = Iterables.getOnlyElement(genericDesiredType.allparams());
    return isSameType(argumentType, requiredType, state);
  }
}
