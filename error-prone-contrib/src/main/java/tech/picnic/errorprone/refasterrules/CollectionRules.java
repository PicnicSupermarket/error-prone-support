package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Collections.disjoint;
import static java.util.stream.Collectors.toUnmodifiableSet;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.Streams;
import com.google.common.collect.UnmodifiableIterator;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.NotMatches;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.matchers.IsRefasterAsVarargs;

/** Refaster rules related to expressions dealing with {@link Collection}s. */
// XXX: There are other Guava `Iterables` methods that should not be called if the input is known to
// be a `Collection`. Add those here.
@OnlineDocumentation
final class CollectionRules {
  private CollectionRules() {}

  /**
   * Prefer {@link Collection#isEmpty()} over non-JDK, less efficient, or more verbose alternatives.
   */
  static final class CollectionIsEmpty<T> {
    @BeforeTemplate
    @SuppressWarnings({
      "java:S1155" /* This violation will be rewritten. */,
      "LexicographicalAnnotationAttributeListing" /* `key-*` entry must remain last. */,
      "CollectionStreamFindFirst" /* This is a more specific template. */,
      "StreamFindAnyIsEmpty" /* This is a more specific template. */,
      "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
    })
    boolean before(Collection<T> iterable) {
      return Refaster.anyOf(
          iterable.size() == 0,
          iterable.size() <= 0,
          iterable.size() < 1,
          Iterables.isEmpty(iterable),
          iterable.stream().findAny().isEmpty(),
          iterable.stream().findFirst().isEmpty());
    }

    @BeforeTemplate
    boolean before(ImmutableCollection<T> iterable) {
      return iterable.asList().isEmpty();
    }

    // XXX: Consider introducing similar templates for other `SetView` methods that derive a
    // stateless object from a `SetView`: `isEmpty()`, `size()`, `contains()`, `containsAll()`,
    // `equals()` and `hashCode()`, as well as the `toArray` overloads.
    // XXX: Consider introducing similar templates for other methods that create an immutable copy
    // of a collection, such as `ImmutableList.copyOf`, `ImmutableSet.copyOf` and
    // `ImmutableMap.copyOf`.
    // XXX: Instead of introducing many Refaster rules to cover the Cartesian product of the above
    // suggestions, consider writing an `UnnecessaryCollectionCopy` Error Prone check that
    // simplifies all such expressions. Some logic for such a rule may be extracted from the
    // `IsEmpty` matcher implementation. (Note that `equals()` and `hashCode()` may need special
    // handling, as they depend on the collection type produced.)
    @BeforeTemplate
    boolean before(SetView<T> iterable) {
      return iterable.immutableCopy().isEmpty();
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(Collection<T> iterable) {
      return iterable.isEmpty();
    }
  }

  /** Prefer {@link Collection#size()} over non-JDK or more verbose alternatives. */
  static final class CollectionSize<T> {
    @BeforeTemplate
    int before(Collection<T> iterable) {
      return Iterables.size(iterable);
    }

    @BeforeTemplate
    int before(ImmutableCollection<T> iterable) {
      return iterable.asList().size();
    }

    @AfterTemplate
    int after(Collection<T> iterable) {
      return iterable.size();
    }
  }

  /** Prefer {@link Collection#contains(Object)} over less efficient alternatives. */
  static final class CollectionContains<T, S> {
    @BeforeTemplate
    boolean before(Collection<T> collection, S value) {
      return collection.stream().anyMatch(value::equals);
    }

    @AfterTemplate
    boolean after(Collection<T> collection, S value) {
      return collection.contains(value);
    }
  }

  /**
   * Prefer {@link Collections#disjoint(Collection, Collection)} over non-JDK or less efficient
   * alternatives.
   */
  static final class Disjoint<T> {
    @BeforeTemplate
    boolean before(Set<T> c1, Set<T> c2) {
      return Sets.intersection(c1, c2).isEmpty();
    }

    // XXX: Other copy operations could be elided too, but these are the most common ones. If we
    // ever introduce a generic "makes a copy" stand-in, use it here.
    @BeforeTemplate
    boolean before(Collection<T> c1, Collection<T> c2) {
      return Refaster.anyOf(
          c1.stream().noneMatch(c2::contains),
          disjoint(ImmutableSet.copyOf(c1), c2),
          disjoint(new HashSet<>(c1), c2),
          disjoint(c1, ImmutableSet.copyOf(c2)),
          disjoint(c1, new HashSet<>(c2)));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    boolean after(Collection<T> c1, Collection<T> c2) {
      return disjoint(c1, c2);
    }
  }

