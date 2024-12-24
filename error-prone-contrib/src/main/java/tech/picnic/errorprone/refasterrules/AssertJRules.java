package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Matches;
import com.google.errorprone.refaster.annotation.NotMatches;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.MapAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.ObjectEnumerableAssert;
import org.assertj.core.api.OptionalDoubleAssert;
import org.assertj.core.api.OptionalIntAssert;
import org.assertj.core.api.OptionalLongAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.matchers.IsArray;
import tech.picnic.errorprone.refaster.matchers.IsEmpty;

/** Refaster rules related to AssertJ expressions and statements. */
// XXX: Most `AbstractIntegerAssert` rules can also be applied for other primitive types. Generate
// these in separate files.
// XXX: Also do for BigInteger/BigDecimal?
// XXX: `assertThat(cmp.compare(a, b)).isZero()` -> make something nicer.
// ^ And variants.
// XXX: Consider splitting this class into multiple classes.
// XXX: Some of these rules may not apply given the updated TestNG rewrite rules. Review.
// XXX: For the rules that "unwrap" explicitly enumerated collections, also introduce variants
// with explicitly enumerated sorted collections. (Requires that the type bound is Comparable.)
// XXX: Handle `.isEqualTo(explicitlyEnumeratedCollection)`. Can be considered equivalent to
// `.containsOnly(elements)`. (This does mean the auto-generated code needs to be more advanced.
// Ponder this.)
// XXX: Most/all of those Iterable rules can also be applied to arrays.
// XXX: Elsewhere add a rule to disallow `Collection.emptyList()` and variants as well as
// `Arrays.asList()` and `Arrays.asList(singleElement)`, maybe other obviously-varargs cases.
// XXX: Can we better handle Multimaps?
// XXX: For the stream overloads, there are also other assertions we can simplify.
// XXX: assertThat(stream.anyMatch(pred)).isTrue() -> simplify.
// XXX: For the generated code, go up to at least 8.
// XXX: Try to fix Refaster so that the `assertThat(Comparable)` overload is matched.
// XXX: Look for `.test()` expressions and see which AssertJ/RxJava combos can be written nicer.
// XXX: Drop String.format inside `.withFailureMessage` and similar variants.
// XXX: `assertThat(...).isEqualTo((Object) someCollection)` ->
// `assertThat(...).containsExactlyInAnyOrder(someCollection)` (not so in general for sorted
// collections)
// XXX: `assertThat(optional.map(fun)).hasValue(v)` ->
// `assertThat(optional).get().extracting(fun).isEqualTo(v)` (if the get fails the map was useless)
// XXX: `someAssert.extracting(pred).isEqualTo(true)` -> `someAssert.matches(pred)`
// XXX: `assertThat(someString.contains(s)).isTrue()` -> assertThat(someString).contains(s)` -> Also
// for collections
// XXX: `assertThat(someString.matches(s)).isTrue()` -> assertThat(someString).matches(s)`
// XXX: `assertThat(n > k).isTrue()` -> assertThat(n).isGreaterThan(k)` (etc. Also `==`!)
// XXX: `assertThat(n > k && n < m).isTrue()` -> assertThat(n).isStrictlyBetween(k, m)` (etc.)
// XXX: `assertThat(ImmutableList.copyOf(iterable))` -> assertThat(iterable)` (etc.)
// XXX: The `assertThat` rules currently don't handle the case where there's a `failMessage`. Decide
// what to do with that.
// XXX: Also cater for `hasSameElementsAs(Sets.newHashSet(...))` and variants?
// XXX: Rewrite `.containsExactlyElementsOf(Arrays.asList(array))` and variants.
// XXX: Right now we use and import `Offset.offset` and `Percentage.withPercentage`. Use the AssertJ
// methods instead. (Also in the TestNG migration.)
//      ^ Also for `Tuple`!
// XXX: `assertThatCode(x).isInstanceOf(clazz)` -> `assertThatThrownBy(x).isInstanceOf(clazz)`
// (etc.)
// XXX: Look into using Assertions#contentOf(URL url, Charset charset) instead of our own test
// method.
// XXX: Write `Optional` rules also for `OptionalInt` and variants.
// XXX: Write plugin to flag `assertThat(compileTimeConstant)` occurrences. Also other likely
// candidates, such as `assertThat(ImmutableSet(foo, bar)).XXX`
// XXX: Write generic plugin to replace explicit array parameters with varargs (`new int[] {1, 2}`
// -> `1, 2`).
// XXX: Write plugin that drops any `.withFailMessage` that doesn't include a compile-time constant
// string? Most of these are useless.
// XXX: Write plugin that identifies `.get().propertyAccess()` and "pushes" this out. Would only
// nicely work for non-special types, though, cause after `extracting(propertyAccess)` many
// operations are not available...
// XXX: Write plugin that identifies repeated `assertThat(someProp.xxx)` calls and bundles these
// somehow.
// XXX: `abstractOptionalAssert.get().satisfies(pred)` ->
// `abstractOptionalAssert.hasValueSatisfying(pred)`.
// XXX: `assertThat(ImmutableList.sortedCopyOf(cmp, values)).somethingExactOrder` -> just compare
// "in any order".
// XXX: Turns out a lot of this is also covered by https://github.com/palantir/assertj-automation.
// See how we can combine these things. Do note that (at present) their Refaster rules don't
// show up as Error Prone checks. So we'd have to build an integration for that.
@OnlineDocumentation
final class AssertJRules {
  private AssertJRules() {}

