package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.Comparators.greatest;
import static com.google.common.collect.Comparators.least;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.comparingLong;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.minBy;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Matches;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.matchers.IsIdentityOperation;

/** Refaster rules related to expressions dealing with {@link Comparator}s. */
@OnlineDocumentation
final class ComparatorRules {
  private ComparatorRules() {}

  /**
   * Prefer {@link Comparator#naturalOrder()} over less explicit, more verbose, or more contrived
   * alternatives.
   */
  static final class NaturalOrder<T extends Comparable<? super T>, U extends T> {
    // XXX: Ideally `? super T` would also be replaced by a class-level type parameter, but Java
    // does not allow a type variable to be followed by other bounds.
    @BeforeTemplate
    Comparator<T> before(@Matches(IsIdentityOperation.class) Function<? super T, U> keyExtractor) {
      return Refaster.anyOf(
          T::compareTo,
          comparing(keyExtractor),
          Collections.<T>reverseOrder(reverseOrder()),
          Comparator.<T>reverseOrder().reversed());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<T> after() {
      return naturalOrder();
    }
  }

  /**
   * Prefer {@link Comparator#reverseOrder()} over less explicit, more verbose, or more contrived
   * alternatives.
   */
  static final class ReverseOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    Comparator<T> before() {
      return Refaster.anyOf(
          Collections.reverseOrder(),
          Collections.<T>reverseOrder(naturalOrder()),
          Comparator.<T>naturalOrder().reversed());
    }

    // XXX: Add `@UseImportPolicy(STATIC_IMPORT_ALWAYS)` if/when
    // https://github.com/google/error-prone/pull/3584 is merged and released.
    @AfterTemplate
    Comparator<T> after() {
      return reverseOrder();
    }
  }

  /** Prefer using the {@link Comparator} as-is over more contrived alternatives. */
  static final class ComparatorIdentity<S, T extends S, U extends T> {
    @BeforeTemplate
    Comparator<T> before(
        Comparator<T> keyComparator,
        @Matches(IsIdentityOperation.class) Function<S, U> keyExtractor) {
      return comparing(keyExtractor, keyComparator);
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<T> after(Comparator<T> keyComparator) {
      return keyComparator;
    }
  }

  /** Prefer {@link Comparator#comparing(Function)} over less explicit alternatives. */
  abstract static class Comparing<E extends Enum<E>, T> {
    @Placeholder(allowsIdentity = true)
    abstract E toEnumFunction(@MayOptionallyUse T value);

    @BeforeTemplate
    @SuppressWarnings("EnumOrdinal" /* This violation will be rewritten. */)
    Comparator<T> before() {
      return comparingInt(v -> toEnumFunction(v).ordinal());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<T> after() {
      return comparing(v -> toEnumFunction(v));
    }
  }

  /** Prefer {@link Comparator#thenComparing(Function)} over more verbose alternatives. */
  static final class ComparatorThenComparing<
      R, S extends R, T extends Comparable<? super T>, U extends T> {
    @BeforeTemplate
    Comparator<S> before(Comparator<S> cmp, Function<R, U> keyExtractor) {
      return cmp.thenComparing(comparing(keyExtractor));
    }

    @AfterTemplate
    Comparator<S> after(Comparator<S> cmp, Function<R, U> keyExtractor) {
      return cmp.thenComparing(keyExtractor);
    }
  }