  /** Prefer {@link Collection#addAll(Collection)} over non-JDK alternatives. */
  static final class CollectionAddAllExpression<T, S extends T> {
    @BeforeTemplate
    boolean before(Collection<T> addTo, Collection<S> elementsToAdd) {
      return Iterables.addAll(addTo, elementsToAdd);
    }

    @AfterTemplate
    boolean after(Collection<T> addTo, Collection<S> elementsToAdd) {
      return addTo.addAll(elementsToAdd);
    }
  }

  /** Prefer {@link Collection#addAll(Collection)} over more verbose alternatives. */
  static final class CollectionAddAllBlock<T, S extends T> {
    @BeforeTemplate
    void before(Collection<T> collection1, Collection<S> collection2) {
      collection2.forEach(collection1::add);
    }

    @BeforeTemplate
    void before2(Collection<T> collection1, Collection<S> collection2) {
      for (T element : collection2) {
        collection1.add(element);
      }
    }

    // XXX: This method is identical to `before2` except for the loop type. Make Refaster smarter so
    // that this is supported out of the box.
    @BeforeTemplate
    void before3(Collection<T> collection1, Collection<S> collection2) {
      for (S element : collection2) {
        collection1.add(element);
      }
    }

    @AfterTemplate
    void after(Collection<T> collection1, Collection<S> collection2) {
      collection1.addAll(collection2);
    }
  }

  /** Prefer {@link Collection#removeAll(Collection)} over non-JDK alternatives. */
  static final class CollectionRemoveAllExpression<T, S extends T> {
    @BeforeTemplate
    boolean before(Collection<T> removeFrom, Collection<S> elementsToRemove) {
      return Iterables.removeAll(removeFrom, elementsToRemove);
    }

    @AfterTemplate
    boolean after(Collection<T> removeFrom, Collection<S> elementsToRemove) {
      return removeFrom.removeAll(elementsToRemove);
    }
  }

  /** Prefer {@link Collection#removeAll(Collection)} over more verbose alternatives. */
  static final class CollectionRemoveAllBlock<T, S extends T> {
    @BeforeTemplate
    void before(Collection<T> collection1, Collection<S> collection2) {
      collection2.forEach(collection1::remove);
    }

    @BeforeTemplate
    void before2(Collection<T> collection1, Collection<S> collection2) {
      for (T element : collection2) {
        collection1.remove(element);
      }
    }

    // XXX: This method is identical to `before2` except for the loop type. Make Refaster smarter so
    // that this is supported out of the box. After doing so, also drop the `S extends T` type
    // constraint; ideally this check applies to any `S`.
    @BeforeTemplate
    void before3(Collection<T> collection1, Collection<S> collection2) {
      for (S element : collection2) {
        collection1.remove(element);
      }
    }

    @AfterTemplate
    void after(Collection<T> collection1, Collection<S> collection2) {
      collection1.removeAll(collection2);
    }
  }

  /** Prefer {@link Set#stream()} over less efficient alternatives. */
  // XXX: This rule assumes that the `Set` relies on `Object#equals`, rather than a custom
  // equivalence relation.
  // XXX: Expressions that drop or reorder elements from the stream, such as `.filter`, `.skip` and
  // `sorted`, can similarly be simplified. Covering all cases is better done using an Error Prone
  // check.
  static final class SetStream<T> {
    @BeforeTemplate
    Stream<T> before(Set<T> set) {
      return set.stream().distinct();
    }

    @AfterTemplate
    Stream<T> after(Set<T> set) {
      return set.stream();
    }
  }

  /** Prefer {@link Set#of(Object[])} over less efficient alternatives. */
  // XXX: Ideally we rewrite both of these expressions directly to `ImmutableSet.of(..)` (and
  // locate this rule in `ImmutableSetRules`), but for now this rule is included as-is for use with
  // OpenRewrite.
  // XXX: The replacement code throws `IllegalArgumentException` on duplicate elements, while the
  // original code deduplicates them.
  static final class SetOf<T> {
    @BeforeTemplate
    Set<T> before(@Repeated T elements) {
      return Stream.of(Refaster.asVarargs(elements)).collect(toUnmodifiableSet());
    }

