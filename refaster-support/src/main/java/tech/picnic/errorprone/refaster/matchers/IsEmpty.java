package tech.picnic.errorprone.refaster.matchers;

import static com.google.common.base.Verify.verify;
import static com.google.errorprone.matchers.FieldMatchers.staticField;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.argumentCount;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.isPrimitiveType;
import static com.google.errorprone.matchers.Matchers.isSameType;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.receiverOfInvocation;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.matchers.Matchers.symbolMatcher;
import static com.google.errorprone.predicates.TypePredicates.anyOf;
import static com.google.errorprone.predicates.TypePredicates.isArray;
import static com.google.errorprone.predicates.TypePredicates.isDescendantOfAny;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.errorprone.VisitorState;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.predicates.TypePredicate;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.BaseStream;

/**
 * A matcher of expressions that are guaranteed to yield "empty" instances, as defined by their
 * respective types.
 */
// XXX: Also match (effectively) final variables that reference provably-empty objects.
// XXX: Also recognize null-hostile "container" expression types that can only reference empty
// instances, such as `ImmutableCollection<Void>`.
// XXX: Consider recognizing constructs such as `{anyMono,anyFlux}.then()` and types such as
// `{Mono,Flux}<Void>`. Those won't emit values (and are thus "empty"), but may have side-effects.
// XXX: Also recognize `emptyCollection::iterator`, perhaps only if the targeted functional
// interface is in fact `Iterable`.
// XXX: Also recognize `EnumSet.noneOf(...)`.
// XXX: Also recognize empty builders (the `EMPTY_DERIVATIVE` matcher will subsequently match
// `emptyBuilder.build()` invocations).
// XXX: Also recognize various factory methods on Guava's `Maps` and `Sets` utility classes.
// XXX: Merge all statements found in the `@BeforeTemplate` methods of `XEmpty` and `EmptyX`
// Refaster rules (such as `FluxEmpty` and `EmptyStream`) into this matcher, and use it as the
// foundation for an Error Prone check that simplifies any "empty" expression with an
// identically-typed canonical representation. All those Refaster rules can then be dropped.
// (Warning: if side-effectful `Publishers` are recognized as empty, then the canonical replacement
// would change semantics. That's clearly undesirable.)
public final class IsEmpty implements Matcher<ExpressionTree> {
  private static final long serialVersionUID = 1L;
  private static final Integer ZERO = 0;
  private static final Pattern EMPTY_INSTANCE_FACTORY_METHOD_PATTERN = Pattern.compile("empty.*");
  private static final Matcher<Tree> EMPTY_COLLECTION_CONSTRUCTOR_ARGUMENT =
      anyOf(isPrimitiveType(), isSubtypeOf(Comparator.class));
  private static final TypePredicate REGULAR_CONTAINER_TYPE =
      anyOf(
          isDescendantOfAny(
              ImmutableList.of(
                  BaseStream.class.getCanonicalName(),
                  Iterable.class.getCanonicalName(),
                  Iterator.class.getCanonicalName(),
                  Map.class.getCanonicalName(),
                  Multimap.class.getCanonicalName(),
                  Optional.class.getCanonicalName(),
                  OptionalDouble.class.getCanonicalName(),
                  OptionalInt.class.getCanonicalName(),
                  OptionalLong.class.getCanonicalName(),
                  Spliterator.class.getCanonicalName())),
          isArray());
  private static final TypePredicate REACTOR_CONTAINER_TYPE =
      anyOf(
          isDescendantOfAny(
              ImmutableList.of(
                  "reactor.core.publisher.Flux",
                  "reactor.core.publisher.Mono",
                  "reactor.util.context.ContextView")),
          isArray());
  private static final TypePredicate CONTAINER_TYPE =
      anyOf(REGULAR_CONTAINER_TYPE, REACTOR_CONTAINER_TYPE);
  // XXX: Extend this list to include additional JDK collection types with a public constructor.
  private static final Matcher<ExpressionTree> MUTABLE_COLLECTION_TYPE =
      anyOf(
          isSameType(ArrayList.class),
          isSameType(HashMap.class),
          isSameType(HashSet.class),
          isSameType(LinkedHashMap.class),
          isSameType(LinkedHashSet.class),
          isSameType(LinkedList.class),
          isSameType(Stack.class),
          isSameType(TreeMap.class),
          isSameType(TreeSet.class),
          isSameType(Vector.class));

  /**
   * A matcher of expressions that produce empty instances, without deriving them from others.
   *
   * @implNote This matcher recognizes a limited number of nullary methods on a large range of
   *     types. None of those types define all these methods; this heuristic just makes for a
   *     compact means of recognizing a wide range of methods that are exceedingly likely to produce
   *     empty instances.
   */
  private static final Matcher<MethodInvocationTree> EMPTY_INSTANCE_FACTORY =
      allOf(
          argumentCount(0),
          anyOf(
              staticMethod().onClass(CONTAINER_TYPE).namedAnyOf("empty", "just", "of", "ofEntries"),
              staticMethod()
                  .onClassAny(
                      Collections.class.getCanonicalName(), Spliterators.class.getCanonicalName())
                  .withNameMatching(EMPTY_INSTANCE_FACTORY_METHOD_PATTERN)));

