package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.MoreCollectors.onlyElement;
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
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
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
 * <p>This check subsumes the arity-specific Refaster rules that would otherwise be required, as
 * Refaster templates bind a fixed number of parameters, and {@code @Repeated} parameters only
 * unify with invocations that resolve to a varargs overload.
 *
 * <p>Note that {@link #TAGS} is not a collection type: rewriting a collection of Micrometer {@code
 * Tag}s to a {@code Tags} instance drops duplicate tag keys and sorts the remaining tags. This
 * matches the behaviour of the Refaster rules superseded by this check.
 */
// XXX: Expressions rewritten by this check may require a second pass to reach their canonical form;
// e.g. `ImmutableList.builder().add(tag).build()` first becomes `ImmutableList.of(tag)` and only
// then `Tags.of(tag)`.
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
  private static final String TAGS = "io.micrometer.core.instrument.Tags";
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
  private static final Matcher<ExpressionTree> GUAVA_INTRODUCTION_ALLOWED =
      (tree, state) -> ThirdPartyLibrary.GUAVA.isIntroductionAllowed(state);
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
          .onClassAny(
              ImmutableList.class.getCanonicalName(), ImmutableSet.class.getCanonicalName())
          .named("of");
  /**
   * The static methods that yield a builder of one of the {@link #GUAVA_FACTORIES} types, excluding
   * those that impose a non-natural ordering.
   */
  private static final ImmutableSet<String> BUILDER_FACTORY_METHODS =
      ImmutableSet.of(
          "builder", "builderWithExpectedKeys", "builderWithExpectedSize", "naturalOrder");

  private static final CanonicalFactory STREAM_FACTORY =
      new CanonicalFactory(
          Stream.class.getCanonicalName(), /* argsPerEntry= */ 1, /* guard= */ anything());
  private static final CanonicalFactory TAGS_FACTORY =
      new CanonicalFactory(TAGS, /* argsPerEntry= */ 1, /* guard= */ ENUMERATES_TAGS);
  private static final ImmutableList<CanonicalFactory> GUAVA_FACTORIES =
      ImmutableList.of(
          guavaFactory(ImmutableBiMap.class, /* argsPerEntry= */ 2),
          guavaFactory(ImmutableList.class, /* argsPerEntry= */ 1),
          guavaFactory(ImmutableListMultimap.class, /* argsPerEntry= */ 2),
          guavaFactory(ImmutableMap.class, /* argsPerEntry= */ 2),
          guavaFactory(ImmutableMultiset.class, /* argsPerEntry= */ 1),
          guavaFactory(ImmutableSet.class, /* argsPerEntry= */ 1),
          guavaFactory(ImmutableSetMultimap.class, /* argsPerEntry= */ 2),
          guavaFactory(ImmutableSortedMap.class, /* argsPerEntry= */ 2, HAS_COMPARABLE_KEYS),
          guavaFactory(ImmutableSortedSet.class, /* argsPerEntry= */ 1, ENUMERATES_COMPARABLES));
  /** The enumerations whose elements can safely be streamed in the order in which they appear. */
  private static final ImmutableList<Enumerator> ORDERED_ENUMERATORS =
      ImmutableList.of(
          arguments(JDK_LIST_FACTORY),
          arguments(IMMUTABLE_LIST_FACTORY),
          builderChain(ImmutableList.class.getCanonicalName()));

  private static final ImmutableMap<Enumerator, CanonicalFactory> RULES = createRules();

  /** Instantiates a new {@link CanonicalEnumeration} instance. */
  public CanonicalEnumeration() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return RULES.entrySet().stream()
        .flatMap(
            rule ->
                rule.getKey()
                    .enumerate(tree, state)
                    .flatMap(elements -> rule.getValue().tryRender(tree, elements, state))
                    .stream())
        .findFirst()
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  private static ImmutableMap<Enumerator, CanonicalFactory> createRules() {
    ImmutableMap.Builder<Enumerator, CanonicalFactory> rules =
        ImmutableMap.<Enumerator, CanonicalFactory>builder()
            .put(arguments(JDK_LIST_FACTORY), guavaFactory(ImmutableList.class))
            .put(arguments(JDK_MAP_FACTORY), guavaFactory(ImmutableMap.class))
            .put(arguments(JDK_SET_FACTORY), guavaFactory(ImmutableSet.class))
            .put(flatteningEntries(arguments(MAP_ENTRIES_FACTORY)), guavaFactory(ImmutableMap.class))
            .put(arguments(IMMUTABLE_MULTIMAP_FACTORY), guavaFactory(ImmutableListMultimap.class))
            .put(arguments(TAG_ENUMERATION_FACTORY), TAGS_FACTORY)
            .put(chained(ORDERED_ENUMERATORS, COLLECTION_STREAM_METHOD), STREAM_FACTORY);
    GUAVA_FACTORIES.forEach(factory -> rules.put(builderChain(factory.ownerFqcn()), factory));
    return rules.buildOrThrow();
  }

  private static CanonicalFactory guavaFactory(Class<?> owner, int argsPerEntry) {
    return guavaFactory(owner, argsPerEntry, anything());
  }

  private static CanonicalFactory guavaFactory(
      Class<?> owner, int argsPerEntry, Matcher<ExpressionTree> guard) {
    return new CanonicalFactory(
        owner.getCanonicalName(), argsPerEntry, allOf(GUAVA_INTRODUCTION_ALLOWED, guard));
  }

  /** Returns the previously declared {@link CanonicalFactory} for the given Guava type. */
  private static CanonicalFactory guavaFactory(Class<?> owner) {
    return GUAVA_FACTORIES.stream()
        .filter(factory -> factory.ownerFqcn().equals(owner.getCanonicalName()))
        .collect(onlyElement());
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
   */
  private static Enumerator builderChain(String ownerFqcn) {
    Matcher<ExpressionTree> builderFactory =
        staticMethod().onClass(ownerFqcn).namedAnyOf(BUILDER_FACTORY_METHODS);
    return (tree, state) -> tryEnumerateBuilderChain(tree, builderFactory, state);
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
      ExpressionTree tree, Matcher<ExpressionTree> builderFactory, VisitorState state) {
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

      if (!(receiver instanceof MethodInvocationTree invocation)
          || !BUILDER_ELEMENT_METHOD.matches(invocation, state)
          || invocation.getArguments().isEmpty()
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
      if (elements.size() % argsPerEntry != 0
          || !hasOverload(elements.size(), state)
          || !guard.matches(tree, state)) {
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
    private boolean hasOverload(int argCount, VisitorState state) {
      Type type = state.getTypeFromString(ownerFqcn);
      if (type == null) {
        return false;
      }

      return ASTHelpers.matchingMethods(
              state.getName("of"),
              method -> method.isStatic() && method.owner.equals(type.tsym),
              type,
              state.getTypes())
          .anyMatch(method -> accepts(method, argCount));
    }

    private static boolean accepts(MethodSymbol method, int argCount) {
      int parameterCount = method.getParameters().size();
      return method.isVarArgs() ? argCount >= parameterCount - 1 : argCount == parameterCount;
    }
  }
}
