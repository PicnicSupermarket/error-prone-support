package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSortedMultiset.toImmutableSortedMultiset;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableSortedMultiset;
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
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link ImmutableSortedMultiset}s. */
@OnlineDocumentation
final class ImmutableSortedMultisetRules {
  private ImmutableSortedMultisetRules() {}

  /**
   * Prefer {@link ImmutableSortedMultiset#orderedBy(Comparator)} over the associated constructor.
   */
  static final class ImmutableSortedMultisetOrderedBy<T> {
    @BeforeTemplate
    ImmutableSortedMultiset.Builder<T> before(Comparator<T> comparator) {
      return new ImmutableSortedMultiset.Builder<>(comparator);
    }

    @AfterTemplate
    ImmutableSortedMultiset.Builder<T> after(Comparator<T> comparator) {
      return ImmutableSortedMultiset.orderedBy(comparator);
    }
  }

  /** Prefer {@link ImmutableSortedMultiset#naturalOrder()} over more verbose alternatives. */
  static final class ImmutableSortedMultisetNaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedMultiset.Builder<T> before() {
      return ImmutableSortedMultiset.orderedBy(Comparator.<T>naturalOrder());
    }

    @AfterTemplate
    ImmutableSortedMultiset.Builder<T> after() {
      return ImmutableSortedMultiset.naturalOrder();
    }
  }

  /** Prefer {@link ImmutableSortedMultiset#reverseOrder()} over more verbose alternatives. */
  static final class ImmutableSortedMultisetReverseOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedMultiset.Builder<T> before() {
      return ImmutableSortedMultiset.orderedBy(Comparator.<T>reverseOrder());
    }

    @AfterTemplate
    ImmutableSortedMultiset.Builder<T> after() {
      return ImmutableSortedMultiset.reverseOrder();
    }
  }

  /**
   * Prefer {@link ImmutableSortedMultiset#of()} over less efficient or more contrived alternatives.
   */
  static final class ImmutableSortedMultisetOf<T extends Comparable<? super T>> {
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
   * Prefer {@link ImmutableSortedMultiset#copyOf(Iterable)} and variants over more verbose, less
   * efficient, or more contrived alternatives.
   */
  // XXX: There's also a variant with a custom Comparator. (And some special cases with
  // `reverseOrder`.) Worth the hassle?
  static final class ImmutableSortedMultisetCopyOf<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedMultiset<T> before(T[] elements) {
      return Refaster.anyOf(
          ImmutableSortedMultiset.<T>naturalOrder().add(elements).build(),
          Arrays.stream(elements).collect(toImmutableSortedMultiset(naturalOrder())));
    }

    @BeforeTemplate
    ImmutableSortedMultiset<T> before(Iterator<T> elements) {
      return Refaster.anyOf(
          ImmutableSortedMultiset.copyOf(naturalOrder(), elements),
          ImmutableSortedMultiset.<T>naturalOrder().addAll(elements).build(),
          Streams.stream(elements).collect(toImmutableSortedMultiset(naturalOrder())));
    }

    @BeforeTemplate
    ImmutableSortedMultiset<T> before(Iterable<T> elements) {
      return Refaster.anyOf(
          ImmutableSortedMultiset.copyOf(naturalOrder(), elements),
          ImmutableSortedMultiset.<T>naturalOrder().addAll(elements).build(),
          Streams.stream(elements).collect(toImmutableSortedMultiset(naturalOrder())));
    }

    @BeforeTemplate
    ImmutableSortedMultiset<T> before(Collection<T> elements) {
      return elements.stream().collect(toImmutableSortedMultiset(naturalOrder()));
    }

    @AfterTemplate
    ImmutableSortedMultiset<T> after(Iterable<T> elements) {
      return ImmutableSortedMultiset.copyOf(elements);
    }
  }

  /**
   * Prefer {@link ImmutableSortedMultiset#toImmutableSortedMultiset(Comparator)} over less
   * idiomatic alternatives.
   */
  // XXX: Also handle the variant with a custom comparator.
  static final class StreamCollectToImmutableSortedMultisetNaturalOrder<
      T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedMultiset<T> before(Stream<T> stream) {
      return ImmutableSortedMultiset.copyOf(stream.iterator());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ImmutableSortedMultiset<T> after(Stream<T> stream) {
      return stream.collect(toImmutableSortedMultiset(naturalOrder()));
    }
  }
}
