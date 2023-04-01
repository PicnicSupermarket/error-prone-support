package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.PERFORMANCE;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static tech.picnic.errorprone.bugpatterns.util.Documentation.BUG_PATTERNS_BASE_URL;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import tech.picnic.errorprone.bugpatterns.util.SourceCode;

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
          staticMethod().onClass(Arrays.class.getCanonicalName()).named("asList"));
  private static final Matcher<ExpressionTree> IMMUTABLE_COLLECTION_BUILDER =
      instanceMethod().onDescendantOf(ImmutableCollection.Builder.class.getCanonicalName());
  private static final Matcher<ExpressionTree> OBJECT_ENUMERABLE_ASSERT =
      instanceMethod().onDescendantOf("org.assertj.core.api.ObjectEnumerableAssert");
  private static final Matcher<ExpressionTree> STEP_VERIFIER_STEP =
      instanceMethod().onDescendantOf("reactor.test.StepVerifier.Step");
  private static final ImmutableTable<Matcher<ExpressionTree>, String, String> ALTERNATIVE_METHODS =
      ImmutableTable.<Matcher<ExpressionTree>, String, String>builder()
          .put(IMMUTABLE_COLLECTION_BUILDER, "addAll", "add")
          .put(OBJECT_ENUMERABLE_ASSERT, "containsAnyElementsOf", "containsAnyOf")
          .put(OBJECT_ENUMERABLE_ASSERT, "containsAll", "contains")
          .put(OBJECT_ENUMERABLE_ASSERT, "containsExactlyElementsOf", "containsExactly")
          .put(
              OBJECT_ENUMERABLE_ASSERT,
              "containsExactlyInAnyOrderElementsOf",
              "containsExactlyInAnyOrder")
          .put(OBJECT_ENUMERABLE_ASSERT, "doesNotContainAnyElementsOf", "doesNotContain")
          .put(OBJECT_ENUMERABLE_ASSERT, "hasSameElementsAs", "containsOnly")
          .put(STEP_VERIFIER_STEP, "expectNextSequence", "expectNext")
          .build();

  /** Instantiates a new {@link ExplicitArgumentEnumeration} instance. */
  public ExplicitArgumentEnumeration() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (tree.getArguments().size() != 1) {
      return Description.NO_MATCH;
    }

    MethodSymbol method = ASTHelpers.getSymbol(tree);
    if (!isUnaryIterableAcceptingMethod(method, state)) {
      return Description.NO_MATCH;
    }

    ExpressionTree argument = tree.getArguments().get(0);
    if (!(argument instanceof MethodInvocationTree)
        || !EXPLICIT_ITERABLE_CREATOR.matches(argument, state)) {
      return Description.NO_MATCH;
    }

    return trySuggestCallingVarargsOverload(method, (MethodInvocationTree) argument, state)
        .or(() -> trySuggestCallingCustomAlternative(tree, (MethodInvocationTree) argument, state))
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static boolean isUnaryIterableAcceptingMethod(MethodSymbol method, VisitorState state) {
    List<VarSymbol> params = method.params();
    return !method.isVarArgs()
        && params.size() == 1
        && ASTHelpers.isSubtype(params.get(0).type, state.getSymtab().iterableType, state);
  }

  private static Optional<SuggestedFix> trySuggestCallingVarargsOverload(
      MethodSymbol method, MethodInvocationTree argument, VisitorState state) {
    ImmutableList<MethodSymbol> overloads =
        ASTHelpers.matchingMethods(
                method.getSimpleName(),
                m -> m.isPublic() && !m.equals(method),
                method.enclClass().type,
                state.getTypes())
            .collect(toImmutableList());

    /*
     * If all overloads have a single parameter, and at least one of them is a varargs method, then
     * we assume that unwrapping the iterable argument will cause a suitable overload to be invoked.
     * (Note that there may be multiple varargs overloads, either with different parameter types, or
     * due to method overriding; this check is does not attempt to determine which exact method or
     * overload will be invoked as a result of the suggested simplification.)
     *
     * Note that this is a (highly!) imperfect heuristic, but it is sufficient to prevent e.g.
     * unwrapping of arguments to `org.jooq.impl.DSL#row`, which can cause the expression's return
     * type to change from `RowN` to (e.g.) `Row2`.
     */
    // XXX: There are certainly cases where it _would_ be nice to unwrap the arguments to
    // `org.jooq.impl.DSL#row(Collection<?>)`. Look into this.
    // XXX: Ideally we do check that one of the overloads accepts the unwrapped arguments.
    // XXX: Ideally we validate that eligible overloads have compatible return types.
    boolean hasLikelySuitableVarargsOverload =
        overloads.stream().allMatch(m -> m.params().size() == 1)
            && overloads.stream().anyMatch(MethodSymbol::isVarArgs);

    return hasLikelySuitableVarargsOverload
        ? Optional.of(SourceCode.unwrapMethodInvocation(argument, state))
        : Optional.empty();
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
    String name = ASTHelpers.getSymbol(tree).getSimpleName().toString();
    String alternative = alternatives.get(name);

    if (alternative == null) {
      return Optional.empty();
    }

    SuggestedFix fix = SourceCode.unwrapMethodInvocation(argument, state);
    return Optional.of(
        alternative.equals(name)
            ? fix
            : SuggestedFix.builder()
                .merge(SuggestedFixes.renameMethodInvocation(tree, alternative, state))
                .merge(fix)
                .build());
  }
}
