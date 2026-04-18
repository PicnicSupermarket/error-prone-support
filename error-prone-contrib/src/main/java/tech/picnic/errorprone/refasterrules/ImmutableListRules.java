package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.common.collect.UnmodifiableIterator;
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
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/** Refaster rules related to expressions dealing with {@link ImmutableList}s. */
@OnlineDocumentation
final class ImmutableListRules {
  private ImmutableListRules() {}

  /** Prefer {@link ImmutableList#builder()} over the associated constructor. */
  // XXX: This rule may drop generic type information, leading to non-compilable code.
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
   * Prefer {@link ImmutableList#copyOf(Iterable)} and variants over less efficient or more
   * contrived alternatives.
   */
  @PossibleSourceIncompatibility
  static final class ImmutableListCopyOf<T> {
    @BeforeTemplate
    ImmutableList<T> before(T[] elements) {
      return Refaster.anyOf(
          ImmutableList.<T>builder().add(elements).build(),
          Arrays.stream(elements).collect(toImmutableList()));
    }

    @BeforeTemplate
    ImmutableList<T> before(Iterator<T> elements) {
      return Refaster.anyOf(
          ImmutableList.<T>builder().addAll(elements).build(),
          Streams.stream(elements).collect(toImmutableList()));
    }

    @BeforeTemplate
    ImmutableList<T> before(Iterable<T> elements) {
      return Refaster.anyOf(
          ImmutableList.<T>builder().addAll(elements).build(),
          Streams.stream(elements).collect(toImmutableList()));
    }

    @BeforeTemplate
    ImmutableList<T> before(Collection<T> elements) {
      return elements.stream().collect(toImmutableList());
    }

    @AfterTemplate
    ImmutableList<T> after(Iterable<T> elements) {
      return ImmutableList.copyOf(elements);
    }
  }

  /** Prefer {@link ImmutableList#toImmutableList()} over less idiomatic alternatives. */
  static final class StreamCollectToImmutableList<T> {
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

  /**
   * Prefer {@link ImmutableList#sortedCopyOf(Iterable)} over more verbose or less efficient
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class ImmutableListSortedCopyOf<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableList<T> before(Iterable<T> elements) {
      return Refaster.anyOf(
          ImmutableList.sortedCopyOf(naturalOrder(), elements),
          Streams.stream(elements).sorted().collect(toImmutableList()));
    }

    @BeforeTemplate
    ImmutableList<T> before(Collection<T> elements) {
      return elements.stream().sorted().collect(toImmutableList());
    }

    @AfterTemplate
    ImmutableList<T> after(Collection<T> elements) {
      return ImmutableList.sortedCopyOf(elements);
    }
  }

  /**
   * Prefer {@link ImmutableList#sortedCopyOf(Comparator, Iterable)} over less efficient
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class ImmutableListSortedCopyOfWithComparator<S, T extends S> {
    @BeforeTemplate
    ImmutableList<T> before(Comparator<S> comparator, Iterable<T> elements) {
      return Streams.stream(elements).sorted(comparator).collect(toImmutableList());
    }

    @BeforeTemplate
    ImmutableList<T> before(Comparator<S> comparator, Collection<T> elements) {
      return elements.stream().sorted(comparator).collect(toImmutableList());
    }

    @AfterTemplate
    ImmutableList<T> after(Comparator<S> comparator, Collection<T> elements) {
      return ImmutableList.sortedCopyOf(comparator, elements);
    }
  }

  /**
   * Prefer {@code ImmutableList.sortedCopyOf(iterable).iterator()} over less efficient
   * alternatives.
   */
  static final class ImmutableListSortedCopyOfIterator<T extends Comparable<? super T>> {
    @BeforeTemplate
    Iterator<T> before(Iterable<T> elements) {
      return Streams.stream(elements).sorted().iterator();
    }

    @BeforeTemplate
    Iterator<T> before(Collection<T> elements) {
      return elements.stream().sorted().iterator();
    }

    @AfterTemplate
    UnmodifiableIterator<T> after(Iterable<T> elements) {
      return ImmutableList.sortedCopyOf(elements).iterator();
    }
  }

  /**
   * Prefer {@code ImmutableList.sortedCopyOf(cmp, iterable).iterator()} over less efficient
   * alternatives.
   */
  static final class ImmutableListSortedCopyOfIteratorWithComparator<S, T extends S> {
    @BeforeTemplate
    Iterator<T> before(Comparator<S> comparator, Iterable<T> elements) {
      return Streams.stream(elements).sorted(comparator).iterator();
    }

    @BeforeTemplate
    Iterator<T> before(Comparator<S> comparator, Collection<T> elements) {
      return elements.stream().sorted(comparator).iterator();
    }

    @AfterTemplate
    UnmodifiableIterator<T> after(Comparator<S> comparator, Iterable<T> elements) {
      return ImmutableList.sortedCopyOf(comparator, elements).iterator();
    }
  }

  /** Prefer {@code stream.collect(toImmutableSet()).asList()} over less efficient alternatives. */
  static final class StreamCollectToImmutableSetAsList<T> {
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

  /** Prefer {@link ImmutableList#of()} over imprecisely typed or less efficient alternatives. */
  // XXX: The `Stream` variant may be too contrived to warrant inclusion. Review its usage if/when
  // this and similar Refaster rules are replaced with an Error Prone check.
  static final class ImmutableListOf0<T> {
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
   * Prefer {@link ImmutableList#of(Object)} over imprecisely typed or less efficient alternatives.
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

  /** Prefer {@link ImmutableList#of(Object, Object)} over imprecisely typed alternatives. */
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
   * Prefer {@link ImmutableList#of(Object, Object, Object)} over imprecisely typed alternatives.
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
   * Prefer {@link ImmutableList#of(Object, Object, Object, Object)} over imprecisely typed
   * alternatives.
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
   * Prefer {@link ImmutableList#of(Object, Object, Object, Object, Object)} over imprecisely typed
   * alternatives.
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
