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
import static tech.picnic.errorprone.utils.MoreTypes.subOf;
import static tech.picnic.errorprone.utils.MoreTypes.type;

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
import com.sun.tools.javac.code.BoundKind;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
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
// `var` declaration or in receiver position) may narrow the expression's type, to the point of
// yielding uncompilable code. The narrowed type may also cause a different overload to be selected.
// XXX: Unlike `Set#of`, the canonical factory methods do not reject duplicate values; rewriting
// `Set#of` invocations therefore drops a runtime precondition.
// XXX: Arguments passed to a builder's sizing factory method, such as
// `ImmutableList#builderWithExpectedSize(int)`, are dropped; any side effects they have are lost.
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
  private static final Matcher<ExpressionTree> ENUMERATES_TAGS =
      MoreMatchers.isSubTypeOf(
          generic(
              type(Iterable.class.getCanonicalName()),
              subOf(type("io.micrometer.core.instrument.Tag"))));
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

  /** The non-canonical enumerations recognized, and the factory methods that supersede them. */
  private static final ImmutableList<Rule> RULES =
      ImmutableList.of(
          guavaRule(arguments(JDK_LIST_FACTORY), ImmutableList.class),
          guavaRule(arguments(JDK_MAP_FACTORY), ImmutableMap.class),
          guavaRule(arguments(JDK_SET_FACTORY), ImmutableSet.class),
          guavaRule(CanonicalEnumeration::enumerateMapEntries, ImmutableMap.class),
          guavaRule(arguments(IMMUTABLE_MULTIMAP_FACTORY), ImmutableListMultimap.class),
          new Rule(
              CanonicalEnumeration::enumerateTags,
              "io.micrometer.core.instrument.Tags",
              ENUMERATES_TAGS),
          new Rule(
              CanonicalEnumeration::enumerateStreamedValues,
              Stream.class.getCanonicalName(),
              anything()),
          builderRule(ImmutableBiMap.class, /* argsPerEntry= */ 2),
          builderRule(ImmutableList.class, /* argsPerEntry= */ 1),
          builderRule(ImmutableListMultimap.class, /* argsPerEntry= */ 2),
          builderRule(ImmutableMap.class, /* argsPerEntry= */ 2),
          builderRule(ImmutableMultiset.class, /* argsPerEntry= */ 1),
          builderRule(ImmutableSet.class, /* argsPerEntry= */ 1),
          builderRule(ImmutableSetMultimap.class, /* argsPerEntry= */ 2),
          builderRule(
              ImmutableSortedMap.class,
              /* argsPerEntry= */ 2,
              CanonicalEnumeration::hasComparableKeys),
          builderRule(
              ImmutableSortedSet.class,
              /* argsPerEntry= */ 1,
              CanonicalEnumeration::enumeratesComparables));

  /**
   * Recognizes the enumerations that may be streamed directly.
   *
   * <p>Only enumerations that preserve both the order and the cardinality of the values enumerated
   * qualify; streaming e.g. a {@link Set} would impose an unspecified iteration order, and
   * streaming a deduplicating collection would change the number of values streamed.
   */
  private static final ImmutableList<Enumerator> STREAMABLE_ENUMERATORS =
      ImmutableList.of(
          arguments(JDK_LIST_FACTORY),
          arguments(IMMUTABLE_LIST_FACTORY),
          builderChain(ImmutableList.class, /* argsPerEntry= */ 1));

  /** Recognizes the enumerations of Micrometer tags that may be rewritten. */
  private static final Enumerator TAG_ENUMERATOR = arguments(TAG_ENUMERATION_FACTORY);

  /** Instantiates a new {@link CanonicalEnumeration} instance. */
  public CanonicalEnumeration() {}

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    return RULES.stream()
        .flatMap(rule -> rule.tryRewrite(tree, state).stream())
        .findFirst()
        .map(fix -> describeMatch(tree, fix))
        .orElse(Description.NO_MATCH);
  }

  /** Returns a {@link Rule} that rewrites matching expressions to a Guava factory invocation. */
  private static Rule guavaRule(Enumerator enumerator, Class<?> target) {
    return new Rule(
        enumerator, target.getCanonicalName(), CanonicalEnumeration::isGuavaIntroductionAllowed);
  }

  /**
   * Returns a {@link Rule} that rewrites a fluent chain of builder invocations to an invocation of
   * the associated type's factory method.
   */
  private static Rule builderRule(Class<?> target, int argsPerEntry) {
    return guavaRule(builderChain(target, argsPerEntry), target);
  }

  /**
   * Returns a {@link Rule} that rewrites a fluent chain of builder invocations to an invocation of
   * the associated type's factory method, provided the given constraint is satisfied.
   */
  private static Rule builderRule(
      Class<?> target, int argsPerEntry, Matcher<ExpressionTree> guard) {
    return new Rule(
        builderChain(target, argsPerEntry),
        target.getCanonicalName(),
        allOf(CanonicalEnumeration::isGuavaIntroductionAllowed, guard));
  }

  // XXX: This guard's outcome cannot be observed: the witness type by way of which Guava's presence
  // is established is itself one of the target types, so absent Guava the target types' factory
  // method overloads cannot be resolved either, and the `Rule#hasOverload` check that precedes this
  // guard already rejects the enumeration. The guard is nevertheless retained, as it is the sole
  // remaining safeguard should that check fail to establish the target type's absence.
  private static boolean isGuavaIntroductionAllowed(ExpressionTree tree, VisitorState state) {
    return ThirdPartyLibrary.GUAVA.isIntroductionAllowed(state);
  }

  /**
   * Tells whether the values enumerated by the given expression are mutually comparable, as
   * required by {@link ImmutableSortedSet}'s factory methods.
   */
  private static boolean enumeratesComparables(ExpressionTree tree, VisitorState state) {
    return hasSelfComparableTypeArgument(tree, Iterable.class, state);
  }

  /**
   * Tells whether the keys enumerated by the given expression are mutually comparable, as required
   * by {@link ImmutableSortedMap}'s factory methods.
   */
  private static boolean hasComparableKeys(ExpressionTree tree, VisitorState state) {
    return hasSelfComparableTypeArgument(tree, Map.class, state);
  }

  /**
   * Tells whether the given expression's first type argument, relative to the given supertype, is
   * comparable to itself.
   */
  private static boolean hasSelfComparableTypeArgument(
      ExpressionTree tree, Class<?> superType, VisitorState state) {
    Symbol symbol =
        JavacElements.instance(state.context).getTypeElement(superType.getCanonicalName());
    Type type = ASTHelpers.getType(tree);
    if (symbol == null || type == null) {
      return false;
    }

    Type superTypeInstance = state.getTypes().asSuper(type, symbol);
    return superTypeInstance != null
        && !superTypeInstance.getTypeArguments().isEmpty()
        && isSelfComparable(superTypeInstance.getTypeArguments().getFirst(), state);
  }

  /**
   * Tells whether the given type {@code T} implements {@code Comparable<? super T>}.
   *
   * <p>This is the bound imposed by the sorted collection types' factory methods. It must be
   * verified exactly: Guava's sorted collection types declare additional unbounded factory method
   * overloads that are selected precisely when this bound is <em>not</em> satisfied, and that throw
   * when invoked. Approximating the bound would therefore yield code that compiles but always
   * fails.
   */
  private static boolean isSelfComparable(Type type, VisitorState state) {
    Type bound =
        state.getType(
            state.getSymtab().comparableType,
            /* isArray= */ false,
            ImmutableList.of(
                new Type.WildcardType(type, BoundKind.SUPER, state.getSymtab().boundClass)));
    return bound != null && state.getTypes().isSubtype(type, bound);
  }

  /**
   * Enumerates the tags enumerated by the given expression, if it enumerates at least one.
   *
   * <p>The empty case is excluded, as a reference to {@code Tags#of()} is ambiguous: {@code Tags}
   * declares both a {@code String}-valued and a {@code Tag}-valued variadic overload.
   */
  private static Optional<ImmutableList<ExpressionTree>> enumerateTags(
      ExpressionTree tree, VisitorState state) {
    return TAG_ENUMERATOR.enumerate(tree, state).filter(tags -> !tags.isEmpty());
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

  /**
   * Enumerates the values added to a fluent chain of invocations rooted at one of the given type's
   * builder factory methods.
   *
   * <p>Each enumerated entry consists of {@code argsPerEntry} arguments. Builder factory methods
   * that impose a non-natural ordering, such as {@code orderedBy} and {@code reverseOrder}, are
   * deliberately omitted.
   */
  private static Enumerator builderChain(Class<?> target, int argsPerEntry) {
    Matcher<ExpressionTree> builderFactory =
        staticMethod()
            .onClass(target.getCanonicalName())
            .namedAnyOf(
                "builder", "builderWithExpectedKeys", "builderWithExpectedSize", "naturalOrder");

    return (tree, state) -> {
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
    };
  }

  /**
   * Enumerates the keys and values of the map entries passed to an {@code ofEntries} invocation.
   *
   * <p>Each entry must be created inline, as only then are its key and value statically known. This
   * requirement also rules out the case in which an array of entries is passed to the associated
   * varargs parameter.
   */
  private static Optional<ImmutableList<ExpressionTree>> enumerateMapEntries(
      ExpressionTree tree, VisitorState state) {
    if (!(tree instanceof MethodInvocationTree invocation)
        || !MAP_ENTRIES_FACTORY.matches(invocation, state)) {
      return Optional.empty();
    }

    ImmutableList.Builder<ExpressionTree> elements = ImmutableList.builder();
    for (ExpressionTree entry : invocation.getArguments()) {
      if (!(entry instanceof MethodInvocationTree entryFactory)
          || !MAP_ENTRY_FACTORY.matches(entryFactory, state)) {
        return Optional.empty();
      }

      elements.addAll(entryFactory.getArguments());
    }

    return Optional.of(elements.build());
  }

  /** Enumerates the values streamed by an invocation of {@link Collection#stream()}. */
  private static Optional<ImmutableList<ExpressionTree>> enumerateStreamedValues(
      ExpressionTree tree, VisitorState state) {
    if (!COLLECTION_STREAM_METHOD.matches(tree, state)) {
      return Optional.empty();
    }

    ExpressionTree receiver = ASTHelpers.getReceiver(tree);
    return receiver == null
        ? Optional.empty()
        : STREAMABLE_ENUMERATORS.stream()
            .flatMap(enumerator -> enumerator.enumerate(receiver, state).stream())
            .findFirst();
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
   * A rewrite of a recognized non-canonical enumeration to the canonical static factory method by
   * way of which it should be expressed.
   *
   * @param enumerator Recognizes the expressions to be rewritten, and the values they enumerate.
   * @param targetFqcn The fully qualified name of the type that declares the factory method.
   * @param guard A constraint on the expression to be replaced, capturing preconditions of the
   *     factory method that are not implied by the number of arguments passed to it.
   */
  private record Rule(Enumerator enumerator, String targetFqcn, Matcher<ExpressionTree> guard) {
    /** Returns a fix that rewrites the given expression, if this rule applies to it. */
    Optional<SuggestedFix> tryRewrite(ExpressionTree tree, VisitorState state) {
      return enumerator
          .enumerate(tree, state)
          .filter(elements -> hasOverload(elements.size(), state) && guard.matches(tree, state))
          .map(
              elements -> {
                SuggestedFix.Builder fix = SuggestedFix.builder();
                String target = SuggestedFixes.qualifyType(state, fix, targetFqcn);
                return fix.replace(
                        tree,
                        elements.stream()
                            .map(element -> SourceCode.treeToString(element, state))
                            .collect(joining(", ", target + ".of(", ")")))
                    .build();
              });
    }

    /** Tells whether the factory method has an overload that accepts the given argument count. */
    private boolean hasOverload(int argCount, VisitorState state) {
      /*
       * The target type is looked up by way of `JavacElements`, as `VisitorState#getTypeFromString`
       * consults only the symbol table: it fails to resolve types that are on the classpath but not
       * referenced by any source file in the compilation, making this check's outcome depend on
       * unrelated files.
       */
      // XXX: The mutant that replaces the early return below with `true` is unkillable: if the
      // target type cannot be resolved then `guard` rejects the enumeration regardless.
      Symbol target = JavacElements.instance(state.context).getTypeElement(targetFqcn);
      if (target == null) {
        return false;
      }

      Type type = target.type;

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
