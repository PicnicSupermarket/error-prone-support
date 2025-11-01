package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.NotMatches;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
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

/** Refaster rules related to expressions dealing with (arbitrary) collections. */
// XXX: There are other Guava `Iterables` methods that should not be called if the input is known to
// be a `Collection`. Add those here.
@OnlineDocumentation
final class CollectionRules {
  private CollectionRules() {}

  /**
   * Prefer {@link Collection#isEmpty()} over alternatives that consult the collection's size or are
   * otherwise more contrived.
   */
  static final class CollectionIsEmpty<T> {
    @BeforeTemplate
    @SuppressWarnings({
      "java:S1155" /* This violation will be rewritten. */,
      "LexicographicalAnnotationAttributeListing" /* `key-*` entry must remain last. */,
      "OptionalFirstCollectionElement" /* This is a more specific template. */,
      "StreamFindAnyIsEmpty" /* This is a more specific template. */,
      "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
    })
    boolean before(Collection<T> collection) {
      return Refaster.anyOf(
          collection.size() == 0,
          collection.size() <= 0,
          collection.size() < 1,
          Iterables.isEmpty(collection),
          collection.stream().findAny().isEmpty(),
          collection.stream().findFirst().isEmpty());
    }

    @BeforeTemplate
    boolean before(ImmutableCollection<T> collection) {
      return collection.asList().isEmpty();
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(Collection<T> collection) {
      return collection.isEmpty();
    }
  }

  /** Prefer {@link Collection#size()} over more contrived alternatives. */
  static final class CollectionSize<T> {
    @BeforeTemplate
    int before(Collection<T> collection) {
      return Iterables.size(collection);
    }

    @BeforeTemplate
    int before(ImmutableCollection<T> collection) {
      return collection.asList().size();
    }

    @AfterTemplate
    int after(Collection<T> collection) {
      return collection.size();
    }
  }

  /** Prefer {@link Collection#contains(Object)} over more contrived alternatives. */
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
   * Don't call {@link Iterables#addAll(Collection, Iterable)} when the elements to be added are
   * already part of a {@link Collection}.
   */
  static final class CollectionAddAllToCollectionExpression<T, S extends T> {
    @BeforeTemplate
    boolean before(Collection<T> addTo, Collection<S> elementsToAdd) {
      return Iterables.addAll(addTo, elementsToAdd);
    }

    @AfterTemplate
    boolean after(Collection<T> addTo, Collection<S> elementsToAdd) {
      return addTo.addAll(elementsToAdd);
    }
  }

  static final class CollectionAddAllToCollectionBlock<T, S extends T> {
    @BeforeTemplate
    void before(Collection<T> addTo, Collection<S> elementsToAdd) {
      elementsToAdd.forEach(addTo::add);
    }

    @BeforeTemplate
    void before2(Collection<T> addTo, Collection<S> elementsToAdd) {
      for (T element : elementsToAdd) {
        addTo.add(element);
      }
    }

    // XXX: This method is identical to `before2` except for the loop type. Make Refaster smarter so
    // that this is supported out of the box.
    @BeforeTemplate
    void before3(Collection<T> addTo, Collection<S> elementsToAdd) {
      for (S element : elementsToAdd) {
        addTo.add(element);
      }
    }

    @AfterTemplate
    void after(Collection<T> addTo, Collection<S> elementsToAdd) {
      addTo.addAll(elementsToAdd);
    }
  }

  /**
   * Don't call {@link Iterables#removeAll(Iterable, Collection)} when the elements to be removed
   * are already part of a {@link Collection}.
   */
  static final class CollectionRemoveAllFromCollectionExpression<T, S extends T> {
    @BeforeTemplate
    boolean before(Collection<T> removeTo, Collection<S> elementsToRemove) {
      return Iterables.removeAll(removeTo, elementsToRemove);
    }

    @AfterTemplate
    boolean after(Collection<T> removeTo, Collection<S> elementsToRemove) {
      return removeTo.removeAll(elementsToRemove);
    }
  }

  static final class CollectionRemoveAllFromCollectionBlock<T, S extends T> {
    @BeforeTemplate
    void before(Collection<T> removeFrom, Collection<S> elementsToRemove) {
      elementsToRemove.forEach(removeFrom::remove);
    }

    @BeforeTemplate
    void before2(Collection<T> removeFrom, Collection<S> elementsToRemove) {
      for (T element : elementsToRemove) {
        removeFrom.remove(element);
      }
    }

    // XXX: This method is identical to `before2` except for the loop type. Make Refaster smarter so
    // that this is supported out of the box. After doing so, also drop the `S extends T` type
    // constraint; ideally this check applies to any `S`.
    @BeforeTemplate
    void before3(Collection<T> removeFrom, Collection<S> elementsToRemove) {
      for (S element : elementsToRemove) {
        removeFrom.remove(element);
      }
    }

    @AfterTemplate
    void after(Collection<T> removeFrom, Collection<S> elementsToRemove) {
      removeFrom.removeAll(elementsToRemove);
    }
  }

