package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link ImmutableList}s. */
final class ImmutableListTemplates {
  private ImmutableListTemplates() {}

  /** Prefer {@link ImmutableList#builder()} over the associated constructor. */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. Anything
  // we can do about that?
  static final class ImmutableListBuilder<T> {
    @BeforeTemplate
    ImmutableList.Builder<T> before() {
      return new ImmutableList.Builder<>();
    }

    @AfterTemplate
    ImmutableList.Builder<T> after() {
      return ImmutableList.builder();
    }
  }

  /** Prefer {@link ImmutableList#of()} over more contrived alternatives. */
  static final class EmptyImmutableList<T> {
    @BeforeTemplate
    ImmutableList<T> before() {
      return Refaster.anyOf(
          ImmutableList.<T>builder().build(), Stream.<T>empty().collect(toImmutableList()));
    }

    @AfterTemplate
    ImmutableList<T> after() {
      return ImmutableList.of();
    }
  }

  /**
   * Prefer {@link ImmutableList#of(Object)} over alternatives that don't communicate the
   * immutability of the resulting list at the type level.
   */
  // XXX: Note that this rewrite rule is incorrect for nullable elements.
  static final class SingletonImmutableList<T> {
    @BeforeTemplate
    List<T> before(T element) {
      return Collections.singletonList(element);
    }

    @AfterTemplate
    ImmutableList<T> after(T element) {
      return ImmutableList.of(element);
    }
  }

  /**
   * Prefer {@link ImmutableList#copyOf(Iterable)} and variants over more contrived alternatives.
   */
  static final class IterableToImmutableList<T> {
    @BeforeTemplate
    ImmutableList<T> before(T[] iterable) {
      return Refaster.anyOf(
          ImmutableList.<T>builder().add(iterable).build(),
          Arrays.stream(iterable).collect(toImmutableList()));
    }

    @BeforeTemplate
    ImmutableList<T> before(Iterator<T> iterable) {
      return Refaster.anyOf(
          ImmutableList.<T>builder().addAll(iterable).build(),
          Streams.stream(iterable).collect(toImmutableList()));
    }

    @BeforeTemplate
    ImmutableList<T> before(Iterable<T> iterable) {
      return Refaster.anyOf(
          ImmutableList.<T>builder().addAll(iterable).build(),
          Streams.stream(iterable).collect(toImmutableList()));
    }

    @BeforeTemplate
    ImmutableList<T> before(Collection<T> iterable) {
      return iterable.stream().collect(toImmutableList());
    }

    @AfterTemplate
    ImmutableList<T> after(Iterable<T> iterable) {
      return ImmutableList.copyOf(iterable);
    }
  }

  /** Prefer {@link ImmutableList#toImmutableList()} over the more verbose alternative. */
  // XXX: Once the code base has been sufficiently cleaned up, we might want to also rewrite
  // `Collectors.toList(`), with the caveat that it allows mutation (though this cannot be relied
  // upon) as well as nulls. Another option is to explicitly rewrite those variants to
  // `Collectors.toSet(ArrayList::new)`.
  static final class StreamToImmutableList<T> {
    @BeforeTemplate
    ImmutableList<T> before(Stream<T> stream) {
      return Refaster.anyOf(
          ImmutableList.copyOf(stream.iterator()),
          stream.collect(collectingAndThen(toList(), ImmutableList::copyOf)));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ImmutableList<T> after(Stream<T> stream) {
      return stream.collect(toImmutableList());
    }
  }

  /** Prefer {@link ImmutableList#sortedCopyOf(Iterable)} over more contrived alternatives. */
  static final class ImmutableListSortedCopyOf<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableList<T> before(Iterable<T> iterable) {
      return Refaster.anyOf(
          ImmutableList.sortedCopyOf(naturalOrder(), iterable),
          Streams.stream(iterable).sorted().collect(toImmutableList()));
    }

    @BeforeTemplate
    ImmutableList<T> before(Collection<T> iterable) {
      return iterable.stream().sorted().collect(toImmutableList());
    }

    @AfterTemplate
    ImmutableList<T> after(Collection<T> iterable) {
      return ImmutableList.sortedCopyOf(iterable);
    }
  }

  /**
   * Prefer {@link ImmutableList#sortedCopyOf(Comparator, Iterable)} over more contrived
   * alternatives.
   */
  static final class ImmutableListSortedCopyOfWithCustomComparator<T> {
    @BeforeTemplate
    ImmutableList<T> before(Iterable<T> iterable, Comparator<T> cmp) {
      return Streams.stream(iterable).sorted(cmp).collect(toImmutableList());
    }

    @BeforeTemplate
    ImmutableList<T> before(Collection<T> iterable, Comparator<T> cmp) {
      return iterable.stream().sorted(cmp).collect(toImmutableList());
    }

    @AfterTemplate
    ImmutableList<T> after(Collection<T> iterable, Comparator<? super T> cmp) {
      return ImmutableList.sortedCopyOf(cmp, iterable);
    }
  }

  /**
   * Collecting to an {@link ImmutableSet} and converting the result to an {@link ImmutableList} may
   * be more efficient than deduplicating a stream and collecting the result to an {@link
   * ImmutableList}.
   */
  static final class StreamToDistinctImmutableList<T> {
    @BeforeTemplate
    ImmutableList<T> before(Stream<T> stream) {
      return stream.distinct().collect(toImmutableList());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ImmutableList<T> after(Stream<T> stream) {
      return stream.collect(toImmutableSet()).asList();
    }
  }

  static final class ImmutableListOf<T> {
    @BeforeTemplate
    List<T> before() {
      return Collections.emptyList();
    }

    @AfterTemplate
    ImmutableList<T> after() {
      return ImmutableList.of();
    }
  }

  static final class ImmutableListOf1<T> {
    @BeforeTemplate
    List<T> before(T item) {
      return List.of(item);
    }

    @AfterTemplate
    ImmutableCollection<T> after(T item) {
      return ImmutableList.of(item);
    }
  }

  static final class ImmutableListOf2<T> {
    @BeforeTemplate
    List<T> before(T item, T item2) {
      return List.of(item, item2);
    }

    @AfterTemplate
    ImmutableCollection<T> after(T item, T item2) {
      return ImmutableList.of(item, item2);
    }
  }

  static final class ImmutableListOf3<T> {
    @BeforeTemplate
    List<T> before(T i1, T i2, T i3) {
      return List.of(i1, i2, i3);
    }

    @AfterTemplate
    ImmutableCollection<T> after(T i1, T i2, T i3) {
      return ImmutableList.of(i1, i2, i3);
    }
  }

  static final class ImmutableListOf4<T> {
    @BeforeTemplate
    List<T> before(T i1, T i2, T i3, T i4) {
      return List.of(i1, i2, i3, i4);
    }

    @AfterTemplate
    ImmutableCollection<T> after(T i1, T i2, T i3, T i4) {
      return ImmutableList.of(i1, i2, i3, i4);
    }
  }

  static final class ImmutableListOf5<T> {
    @BeforeTemplate
    List<T> before(T i1, T i2, T i3, T i4, T i5) {
      return List.of(i1, i2, i3, i4, i5);
    }

    @AfterTemplate
    ImmutableCollection<T> after(T i1, T i2, T i3, T i4, T i5) {
      return ImmutableList.of(i1, i2, i3, i4, i5);
    }
  }
}
