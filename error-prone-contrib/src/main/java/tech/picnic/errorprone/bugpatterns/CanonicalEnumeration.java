package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.LinkType.CUSTOM;
import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.BugPattern.StandardTags.PERFORMANCE;
import static com.google.errorprone.BugPattern.StandardTags.SIMPLIFICATION;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.anything;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static java.util.stream.Collectors.joining;
import static tech.picnic.errorprone.utils.Documentation.BUG_PATTERNS_BASE_URL;
import static tech.picnic.errorprone.utils.MoreTypes.generic;
import static tech.picnic.errorprone.utils.MoreTypes.raw;
import static tech.picnic.errorprone.utils.MoreTypes.subOf;
import static tech.picnic.errorprone.utils.MoreTypes.type;
import static tech.picnic.errorprone.utils.MoreTypes.unbound;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.suppliers.Supplier;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.Name;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import tech.picnic.errorprone.utils.MoreMatchers;
import tech.picnic.errorprone.utils.SourceCode;
import tech.picnic.errorprone.utils.ThirdPartyLibrary;

/**
 * A {@link BugChecker} that flags expressions which enumerate a fixed number of values in a
 * non-canonical way.
 *
 * <p>Java offers many ways of enumerating a statically known number of values: JDK factory methods
 * such as {@link List#of}, their Guava counterparts, and the builders associated with Guava's
 * immutable collection types. This check rewrites such expressions to the canonical factory method
 * for the type at hand, irrespective of the number of values enumerated.
 *
 * <p>Note that Micrometer's {@code Tags} is not a collection type: rewriting a collection of {@code
 * Tag}s to a {@code Tags} instance drops duplicate tag keys, sorts the remaining tags, and may
 * yield uncompilable code if the enclosing expression requires a collection type.
 */
// XXX: Consider also enumerating values held by a builder that is assigned to a local variable.
// Doing so requires proving that the variable does not escape and that no element is added
// conditionally.
// XXX: Explicit type arguments are dropped, which in the absence of a target type (such as in a
// `var` declaration or in receiver position) may narrow the expression's type.
// XXX: The `Comparable` guards approximate the `E extends Comparable<? super E>` bound of the
// sorted collections' factory methods; an `E extends Comparable<SomethingElse>` would slip through.
@AutoService(BugChecker.class)
@BugPattern(
    summary = "Prefer the canonical factory method for enumerating a fixed number of values",
    link = BUG_PATTERNS_BASE_URL + "CanonicalEnumeration",
    linkType = CUSTOM,
    severity = SUGGESTION,
    tags = {PERFORMANCE, SIMPLIFICATION})