  /** Don't unnecessarily call {@link Stream#distinct()} on an already-unique stream of elements. */
  // XXX: This rule assumes that the `Set` relies on `Object#equals`, rather than a custom
  // equivalence relation.
  // XXX: Expressions that drop or reorder elements from the stream, such as `.filter`, `.skip` and
  // `sorted`, can similarly be simplified. Covering all cases is better done using an Error Prone
  // check.
  static final class SetStream<T> {
    @BeforeTemplate
    Stream<?> before(Set<T> set) {
      return set.stream().distinct();
    }

    @AfterTemplate
    Stream<?> after(Set<T> set) {
      return set.stream();
    }
  }

  /** Prefer {@link ArrayList#ArrayList(Collection)} over the Guava alternative. */
  @SuppressWarnings(
      "NonApiType" /* Matching against `List` would unnecessarily constrain the rule. */)
  static final class NewArrayListFromCollection<T> {
    @BeforeTemplate
    ArrayList<T> before(Collection<T> collection) {
      return Lists.newArrayList(collection);
    }

    @AfterTemplate
    ArrayList<T> after(Collection<T> collection) {
      return new ArrayList<>(collection);
    }
  }

  /** Prefer {@link ImmutableCollection#asList()} over the more verbose alternative. */
  static final class ImmutableCollectionAsList<T> {
    @BeforeTemplate
    ImmutableList<T> before(ImmutableCollection<T> collection) {
      return ImmutableList.copyOf(collection);
    }

    @AfterTemplate
    ImmutableList<T> after(ImmutableCollection<T> collection) {
      return collection.asList();
    }
  }

  /**
   * Don't call {@link ImmutableCollection#asList()} if the result is going to be streamed; stream
   * directly.
   */
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

  /**
   * Don't call {@link ImmutableCollection#asList()} if {@link Collection#contains(Object)} is
   * called on the result; call it directly.
   */
  static final class ImmutableCollectionContains<T, S> {
    @BeforeTemplate
    boolean before(ImmutableCollection<T> collection, S elem) {
      return collection.asList().contains(elem);
    }

    @AfterTemplate
    boolean after(ImmutableCollection<T> collection, S elem) {
      return collection.contains(elem);
    }
  }

  /**
   * Don't call {@link ImmutableCollection#asList()} if {@link ImmutableCollection#parallelStream()}
   * is called on the result; call it directly.
   */
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

  /**
   * Don't call {@link ImmutableCollection#asList()} if {@link ImmutableCollection#toString()} is
   * called on the result; call it directly.
   */
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

  /** Prefer {@link Arrays#asList(Object[])} over more contrived alternatives. */
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

  /** Prefer calling {@link Collection#toArray()} over more contrived alternatives. */
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

  /**
   * Don't call {@link ImmutableCollection#asList()} if {@link
   * ImmutableCollection#toArray(Object[])}` is called on the result; call it directly.
   */
  static final class ImmutableCollectionToArrayWithArray<T, S> {
    @BeforeTemplate
    Object[] before(ImmutableCollection<T> collection, S[] array) {
      return collection.asList().toArray(array);
    }

    @AfterTemplate
    Object[] after(ImmutableCollection<T> collection, S[] array) {
      return collection.toArray(array);
    }
  }

  /**
   * Don't call {@link ImmutableCollection#asList()} if {@link
   * ImmutableCollection#toArray(IntFunction)}} is called on the result; call it directly.
   */
  static final class ImmutableCollectionToArrayWithGenerator<T, S> {
    @BeforeTemplate
    S[] before(ImmutableCollection<T> collection, IntFunction<S[]> generator) {
      return collection.asList().toArray(generator);
    }

    @AfterTemplate
    S[] after(ImmutableCollection<T> collection, IntFunction<S[]> generator) {
      return collection.toArray(generator);
    }
  }

  /** Prefer {@link Collection#iterator()} over more contrived or less efficient alternatives. */
  static final class CollectionIterator<T> {
    @BeforeTemplate
    Iterator<T> before(Collection<T> collection) {
      return collection.stream().iterator();
    }

    @BeforeTemplate
    Iterator<T> before(ImmutableCollection<T> collection) {
      return collection.asList().iterator();
    }

    @AfterTemplate
    Iterator<T> after(Collection<T> collection) {
      return collection.iterator();
    }
  }

  /**
   * Don't use the ternary operator to extract the first element of a possibly-empty {@link
   * Collection} as an {@link Optional}, and (when applicable) prefer {@link Stream#findFirst()}
   * over {@link Stream#findAny()} to communicate that the collection's first element (if any,
   * according to iteration order) will be returned.
   */
  static final class OptionalFirstCollectionElement<T> {
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
   * Avoid contrived constructions when peeking at the first element of a possibly empty {@link
   * Queue}.
   */
  static final class OptionalFirstQueueElement<T> {
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

