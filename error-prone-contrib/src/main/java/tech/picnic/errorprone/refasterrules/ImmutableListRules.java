package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link ImmutableList}s. */
@OnlineDocumentation
final class ImmutableListRules {
  private ImmutableListRules() {}

  /** Prefer {@link ImmutableList#builder()} over the associated constructor. */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. See
  // https://github.com/google/error-prone/pull/2706.
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

  /** Prefer {@link ImmutableList#toImmutableList()} over less idiomatic alternatives. */
  static final class StreamToImmutableList<T> {
    @BeforeTemplate
    ImmutableList<T> before(Stream<T> stream) {
      return ImmutableList.copyOf(stream.iterator());
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
    ImmutableList<T> before(Comparator<T> cmp, Iterable<T> iterable) {
      return Streams.stream(iterable).sorted(cmp).collect(toImmutableList());
    }

    @BeforeTemplate
    ImmutableList<T> before(Comparator<T> cmp, Collection<T> iterable) {
      return iterable.stream().sorted(cmp).collect(toImmutableList());
    }

    @AfterTemplate
    ImmutableList<T> after(Comparator<? super T> cmp, Collection<T> iterable) {
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

  /**
   * Prefer {@link ImmutableList#of()} over more contrived alternatives or alternatives that don't
   * communicate the immutability of the resulting list at the type level.
   */
  // XXX: The `Stream` variant may be too contrived to warrant inclusion. Review its usage if/when
  // this and similar Refaster rules are replaced with an Error Prone check.
  static final class ImmutableListOf<T> {
    @BeforeTemplate
    List<T> before() {
      return Refaster.anyOf(
          ImmutableList.<T>builder().build(),
          Stream.<T>empty().collect(toImmutableList()),
          emptyList(),
          List.of());
    }

    @AfterTemplate
    ImmutableList<T> after() {
      return ImmutableList.of();
    }
  }

  /**
   * Prefer {@link ImmutableList#of(Object)} over more contrived alternatives or alternatives that
   * don't communicate the immutability of the resulting list at the type level.
   */
  // XXX: Note that the replacement of `Collections#singletonList` is incorrect for nullable
  // elements.
  static final class ImmutableListOf1<T> {
    @BeforeTemplate
    List<T> before(T e1) {
      return Refaster.anyOf(
          ImmutableList.<T>builder().add(e1).build(), singletonList(e1), List.of(e1));
    }

    @AfterTemplate
    ImmutableList<T> after(T e1) {
      return ImmutableList.of(e1);
    }
  }

  /**
   * Prefer {@link ImmutableList#of(Object, Object)} over alternatives that don't communicate the
   * immutability of the resulting list at the type level.
   */
  // XXX: Consider writing an Error Prone check that also flags straightforward
  // `ImmutableList.builder()` usages.
  static final class ImmutableListOf2<T> {
    @BeforeTemplate
    List<T> before(T e1, T e2) {
      return List.of(e1, e2);
    }

    @AfterTemplate
    ImmutableList<T> after(T e1, T e2) {
      return ImmutableList.of(e1, e2);
    }
  }

  /**
   * Prefer {@link ImmutableList#of(Object, Object, Object)} over alternatives that don't
   * communicate the immutability of the resulting list at the type level.
   */
  // XXX: Consider writing an Error Prone check that also flags straightforward
  // `ImmutableList.builder()` usages.
  static final class ImmutableListOf3<T> {
    @BeforeTemplate
    List<T> before(T e1, T e2, T e3) {
      return List.of(e1, e2, e3);
    }

    @AfterTemplate
    ImmutableList<T> after(T e1, T e2, T e3) {
      return ImmutableList.of(e1, e2, e3);
    }
  }

  /**
   * Prefer {@link ImmutableList#of(Object, Object, Object, Object)} over alternatives that don't
   * communicate the immutability of the resulting list at the type level.
   */
  // XXX: Consider writing an Error Prone check that also flags straightforward
  // `ImmutableList.builder()` usages.
  static final class ImmutableListOf4<T> {
    @BeforeTemplate
    List<T> before(T e1, T e2, T e3, T e4) {
      return List.of(e1, e2, e3, e4);
    }

    @AfterTemplate
    ImmutableList<T> after(T e1, T e2, T e3, T e4) {
      return ImmutableList.of(e1, e2, e3, e4);
    }
  }

  /**
   * Prefer {@link ImmutableList#of(Object, Object, Object, Object, Object)} over alternatives that
   * don't communicate the immutability of the resulting list at the type level.
   */
  // XXX: Consider writing an Error Prone check that also flags straightforward
  // `ImmutableList.builder()` usages.
  static final class ImmutableListOf5<T> {
    @BeforeTemplate
    List<T> before(T e1, T e2, T e3, T e4, T e5) {
      return List.of(e1, e2, e3, e4, e5);
    }

    @AfterTemplate
    ImmutableList<T> after(T e1, T e2, T e3, T e4, T e5) {
      return ImmutableList.of(e1, e2, e3, e4, e5);
    }
  }
}
