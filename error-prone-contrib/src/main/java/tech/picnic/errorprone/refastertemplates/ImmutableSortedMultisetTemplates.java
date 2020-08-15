package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSortedMultiset.toImmutableSortedMultiset;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link ImmutableSortedMultiset}s. */
final class ImmutableSortedMultisetTemplates {
  private ImmutableSortedMultisetTemplates() {}

  /**
   * Prefer {@link ImmutableSortedMultiset#orderedBy(Comparator)} over the associated constructor.
   */
  static final class ImmutableSortedMultisetBuilder<T> {
    @BeforeTemplate
    ImmutableSortedMultiset.Builder<T> before(Comparator<T> cmp) {
      return new ImmutableSortedMultiset.Builder<>(cmp);
    }

    @AfterTemplate
    ImmutableSortedMultiset.Builder<T> after(Comparator<T> cmp) {
      return ImmutableSortedMultiset.orderedBy(cmp);
    }
  }

  /**
   * Prefer {@link ImmutableSortedMultiset#naturalOrder()} over the alternative that requires
   * explicitly providing the {@link Comparator}.
   */
  static final class ImmutableSortedMultisetNaturalOrderBuilder<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedMultiset.Builder<T> before() {
      return ImmutableSortedMultiset.orderedBy(Comparator.<T>naturalOrder());
    }

    @AfterTemplate
    ImmutableSortedMultiset.Builder<T> after() {
      return ImmutableSortedMultiset.naturalOrder();
    }
  }

  /**
   * Prefer {@link ImmutableSortedMultiset#reverseOrder()} over the alternative that requires
   * explicitly providing the {@link Comparator}.
   */
  static final class ImmutableSortedMultisetReverseOrderBuilder<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedMultiset.Builder<T> before() {
      return ImmutableSortedMultiset.orderedBy(Comparator.<T>reverseOrder());
    }

    @AfterTemplate
    ImmutableSortedMultiset.Builder<T> after() {
      return ImmutableSortedMultiset.reverseOrder();
    }
  }

  /** Prefer {@link ImmutableSortedMultiset#of()} over more contrived alternatives. */
  static final class EmptyImmutableSortedMultiset<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedMultiset<T> before() {
      return Refaster.anyOf(
          ImmutableSortedMultiset.<T>naturalOrder().build(),
          Stream.<T>empty().collect(toImmutableSortedMultiset(naturalOrder())));
    }

    @AfterTemplate
    ImmutableSortedMultiset<T> after() {
      return ImmutableSortedMultiset.of();
    }
  }

  /**
   * Prefer {@link ImmutableSortedMultiset#copyOf(Iterable)} and variants over more contrived
   * alternatives.
   */
  // XXX: There's also a variant with a custom Comparator. (And some special cases with
  // `reverseOrder`.) Worth the hassle?
  static final class IterableToImmutableSortedMultiset<T extends Comparable<? super T>> {
    // XXX: Drop the inner `Refaster.anyOf` if/when we introduce a rule to choose between one and
    // the other.
    @BeforeTemplate
    ImmutableMultiset<T> before(T[] iterable) {
      return Refaster.anyOf(
          ImmutableSortedMultiset.<T>naturalOrder().add(iterable).build(),
          Refaster.anyOf(Stream.of(iterable), Arrays.stream(iterable))
              .collect(toImmutableSortedMultiset(naturalOrder())));
    }

    @BeforeTemplate
    ImmutableMultiset<T> before(Iterator<T> iterable) {
      return Refaster.anyOf(
          ImmutableSortedMultiset.copyOf(naturalOrder(), iterable),
          ImmutableSortedMultiset.<T>naturalOrder().addAll(iterable).build(),
          Streams.stream(iterable).collect(toImmutableSortedMultiset(naturalOrder())));
    }

    @BeforeTemplate
    ImmutableMultiset<T> before(Iterable<T> iterable) {
      return Refaster.anyOf(
          ImmutableSortedMultiset.copyOf(naturalOrder(), iterable),
          ImmutableSortedMultiset.<T>naturalOrder().addAll(iterable).build(),
          Streams.stream(iterable).collect(toImmutableSortedMultiset(naturalOrder())));
    }

    @BeforeTemplate
    ImmutableSortedMultiset<T> before(Collection<T> iterable) {
      return iterable.stream().collect(toImmutableSortedMultiset(naturalOrder()));
    }

    @AfterTemplate
    ImmutableSortedMultiset<T> after(Iterable<T> iterable) {
      return ImmutableSortedMultiset.copyOf(iterable);
    }
  }

  /**
   * Prefer {@link ImmutableSortedMultiset#toImmutableSortedMultiset(Comparator)} over less
   * idiomatic alternatives.
   */
  // XXX: Also handle the variant with a custom comparator.
  static final class StreamToImmutableSortedMultiset<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedMultiset<T> before(Stream<T> stream) {
      return Refaster.anyOf(
          ImmutableSortedMultiset.copyOf(stream.iterator()),
          ImmutableSortedMultiset.copyOf(stream::iterator),
          stream.collect(collectingAndThen(toList(), ImmutableSortedMultiset::copyOf)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableSortedMultiset<T> after(Stream<T> stream) {
      return stream.collect(toImmutableSortedMultiset(naturalOrder()));
    }
  }
}
