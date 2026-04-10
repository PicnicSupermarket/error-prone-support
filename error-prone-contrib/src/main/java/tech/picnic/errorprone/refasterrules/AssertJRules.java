package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Multiset;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
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
import org.assertj.core.api.AbstractCollectionAssert;
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
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;
import tech.picnic.errorprone.refaster.matchers.IsArray;

/** Refaster rules related to AssertJ expressions and statements. */
// XXX: `assertThat(cmp.compare(a, b)).isZero()` -> make something nicer.
// ^ And variants.
// XXX: Consider splitting this class into multiple classes.
// XXX: Some of these rules may not apply given the updated TestNG rewrite rules. Review.
// XXX: For the rules that "unwrap" explicitly enumerated collections, also introduce variants
// with explicitly enumerated sorted collections. (Requires that the type bound is Comparable.)
// XXX: Handle `.isEqualTo(explicitlyEnumeratedCollection)`. Can be considered equivalent to
// `.containsOnly(elements)`. (This does mean the auto-generated code needs to be more advanced.
// Ponder this.)
// XXX: Elsewhere add a rule to disallow `Collection.emptyList()` and variants as well as
// `Arrays.asList()` and `Arrays.asList(singleElement)`, maybe other obviously-varargs cases.
// XXX: Can we better handle Multimaps?
// XXX: For the stream overloads, there are also other assertions we can simplify.
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
// XXX: Cover all cases listed by https://rules.sonarsource.com/java/RSPEC-5838/
// XXX: For `E extends Comparable<? super E>`, rewrite
// `assertThat(iterable).isEqualTo(Refaster.<Object>anyOf(ImmutableSortedSet.of(expected),
// ImmutableSortedMultiset.of(expected)))` to `assertThat(iterable).containsExactly(expected)`.
@OnlineDocumentation
final class AssertJRules {
  private AssertJRules() {}

  //
  // OptionalDouble
  //

  /** Prefer {@link OptionalDoubleAssert#hasValue(double)} over more fragile alternatives. */
  // XXX: There are several other variations that can also be optimized so as to avoid
  // unconditionally calling `getAsDouble`.
  @PossibleSourceIncompatibility
  static final class AssertThatHasValueOptionalDouble {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(OptionalDouble actual, double expectedValue) {
      return assertThat(actual.getAsDouble()).isEqualTo(expectedValue);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    OptionalDoubleAssert after(OptionalDouble actual, double expectedValue) {
      return assertThat(actual).hasValue(expectedValue);
    }
  }

  //
  // OptionalInt
  //

  /** Prefer {@link OptionalIntAssert#hasValue(int)} over more fragile alternatives. */
  // XXX: There are several other variations that can also be optimized so as to avoid
  // unconditionally calling `getAsInt`.
  @PossibleSourceIncompatibility
  static final class AssertThatHasValueOptionalInt {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(OptionalInt actual, int expectedValue) {
      return assertThat(actual.getAsInt()).isEqualTo(expectedValue);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    OptionalIntAssert after(OptionalInt actual, int expectedValue) {
      return assertThat(actual).hasValue(expectedValue);
    }
  }

  //
  // OptionalLong
  //

  /** Prefer {@link OptionalLongAssert#hasValue(long)} over more fragile alternatives. */
  // XXX: There are several other variations that can also be optimized so as to avoid
  // unconditionally calling `getAsLong`.
  @PossibleSourceIncompatibility
  static final class AssertThatHasValueOptionalLong {
    @BeforeTemplate
    AbstractLongAssert<?> before(OptionalLong actual, long expectedValue) {
      return assertThat(actual.getAsLong()).isEqualTo(expectedValue);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    OptionalLongAssert after(OptionalLong actual, long expectedValue) {
      return assertThat(actual).hasValue(expectedValue);
    }
  }

  //
  // ObjectEnumerable
  //

  /** Prefer {@link ObjectEnumerableAssert#contains(Object[])} over more contrived alternatives. */
  static final class ObjectEnumerableAssertContains<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    ObjectEnumerableAssert<?, S> before(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return Refaster.anyOf(
          iterAssert.containsAnyOf(element),
          iterAssert.containsSequence(element),
          iterAssert.containsSubsequence(element));
    }

    @AfterTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    ObjectEnumerableAssert<?, S> after(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return iterAssert.contains(element);
    }
  }

  /**
   * Prefer {@link ObjectEnumerableAssert#doesNotContain(Object[])} over more contrived
   * alternatives.
   */
  static final class ObjectEnumerableAssertDoesNotContain<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    ObjectEnumerableAssert<?, S> before(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return iterAssert.doesNotContainSequence(element);
    }

