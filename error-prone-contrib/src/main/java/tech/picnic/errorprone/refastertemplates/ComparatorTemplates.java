package tech.picnic.errorprone.refastertemplates;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.comparingLong;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/** Refaster rules related to expressions dealing with {@link Comparator}s. */
final class ComparatorTemplates {
  private ComparatorTemplates() {}

  /** Prefer {@link Comparator#naturalOrder()} over more complicated constructs. */
  static final class NaturalOrder<T extends Comparable<? super T>> {
    // XXX: Drop the `Refaster.anyOf` if/when we decide to rewrite one to the other.
    @BeforeTemplate
    Comparator<T> before() {
      return Refaster.anyOf(
          comparing(Refaster.anyOf(identity(), v -> v)), Comparator.<T>reverseOrder().reversed());
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
      return Comparator.<T>naturalOrder().reversed();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<T> after() {
      return reverseOrder();
    }
  }

  static final class CustomComparator<T> {
    // XXX: Drop the `Refaster.anyOf` if/when we decide to rewrite one to the other.
    @BeforeTemplate
    Comparator<T> before(Comparator<T> cmp) {
      return comparing(Refaster.anyOf(identity(), v -> v), cmp);
    }

    @AfterTemplate
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
   * Where applicable, prefer {@link Comparator#naturalOrder()} over {@link Function#identity()}, as
   * it more clearly states intent.
   */
  static final class ThenComparingNaturalOrder<T extends Comparable<? super T>> {
    // XXX: Drop the `Refaster.anyOf` if/when we decide to rewrite one to the other.
    @BeforeTemplate
    Comparator<T> before(Comparator<T> cmp) {
      return cmp.thenComparing(Refaster.anyOf(identity(), v -> v));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Comparator<T> after(Comparator<T> cmp) {
      return cmp.thenComparing(naturalOrder());
    }
  }

  /** Prefer {@link Comparators#min(Comparable, Comparable)}} over more verbose alternatives. */
  static final class MinOfPairNaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    T before(T value1, T value2) {
      return Collections.min(
          Refaster.anyOf(
              Arrays.asList(value1, value2),
              ImmutableList.of(value1, value2),
              ImmutableSet.of(value1, value2)));
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
    T before(T value1, T value2, Comparator<T> cmp) {
      return Collections.min(
          Refaster.anyOf(
              Arrays.asList(value1, value2),
              ImmutableList.of(value1, value2),
              ImmutableSet.of(value1, value2)),
          cmp);
    }

    @AfterTemplate
    T after(T value1, T value2, Comparator<T> cmp) {
      return Comparators.min(value1, value2, cmp);
    }
  }

  /** Prefer {@link Comparators#max(Comparable, Comparable)}} over more verbose alternatives. */
  static final class MaxOfPairNaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    T before(T value1, T value2) {
      return Collections.max(
          Refaster.anyOf(
              Arrays.asList(value1, value2),
              ImmutableList.of(value1, value2),
              ImmutableSet.of(value1, value2)));
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
    T before(T value1, T value2, Comparator<T> cmp) {
      return Collections.max(
          Refaster.anyOf(
              Arrays.asList(value1, value2),
              ImmutableList.of(value1, value2),
              ImmutableSet.of(value1, value2)),
          cmp);
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
}
