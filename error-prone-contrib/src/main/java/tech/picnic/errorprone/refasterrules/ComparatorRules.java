package tech.picnic.errorprone.refasterrules;

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
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Matches;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.matchers.IsIdentityOperation;

/** Refaster rules related to expressions dealing with {@link Comparator}s. */
@OnlineDocumentation
final class ComparatorRules {
  private ComparatorRules() {}

  /** Prefer {@link Comparator#naturalOrder()} over more complicated constructs. */
  static final class NaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    Comparator<T> before(
        @Matches(IsIdentityOperation.class) Function<? super T, ? extends T> keyExtractor) {
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

  /** Prefer {@link Comparator#reverseOrder()} over more complicated constructs. */
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

  static final class CustomComparator<T> {
    @BeforeTemplate
    Comparator<T> before(
        Comparator<T> cmp,
        @Matches(IsIdentityOperation.class) Function<? super T, ? extends T> keyExtractor) {
      return comparing(keyExtractor, cmp);
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<T> after(Comparator<T> cmp) {
      return cmp;
    }
  }

  /** Don't explicitly create {@link Comparator}s unnecessarily. */
  static final class ThenComparing<S, T extends Comparable<? super T>> {
    @BeforeTemplate
    Comparator<S> before(Comparator<S> cmp, Function<? super S, ? extends T> function) {
      return cmp.thenComparing(comparing(function));
    }

    @AfterTemplate
    Comparator<S> after(Comparator<S> cmp, Function<? super S, ? extends T> function) {
      return cmp.thenComparing(function);
    }
  }

  /** Don't explicitly create {@link Comparator}s unnecessarily. */
  static final class ThenComparingReversed<S, T extends Comparable<? super T>> {
    @BeforeTemplate
    Comparator<S> before(Comparator<S> cmp, Function<? super S, ? extends T> function) {
      return cmp.thenComparing(comparing(function).reversed());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<S> after(Comparator<S> cmp, Function<? super S, ? extends T> function) {
      return cmp.thenComparing(function, reverseOrder());
    }
  }

  /** Don't explicitly create {@link Comparator}s unnecessarily. */
  static final class ThenComparingCustom<S, T> {
    @BeforeTemplate
    Comparator<S> before(
        Comparator<S> cmp, Function<? super S, ? extends T> function, Comparator<? super T> cmp2) {
      return cmp.thenComparing(comparing(function, cmp2));
    }

    @AfterTemplate
    Comparator<S> after(
        Comparator<S> cmp, Function<? super S, ? extends T> function, Comparator<? super T> cmp2) {
      return cmp.thenComparing(function, cmp2);
    }
  }

  /** Don't explicitly create {@link Comparator}s unnecessarily. */
  static final class ThenComparingCustomReversed<S, T> {
    @BeforeTemplate
    Comparator<S> before(
        Comparator<S> cmp, Function<? super S, ? extends T> function, Comparator<? super T> cmp2) {
      return cmp.thenComparing(comparing(function, cmp2).reversed());
    }

    @AfterTemplate
    Comparator<S> after(
        Comparator<S> cmp, Function<? super S, ? extends T> function, Comparator<? super T> cmp2) {
      return cmp.thenComparing(function, cmp2.reversed());
    }
  }

  /** Don't explicitly create {@link Comparator}s unnecessarily. */
  static final class ThenComparingDouble<T> {
    @BeforeTemplate
    Comparator<T> before(Comparator<T> cmp, ToDoubleFunction<? super T> function) {
      return cmp.thenComparing(comparingDouble(function));
    }

    @AfterTemplate
    Comparator<T> after(Comparator<T> cmp, ToDoubleFunction<? super T> function) {
      return cmp.thenComparingDouble(function);
    }
  }

  /** Don't explicitly create {@link Comparator}s unnecessarily. */
  static final class ThenComparingInt<T> {
    @BeforeTemplate
    Comparator<T> before(Comparator<T> cmp, ToIntFunction<? super T> function) {
      return cmp.thenComparing(comparingInt(function));
    }

    @AfterTemplate
    Comparator<T> after(Comparator<T> cmp, ToIntFunction<? super T> function) {
      return cmp.thenComparingInt(function);
    }
  }

  /** Don't explicitly create {@link Comparator}s unnecessarily. */
  static final class ThenComparingLong<T> {
    @BeforeTemplate
    Comparator<T> before(Comparator<T> cmp, ToLongFunction<? super T> function) {
      return cmp.thenComparing(comparingLong(function));
    }

    @AfterTemplate
    Comparator<T> after(Comparator<T> cmp, ToLongFunction<? super T> function) {
      return cmp.thenComparingLong(function);
    }
  }

  /**
   * Where applicable, prefer {@link Comparator#naturalOrder()} over identity function-based
   * comparisons, as the former more clearly states intent.
   */
  static final class ThenComparingNaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    Comparator<T> before(
        Comparator<T> cmp,
        @Matches(IsIdentityOperation.class) Function<? super T, ? extends T> keyExtractor) {
      return cmp.thenComparing(keyExtractor);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<T> after(Comparator<T> cmp) {
      return cmp.thenComparing(naturalOrder());
    }
  }

  /** Prefer {@link Comparable#compareTo(Object)}} over more verbose alternatives. */
  static final class CompareTo<T extends Comparable<? super T>> {
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

  /**
   * Avoid unnecessary creation of a {@link Stream} to determine the minimum of a known collection
   * of values.
   */
  static final class MinOfVarargs<T> {
    @BeforeTemplate
    T before(@Repeated T value, Comparator<T> cmp) {
      return Stream.of(Refaster.asVarargs(value)).min(cmp).orElseThrow();
    }

    @AfterTemplate
    T after(@Repeated T value, Comparator<T> cmp) {
      return Collections.min(Arrays.asList(value), cmp);
    }
  }

  /** Prefer {@link Comparators#min(Comparable, Comparable)}} over more verbose alternatives. */
  static final class MinOfPairNaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    @SuppressWarnings("java:S1067" /* The conditional operators are independent. */)
    T before(T value1, T value2) {
      return Refaster.anyOf(
          value1.compareTo(value2) <= 0 ? value1 : value2,
          value1.compareTo(value2) > 0 ? value2 : value1,
          value2.compareTo(value1) < 0 ? value2 : value1,
          value2.compareTo(value1) >= 0 ? value1 : value2,
          Comparators.min(value1, value2, naturalOrder()),
          Comparators.max(value1, value2, reverseOrder()),
          Collections.min(
              Refaster.anyOf(
                  Arrays.asList(value1, value2),
                  ImmutableList.of(value1, value2),
                  ImmutableSet.of(value1, value2))));
    }

    @AfterTemplate
    T after(T value1, T value2) {
      return Comparators.min(value1, value2);
    }
  }

  /**
   * Prefer {@link Comparators#min(Object, Object, Comparator)}}} over more verbose alternatives.
   */
  static final class MinOfPairCustomOrder<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S1067" /* The conditional operators are independent. */)
    T before(T value1, T value2, Comparator<T> cmp) {
      return Refaster.anyOf(
          cmp.compare(value1, value2) <= 0 ? value1 : value2,
          cmp.compare(value1, value2) > 0 ? value2 : value1,
          cmp.compare(value2, value1) < 0 ? value2 : value1,
          cmp.compare(value2, value1) >= 0 ? value1 : value2,
          Collections.min(
              Refaster.anyOf(
                  Arrays.asList(value1, value2),
                  ImmutableList.of(value1, value2),
                  ImmutableSet.of(value1, value2)),
              cmp));
    }

    @AfterTemplate
    T after(T value1, T value2, Comparator<T> cmp) {
      return Comparators.min(value1, value2, cmp);
    }
  }

  /**
   * Avoid unnecessary creation of a {@link Stream} to determine the maximum of a known collection
   * of values.
   */
  static final class MaxOfVarargs<T> {
    @BeforeTemplate
    T before(@Repeated T value, Comparator<T> cmp) {
      return Stream.of(Refaster.asVarargs(value)).max(cmp).orElseThrow();
    }

    @AfterTemplate
    T after(@Repeated T value, Comparator<T> cmp) {
      return Collections.max(Arrays.asList(value), cmp);
    }
  }

  /** Prefer {@link Comparators#max(Comparable, Comparable)}} over more verbose alternatives. */
  static final class MaxOfPairNaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    @SuppressWarnings("java:S1067" /* The conditional operators are independent. */)
    T before(T value1, T value2) {
      return Refaster.anyOf(
          value1.compareTo(value2) >= 0 ? value1 : value2,
          value1.compareTo(value2) < 0 ? value2 : value1,
          value2.compareTo(value1) > 0 ? value2 : value1,
          value2.compareTo(value1) <= 0 ? value1 : value2,
          Comparators.max(value1, value2, naturalOrder()),
          Comparators.min(value1, value2, reverseOrder()),
          Collections.max(
              Refaster.anyOf(
                  Arrays.asList(value1, value2),
                  ImmutableList.of(value1, value2),
                  ImmutableSet.of(value1, value2))));
    }

    @AfterTemplate
    T after(T value1, T value2) {
      return Comparators.max(value1, value2);
    }
  }

  /**
   * Prefer {@link Comparators#max(Object, Object, Comparator)}}} over more verbose alternatives.
   */
  static final class MaxOfPairCustomOrder<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S1067" /* The conditional operators are independent. */)
    T before(T value1, T value2, Comparator<T> cmp) {
      return Refaster.anyOf(
          cmp.compare(value1, value2) >= 0 ? value1 : value2,
          cmp.compare(value1, value2) < 0 ? value2 : value1,
          cmp.compare(value2, value1) > 0 ? value2 : value1,
          cmp.compare(value2, value1) <= 0 ? value1 : value2,
          Collections.max(
              Refaster.anyOf(
                  Arrays.asList(value1, value2),
                  ImmutableList.of(value1, value2),
                  ImmutableSet.of(value1, value2)),
              cmp));
    }

    @AfterTemplate
    T after(T value1, T value2, Comparator<T> cmp) {
      return Comparators.max(value1, value2, cmp);
    }
  }

  /**
   * Prefer a method reference to {@link Comparators#min(Comparable, Comparable)} over calling
   * {@link BinaryOperator#minBy(Comparator)} with {@link Comparator#naturalOrder()}.
   */
  static final class ComparatorsMin<T extends Comparable<? super T>> {
    @BeforeTemplate
    BinaryOperator<T> before() {
      return BinaryOperator.minBy(naturalOrder());
    }

    @AfterTemplate
    BinaryOperator<T> after() {
      return Comparators::min;
    }
  }

  /**
   * Prefer a method reference to {@link Comparators#max(Comparable, Comparable)} over calling
   * {@link BinaryOperator#minBy(Comparator)} with {@link Comparator#naturalOrder()}.
   */
  static final class ComparatorsMax<T extends Comparable<? super T>> {
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
   * Prefer {@link Comparator#naturalOrder()} over {@link Comparator#reverseOrder()} where possible.
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
   * Prefer {@link Comparator#naturalOrder()} over {@link Comparator#reverseOrder()} where possible.
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
}