    @AfterTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    ObjectEnumerableAssert<?, S> after(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return iterAssert.doesNotContain(element);
    }
  }

  /**
   * Prefer {@link ObjectEnumerableAssert#containsExactly(Object[])} over more contrived
   * alternatives.
   */
  static final class ObjectEnumerableAssertContainsExactly<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    ObjectEnumerableAssert<?, S> before(
        ObjectEnumerableAssert<?, S> iterAssert, @NotMatches(IsArray.class) T element) {
      return iterAssert.containsExactlyInAnyOrder(element);
    }

    @AfterTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    ObjectEnumerableAssert<?, S> after(ObjectEnumerableAssert<?, S> iterAssert, T element) {
      return iterAssert.containsExactly(element);
    }
  }

  /** Prefer {@code assertThat(set).containsExactly(element)} over less explicit alternatives. */
  static final class AssertThatContainsExactlySet<S, T extends S> {
    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends S>, S, ObjectAssert<S>> before(
        Set<S> actual, T element) {
      return assertThat(actual).containsOnly(element);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractCollectionAssert<?, Collection<? extends S>, S, ObjectAssert<S>> after(
        Set<S> actual, T element) {
      return assertThat(actual).containsExactly(element);
    }
  }

  //
  // List
  //

  /**
   * Prefer {@link ListAssert#containsExactlyElementsOf(Iterable)} over less explicit alternatives.
   */
  static final class AssertThatContainsExactlyElementsOfList<S, T extends S> {
    @BeforeTemplate
    ListAssert<S> before(List<S> actual, Iterable<T> iterable) {
      return assertThat(actual).isEqualTo(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(List<S> actual, Iterable<T> iterable) {
      return assertThat(actual).containsExactlyElementsOf(iterable);
    }
  }

  //
  // Set
  //

  /**
   * Prefer {@link AbstractCollectionAssert#hasSameElementsAs(Iterable)} over less explicit or more
   * contrived alternatives.
   */
  static final class AssertThatHasSameElementsAsSet<S, T extends S> {
    @BeforeTemplate
    AbstractCollectionAssert<
            ? extends AbstractCollectionAssert<?, Collection<? extends S>, S, ObjectAssert<S>>,
            Collection<? extends S>,
            S,
            ObjectAssert<S>>
        before(Set<S> actual, Iterable<T> iterable) {
      return Refaster.anyOf(
          assertThat(actual).isEqualTo(iterable),
          assertThat(actual).containsExactlyInAnyOrderElementsOf(iterable));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractCollectionAssert<?, Collection<? extends S>, S, ObjectAssert<S>> after(
        Set<S> actual, Iterable<T> iterable) {
      return assertThat(actual).hasSameElementsAs(iterable);
    }
  }

  //
  // Multiset
  //

  /**
   * Prefer {@link AbstractCollectionAssert#containsExactlyInAnyOrderElementsOf(Iterable)} over less
   * explicit alternatives.
   */
  static final class AssertThatContainsExactlyInAnyOrderElementsOfMultiset<S, T extends S> {
    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends S>, S, ObjectAssert<S>> before(
        Multiset<S> actual, Iterable<T> values) {
      return assertThat(actual).isEqualTo(values);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractCollectionAssert<?, Collection<? extends S>, S, ObjectAssert<S>> after(
        Multiset<S> actual, Iterable<T> values) {
      return assertThat(actual).containsExactlyInAnyOrderElementsOf(values);
    }
  }

  //
  // Map
  //

  /** Prefer {@link MapAssert#containsEntry(Object, Object)} over more contrived alternatives. */
  // XXX: To match in all cases there'll need to be a `@BeforeTemplate` variant for each
  // `assertThat` overload. Consider defining a `BugChecker` instead.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsEntry<K, V> {
    @BeforeTemplate
    ObjectAssert<V> before(Map<K, V> actual, K key, V value) {
      return assertThat(actual.get(key)).isEqualTo(value);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    MapAssert<K, V> after(Map<K, V> actual, K key, V value) {
      return assertThat(actual).containsEntry(key, value);
    }
  }

  //
  // Stream
  //

  /** Prefer {@link ListAssert#containsAnyElementsOf(Iterable)} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsAnyElementsOf<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> actual, Iterable<U> iterable, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).containsAnyElementsOf(iterable);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual,
        Iterable<U> iterable,
        Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).containsAnyElementsOf(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before3(
        Stream<S> actual, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsAnyElementsOf(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, Iterable<U> iterable) {
      return assertThat(actual).containsAnyElementsOf(iterable);
    }
  }

  /** Prefer {@link ListAssert#containsAnyOf(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsAnyOf<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).containsAnyOf(array);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).containsAnyOf(array);
    }

    @BeforeTemplate
    ListAssert<T> before3(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsAnyOf(array);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, U[] array) {
      return assertThat(actual).containsAnyOf(array);
    }
  }

  /** Prefer {@link ListAssert#containsAnyOf(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsAnyOfVarargs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatContainsAnyOf" /* Varargs converted to array. */)
    IterableAssert<T> before(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).containsAnyOf(Refaster.asVarargs(values));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatContainsAnyOf" /* Varargs converted to array. */)
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).containsAnyOf(Refaster.asVarargs(values));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatContainsAnyOf" /* Varargs converted to array. */)
    ListAssert<T> before3(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsAnyOf(Refaster.asVarargs(values));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, @Repeated U values) {
      return assertThat(actual).containsAnyOf(Refaster.asVarargs(values));
    }
  }