  //
  // OptionalDouble
  //

  // XXX: There are several other variations that can also be optimized so as to avoid
  // unconditionally calling `getAsDouble`.
  static final class AssertThatOptionalDouble {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(OptionalDouble optional, double expected) {
      return assertThat(optional.getAsDouble()).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    OptionalDoubleAssert after(OptionalDouble optional, double expected) {
      return assertThat(optional).hasValue(expected);
    }
  }

  //
  // OptionalInt
  //

  // XXX: There are several other variations that can also be optimized so as to avoid
  // unconditionally calling `getAsInt`.
  static final class AssertThatOptionalInt {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(OptionalInt optional, int expected) {
      return assertThat(optional.getAsInt()).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    OptionalIntAssert after(OptionalInt optional, int expected) {
      return assertThat(optional).hasValue(expected);
    }
  }

  //
  // OptionalLong
  //

  // XXX: There are several other variations that can also be optimized so as to avoid
  // unconditionally calling `getAsLong`.
  static final class AssertThatOptionalLong {
    @BeforeTemplate
    AbstractLongAssert<?> before(OptionalLong optional, long expected) {
      return assertThat(optional.getAsLong()).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    OptionalLongAssert after(OptionalLong optional, long expected) {
      return assertThat(optional).hasValue(expected);
    }
  }

  //
  // ObjectEnumerable
  //

  static final class AssertThatObjectEnumerableIsEmpty<E> {
    @BeforeTemplate
    @SuppressWarnings("unchecked")
    void before(
        ObjectEnumerableAssert<?, E> enumAssert,
        @Matches(IsEmpty.class) Iterable<? extends E> wellTypedIterable,
        @Matches(IsEmpty.class) Iterable<?> arbitrarilyTypedIterable) {
      Refaster.anyOf(
          enumAssert.containsExactlyElementsOf(wellTypedIterable),
          enumAssert.containsExactlyInAnyOrderElementsOf(wellTypedIterable),
          enumAssert.hasSameElementsAs(wellTypedIterable),
          enumAssert.hasSameSizeAs(arbitrarilyTypedIterable),
          enumAssert.isSubsetOf(wellTypedIterable),
          enumAssert.containsExactly(),
          enumAssert.containsExactlyInAnyOrder(),
          enumAssert.containsOnly(),
          enumAssert.isSubsetOf());
    }

    @AfterTemplate
    void after(ObjectEnumerableAssert<?, E> enumAssert) {
      enumAssert.isEmpty();
    }
  }

  static final class ObjectEnumerableContainsOneElement<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("unchecked")
    ObjectEnumerableAssert<?, S> before(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return Refaster.anyOf(
          iterAssert.containsAnyOf(element),
          iterAssert.containsSequence(element),
          iterAssert.containsSubsequence(element));
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    ObjectEnumerableAssert<?, S> after(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return iterAssert.contains(element);
    }
  }

  static final class ObjectEnumerableDoesNotContainOneElement<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("unchecked")
    ObjectEnumerableAssert<?, S> before(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return iterAssert.doesNotContainSequence(element);
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    ObjectEnumerableAssert<?, S> after(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return iterAssert.doesNotContain(element);
    }
  }