  /**
   * Prefer {@link Comparator#thenComparing(Function, Comparator)} over more verbose alternatives.
   */
  static final class ComparatorThenComparingReverseOrder<
      R, S extends R, T extends Comparable<? super T>, U extends T> {
    @BeforeTemplate
    Comparator<S> before(Comparator<S> cmp, Function<R, U> keyExtractor) {
      return cmp.thenComparing(comparing(keyExtractor).reversed());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<S> after(Comparator<S> cmp, Function<R, U> keyExtractor) {
      return cmp.thenComparing(keyExtractor, reverseOrder());
    }
  }

  /**
   * Prefer {@link Comparator#thenComparing(Function, Comparator)} over more verbose alternatives.
   */
  static final class ComparatorThenComparingWithComparator<R, S extends R, V, U extends V> {
    @BeforeTemplate
    Comparator<S> before(
        Comparator<S> cmp, Function<R, U> keyExtractor, Comparator<V> keyComparator) {
      return cmp.thenComparing(comparing(keyExtractor, keyComparator));
    }

    @AfterTemplate
    Comparator<S> after(
        Comparator<S> cmp, Function<R, U> keyExtractor, Comparator<V> keyComparator) {
      return cmp.thenComparing(keyExtractor, keyComparator);
    }
  }

  /**
   * Prefer {@link Comparator#thenComparing(Function, Comparator)} over more contrived alternatives.
   */
  static final class ComparatorThenComparingComparatorReversed<R, S extends R, V, U extends V> {
    @BeforeTemplate
    Comparator<S> before(
        Comparator<S> cmp, Function<R, U> keyExtractor, Comparator<V> keyComparator) {
      return cmp.thenComparing(comparing(keyExtractor, keyComparator).reversed());
    }

    @AfterTemplate
    Comparator<S> after(
        Comparator<S> cmp, Function<R, U> keyExtractor, Comparator<V> keyComparator) {
      return cmp.thenComparing(keyExtractor, keyComparator.reversed());
    }
  }

  /**
   * Prefer {@link Comparator#thenComparingDouble(ToDoubleFunction)} over more verbose alternatives.
   */
  static final class ComparatorThenComparingDouble<S, T extends S> {
    @BeforeTemplate
    Comparator<T> before(Comparator<T> cmp, ToDoubleFunction<S> keyExtractor) {
      return cmp.thenComparing(comparingDouble(keyExtractor));
    }

    @AfterTemplate
    Comparator<T> after(Comparator<T> cmp, ToDoubleFunction<S> keyExtractor) {
      return cmp.thenComparingDouble(keyExtractor);
    }
  }

  /** Prefer {@link Comparator#thenComparingInt(ToIntFunction)} over more verbose alternatives. */
  static final class ComparatorThenComparingInt<S, T extends S> {
    @BeforeTemplate
    Comparator<T> before(Comparator<T> cmp, ToIntFunction<S> keyExtractor) {
      return cmp.thenComparing(comparingInt(keyExtractor));
    }

    @AfterTemplate
    Comparator<T> after(Comparator<T> cmp, ToIntFunction<S> keyExtractor) {
      return cmp.thenComparingInt(keyExtractor);
    }
  }

  /** Prefer {@link Comparator#thenComparingLong(ToLongFunction)} over more verbose alternatives. */
  static final class ComparatorThenComparingLong<S, T extends S> {
    @BeforeTemplate
    Comparator<T> before(Comparator<T> cmp, ToLongFunction<S> keyExtractor) {
      return cmp.thenComparing(comparingLong(keyExtractor));
    }

    @AfterTemplate
    Comparator<T> after(Comparator<T> cmp, ToLongFunction<S> keyExtractor) {
      return cmp.thenComparingLong(keyExtractor);
    }
  }

  /** Prefer {@link Comparator#thenComparing(Comparator)} over less explicit alternatives. */
  static final class ComparatorThenComparingNaturalOrder<
      T extends Comparable<? super T>, U extends T> {
    // XXX: Ideally `? super T` would also be replaced by a class-level type parameter, but Java
    // does not allow a type variable to be followed by other bounds.
    @BeforeTemplate
    Comparator<T> before(
        Comparator<T> cmp,
        @Matches(IsIdentityOperation.class) Function<? super T, U> keyExtractor) {
      return cmp.thenComparing(keyExtractor);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<T> after(Comparator<T> cmp) {
      return cmp.thenComparing(naturalOrder());
    }
  }

  /** Prefer {@link Comparable#compareTo(Object)} over more verbose alternatives. */
  static final class ComparableCompareTo<T extends Comparable<? super T>> {
    @BeforeTemplate
    int before(T value1, T value2) {
      return Refaster.anyOf(
          Comparator.<T>naturalOrder().compare(value1, value2),
          Comparator.<T>reverseOrder().compare(value2, value1));
    }

    @AfterTemplate
    int after(T value1, T value2) {
      return value1.compareTo(value2);
    }
  }

  /** Prefer {@link Collections#sort(List)} over more verbose alternatives. */
  static final class CollectionsSort<T extends Comparable<? super T>> {
    @BeforeTemplate
    void before(List<T> list) {
      Collections.sort(list, naturalOrder());
    }

    @AfterTemplate
    void after(List<T> list) {
      Collections.sort(list);
    }
  }

  /**
   * Prefer {@link Collections#min(Collection)} over more verbose or more contrived alternatives.
   */
  static final class CollectionsMin<T extends Comparable<? super T>> {
    @BeforeTemplate
    T before(Collection<T> coll) {
      return Refaster.anyOf(
          Collections.min(coll, naturalOrder()), Collections.max(coll, reverseOrder()));
    }

    @AfterTemplate
    T after(Collection<T> coll) {
      return Collections.min(coll);
    }
  }

  /** Prefer {@link Collections#min(Collection, Comparator)} over less efficient alternatives. */
  static final class CollectionsMinArraysAsList<S, T extends S> {
    @BeforeTemplate
    T before(T[] array, Comparator<S> comp) {
      return Arrays.stream(array).min(comp).orElseThrow();
    }

    @AfterTemplate
    T after(T[] array, Comparator<S> comp) {
      return Collections.min(Arrays.asList(array), comp);
    }
  }

  /** Prefer {@link Collections#min(Collection, Comparator)} over less efficient alternatives. */
  static final class CollectionsMinWithComparator<S, T extends S> {
    @BeforeTemplate
    T before(Collection<T> coll, Comparator<S> comp) {
      return coll.stream().min(comp).orElseThrow();
    }

    @AfterTemplate
    T after(Collection<T> coll, Comparator<S> comp) {
      return Collections.min(coll, comp);
    }
  }

  /** Prefer {@link Collections#min(Collection, Comparator)} over less efficient alternatives. */
  static final class CollectionsMinArraysAsListVarargs<S, T extends S> {
    @BeforeTemplate
    T before(@Repeated T a, Comparator<S> comp) {
      return Stream.of(Refaster.asVarargs(a)).min(comp).orElseThrow();
    }

    @AfterTemplate
    T after(@Repeated T a, Comparator<S> comp) {
      return Collections.min(Arrays.asList(Refaster.asVarargs(a)), comp);
    }
  }

  /**
   * Prefer {@link Comparators#min(Comparable, Comparable)} over less efficient, more verbose, or
   * more contrived alternatives.
   */
  static final class ComparatorsMin2<T extends Comparable<? super T>> {
    @BeforeTemplate
    @SuppressWarnings("java:S1067" /* The conditional operators are independent. */)
    T before(T a, T b) {
      return Refaster.anyOf(
          a.compareTo(b) <= 0 ? a : b,
          a.compareTo(b) > 0 ? b : a,
          b.compareTo(a) < 0 ? b : a,
          b.compareTo(a) >= 0 ? a : b,
          Comparators.min(a, b, naturalOrder()),
          Comparators.max(a, b, reverseOrder()),
          Collections.min(
              Refaster.anyOf(Arrays.asList(a, b), ImmutableList.of(a, b), ImmutableSet.of(a, b))));
    }

    @AfterTemplate
    T after(T a, T b) {
      return Comparators.min(a, b);
    }
  }

  /**
   * Prefer {@link Comparators#min(Object, Object, Comparator)} over less efficient or more verbose
   * alternatives.
   */
  static final class ComparatorsMin3<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("java:S1067" /* The conditional operators are independent. */)
    T before(T a, T b, Comparator<S> comparator) {
      return Refaster.anyOf(
          comparator.compare(a, b) <= 0 ? a : b,
          comparator.compare(a, b) > 0 ? b : a,
          comparator.compare(b, a) < 0 ? b : a,
          comparator.compare(b, a) >= 0 ? a : b,
          Collections.min(
              Refaster.anyOf(Arrays.asList(a, b), ImmutableList.of(a, b), ImmutableSet.of(a, b)),
              comparator));
    }

    @AfterTemplate
    T after(T a, T b, Comparator<S> comparator) {
      return Comparators.min(a, b, comparator);
    }
  }

  /**
   * Prefer {@link Collections#max(Collection)} over more verbose or more contrived alternatives.
   */
  static final class CollectionsMax<T extends Comparable<? super T>> {
    @BeforeTemplate
    T before(Collection<T> coll) {
      return Refaster.anyOf(
          Collections.max(coll, naturalOrder()), Collections.min(coll, reverseOrder()));
    }

    @AfterTemplate
    T after(Collection<T> coll) {
      return Collections.max(coll);
    }
  }

  /** Prefer {@link Collections#max(Collection, Comparator)} over less efficient alternatives. */
  static final class CollectionsMaxArraysAsList<S, T extends S> {
    @BeforeTemplate
    T before(T[] array, Comparator<S> comp) {
      return Arrays.stream(array).max(comp).orElseThrow();
    }

    @AfterTemplate
    T after(T[] array, Comparator<S> comp) {
      return Collections.max(Arrays.asList(array), comp);
    }
  }

  /** Prefer {@link Collections#max(Collection, Comparator)} over less efficient alternatives. */
  static final class CollectionsMaxWithComparator<S, T extends S> {
    @BeforeTemplate
    T before(Collection<T> coll, Comparator<S> comp) {
      return coll.stream().max(comp).orElseThrow();
    }

    @AfterTemplate
    T after(Collection<T> coll, Comparator<S> comp) {
      return Collections.max(coll, comp);
    }
  }

  /** Prefer {@link Collections#max(Collection, Comparator)} over less efficient alternatives. */
  static final class CollectionsMaxArraysAsListVarargs<S, T extends S> {
    @BeforeTemplate
    T before(@Repeated T a, Comparator<S> comp) {
      return Stream.of(Refaster.asVarargs(a)).max(comp).orElseThrow();
    }

    @AfterTemplate
    T after(@Repeated T a, Comparator<S> comp) {
      return Collections.max(Arrays.asList(Refaster.asVarargs(a)), comp);
    }
  }

  /**
   * Prefer {@link Comparators#max(Comparable, Comparable)} over less efficient, more verbose, or
   * more contrived alternatives.
   */
  static final class ComparatorsMax2<T extends Comparable<? super T>> {
    @BeforeTemplate
    @SuppressWarnings("java:S1067" /* The conditional operators are independent. */)
    T before(T a, T b) {
      return Refaster.anyOf(
          a.compareTo(b) >= 0 ? a : b,
          a.compareTo(b) < 0 ? b : a,
          b.compareTo(a) > 0 ? b : a,
          b.compareTo(a) <= 0 ? a : b,
          Comparators.max(a, b, naturalOrder()),
          Comparators.min(a, b, reverseOrder()),
          Collections.max(
              Refaster.anyOf(Arrays.asList(a, b), ImmutableList.of(a, b), ImmutableSet.of(a, b))));
    }

    @AfterTemplate
    T after(T a, T b) {
      return Comparators.max(a, b);
    }
  }

  /**
   * Prefer {@link Comparators#max(Object, Object, Comparator)} over less efficient or more verbose
   * alternatives.
   */
  static final class ComparatorsMax3<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("java:S1067" /* The conditional operators are independent. */)
    T before(T a, T b, Comparator<S> comparator) {
      return Refaster.anyOf(
          comparator.compare(a, b) >= 0 ? a : b,
          comparator.compare(a, b) < 0 ? b : a,
          comparator.compare(b, a) > 0 ? b : a,
          comparator.compare(b, a) <= 0 ? a : b,
          Collections.max(
              Refaster.anyOf(Arrays.asList(a, b), ImmutableList.of(a, b), ImmutableSet.of(a, b)),
              comparator));
    }

    @AfterTemplate
    T after(T a, T b, Comparator<S> comparator) {
      return Comparators.max(a, b, comparator);
    }
  }