  /** Prefer {@link ListAssert#containsAll(Iterable)} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsAll<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> actual, Iterable<U> iterable, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).containsAll(iterable);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual,
        Iterable<U> iterable,
        Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).containsAll(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before3(
        Stream<S> actual, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsAll(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, Iterable<U> iterable) {
      return assertThat(actual).containsAll(iterable);
    }
  }

  /** Prefer {@link ListAssert#contains(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContains<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).contains(array);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).contains(array);
    }

    @BeforeTemplate
    ListAssert<T> before3(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).contains(array);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, U[] array) {
      return assertThat(actual).contains(array);
    }
  }

  /** Prefer {@link ListAssert#contains(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsVarargs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatContains" /* Varargs converted to array. */)
    IterableAssert<T> before(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).contains(Refaster.asVarargs(values));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatContains" /* Varargs converted to array. */)
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).contains(Refaster.asVarargs(values));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatContains" /* Varargs converted to array. */)
    ListAssert<T> before3(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).contains(Refaster.asVarargs(values));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, @Repeated U values) {
      return assertThat(actual).contains(Refaster.asVarargs(values));
    }
  }

  /**
   * Prefer {@link ListAssert#containsExactlyElementsOf(Iterable)} over less efficient alternatives.
   */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsExactlyElementsOfStream<S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> actual, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsExactlyElementsOf(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, Iterable<U> iterable) {
      return assertThat(actual).containsExactlyElementsOf(iterable);
    }
  }

  /** Prefer {@link ListAssert#containsExactly(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsExactlyStream<S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsExactly(array);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, U[] array) {
      return assertThat(actual).containsExactly(array);
    }
  }

  /** Prefer {@link ListAssert#containsExactly(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsExactlyVarargs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatContainsExactlyStream" /* Varargs converted to array. */)
    ListAssert<T> before(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsExactly(Refaster.asVarargs(values));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, @Repeated U values) {
      return assertThat(actual).containsExactly(Refaster.asVarargs(values));
    }
  }

  /**
   * Prefer {@link ListAssert#containsExactlyInAnyOrderElementsOf(Iterable)} over less efficient
   * alternatives.
   */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsExactlyInAnyOrderElementsOfStream<
      S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> actual, Iterable<U> values, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsExactlyInAnyOrderElementsOf(values);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, Iterable<U> values, Collector<S, ?, ? extends Multiset<T>> collector) {
      return assertThat(actual.collect(collector)).containsExactlyInAnyOrderElementsOf(values);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, Iterable<U> values) {
      return assertThat(actual).containsExactlyInAnyOrderElementsOf(values);
    }
  }

  /**
   * Prefer {@link ListAssert#containsExactlyInAnyOrder(Object[])} over less efficient alternatives.
   */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsExactlyInAnyOrder<S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsExactlyInAnyOrder(array);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends Multiset<T>> collector) {
      return assertThat(actual.collect(collector)).containsExactlyInAnyOrder(array);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, U[] array) {
      return assertThat(actual).containsExactlyInAnyOrder(array);
    }
  }

  /**
   * Prefer {@link ListAssert#containsExactlyInAnyOrder(Object[])} over less efficient alternatives.
   */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsExactlyInAnyOrderVarargs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatContainsExactlyInAnyOrder" /* Varargs converted to array. */)
    ListAssert<T> before(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector))
          .containsExactlyInAnyOrder(Refaster.asVarargs(values));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatContainsExactlyInAnyOrder" /* Varargs converted to array. */)
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends Multiset<T>> collector) {
      return assertThat(actual.collect(collector))
          .containsExactlyInAnyOrder(Refaster.asVarargs(values));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, @Repeated U values) {
      return assertThat(actual).containsExactlyInAnyOrder(Refaster.asVarargs(values));
    }
  }

  /** Prefer {@link ListAssert#containsSequence(Iterable)} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsSequence<S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> actual, Iterable<U> sequence, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsSequence(sequence);
    }

    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> actual, U[] sequence, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsSequence(sequence);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, Iterable<U> sequence) {
      return assertThat(actual).containsSequence(sequence);
    }
  }

  /** Prefer {@link ListAssert#containsSequence(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsSequenceVarargs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatContainsSequence" /* Varargs converted to array. */)
    ListAssert<T> before(
        Stream<S> actual, @Repeated U sequence, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsSequence(Refaster.asVarargs(sequence));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, @Repeated U sequence) {
      return assertThat(actual).containsSequence(Refaster.asVarargs(sequence));
    }
  }

  /** Prefer {@link ListAssert#containsSubsequence(Iterable)} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsSubsequence<S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> actual, Iterable<U> subsequence, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsSubsequence(subsequence);
    }

    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> actual, U[] subsequence, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsSubsequence(subsequence);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, Iterable<U> subsequence) {
      return assertThat(actual).containsSubsequence(subsequence);
    }
  }

  /** Prefer {@link ListAssert#containsSubsequence(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsSubsequenceVarargs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatContainsSubsequence" /* Varargs converted to array. */)
    ListAssert<T> before(
        Stream<S> actual, @Repeated U subsequence, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector))
          .containsSubsequence(Refaster.asVarargs(subsequence));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, @Repeated U subsequence) {
      return assertThat(actual).containsSubsequence(Refaster.asVarargs(subsequence));
    }
  }

  /**
   * Prefer {@link ListAssert#doesNotContainAnyElementsOf(Iterable)} over less efficient
   * alternatives.
   */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatDoesNotContainAnyElementsOf<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> actual, Iterable<U> iterable, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).doesNotContainAnyElementsOf(iterable);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual,
        Iterable<U> iterable,
        Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).doesNotContainAnyElementsOf(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before3(
        Stream<S> actual, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).doesNotContainAnyElementsOf(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, Iterable<U> iterable) {
      return assertThat(actual).doesNotContainAnyElementsOf(iterable);
    }
  }

  /** Prefer {@link ListAssert#doesNotContain(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatDoesNotContain<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).doesNotContain(array);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).doesNotContain(array);
    }

    @BeforeTemplate
    ListAssert<T> before3(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).doesNotContain(array);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, U[] array) {
      return assertThat(actual).doesNotContain(array);
    }
  }

  /** Prefer {@link ListAssert#doesNotContain(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatDoesNotContainVarargs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatDoesNotContain" /* Varargs converted to array. */)
    IterableAssert<T> before(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).doesNotContain(Refaster.asVarargs(values));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatDoesNotContain" /* Varargs converted to array. */)
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).doesNotContain(Refaster.asVarargs(values));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatDoesNotContain" /* Varargs converted to array. */)
    ListAssert<T> before3(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).doesNotContain(Refaster.asVarargs(values));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, @Repeated U values) {
      return assertThat(actual).doesNotContain(Refaster.asVarargs(values));
    }
  }

  /**
   * Prefer {@link ListAssert#doesNotContainSequence(Iterable)} over less efficient alternatives.
   */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatDoesNotContainSequence<S, T extends S, U extends T> {
    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> actual, Iterable<U> sequence, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).doesNotContainSequence(sequence);
    }

    @BeforeTemplate
    ListAssert<T> before(
        Stream<S> actual, U[] sequence, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).doesNotContainSequence(sequence);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, Iterable<U> sequence) {
      return assertThat(actual).doesNotContainSequence(sequence);
    }
  }

  /**
   * Prefer {@link ListAssert#doesNotContainSequence(Object[])} over less efficient alternatives.
   */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatDoesNotContainSequenceVarargs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatDoesNotContainSequence" /* Varargs converted to array. */)
    ListAssert<T> before(
        Stream<S> actual, @Repeated U sequence, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector))
          .doesNotContainSequence(Refaster.asVarargs(sequence));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, @Repeated U sequence) {
      return assertThat(actual).doesNotContainSequence(Refaster.asVarargs(sequence));
    }
  }

  /** Prefer {@link ListAssert#hasSameElementsAs(Iterable)} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatHasSameElementsAsStream<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> actual, Iterable<U> iterable, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).hasSameElementsAs(iterable);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual,
        Iterable<U> iterable,
        Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).hasSameElementsAs(iterable);
    }

    @BeforeTemplate
    ListAssert<T> before3(
        Stream<S> actual, Iterable<U> iterable, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).hasSameElementsAs(iterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, Iterable<U> iterable) {
      return assertThat(actual).hasSameElementsAs(iterable);
    }
  }

  /** Prefer {@link ListAssert#containsOnly(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsOnly<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).containsOnly(array);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).containsOnly(array);
    }

    @BeforeTemplate
    ListAssert<T> before3(
        Stream<S> actual, U[] array, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsOnly(array);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, U[] array) {
      return assertThat(actual).containsOnly(array);
    }
  }

  /** Prefer {@link ListAssert#containsOnly(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatContainsOnlyVarargs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatContainsOnly" /* Varargs converted to array. */)
    IterableAssert<T> before(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).containsOnly(Refaster.asVarargs(values));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatContainsOnly" /* Varargs converted to array. */)
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).containsOnly(Refaster.asVarargs(values));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatContainsOnly" /* Varargs converted to array. */)
    ListAssert<T> before3(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).containsOnly(Refaster.asVarargs(values));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, @Repeated U values) {
      return assertThat(actual).containsOnly(Refaster.asVarargs(values));
    }
  }

  /** Prefer {@link ListAssert#isSubsetOf(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatIsSubsetOf<S, T extends S, U extends T> {
    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> actual, Iterable<U> values, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).isSubsetOf(values);
    }

    @BeforeTemplate
    IterableAssert<T> before(
        Stream<S> actual, U[] values, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).isSubsetOf(values);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, Iterable<U> values, Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).isSubsetOf(values);
    }

    @BeforeTemplate
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, U[] values, Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).isSubsetOf(values);
    }

    @BeforeTemplate
    ListAssert<T> before3(
        Stream<S> actual, Iterable<U> values, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).isSubsetOf(values);
    }

    @BeforeTemplate
    ListAssert<T> before3(
        Stream<S> actual, U[] values, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).isSubsetOf(values);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, U[] values) {
      return assertThat(actual).isSubsetOf(values);
    }
  }

  /** Prefer {@link ListAssert#isSubsetOf(Object[])} over less efficient alternatives. */
  // XXX: This rule assumes the `collector` doesn't completely discard certain values.
  @PossibleSourceIncompatibility
  static final class AssertThatIsSubsetOfVarargs<S, T extends S, U extends T> {
    @BeforeTemplate
    @SuppressWarnings("AssertThatIsSubsetOf" /* Varargs converted to array. */)
    IterableAssert<T> before(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends Iterable<T>> collector) {
      return assertThat(actual.collect(collector)).isSubsetOf(Refaster.asVarargs(values));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatIsSubsetOf" /* Varargs converted to array. */)
    AbstractCollectionAssert<?, Collection<? extends T>, T, ObjectAssert<T>> before2(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends Collection<T>> collector) {
      return assertThat(actual.collect(collector)).isSubsetOf(Refaster.asVarargs(values));
    }

    @BeforeTemplate
    @SuppressWarnings("AssertThatIsSubsetOf" /* Varargs converted to array. */)
    ListAssert<T> before3(
        Stream<S> actual, @Repeated U values, Collector<S, ?, ? extends List<T>> collector) {
      return assertThat(actual.collect(collector)).isSubsetOf(Refaster.asVarargs(values));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ListAssert<S> after(Stream<S> actual, @Repeated U values) {
      return assertThat(actual).isSubsetOf(Refaster.asVarargs(values));
    }
  }

  //
  // Predicate
  //

  /** Prefer {@code assertThat(predicate).accepts(object)} over less explicit alternatives. */
  static final class AssertThatAccepts<T> {
    @BeforeTemplate
    void before(Predicate<T> actual, T object) {
      assertThat(actual.test(object)).isTrue();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Predicate<T> actual, T object) {
      assertThat(actual).accepts(object);
    }
  }

  /** Prefer {@code assertThat(predicate).rejects(object)} over less explicit alternatives. */
  static final class AssertThatRejects<T> {
    @BeforeTemplate
    void before(Predicate<T> actual, T object) {
      assertThat(actual.test(object)).isFalse();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(Predicate<T> actual, T object) {
      assertThat(actual).rejects(object);
    }
  }
}