  static final class ObjectEnumerableContainsExactlyOneElement<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("unchecked")
    ObjectEnumerableAssert<?, S> before(
        ObjectEnumerableAssert<?, S> iterAssert, @NotMatches(IsArray.class) T element) {
      return iterAssert.containsExactlyInAnyOrder(element);
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    ObjectEnumerableAssert<?, S> after(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return iterAssert.containsExactly(element);
    }
  }

  static final class AssertThatSetContainsExactlyOneElement<S, T extends S> {
    @BeforeTemplate
    ObjectEnumerableAssert<?, S> before(Set<S> set, T element) {
      return assertThat(set).containsOnly(element);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectEnumerableAssert<?, S> after(Set<S> set, T element) {
      return assertThat(set).containsExactly(element);
    }
  }

  //
  // Iterable
  //

  static final class AssertThatIterableIsEmpty<E> {
    @BeforeTemplate
    void before(Iterable<E> iterable) {
      Refaster.anyOf(
          assertThat(iterable).hasSize(0),
          assertThat(iterable.iterator().hasNext()).isFalse(),
          assertThat(Iterables.size(iterable)).isEqualTo(0L),
          assertThat(Iterables.size(iterable)).isNotPositive());
    }

    @BeforeTemplate
    void before(Collection<E> iterable) {
      Refaster.anyOf(
          assertThat(iterable.isEmpty()).isTrue(),
          assertThat(iterable.size()).isEqualTo(0L),
          assertThat(iterable.size()).isNotPositive());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Collection<E> iterable) {
      assertThat(iterable).isEmpty();
    }
  }

  static final class AssertThatIterableIsNotEmpty<E> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Iterable<E> iterable) {
      return Refaster.anyOf(
          assertThat(iterable.iterator().hasNext()).isTrue(),
          assertThat(Iterables.size(iterable)).isNotEqualTo(0),
          assertThat(Iterables.size(iterable)).isPositive());
    }

    @BeforeTemplate
    AbstractAssert<?, ?> before(Collection<E> iterable) {
      return Refaster.anyOf(
          assertThat(iterable.isEmpty()).isFalse(),
          assertThat(iterable.size()).isNotEqualTo(0),
          assertThat(iterable.size()).isPositive());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable) {
      return assertThat(iterable).isNotEmpty();
    }
  }

  static final class AssertThatIterableHasSize<E> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(Iterable<E> iterable, int length) {
      return assertThat(Iterables.size(iterable)).isEqualTo(length);
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(Collection<E> iterable, int length) {
      return assertThat(iterable.size()).isEqualTo(length);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IterableAssert<E> after(Iterable<E> iterable, int length) {
      return assertThat(iterable).hasSize(length);
    }
  }

  static final class AssertThatIterableHasOneElementEqualTo<S, T extends S> {
    @BeforeTemplate
    ObjectAssert<S> before(Iterable<S> iterable, T element) {
      return assertThat(Iterables.getOnlyElement(iterable)).isEqualTo(element);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IterableAssert<S> after(Iterable<S> iterable, T element) {
      return assertThat(iterable).containsExactly(element);
    }
  }