  /** Prefer {@link Comparators#least(int, Comparator)} over more contrived alternatives. */
  static final class Least<S, T extends S> {
    @BeforeTemplate
    Collector<T, ?, List<T>> before(int k, Comparator<S> comparator) {
      return greatest(k, comparator.reversed());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Collector<T, ?, List<T>> after(int k, Comparator<S> comparator) {
      return least(k, comparator);
    }
  }

  /** Prefer {@link Comparators#greatest(int, Comparator)} over more contrived alternatives. */
  static final class Greatest<S, T extends S> {
    @BeforeTemplate
    Collector<T, ?, List<T>> before(int k, Comparator<S> comparator) {
      return least(k, comparator.reversed());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Collector<T, ?, List<T>> after(int k, Comparator<S> comparator) {
      return greatest(k, comparator);
    }
  }

  /**
   * Prefer {@link Comparators#least(int, Comparator)} with {@link Comparator#naturalOrder()} over
   * more contrived alternatives.
   */
  static final class LeastNaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    Collector<T, ?, List<T>> before(int k) {
      return greatest(k, reverseOrder());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Collector<T, ?, List<T>> after(int k) {
      return least(k, naturalOrder());
    }
  }

  /**
   * Prefer {@link Comparators#greatest(int, Comparator)} with {@link Comparator#naturalOrder()}
   * over more contrived alternatives.
   */
  static final class GreatestNaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    Collector<T, ?, List<T>> before(int k) {
      return least(k, reverseOrder());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Collector<T, ?, List<T>> after(int k) {
      return greatest(k, naturalOrder());
    }
  }