  private static final Matcher<ExpressionTree> EMPTY_INSTANCE_CONSTANT =
      anyOf(
          staticField(Collections.class.getCanonicalName(), "EMPTY_LIST"),
          staticField(Collections.class.getCanonicalName(), "EMPTY_MAP"),
          staticField(Collections.class.getCanonicalName(), "EMPTY_SET"));

  /**
   * A matcher of operations on empty container expressions that yield (another) empty instance.
   *
   * @implNote This matcher assumes that {@link #REGULAR_CONTAINER_TYPE regular container types}
   *     generally do not expose methods that both (a) allow one to derive a non-empty instance from
   *     an empty instance and (b) return the result of said operation. Based on a manual analysis
   *     of a majority of the JDK and Guava types recognized by this matcher, this heuristic should
   *     yield very few false positives. This approach avoids requiring the explicit enumeration of
   *     a large number of (type, method) tuples.
   * @implNote This matcher flags only method invocations for which the associated <em>method
   *     symbol</em> has a container return type. By not looking at the <em>expression's</em> return
   *     type, we prevent flagging e.g. {@code List.<List<String>>of().get(0)} (which throws an
   *     {@link IndexOutOfBoundsException}) or {@code Map.<String,
   *     List<String>>of().getOrDefault("foo", List.of("bar))} (which does not return an empty
   *     instance).
   */
  // XXX: If it turns out that the heuristic implemented here is too permissive, then restrict the
  // set of supported receiver types, e.g. by only supporting `java.*` and
  // `com.google.common.collect.*` packages.
  // XXX: Review whether to exclude `Stream#gather`, as it could return anything.
  // XXX: Review whether to include `Stream#collect` calls that return a container type, because
  // while invocations of that method may in principle return anything, _generally_ they will map an
  // empty stream to some other empty container type. This seems like a fair heuristic.
  // XXX: Extend this matcher to include selected methods on `Flux` and `Mono`.
  // XXX: Extend this matcher to include various static factory methods, such that e.g.
  // `ImmutableSet.copyOf(emptyIterable)`, `ImmutableList.sortedCopyOf(emptyIterable)`,
  // `Sets.immutableEnumSet(emptyIterable)` and `Flux.fromIterable(emptyIterable)` are also
  // recognized. See Refaster rules with `@Matches(IsEmpty.class)` for possible candidates.
  // XXX: Though it may return a non-empty `Set` even when applied to an empty instance, this
  // matcher does not exclude Guava's `SetView#copyInto` method, because in practice `SetView`s are
  // returned only by methods in the `Sets` utility class, none of which are currently recognized by
  // this matcher.
  private static final Matcher<MethodInvocationTree> EMPTY_DERIVATIVE =
      allOf(
          anyOf(
              symbolReturns(REGULAR_CONTAINER_TYPE),
              allOf(
                  symbolReturns(REACTOR_CONTAINER_TYPE),
                  instanceMethod().anyClass().namedAnyOf("readOnly", "delete"))),
          receiverOfInvocation(new IsEmpty()));

  /** Instantiates a new {@link IsEmpty} instance. */
  public IsEmpty() {}

  @Override
  public boolean matches(ExpressionTree tree, VisitorState state) {
    return switch (tree) {
      case MethodInvocationTree methodInvocation ->
          EMPTY_INSTANCE_FACTORY.matches(methodInvocation, state)
              || EMPTY_DERIVATIVE.matches(methodInvocation, state);
      case NewArrayTree newArray -> isEmptyArrayCreation(newArray);
      case NewClassTree newClass -> isEmptyCollectionConstructor(newClass, state);
      case ParenthesizedTree parenthesized -> matches(parenthesized.getExpression(), state);
      case TypeCastTree typeCast -> matches(typeCast.getExpression(), state);
      default -> EMPTY_INSTANCE_CONSTANT.matches(tree, state);
    };
  }

  private boolean isEmptyCollectionConstructor(NewClassTree newClass, VisitorState state) {
    if (!MUTABLE_COLLECTION_TYPE.matches(newClass, state)) {
      return false;
    }

    List<? extends ExpressionTree> arguments = newClass.getArguments();
    if (arguments.stream().allMatch(a -> EMPTY_COLLECTION_CONSTRUCTOR_ARGUMENT.matches(a, state))) {
      /*
       * This is a default constructor, or a constructor that creates an empty collection using
       * custom (re)size/load factor parameters and/or a custom `Comparator`.
       */
      return true;
    }

    /*
     * This looks like a copy constructor, in which case the resultant collection is empty if its
     * argument is.
     */
    verify(arguments.size() == 1, "Unexpected %s-ary constructor", arguments.size());
    return matches(arguments.getFirst(), state);
  }

  private static boolean isEmptyArrayCreation(NewArrayTree newArray) {
    return (!newArray.getDimensions().isEmpty()
            && ZERO.equals(
                ASTHelpers.constValue(newArray.getDimensions().getFirst(), Integer.class)))
        || (newArray.getInitializers() != null && newArray.getInitializers().isEmpty());
  }

  private static Matcher<MethodInvocationTree> symbolReturns(TypePredicate predicate) {
    return symbolMatcher(
        (symbol, state) -> predicate.apply(((MethodSymbol) symbol).getReturnType(), state));
  }
}
