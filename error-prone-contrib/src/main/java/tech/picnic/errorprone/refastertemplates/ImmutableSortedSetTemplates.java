package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link ImmutableSortedSet}s. */
final class ImmutableSortedSetTemplates {
  private ImmutableSortedSetTemplates() {}

  /** Prefer {@link ImmutableSortedSet#orderedBy(Comparator)} over the associated constructor. */
  static final class ImmutableSortedSetBuilder<T> {
    @BeforeTemplate
    ImmutableSortedSet.Builder<T> before(Comparator<T> cmp) {
      return new ImmutableSortedSet.Builder<>(cmp);
    }

    @AfterTemplate
    ImmutableSortedSet.Builder<T> after(Comparator<T> cmp) {
      return ImmutableSortedSet.orderedBy(cmp);
    }
  }

  /**
   * Prefer {@link ImmutableSortedSet#naturalOrder()} over the alternative that requires explicitly
   * providing the {@link Comparator}.
   */
  static final class ImmutableSortedSetNaturalOrderBuilder<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedSet.Builder<T> before() {
      return ImmutableSortedSet.orderedBy(Comparator.<T>naturalOrder());
    }

    @AfterTemplate
    ImmutableSortedSet.Builder<T> after() {
      return ImmutableSortedSet.naturalOrder();
    }
  }

  /**
   * Prefer {@link ImmutableSortedSet#reverseOrder()} over the alternative that requires explicitly
   * providing the {@link Comparator}.
   */
  static final class ImmutableSortedSetReverseOrderBuilder<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedSet.Builder<T> before() {
      return ImmutableSortedSet.orderedBy(Comparator.<T>reverseOrder());
    }

    @AfterTemplate
    ImmutableSortedSet.Builder<T> after() {
      return ImmutableSortedSet.reverseOrder();
    }
  }

  /** Prefer {@link ImmutableSortedSet#of()} over more contrived alternatives. */
  static final class EmptyImmutableSortedSet<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedSet<T> before() {
      return Refaster.anyOf(
          ImmutableSortedSet.<T>naturalOrder().build(),
          Stream.<T>empty().collect(toImmutableSortedSet(naturalOrder())));
    }

    @AfterTemplate
    ImmutableSortedSet<T> after() {
      return ImmutableSortedSet.of();
    }
  }

  /**
   * Prefer {@link ImmutableSortedSet#copyOf(Iterable)} and variants over more contrived
   * alternatives.
   */
  // XXX: There's also a variant with a custom Comparator. (And some special cases with
  // `reverseOrder`.) Worth the hassle?
  static final class IterableToImmutableSortedSet<T extends Comparable<? super T>> {
    // XXX: Drop the inner `Refaster.anyOf` if/when we introduce a rule to choose between one and
    // the other.
    @BeforeTemplate
    ImmutableSet<T> before(T[] iterable) {
      return Refaster.anyOf(
          ImmutableSortedSet.<T>naturalOrder().add(iterable).build(),
          Refaster.anyOf(Stream.of(iterable), Arrays.stream(iterable))
              .collect(toImmutableSortedSet(naturalOrder())));
    }

    @BeforeTemplate
    ImmutableSet<T> before(Iterator<T> iterable) {
      return Refaster.anyOf(
          ImmutableSortedSet.<T>naturalOrder().addAll(iterable).build(),
          Streams.stream(iterable).collect(toImmutableSortedSet(naturalOrder())));
    }

    @BeforeTemplate
    ImmutableSet<T> before(Iterable<T> iterable) {
      return Refaster.anyOf(
          ImmutableSortedSet.copyOf(naturalOrder(), iterable),
          ImmutableSortedSet.<T>naturalOrder().addAll(iterable).build(),
          Streams.stream(iterable).collect(toImmutableSortedSet(naturalOrder())));
    }

    @BeforeTemplate
    ImmutableSortedSet<T> before(Collection<T> iterable) {
      return iterable.stream().collect(toImmutableSortedSet(naturalOrder()));
    }

    @AfterTemplate
    ImmutableSortedSet<T> after(Iterable<T> iterable) {
      return ImmutableSortedSet.copyOf(iterable);
    }
  }
}