  /** Prefer {@link Comparators#min(Comparable, Comparable)} over more verbose alternatives. */
  static final class ComparatorsMin0<T extends Comparable<? super T>> {
    @BeforeTemplate
    BinaryOperator<T> before() {
      return BinaryOperator.minBy(naturalOrder());
    }

    @AfterTemplate
    BinaryOperator<T> after() {
      return Comparators::min;
    }
  }

  /** Prefer {@link Comparators#max(Comparable, Comparable)} over more verbose alternatives. */
  static final class ComparatorsMax0<T extends Comparable<? super T>> {
    @BeforeTemplate
    BinaryOperator<T> before() {
      return BinaryOperator.maxBy(naturalOrder());
    }

    @AfterTemplate
    BinaryOperator<T> after() {
      return Comparators::max;
    }
  }

  /**
   * Prefer {@link Collectors#minBy(Comparator)} with {@link Comparator#naturalOrder()} over less
   * explicit or more contrived alternatives.
   */
  static final class MinByNaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    Collector<T, ?, Optional<T>> before() {
      return maxBy(reverseOrder());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Collector<T, ?, Optional<T>> after() {
      return minBy(naturalOrder());
    }
  }

  /**
   * Prefer {@link Collectors#maxBy(Comparator)} with {@link Comparator#naturalOrder()} over less
   * explicit or more contrived alternatives.
   */
  static final class MaxByNaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    Collector<T, ?, Optional<T>> before() {
      return minBy(reverseOrder());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Collector<T, ?, Optional<T>> after() {
      return maxBy(naturalOrder());
    }
  }

  /** Prefer {@link Enum#compareTo(Enum)} over less explicit alternatives. */
  static final class EnumIsLessThan<E extends Enum<E>> {
    @BeforeTemplate
    @SuppressWarnings("EnumOrdinal" /* This violation will be rewritten. */)
    boolean before(E value1, E o) {
      return value1.ordinal() < o.ordinal();
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(E value1, E o) {
      return value1.compareTo(o) < 0;
    }
  }

  /** Prefer {@link Enum#compareTo(Enum)} over less explicit alternatives. */
  static final class EnumIsLessThanOrEqualTo<E extends Enum<E>> {
    @BeforeTemplate
    @SuppressWarnings("EnumOrdinal" /* This violation will be rewritten. */)
    boolean before(E value1, E o) {
      return value1.ordinal() <= o.ordinal();
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(E value1, E o) {
      return value1.compareTo(o) <= 0;
    }
  }
}