    @AfterTemplate
    Set<T> after(@Repeated T elements) {
      return Set.of(Refaster.asVarargs(elements));
    }
  }

  /** Prefer {@link ArrayList#ArrayList(Collection)} over non-JDK alternatives. */
  @SuppressWarnings(
      "NonApiType" /* Matching against `List` would unnecessarily constrain the rule. */)
  static final class NewArrayList<T> {
    @BeforeTemplate
    ArrayList<T> before(Collection<T> elements) {
      return Lists.newArrayList(elements);
    }

    @AfterTemplate
    ArrayList<T> after(Collection<T> elements) {
      return new ArrayList<>(elements);
    }
  }

  /** Prefer {@link ImmutableCollection#asList()} over more verbose alternatives. */
  static final class ImmutableCollectionAsList<T> {
    @BeforeTemplate
    ImmutableList<T> before(ImmutableCollection<T> elements) {
      return ImmutableList.copyOf(elements);
    }

    @AfterTemplate
    ImmutableList<T> after(ImmutableCollection<T> elements) {
      return elements.asList();
    }
  }

  /** Prefer {@link ImmutableCollection#stream()} over more verbose alternatives. */
  static final class ImmutableCollectionStream<T> {
    @BeforeTemplate
    Stream<T> before(ImmutableCollection<T> collection) {
      return collection.asList().stream();
    }

    @AfterTemplate
    Stream<T> after(ImmutableCollection<T> collection) {
      return collection.stream();
    }
  }

  /** Prefer {@link ImmutableCollection#contains(Object)} over more verbose alternatives. */
  static final class ImmutableCollectionContains<T, S> {
    @BeforeTemplate
    boolean before(ImmutableCollection<T> collection, S object) {
      return collection.asList().contains(object);
    }

    @AfterTemplate
    boolean after(ImmutableCollection<T> collection, S object) {
      return collection.contains(object);
    }
  }

  /** Prefer {@link ImmutableCollection#parallelStream()} over more verbose alternatives. */
  static final class ImmutableCollectionParallelStream<T> {
    @BeforeTemplate
    Stream<T> before(ImmutableCollection<T> collection) {
      return collection.asList().parallelStream();
    }

    @AfterTemplate
    Stream<T> after(ImmutableCollection<T> collection) {
      return collection.parallelStream();
    }
  }

  /** Prefer {@link ImmutableCollection#toString()} over more verbose alternatives. */
  static final class ImmutableCollectionToString<T> {
    @BeforeTemplate
    String before(ImmutableCollection<T> collection) {
      return collection.asList().toString();
    }

    @AfterTemplate
    String after(ImmutableCollection<T> collection) {
      return collection.toString();
    }
  }

  /** Prefer {@link Arrays#asList(Object[])} over less efficient alternatives. */
  // XXX: Consider moving this rule to `ImmutableListRules` and having it suggest
  // `ImmutableList#copyOf`. That would retain immutability, at the cost of no longer handling
  // `null`s.
  static final class ArraysAsList<T> {
    // XXX: This expression produces an unmodifiable list, while the alternative doesn't.
    @BeforeTemplate
    List<T> before(@NotMatches(IsRefasterAsVarargs.class) T[] array) {
      return Arrays.stream(array).toList();
    }

    @AfterTemplate
    List<T> after(T[] array) {
      return Arrays.asList(array);
    }
  }

  /** Prefer {@link Collection#toArray()} over less efficient or more verbose alternatives. */
  static final class CollectionToArray<T> {
    @BeforeTemplate
    Object[] before(Collection<T> collection, int size) {
      return Refaster.anyOf(
          collection.toArray(new Object[size]), collection.toArray(Object[]::new));
    }

    @BeforeTemplate
    Object[] before(ImmutableCollection<T> collection) {
      return collection.asList().toArray();
    }

    @AfterTemplate
    Object[] after(Collection<T> collection) {
      return collection.toArray();
    }
  }

  /** Prefer {@link ImmutableCollection#toArray(Object[])} over more verbose alternatives. */
  static final class ImmutableCollectionToArrayObject<T, S> {
    @BeforeTemplate
    S[] before(ImmutableCollection<T> collection, S[] other) {
      return collection.asList().toArray(other);
    }

