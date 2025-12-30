package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.PERFORMANCE;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.symbolMatcher;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.google.errorprone.util.Visibility;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.WildcardType;
import com.sun.tools.javac.code.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Element;
import tech.picnic.errorprone.utils.SourceCode;

/**
 * A {@link BugChecker} that flags single-argument method invocations with an iterable of explicitly
 * enumerated values, for which a semantically equivalent varargs variant (appears to) exists as
 * well.
 *
 * <p>This check drops selected {@link ImmutableSet#of} and {@link Set#of} invocations, with the
 * assumption that these operations do not deduplicate the collection of explicitly enumerated
 * values. It also drops {@link ImmutableMultiset#of} and {@link Set#of} invocations, with the
 * assumption that these do not materially impact iteration order.
 *
 * <p>This checker attempts to identify {@link Iterable}-accepting methods for which a varargs
 * overload exists, and suggests calling the varargs overload instead. This is an imperfect
 * heuristic, but it e.g. allows invocations of <a
 * href="https://immutables.github.io/immutable.html#copy-methods">Immutables-generated {@code
 * with*}</a> methods to be simplified.
 */
// XXX: Besides explicitly enumerated iterables, consider also simplifying expressions that contain
// explicitly enumerated `Stream`s, defined using `Stream.of(...)` or `() -> Stream.of(...)`. This
// would allow simplifying e.g. invocations of `Flux#fromStream` to `Flux#just`.
// XXX: Consider also rewrites in the opposite direction, where `overload(collection.toArray(..))`
// can be simplified to `overload(collection)`, provided that a suitable overload exists.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Iterable creation can be avoided by using a varargs alternative method",
    link = BUG_PATTERNS_BASE_URL + "ExplicitArgumentEnumeration",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = {PERFORMANCE, SIMPLIFICATION})