  // XXX: This overload is here because `assertThat` has an overload for `Comparable` types.
  // Unfortunately this still doesn't convince Refaster to match this rule in the context of
  // Comparable types. Figure out why! Note that this also affects the `AssertThatOptional` rule.
  static final class AssertThatIterableHasOneComparableElementEqualTo<
      S extends Comparable<? super S>, T extends S> {
    @BeforeTemplate
    AbstractComparableAssert<?, S> before(Iterable<S> iterable, T element) {
      return assertThat(Iterables.getOnlyElement(iterable)).isEqualTo(element);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    IterableAssert<S> after(Iterable<S> iterable, T element) {
      return assertThat(iterable).containsExactly(element);
    }
  }

  //
  // List
  //

  static final class AssertThatListsAreEqual<S, T extends S> {
    @BeforeTemplate
    ListAssert<S> before(List<S> list1, List<T> list2) {
      return assertThat(list1).isEqualTo(list2);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(List<S> list1, List<T> list2) {
      return assertThat(list1).containsExactlyElementsOf(list2);
    }
  }

  //
  // Set
  //

  static final class AssertThatSetsAreEqual<S, T extends S> {
    @BeforeTemplate
    AbstractCollectionAssert<?, ?, S, ?> before(Set<S> set1, Set<T> set2) {
      return Refaster.anyOf(
          assertThat(set1).isEqualTo(set2),
          assertThat(set1).containsExactlyInAnyOrderElementsOf(set2));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractCollectionAssert<?, ?, S, ?> after(Set<S> set1, Set<T> set2) {
      return assertThat(set1).hasSameElementsAs(set2);
    }
  }

  //
  // Multiset
  //

  static final class AssertThatMultisetsAreEqual<S, T extends S> {
    @BeforeTemplate
    AbstractCollectionAssert<?, ?, S, ?> before(Multiset<S> multiset1, Multiset<T> multiset2) {
      return assertThat(multiset1).isEqualTo(multiset2);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractCollectionAssert<?, ?, S, ?> after(Multiset<S> multiset1, Multiset<T> multiset2) {
      return assertThat(multiset1).containsExactlyInAnyOrderElementsOf(multiset2);
    }
  }

  //
  // Map
  //

  // XXX: To match in all cases there'll need to be a `@BeforeTemplate` variant for each
  // `assertThat` overload. Consider defining a `BugChecker` instead.
  static final class AssertThatMapContainsEntry<K, V> {
    @BeforeTemplate
    ObjectAssert<?> before(Map<K, V> map, K key, V value) {
      return assertThat(map.get(key)).isEqualTo(value);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> map, K key, V value) {
      return assertThat(map).containsEntry(key, value);
    }
  }

  //
  // Stream
  //

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsAnyElementsOf<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).containsAnyElementsOf(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before2(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsAnyElementsOf(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, Iterable<U> iterable) {
      return assertThat(stream).containsAnyElementsOf(iterable);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsAnyOf<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> stream, U[] array, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).containsAnyOf(array);
    }

    @BeforeTemplate
    ListAssert<T> before2(
        Stream<S> stream, U[] array, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsAnyOf(array);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, U[] array) {
      return assertThat(stream).containsAnyOf(array);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsAnyOfVarArgs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamContainsAnyOf" /* Varargs converted to array. */)
    IterableAssert<T> before(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).containsAnyOf(Refaster.asVarargs(elements));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamContainsAnyOf" /* Varargs converted to array. */)
    ListAssert<T> before2(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsAnyOf(Refaster.asVarargs(elements));
    }

    @AfterTemplate
    @SuppressWarnings("ObjectEnumerableContainsOneElement" /* Not a true singleton. */)
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, @Repeated U elements) {
      return assertThat(stream).containsAnyOf(elements);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsAll<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).containsAll(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before2(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsAll(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, Iterable<U> iterable) {
      return assertThat(stream).containsAll(iterable);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContains<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> stream, U[] array, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).contains(array);
    }

    @BeforeTemplate
    ListAssert<T> before2(
        Stream<S> stream, U[] array, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).contains(array);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, U[] array) {
      return assertThat(stream).contains(array);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsVarArgs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamContains" /* Varargs converted to array. */)
    IterableAssert<T> before(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).contains(Refaster.asVarargs(elements));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamContains" /* Varargs converted to array. */)
    ListAssert<T> before2(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).contains(Refaster.asVarargs(elements));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, @Repeated U elements) {
      return assertThat(stream).contains(elements);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsExactlyElementsOf<S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsExactlyElementsOf(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, Iterable<U> iterable) {
      return assertThat(stream).containsExactlyElementsOf(iterable);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsExactly<S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> stream, U[] array, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsExactly(array);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, U[] array) {
      return assertThat(stream).containsExactly(array);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsExactlyVarargs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamContainsExactly" /* Varargs converted to array. */)
    ListAssert<T> before(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsExactly(Refaster.asVarargs(elements));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, @Repeated U elements) {
      return assertThat(stream).containsExactly(elements);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsExactlyInAnyOrderElementsOf<
      S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsExactlyInAnyOrderElementsOf(iterable);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, ?, T, ?> before2(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends Multiset<T>> collector) {
      return assertThat(stream.collect(collector)).containsExactlyInAnyOrderElementsOf(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, Iterable<U> iterable) {
      return assertThat(stream).containsExactlyInAnyOrderElementsOf(iterable);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsExactlyInAnyOrder<S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> stream, U[] array, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsExactlyInAnyOrder(array);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, ?, T, ?> before2(
        Stream<S> stream, U[] array, Collector<S, ?, ? extends Multiset<T>> collector) {
      return assertThat(stream.collect(collector)).containsExactlyInAnyOrder(array);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, U[] array) {
      return assertThat(stream).containsExactlyInAnyOrder(array);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsExactlyInAnyOrderVarArgs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamContainsExactlyInAnyOrder" /* Varargs converted to array. */)
    ListAssert<T> before(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector))
          .containsExactlyInAnyOrder(Refaster.asVarargs(elements));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamContainsExactlyInAnyOrder" /* Varargs converted to array. */)
    AbstractCollectionAssert<?, ?, T, ?> before2(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends Multiset<T>> collector) {
      return assertThat(stream.collect(collector))
          .containsExactlyInAnyOrder(Refaster.asVarargs(elements));
    }

    @AfterTemplate
    @SuppressWarnings("ObjectEnumerableContainsExactlyOneElement" /* Not a true singleton. */)
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, @Repeated U elements) {
      return assertThat(stream).containsExactlyInAnyOrder(elements);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsSequence<S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsSequence(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> stream, U[] iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsSequence(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, Iterable<U> iterable) {
      return assertThat(stream).containsSequence(iterable);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsSequenceVarArgs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamContainsSequence" /* Varargs converted to array. */)
    ListAssert<T> before(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsSequence(Refaster.asVarargs(elements));
    }

    @AfterTemplate
    @SuppressWarnings("ObjectEnumerableContainsOneElement" /* Not a true singleton. */)
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, @Repeated U elements) {
      return assertThat(stream).containsSequence(elements);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsSubsequence<S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsSubsequence(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> stream, U[] iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsSubsequence(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, Iterable<U> iterable) {
      return assertThat(stream).containsSubsequence(iterable);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsSubsequenceVarArgs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamContainsSubsequence" /* Varargs converted to array. */)
    ListAssert<T> before(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector))
          .containsSubsequence(Refaster.asVarargs(elements));
    }

    @AfterTemplate
    @SuppressWarnings("ObjectEnumerableContainsOneElement" /* Not a true singleton. */)
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, @Repeated U elements) {
      return assertThat(stream).containsSubsequence(elements);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamDoesNotContainAnyElementsOf<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).doesNotContainAnyElementsOf(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before2(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).doesNotContainAnyElementsOf(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, Iterable<U> iterable) {
      return assertThat(stream).doesNotContainAnyElementsOf(iterable);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamDoesNotContain<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> stream, U[] array, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).doesNotContain(array);
    }

    @BeforeTemplate
    ListAssert<T> before2(
        Stream<S> stream, U[] array, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).doesNotContain(array);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, U[] array) {
      return assertThat(stream).doesNotContain(array);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamDoesNotContainVarArgs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamDoesNotContain" /* Varargs converted to array. */)
    IterableAssert<T> before(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).doesNotContain(Refaster.asVarargs(elements));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamDoesNotContain" /* Varargs converted to array. */)
    ListAssert<T> before2(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).doesNotContain(Refaster.asVarargs(elements));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, @Repeated U elements) {
      return assertThat(stream).doesNotContain(elements);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamDoesNotContainSequence<S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).doesNotContainSequence(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> stream, U[] iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).doesNotContainSequence(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, Iterable<U> iterable) {
      return assertThat(stream).doesNotContainSequence(iterable);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamDoesNotContainSequenceVarArgs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamDoesNotContainSequence" /* Varargs converted to array. */)
    ListAssert<T> before(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector))
          .doesNotContainSequence(Refaster.asVarargs(elements));
    }

    @AfterTemplate
    @SuppressWarnings("ObjectEnumerableDoesNotContainOneElement" /* Not a true singleton. */)
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, @Repeated U elements) {
      return assertThat(stream).doesNotContainSequence(elements);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamHasSameElementsAs<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).hasSameElementsAs(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before2(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).hasSameElementsAs(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, Iterable<U> iterable) {
      return assertThat(stream).hasSameElementsAs(iterable);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsOnly<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> stream, U[] array, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).containsOnly(array);
    }

    @BeforeTemplate
    ListAssert<T> before2(
        Stream<S> stream, U[] array, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsOnly(array);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, U[] array) {
      return assertThat(stream).containsOnly(array);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamContainsOnlyVarArgs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamContainsOnly" /* Varargs converted to array. */)
    IterableAssert<T> before(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).containsOnly(Refaster.asVarargs(elements));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamContainsOnly" /* Varargs converted to array. */)
    ListAssert<T> before2(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).containsOnly(Refaster.asVarargs(elements));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, @Repeated U elements) {
      return assertThat(stream).containsOnly(elements);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamIsSubsetOf<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).isSubsetOf(iterable);
    }

    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> stream, U[] iterable, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).isSubsetOf(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before2(
        Stream<S> stream, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).isSubsetOf(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before2(
        Stream<S> stream, U[] iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).isSubsetOf(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, U[] iterable) {
      return assertThat(stream).isSubsetOf(iterable);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamIsSubsetOfVarArgs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamIsSubsetOf" /* Varargs converted to array. */)
    IterableAssert<T> before(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(stream.collect(collector)).isSubsetOf(Refaster.asVarargs(elements));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatStreamIsSubsetOf" /* Varargs converted to array. */)
    ListAssert<T> before2(
        Stream<S> stream, @Repeated U elements, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(stream.collect(collector)).isSubsetOf(Refaster.asVarargs(elements));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> stream, @Repeated U elements) {
      return assertThat(stream).isSubsetOf(elements);
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamIsEmpty<S, T extends S> {
    @BeforeTemplate
    void before(Stream<S> stream, Collector<S, ?, ? extends Iterable<T>> collector) {
      assertThat(stream.collect(collector)).isEmpty();
    }

    @BeforeTemplate
    void before2(Stream<S> stream, Collector<S, ?, ? extends List<T>> collector) {
      assertThat(stream.collect(collector)).isEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<S> stream) {
      assertThat(stream).isEmpty();
    }
  }

  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  static final class AssertThatStreamIsNotEmpty<S, T extends S> {
    @BeforeTemplate
    void before(Stream<S> stream, Collector<S, ?, ? extends Iterable<T>> collector) {
      assertThat(stream.collect(collector)).isNotEmpty();
    }

    @BeforeTemplate
    void before2(Stream<S> stream, Collector<S, ?, ? extends List<T>> collector) {
      assertThat(stream.collect(collector)).isNotEmpty();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<S> stream) {
      assertThat(stream).isNotEmpty();
    }
  }

  static final class AssertThatStreamHasSize<T> {
    @BeforeTemplate
    void before(Stream<T> stream, int size) {
      assertThat(stream.count()).isEqualTo(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Stream<T> stream, int size) {
      assertThat(stream).hasSize(size);
    }
  }

  //
  // Predicate
  //

  static final class AssertThatPredicateAccepts<T> {
    @BeforeTemplate
    void before(Predicate<T> predicate, T object) {
      assertThat(predicate.test(object)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Predicate<T> predicate, T object) {
      assertThat(predicate).accepts(object);
    }
  }

  static final class AssertThatPredicateRejects<T> {
    @BeforeTemplate
    void before(Predicate<T> predicate, T object) {
      assertThat(predicate.test(object)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Predicate<T> predicate, T object) {
      assertThat(predicate).rejects(object);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////
  // Organize the code below.

  // XXX: Do the "single Comparable" match shown below.
  //    static final class AssertThatOnlyComparableElementIsEqualTo<E extends Comparable<? super E>>
  // {
  //        @BeforeTemplate
  //        AbstractAssert<?, ?> before(Iterable<E> iterable, E expected) {
  //            return assertThat(iterable)
  //                    .isEqualTo(
  //                            Refaster.<Object>anyOf(
  //                                    ImmutableSortedSet.of(expected),
  //                                    ImmutableSortedMultiset.of(expected)));
  //        }
  //
  //        @AfterTemplate
  //        @UseImportPolicy(STATIC_IMPORT_ALWAYS)
  //        IterableAssert<E> after(Iterable<E> iterable, E expected) {
  //            return assertThat(iterable).containsExactly(expected);
  //        }
  //    }
  //
}