    @AfterTemplate
    S[] after(ImmutableCollection<T> collection, S[] other) {
      return collection.toArray(other);
    }
  }

  /** Prefer {@link ImmutableCollection#toArray(IntFunction)} over more verbose alternatives. */
  static final class ImmutableCollectionToArrayIntFunction<T, S> {
    @BeforeTemplate
    S[] before(ImmutableCollection<T> collection, IntFunction<S[]> generator) {
      return collection.asList().toArray(generator);
    }

    @AfterTemplate
    S[] after(ImmutableCollection<T> collection, IntFunction<S[]> generator) {
      return collection.toArray(generator);
    }
  }

  /** Prefer {@link Collection#iterator()} over less efficient or more verbose alternatives. */
  static final class CollectionIterator<T> {
    @BeforeTemplate
    Iterator<T> before(Collection<T> collection) {
      return collection.stream().iterator();
    }

    @BeforeTemplate
    UnmodifiableIterator<T> before(ImmutableCollection<T> collection) {
      return collection.asList().iterator();
    }

    @AfterTemplate
    Iterator<T> after(Collection<T> collection) {
      return collection.iterator();
    }
  }

  /**
   * Prefer {@code collection.stream().findFirst()} over less explicit or more verbose alternatives.
   */
  static final class CollectionStreamFindFirst<T> {
    @BeforeTemplate
    Optional<T> before(Collection<T> collection) {
      return Refaster.anyOf(
          collection.stream().findAny(),
          collection.isEmpty() ? Optional.empty() : Optional.of(collection.iterator().next()));
    }

    @BeforeTemplate
    Optional<T> before(List<T> collection) {
      return collection.isEmpty() ? Optional.empty() : Optional.of(collection.getFirst());
    }

    @BeforeTemplate
    Optional<T> before(SortedSet<T> collection) {
      return collection.isEmpty() ? Optional.empty() : Optional.of(collection.first());
    }

    @AfterTemplate
    Optional<T> after(Collection<T> collection) {
      return collection.stream().findFirst();
    }
  }

  /**
   * Prefer {@link Optional#ofNullable(Object)} over less efficient or more contrived alternatives.
   */
  static final class OptionalOfNullableQueuePeek<T> {
    @BeforeTemplate
    Optional<T> before(Queue<T> queue) {
      return Refaster.anyOf(
          queue.stream().findFirst(),
          queue.isEmpty()
              ? Optional.empty()
              : Refaster.anyOf(Optional.of(queue.peek()), Optional.ofNullable(queue.peek())));
    }

    @AfterTemplate
    Optional<T> after(Queue<T> queue) {
      return Optional.ofNullable(queue.peek());
    }
  }

  /** Prefer {@link Optional#ofNullable(Object)} over more contrived alternatives. */
  static final class OptionalOfNullableNavigableSetPollFirst<T> {
    @BeforeTemplate
    Optional<T> before(NavigableSet<T> navigableSet) {
      return navigableSet.isEmpty()
          ? Optional.empty()
          : Refaster.anyOf(
              Optional.of(navigableSet.pollFirst()), Optional.ofNullable(navigableSet.pollFirst()));
    }

    @AfterTemplate
    Optional<T> after(NavigableSet<T> navigableSet) {
      return Optional.ofNullable(navigableSet.pollFirst());
    }
  }

  /** Prefer {@link Optional#ofNullable(Object)} over more contrived alternatives. */
  static final class OptionalOfNullableQueuePoll<T> {
    @BeforeTemplate
    Optional<T> before(Queue<T> queue) {
      return queue.isEmpty()
          ? Optional.empty()
          : Refaster.anyOf(
              Optional.of(Refaster.anyOf(queue.poll(), queue.remove())),
              Optional.ofNullable(Refaster.anyOf(queue.poll(), queue.remove())));
    }

    @AfterTemplate
    Optional<T> after(Queue<T> queue) {
      return Optional.ofNullable(queue.poll());
    }
  }

  /** Prefer {@link Collection#forEach(Consumer)} over less efficient alternatives. */
  static final class CollectionForEach<S, T extends S> {
    @BeforeTemplate
    void before(Collection<T> collection, Consumer<S> action) {
      collection.stream().forEach(action);
    }

