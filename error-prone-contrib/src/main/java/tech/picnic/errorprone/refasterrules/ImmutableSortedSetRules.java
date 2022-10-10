package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Stream;

/** Refaster rules related to expressions dealing with {@link ImmutableSortedSet}s. */
final class ImmutableSortedSetRules {
  private ImmutableSortedSetRules() {}

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
    @BeforeTemplate
    ImmutableSortedSet<T> before(T[] iterable) {
      return Refaster.anyOf(
          ImmutableSortedSet.<T>naturalOrder().add(iterable).build(),
          Arrays.stream(iterable).collect(toImmutableSortedSet(naturalOrder())));
    }

    @BeforeTemplate
    ImmutableSortedSet<T> before(Iterator<T> iterable) {
      return Refaster.anyOf(
          ImmutableSortedSet.copyOf(naturalOrder(), iterable),
          ImmutableSortedSet.<T>naturalOrder().addAll(iterable).build(),
          Streams.stream(iterable).collect(toImmutableSortedSet(naturalOrder())));
    }

    @BeforeTemplate
    ImmutableSortedSet<T> before(Iterable<T> iterable) {
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

  /**
   * Prefer {@link ImmutableSortedSet#toImmutableSortedSet(Comparator)} over less idiomatic
   * alternatives.
   */
  // XXX: Also handle the variant with a custom comparator.
  // XXX: Note that this rule rewrites fewer expressions than `StreamToImmutableSet`, because
  // `#compareTo` and `#equals` may be inconsistent. We should separately flag such cases.
  static final class StreamToImmutableSortedSet<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedSet<T> before(Stream<T> stream) {
      return ImmutableSortedSet.copyOf(stream.iterator());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ImmutableSortedSet<T> after(Stream<T> stream) {
      return stream.collect(toImmutableSortedSet(naturalOrder()));
    }
  }
}