  /**
   * Avoid contrived constructions when extracting the first element from a possibly empty {@link
   * NavigableSet}.
   */
  static final class RemoveOptionalFirstNavigableSetElement<T> {
    @BeforeTemplate
    Optional<T> before(NavigableSet<T> set) {
      return set.isEmpty()
          ? Optional.empty()
          : Refaster.anyOf(Optional.of(set.pollFirst()), Optional.ofNullable(set.pollFirst()));
    }

    @AfterTemplate
    Optional<T> after(NavigableSet<T> set) {
      return Optional.ofNullable(set.pollFirst());
    }
  }

  /**
   * Avoid contrived constructions when extracting the first element from a possibly empty {@link
   * Queue}.
   */
  static final class RemoveOptionalFirstQueueElement<T> {
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

  /** Prefer {@link Collection#forEach(Consumer)} over more contrived alternatives. */
  static final class CollectionForEach<T> {
    @BeforeTemplate
    void before(Collection<T> collection, Consumer<? super T> consumer) {
      collection.stream().forEach(consumer);
    }

    @AfterTemplate
    void after(Collection<T> collection, Consumer<? super T> consumer) {
      collection.forEach(consumer);
    }
  }

  /** Prefer {@code collection.iterator().next()} over more contrived alternatives. */
  static final class CollectionIteratorNext<S, T extends S> {
    @BeforeTemplate
    S before(Collection<T> collection) {
      return collection.stream().findFirst().orElseThrow();
    }

    @AfterTemplate
    S after(Collection<T> collection) {
      return collection.iterator().next();
    }
  }

  /** Prefer {@link SequencedCollection#getFirst()} over less idiomatic alternatives. */
  static final class SequencedCollectionGetFirst<S, T extends S> {
    @BeforeTemplate
    S before(SequencedCollection<T> collection) {
      return collection.iterator().next();
    }

    @BeforeTemplate
    S before(List<T> collection) {
      return collection.get(0);
    }

    @AfterTemplate
    S after(SequencedCollection<T> collection) {
      return collection.getFirst();
    }
  }

  /** Prefer {@link SequencedCollection#getLast()} over less idiomatic alternatives. */
  static final class SequencedCollectionGetLast<S, T extends S> {
    @BeforeTemplate
    S before(SequencedCollection<T> collection) {
      return Refaster.anyOf(
          collection.reversed().getFirst(), Streams.findLast(collection.stream()).orElseThrow());
    }

    @BeforeTemplate
    S before(List<T> collection) {
      return collection.get(collection.size() - 1);
    }

    @AfterTemplate
    S after(SequencedCollection<T> collection) {
      return collection.getLast();
    }
  }

  /** Prefer {@link List#addFirst(Object)} over less idiomatic alternatives. */
  static final class ListAddFirst<S, T extends S> {
    @BeforeTemplate
    void before(List<S> list, T element) {
      list.add(0, element);
    }

    @AfterTemplate
    void after(List<S> list, T element) {
      list.addFirst(element);
    }
  }

  /** Prefer {@link List#add(Object)} over less idiomatic alternatives. */
  static final class ListAdd<S, T extends S> {
    @BeforeTemplate
    void before(List<S> list, T element) {
      list.addLast(element);
    }

    @BeforeTemplate
    void before2(List<S> list, T element) {
      list.add(list.size(), element);
    }

    @AfterTemplate
    void after(List<S> list, T element) {
      list.add(element);
    }
  }

  /** Prefer {@link List#removeFirst()}} over less idiomatic alternatives. */
  // XXX: This rule changes the exception thrown for empty lists from `IndexOutOfBoundsException` to
  // `NoSuchElementException`.
  static final class ListRemoveFirst<S, T extends S> {
    @BeforeTemplate
    S before(List<T> list) {
      return list.remove(0);
    }

    @AfterTemplate
    S after(List<T> list) {
      return list.removeFirst();
    }
  }

  /** Prefer {@link List#removeLast()}} over less idiomatic alternatives. */
  // XXX: This rule changes the exception thrown for empty lists from `IndexOutOfBoundsException` to
  // `NoSuchElementException`.
  static final class ListRemoveLast<S, T extends S> {
    @BeforeTemplate
    S before(List<T> list) {
      return list.remove(list.size() - 1);
    }

    @AfterTemplate
    S after(List<T> list) {
      return list.removeLast();
    }
  }

  /** Prefer {@link SortedSet#first()} over more verbose alternatives. */
  static final class SortedSetFirst<S, T extends S> {
    @BeforeTemplate
    S before(SortedSet<T> set) {
      return set.getFirst();
    }

    @AfterTemplate
    S after(SortedSet<T> set) {
      return set.first();
    }
  }

  /** Prefer {@link SortedSet#last()} over more verbose alternatives. */
  static final class SortedSetLast<S, T extends S> {
    @BeforeTemplate
    S before(SortedSet<T> set) {
      return set.getLast();
    }

    @AfterTemplate
    S after(SortedSet<T> set) {
      return set.last();
    }
  }

  // XXX: collection.stream().noneMatch(e -> e.equals(other))
  // ^ This is !collection.contains(other). Do we already rewrite variations on this?
}
