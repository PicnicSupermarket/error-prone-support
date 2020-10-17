package tech.picnic.errorprone.refastertemplates;

import static java.util.function.Function.identity;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Function;

/** Refaster templates related to expressions dealing with {@link Comparator}s. */
final class ComparatorTemplates {
  private ComparatorTemplates() {}

  /** Prefer {@link Comparator#naturalOrder()} over more complicated constructs. */
  static final class NaturalOrderComparator<T extends Comparable<? super T>> {
    // XXX: Drop the `Refaster.anyOf` if/when we decide to rewrite one to the other.
    @BeforeTemplate
    Comparator<T> before() {
      return Refaster.anyOf(Comparator.comparing(Refaster.anyOf(identity(), v -> v)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    Comparator<T> after() {
      return Comparator.naturalOrder();
    }
  }

  /**
   * Where applicable, prefer {@link Comparator#naturalOrder()} over {@link Function#identity()}, as
   * it more clearly states intent.
   */
  static final class NaturalOrderComparatorFallback<T extends Comparable<? super T>> {
    // XXX: Drop the `Refaster.anyOf` if/when we decide to rewrite one to the other.
    @BeforeTemplate
    Comparator<T> before(Comparator<T> cmp) {
      return cmp.thenComparing(Refaster.anyOf(identity(), v -> v));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    Comparator<T> after(Comparator<T> cmp) {
      return cmp.thenComparing(Comparator.naturalOrder());
    }
  }

  /** Prefer {@link Comparator#reverseOrder()} over more complicated constructs. */
  static final class ReverseOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    Comparator<T> before() {
      return Comparator.<T>naturalOrder().reversed();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    Comparator<T> after() {
      return Comparator.reverseOrder();
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
}