public final class ExplicitArgumentEnumeration extends BugChecker
    implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> EXPLICIT_ITERABLE_CREATOR =
      anyOf(
          staticMethod()
              .onClassAny(
                  ImmutableList.class.getCanonicalName(),
                  ImmutableMultiset.class.getCanonicalName(),
                  ImmutableSet.class.getCanonicalName(),
                  List.class.getCanonicalName(),
                  Set.class.getCanonicalName())
              .named("of"),
          allOf(
              staticMethod()
                  .onClassAny(
                      ImmutableList.class.getCanonicalName(),
                      ImmutableMultiset.class.getCanonicalName(),
                      ImmutableSet.class.getCanonicalName())
                  .named("copyOf"),
              symbolMatcher(
                  (symbol, state) ->
                      state.getTypes().isArray(((MethodSymbol) symbol).params().getFirst().type))),
          staticMethod().onClass(Arrays.class.getCanonicalName()).named("asList"));
  private static final Matcher<ExpressionTree> FLUX =
      staticMethod().onDescendantOf("reactor.core.publisher.Flux");
  private static final Matcher<ExpressionTree> IMMUTABLE_COLLECTION_BUILDER =
      instanceMethod().onDescendantOf(ImmutableCollection.Builder.class.getCanonicalName());
  private static final Matcher<ExpressionTree> OBJECT_ENUMERABLE_ASSERT =
      instanceMethod().onDescendantOf("org.assertj.core.api.ObjectEnumerableAssert");
  private static final Matcher<ExpressionTree> STEP_VERIFIER_STEP =
      instanceMethod().onDescendantOf("reactor.test.StepVerifier.Step");
  private static final ImmutableTable<Matcher<ExpressionTree>, String, String> ALTERNATIVE_METHODS =
      ImmutableTable.<Matcher<ExpressionTree>, String, String>builder()
          .put(FLUX, "fromIterable", "just")
          .put(IMMUTABLE_COLLECTION_BUILDER, "addAll", "add")
          .put(OBJECT_ENUMERABLE_ASSERT, "containsAnyElementsOf", "containsAnyOf")
          .put(OBJECT_ENUMERABLE_ASSERT, "containsAll", "contains")
          .put(OBJECT_ENUMERABLE_ASSERT, "containsExactlyElementsOf", "containsExactly")
          .put(
              OBJECT_ENUMERABLE_ASSERT,
              "containsExactlyInAnyOrderElementsOf",
              "containsExactlyInAnyOrder")
          .put(OBJECT_ENUMERABLE_ASSERT, "containsOnlyElementsOf", "containsOnly")
          .put(OBJECT_ENUMERABLE_ASSERT, "containsOnlyOnceElementsOf", "containsOnlyOnce")
          .put(OBJECT_ENUMERABLE_ASSERT, "doesNotContainAnyElementsOf", "doesNotContain")
          .put(OBJECT_ENUMERABLE_ASSERT, "hasSameElementsAs", "containsOnly")
          .put(STEP_VERIFIER_STEP, "expectNextSequence", "expectNext")
          .buildOrThrow();

  /** Instantiates a new {@link ExplicitArgumentEnumeration} instance. */
  public ExplicitArgumentEnumeration() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    MethodSymbol method = ASTHelpers.getSymbol(tree);
    if (!acceptsIterableAsFinalArg(method, state) || isLocalOverload(method, state)) {
      /*
       * This isn't a method invocation we can simplify, or it's an invocation of a local overload.
       * The latter type of invocation we do not suggest replacing, as this is fairly likely to
       * introduce an unbounded recursive call chain.
       *
       * NB: Note that strictly speaking we don't need to check whether the invoked method's final
       * parameter is `Iterable`, as the `EXPLICIT_ITERABLE_CREATOR` matcher employed below will
       * only match `Iterable` arguments. However, this check serves as a cheap early filter.
       */
      return Description.NO_MATCH;
    }

    ExpressionTree argument = tree.getArguments().getLast();
    if (!(argument instanceof MethodInvocationTree methodInvocation)
        || !EXPLICIT_ITERABLE_CREATOR.matches(argument, state)) {
      return Description.NO_MATCH;
    }

    return trySuggestCallingVarargsOverload(method, methodInvocation, state)
        .or(() -> trySuggestCallingCustomAlternative(tree, methodInvocation, state))
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static boolean acceptsIterableAsFinalArg(MethodSymbol method, VisitorState state) {
    List<VarSymbol> params = method.params();
    return !method.isVarArgs()
        && !params.isEmpty()
        && ASTHelpers.isSubtype(params.getLast().type, state.getSymtab().iterableType, state);
  }

  private static boolean isLocalOverload(MethodSymbol calledMethod, VisitorState state) {
    MethodTree enclosingMethod = state.findEnclosing(MethodTree.class);
    if (enclosingMethod == null) {
      return false;
    }

    MethodSymbol callingMethod = ASTHelpers.getSymbol(enclosingMethod);
    return Objects.equals(callingMethod.getEnclosingElement(), calledMethod.getEnclosingElement())
        && callingMethod.getSimpleName().equals(calledMethod.getSimpleName());
  }

  private static Optional<SuggestedFix> trySuggestCallingVarargsOverload(
      MethodSymbol method, MethodInvocationTree argument, VisitorState state) {
    /*
     * Collect all overloads of the given method that we are sure to be able to call. Note that the
     * `isAtLeastAsVisible` check is conservative heuristic.
     */
    ImmutableList<MethodSymbol> overloads =
        ASTHelpers.matchingMethods(
                method.getSimpleName(),
                m -> isAtLeastAsVisible(m, method),
                method.enclClass().type,
                state.getTypes())
            .collect(toImmutableList());

    return hasLikelySuitableVarargsOverload(method, overloads, state)
        ? Optional.of(SourceCode.unwrapMethodInvocation(argument, state))
        : Optional.empty();
  }

  /**
   * Tells whether it is likely that, if the final argument to the given method is unwrapped, a
   * suitable varargs overload will be invoked instead.
   *
   * <p>If none of the overloads has a larger number of parameters than the given method, and at
   * least one of them is a suitably-typed varargs method, then we assume that unwrapping the
   * iterable argument will cause a suitable overload to be invoked. (Note that there may be
   * multiple varargs overloads due to method overriding; this check does not attempt to determine
   * which exact method or overload will be invoked as a result of the suggested simplification.)
   *
   * <p>Note that this is a (highly!) imperfect heuristic, but it is sufficient to prevent e.g.
   * unwrapping of arguments to `org.jooq.impl.DSL#row`, which can cause the expression's return
   * type to change from `RowN` to (e.g.) `Row2`.
   */
  // XXX: There are certainly cases where it _would_ be nice to unwrap the arguments to
  // `org.jooq.impl.DSL#row(Collection<?>)`. Look into this.
  private static boolean hasLikelySuitableVarargsOverload(
      MethodSymbol method, ImmutableList<MethodSymbol> overloads, VisitorState state) {
    List<VarSymbol> parameters = method.getParameters();
    if (overloads.stream().anyMatch(m -> m.params().size() > parameters.size())) {
      return false;
    }

    ImmutableList<Type> initialParameterTypes =
        parameters.stream()
            .limit(parameters.size() - 1)
            .map(p -> p.type)
            .collect(toImmutableList());

    // XXX: This logic is fragile, as it assumes that the method parameter's type is of the form
    // `X<T>`, where `T` is the type of the explicitly enumerated values passed to the expression to
    // be unwrapped. This should generally hold, given the types returned by the
    // `EXPLICIT_ITERABLE_CREATOR` expressions: `Iterable<T>`, `List<T>`, `Set<T>`, etc. For
    // example, we don't need to consider a `StringList` defined as `class StringList extends
    // ArrayList<String>` or a `ListMap<K, V>` defined as `abstract ListMap<K, V> extends
    // AbstractMap<K, V> implements List<V>`. Raw types are handled by assuming `Object` as the
    // parameter type.
    Type finalParameterType =
        Iterables.getOnlyElement(
            parameters.getLast().type.getTypeArguments(), state.getSymtab().objectType);
    return overloads.stream()
        .anyMatch(
            m ->
                isCompatibleVarargsOverload(
                    m, method.getReturnType(), initialParameterTypes, finalParameterType, state));
  }

  private static boolean isCompatibleVarargsOverload(
      MethodSymbol overload,
      Type returnType,
      ImmutableList<Type> initialParameterTypes,
      Type varargsType,
      VisitorState state) {
    Types types = state.getTypes();

    List<VarSymbol> parameters = overload.getParameters();
    if (!overload.isVarArgs()
        || parameters.size() != initialParameterTypes.size() + 1
        || !types.isSubtype(overload.getReturnType(), returnType)) {
      return false;
    }

    for (int i = 0; i < initialParameterTypes.size(); i++) {
      if (!types.isSubtype(initialParameterTypes.get(i), parameters.get(i).type)) {
        return false;
      }
    }

    Type actualVarargsType = types.elemtype(parameters.getLast().type);
    return switch (varargsType) {
      case WildcardType wildcard when wildcard.isExtendsBound() ->
          types.isSubtype(wildcard.getExtendsBound(), actualVarargsType);
      default -> types.isSubtype(varargsType, actualVarargsType);
    };
  }

  private static Optional<SuggestedFix> trySuggestCallingCustomAlternative(
      MethodInvocationTree tree, MethodInvocationTree argument, VisitorState state) {
    return ALTERNATIVE_METHODS.rowMap().entrySet().stream()
        .filter(e -> e.getKey().matches(tree, state))
        .findFirst()
        .flatMap(e -> trySuggestCallingCustomAlternative(tree, argument, state, e.getValue()));
  }

  private static Optional<SuggestedFix> trySuggestCallingCustomAlternative(
      MethodInvocationTree tree,
      MethodInvocationTree argument,
      VisitorState state,
      Map<String, String> alternatives) {
    return Optional.ofNullable(
            alternatives.get(ASTHelpers.getSymbol(tree).getSimpleName().toString()))
        .map(
            replacement ->
                SuggestedFixes.renameMethodInvocation(tree, replacement, state).toBuilder()
                    .merge(SourceCode.unwrapMethodInvocation(argument, state))
                    .build());
  }

  private static boolean isAtLeastAsVisible(Element symbol, Element reference) {
    return Visibility.fromModifiers(symbol.getModifiers())
            .compareTo(Visibility.fromModifiers(reference.getModifiers()))
        >= 0;
  }
}