public final class CanonicalEnumeration extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> BUILDER_ELEMENT_METHOD =
      instanceMethod().anyClass().namedAnyOf("add", "put");
  private static final Matcher<ExpressionTree> BUILDER_TERMINAL_METHOD =
      instanceMethod().anyClass().namedAnyOf("build", "buildOrThrow").withNoParameters();
  private static final Matcher<ExpressionTree> COLLECTION_STREAM_METHOD =
      instanceMethod()
          .onDescendantOf(Collection.class.getCanonicalName())
          .named("stream")
          .withNoParameters();
  private static final Matcher<ExpressionTree> ENUMERATES_COMPARABLES =
      MoreMatchers.isSubTypeOf(
          generic(
              type(Iterable.class.getCanonicalName()),
              subOf(raw(type(Comparable.class.getCanonicalName())))));
  private static final Matcher<ExpressionTree> ENUMERATES_TAGS =
      MoreMatchers.isSubTypeOf(
          generic(
              type(Iterable.class.getCanonicalName()),
              subOf(type("io.micrometer.core.instrument.Tag"))));
  private static final Matcher<ExpressionTree> HAS_COMPARABLE_KEYS =
      MoreMatchers.isSubTypeOf(
          generic(
              type(Map.class.getCanonicalName()),
              subOf(raw(type(Comparable.class.getCanonicalName()))),
              unbound()));
  private static final Matcher<ExpressionTree> IMMUTABLE_LIST_FACTORY =
      staticMethod().onClass(ImmutableList.class.getCanonicalName()).named("of");
  private static final Matcher<ExpressionTree> IMMUTABLE_MULTIMAP_FACTORY =
      staticMethod().onClass(ImmutableMultimap.class.getCanonicalName()).named("of");
  private static final Matcher<ExpressionTree> JDK_LIST_FACTORY =
      staticMethod().onClass(List.class.getCanonicalName()).named("of");
  private static final Matcher<ExpressionTree> JDK_MAP_FACTORY =
      staticMethod().onClass(Map.class.getCanonicalName()).named("of");
  private static final Matcher<ExpressionTree> JDK_SET_FACTORY =
      staticMethod().onClass(Set.class.getCanonicalName()).named("of");
  private static final Matcher<ExpressionTree> MAP_ENTRIES_FACTORY =
      staticMethod()
          .onClassAny(Map.class.getCanonicalName(), ImmutableMap.class.getCanonicalName())
          .named("ofEntries");
  private static final Matcher<ExpressionTree> MAP_ENTRY_FACTORY =
      anyOf(
          staticMethod().onClass(Map.class.getCanonicalName()).named("entry"),
          staticMethod().onClass(Maps.class.getCanonicalName()).named("immutableEntry"));
  private static final Matcher<ExpressionTree> TAG_ENUMERATION_FACTORY =
      staticMethod()
          .onClassAny(ImmutableList.class.getCanonicalName(), ImmutableSet.class.getCanonicalName())
          .named("of");
  private static final Supplier<Name> FACTORY_METHOD_NAME =
      VisitorState.memoize(state -> state.getName("of"));
  private static final ImmutableMap<Enumerator, CanonicalFactory> RULES = createRules();

  /** Instantiates a new {@link CanonicalEnumeration} instance. */
  public CanonicalEnumeration() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return RULES.entrySet().stream()
        .flatMap(
            rule ->
                rule
                    .getKey()
                    .enumerate(tree, state)
                    .flatMap(elements -> rule.getValue().tryRender(tree, elements, state))
                    .stream())
        .findFirst()
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static ImmutableMap<Enumerator, CanonicalFactory> createRules() {
    CanonicalFactory immutableList = guavaFactory(ImmutableList.class, /* argsPerEntry= */ 1);
    CanonicalFactory immutableListMultimap =
        guavaFactory(ImmutableListMultimap.class, /* argsPerEntry= */ 2);
    CanonicalFactory immutableMap = guavaFactory(ImmutableMap.class, /* argsPerEntry= */ 2);
    CanonicalFactory immutableSet = guavaFactory(ImmutableSet.class, /* argsPerEntry= */ 1);

    ImmutableMap.Builder<Enumerator, CanonicalFactory> rules =
        ImmutableMap.<Enumerator, CanonicalFactory>builder()
            .put(arguments(JDK_LIST_FACTORY), immutableList)
            .put(arguments(JDK_MAP_FACTORY), immutableMap)
            .put(arguments(JDK_SET_FACTORY), immutableSet)
            .put(flatteningEntries(arguments(MAP_ENTRIES_FACTORY)), immutableMap)
            .put(arguments(IMMUTABLE_MULTIMAP_FACTORY), immutableListMultimap)
            .put(
                arguments(TAG_ENUMERATION_FACTORY),
                new CanonicalFactory(
                    "io.micrometer.core.instrument.Tags",
                    /* argsPerEntry= */ 1,
                    /* guard= */ ENUMERATES_TAGS))
            /*
             * Only enumerations that preserve both the order and the cardinality of the values
             * enumerated may be streamed directly; doing so for e.g. a `Set` would impose an
             * unspecified iteration order, and for a deduplicating collection it would change the
             * number of values streamed.
             */
            .put(
                chained(
                    ImmutableList.of(
                        arguments(JDK_LIST_FACTORY),
                        arguments(IMMUTABLE_LIST_FACTORY),
                        builderChain(
                            ImmutableList.class.getCanonicalName(), /* argsPerEntry= */ 1)),
                    COLLECTION_STREAM_METHOD),
                new CanonicalFactory(
                    Stream.class.getCanonicalName(),
                    /* argsPerEntry= */ 1,
                    /* guard= */ anything()));

    ImmutableList.of(
            guavaFactory(ImmutableBiMap.class, /* argsPerEntry= */ 2),
            immutableList,
            immutableListMultimap,
            immutableMap,
            guavaFactory(ImmutableMultiset.class, /* argsPerEntry= */ 1),
            immutableSet,
            guavaFactory(ImmutableSetMultimap.class, /* argsPerEntry= */ 2),
            guavaFactory(ImmutableSortedMap.class, /* argsPerEntry= */ 2, HAS_COMPARABLE_KEYS),
            guavaFactory(ImmutableSortedSet.class, /* argsPerEntry= */ 1, ENUMERATES_COMPARABLES))
        .forEach(
            factory ->
                rules.put(builderChain(factory.ownerFqcn(), factory.argsPerEntry()), factory));

    return rules.buildOrThrow();
  }

  private static CanonicalFactory guavaFactory(Class<?> owner, int argsPerEntry) {
    return guavaFactory(owner, argsPerEntry, anything());
  }

  private static CanonicalFactory guavaFactory(
      Class<?> owner, int argsPerEntry, Matcher<ExpressionTree> guard) {
    return new CanonicalFactory(
        owner.getCanonicalName(),
        argsPerEntry,
        allOf(CanonicalEnumeration::isGuavaIntroductionAllowed, guard));
  }

  // XXX: This guard cannot be exercised by a test, as Guava is on the classpath of every test. It
  // is retained for explicitness; absent Guava the target types' factory method overloads cannot be
  // resolved either, so `CanonicalFactory#hasOverload` would reject the enumeration regardless.
  private static boolean isGuavaIntroductionAllowed(ExpressionTree tree, VisitorState state) {
    return ThirdPartyLibrary.GUAVA.isIntroductionAllowed(state);
  }

  /** Enumerates the arguments of an invocation of the given method. */
  private static Enumerator arguments(Matcher<ExpressionTree> matcher) {
    return (tree, state) ->
        tree instanceof MethodInvocationTree invocation
                && matcher.matches(invocation, state)
                && !isArrayVarargsInvocation(invocation, state)
            ? Optional.of(ImmutableList.copyOf(invocation.getArguments()))
            : Optional.empty();
  }

  /** Enumerates the keys and values of the map entries enumerated by the given delegate. */
  private static Enumerator flatteningEntries(Enumerator delegate) {
    return (tree, state) ->
        delegate.enumerate(tree, state).flatMap(entries -> tryFlattenEntries(entries, state));
  }

  /**
   * Enumerates the values added to a fluent chain of invocations rooted at one of the given type's
   * builder factory methods.
   *
   * <p>Builder factory methods that impose a non-natural ordering, such as {@code orderedBy} and
   * {@code reverseOrder}, are deliberately omitted.
   */
  private static Enumerator builderChain(String ownerFqcn, int argsPerEntry) {
    Matcher<ExpressionTree> builderFactory =
        staticMethod()
            .onClass(ownerFqcn)
            .namedAnyOf(
                "builder", "builderWithExpectedKeys", "builderWithExpectedSize", "naturalOrder");
    return (tree, state) -> tryEnumerateBuilderChain(tree, builderFactory, argsPerEntry, state);
  }

  /** Enumerates the values enumerated by the receiver of an invocation of the given method. */
  private static Enumerator chained(
      ImmutableList<Enumerator> delegates, Matcher<ExpressionTree> matcher) {
    return (tree, state) -> {
      if (!matcher.matches(tree, state)) {
        return Optional.empty();
      }

      ExpressionTree receiver = ASTHelpers.getReceiver(tree);
      return receiver == null
          ? Optional.empty()
          : delegates.stream()
              .flatMap(delegate -> delegate.enumerate(receiver, state).stream())
              .findFirst();
    };
  }

  private static Optional<ImmutableList<ExpressionTree>> tryFlattenEntries(
      ImmutableList<ExpressionTree> entries, VisitorState state) {
    ImmutableList.Builder<ExpressionTree> elements = ImmutableList.builder();
    for (ExpressionTree entry : entries) {
      if (!(entry instanceof MethodInvocationTree invocation)
          || !MAP_ENTRY_FACTORY.matches(invocation, state)) {
        return Optional.empty();
      }

      elements.addAll(invocation.getArguments());
    }

    return Optional.of(elements.build());
  }

  private static Optional<ImmutableList<ExpressionTree>> tryEnumerateBuilderChain(
      ExpressionTree tree,
      Matcher<ExpressionTree> builderFactory,
      int argsPerEntry,
      VisitorState state) {
    if (!BUILDER_TERMINAL_METHOD.matches(tree, state)) {
      return Optional.empty();
    }

    ImmutableList.Builder<ExpressionTree> elements = ImmutableList.builder();
    for (ExpressionTree receiver = ASTHelpers.getReceiver(tree);
        receiver != null;
        receiver = ASTHelpers.getReceiver(receiver)) {
      if (builderFactory.matches(receiver, state)) {
        return Optional.of(elements.build().reverse());
      }

      /*
       * Each invocation must contribute whole entries; this rejects e.g.
       * `ImmutableMap.Builder#put(Map.Entry)`, whose sole argument is not itself a map key.
       */
      if (!(receiver instanceof MethodInvocationTree invocation)
          || !BUILDER_ELEMENT_METHOD.matches(invocation, state)
          || invocation.getArguments().size() % argsPerEntry != 0
          || isArrayVarargsInvocation(invocation, state)) {
        return Optional.empty();
      }

      elements.addAll(invocation.getArguments().reversed());
    }

    return Optional.empty();
  }

  /**
   * Tells whether the given invocation passes an array to a varargs parameter, in which case its
   * arguments do not correspond to the values enumerated.
   */
  private static boolean isArrayVarargsInvocation(MethodInvocationTree tree, VisitorState state) {
    MethodSymbol symbol = ASTHelpers.getSymbol(tree);
    if (!symbol.isVarArgs() || tree.getArguments().size() != symbol.getParameters().size()) {
      return false;
    }

    Type type = ASTHelpers.getType(tree.getArguments().getLast());
    return type != null && state.getTypes().isArray(type);
  }

  /** An expression that enumerates a statically known number of values. */
  @FunctionalInterface
  private interface Enumerator {
    /**
     * Returns the values enumerated by the given expression, if it is an enumeration of the kind
     * recognized by this {@link Enumerator}.
     */
    Optional<ImmutableList<ExpressionTree>> enumerate(ExpressionTree tree, VisitorState state);
  }

  /**
   * The canonical static factory method by way of which an enumeration of values should be
   * expressed.
   *
   * @param ownerFqcn The fully qualified name of the type that declares the factory method.
   * @param argsPerEntry The number of arguments that make up a single enumerated entry.
   * @param guard A constraint on the expression to be replaced, capturing preconditions of the
   *     factory method that are not implied by the number of arguments passed to it.
   */
  private record CanonicalFactory(
      String ownerFqcn, int argsPerEntry, Matcher<ExpressionTree> guard) {
    Optional<SuggestedFix> tryRender(
        ExpressionTree tree, ImmutableList<ExpressionTree> elements, VisitorState state) {
      if (!hasOverload(elements.size(), state) || !guard.matches(tree, state)) {
        return Optional.empty();
      }

      SuggestedFix.Builder fix = SuggestedFix.builder();
      String owner = SuggestedFixes.qualifyType(state, fix, ownerFqcn);
      return Optional.of(
          fix.replace(
                  tree,
                  elements.stream()
                      .map(element -> SourceCode.treeToString(element, state))
                      .collect(joining(", ", owner + ".of(", ")")))
              .build());
    }

    /** Tells whether the factory method has an overload that accepts the given argument count. */
    // XXX: The null check below is unkillable: every target type is either part of the JDK or
    // implied by the matched expression's own type.
    private boolean hasOverload(int argCount, VisitorState state) {
      Type type = state.getTypeFromString(ownerFqcn);
      if (type == null) {
        return false;
      }

      /*
       * Only overloads declared by the target type itself are considered; static methods declared
       * by supertypes (such as `Set#of`, reachable through `ImmutableSet`'s type closure) are not
       * inherited, and hence cannot be invoked by way of the target type.
       */
      // XXX: This predicate's mutants are unkillable: no target type declares an instance method
      // named `of`, and no supertype of a target type declares an `of` overload that accepts an
      // argument count that the target type itself rejects. The predicate is nevertheless required
      // for the arity derivation to be meaningful.
      return ASTHelpers.matchingMethods(
              FACTORY_METHOD_NAME.get(state),
              method -> method.isStatic() && method.owner.equals(type.tsym),
              type,
              state.getTypes())
          .anyMatch(method -> accepts(method, argCount));
    }

    // XXX: The lower bound imposed on varargs invocations is unkillable: for every target type, the
    // argument counts below that bound are also accepted by a fixed-arity overload. The bound is
    // nevertheless required to avoid suggesting uncompilable code for future target types.
    private static boolean accepts(MethodSymbol method, int argCount) {
      int parameterCount = method.getParameters().size();
      return method.isVarArgs() ? argCount >= parameterCount - 1 : argCount == parameterCount;
    }
  }
}