    @AfterTemplate
    void after(Collection<T> collection, Consumer<S> action) {
      collection.forEach(action);
    }
  }

  /** Prefer {@code collection.iterator().next()} over less efficient alternatives. */
  static final class CollectionIteratorNext<T> {
    @BeforeTemplate
    T before(Collection<T> collection) {
      return collection.stream().findFirst().orElseThrow();
    }

    @AfterTemplate
    T after(Collection<T> collection) {
      return collection.iterator().next();
    }
  }

  /** Prefer {@link SequencedCollection#getFirst()} over less idiomatic alternatives. */
  static final class SequencedCollectionGetFirst<T> {
    @BeforeTemplate
    T before(SequencedCollection<T> collection) {
      return collection.iterator().next();
    }

    @BeforeTemplate
    T before(List<T> collection) {
      return collection.get(0);
    }

    @AfterTemplate
    T after(SequencedCollection<T> collection) {
      return collection.getFirst();
    }
  }

  /**
   * Prefer {@link SequencedCollection#getLast()} over less idiomatic or more verbose alternatives.
   */
  static final class SequencedCollectionGetLast<T> {
    @BeforeTemplate
    T before(SequencedCollection<T> collection) {
      return Refaster.anyOf(
          collection.reversed().getFirst(), Streams.findLast(collection.stream()).orElseThrow());
    }

    @BeforeTemplate
    T before(List<T> collection) {
      return collection.get(collection.size() - 1);
    }

    @AfterTemplate
    T after(SequencedCollection<T> collection) {
      return collection.getLast();
    }
  }

  /** Prefer {@link List#addFirst(Object)} over less idiomatic alternatives. */
  static final class ListAddFirst<T> {
    @BeforeTemplate
    void before(List<T> list, T e) {
      list.add(0, e);
    }

    @AfterTemplate
    void after(List<T> list, T e) {
      list.addFirst(e);
    }
  }

  /** Prefer {@link List#add(Object)} over less idiomatic or more verbose alternatives. */
  static final class ListAdd<T> {
    @BeforeTemplate
    void before(List<T> list, T e) {
      list.addLast(e);
    }

    @BeforeTemplate
    void before2(List<T> list, T e) {
      list.add(list.size(), e);
    }

    @AfterTemplate
    void after(List<T> list, T e) {
      list.add(e);
    }
  }

  /**
   * Prefer {@link List#removeFirst()} over less idiomatic alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite changes the exception thrown for empty lists from
   * {@link IndexOutOfBoundsException} to {@link NoSuchElementException}.
   */
  static final class ListRemoveFirst<T> {
    @BeforeTemplate
    T before(List<T> list) {
      return list.remove(0);
    }

    @AfterTemplate
    T after(List<T> list) {
      return list.removeFirst();
    }
  }

  /**
   * Prefer {@link List#removeLast()} over less idiomatic alternatives.
   *
   * <p><strong>Warning:</strong> this rewrite changes the exception thrown for empty lists from
   * {@link IndexOutOfBoundsException} to {@link NoSuchElementException}.
   */
  static final class ListRemoveLast<T> {
    @BeforeTemplate
    T before(List<T> list) {
      return list.remove(list.size() - 1);
    }

    @AfterTemplate
    T after(List<T> list) {
      return list.removeLast();
    }
  }

  /** Prefer {@link SortedSet#first()} over less idiomatic alternatives. */
  static final class SortedSetFirst<T> {
    @BeforeTemplate
    T before(SortedSet<T> sortedSet) {
      return sortedSet.getFirst();
    }

    @AfterTemplate
    T after(SortedSet<T> sortedSet) {
      return sortedSet.first();
    }
  }

  /** Prefer {@link SortedSet#last()} over less idiomatic alternatives. */
  static final class SortedSetLast<T> {
    @BeforeTemplate
    T before(SortedSet<T> sortedSet) {
      return sortedSet.getLast();
    }

    @AfterTemplate
    T after(SortedSet<T> sortedSet) {
      return sortedSet.last();
    }
  }

  // XXX: collection.stream().noneMatch(e -> e.equals(other))
  // ^ This is !collection.contains(other). Do we already rewrite variations on this?
}
