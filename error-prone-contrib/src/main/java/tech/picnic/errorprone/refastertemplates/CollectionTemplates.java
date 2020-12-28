package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Queue;
import java.util.SortedSet;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with (arbitrary) collections. */
// XXX: There are other Guava `Iterables` methods that should not be called if the input is known to
// be a `Collection`. Add those here.
final class CollectionTemplates {
  private CollectionTemplates() {}

  /**
   * Prefer {@link Collection#isEmpty()} over alternatives that consult the collection's size or are
   * otherwise more contrived.
   */
  static final class CollectionIsEmpty<T> {
    @BeforeTemplate
    boolean before(Collection<T> collection) {
      return Refaster.anyOf(
          collection.size() == 0,
          collection.size() <= 0,
          collection.size() < 1,
          Iterables.isEmpty(collection));
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

    @AfterTemplate
    int after(Collection<T> collection) {
      return collection.size();
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
    void before(Collection<T> removeTo, Collection<S> elementsToRemove) {
      elementsToRemove.forEach(removeTo::remove);
    }

    @BeforeTemplate
    void before2(Collection<T> removeTo, Collection<S> elementsToRemove) {
      for (T element : elementsToRemove) {
        removeTo.remove(element);
      }
    }

    // XXX: This method is identical to `before2` except for the loop type. Make Refaster smarter so
    // that this is supported out of the box.
    @BeforeTemplate
    void before3(Collection<T> removeTo, Collection<S> elementsToRemove) {
      for (S element : elementsToRemove) {
        removeTo.remove(element);
      }
    }

    @AfterTemplate
    void after(Collection<T> removeTo, Collection<S> elementsToRemove) {
      removeTo.removeAll(elementsToRemove);
    }
  }

  /** Prefer {@link ArrayList#ArrayList(Collection)} over the Guava alternative. */
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
  // XXX: Similar rules could be implemented for the following variants:
  // collection.asList().iterator();
  // collection.asList().size();
  // collection.asList().toArray();
  // collection.asList().toArray(Object[]::new);
  // collection.asList().toArray(new Object[0]);
  // collection.asList().toString();
  static final class ImmutableCollectionAsListToStream<T> {
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
   * Don't call {@link ImmutableCollection#asList()} if `isEmpty()` is called on the result; call it
   * directly.
   */
  static final class ImmutableCollectionAsListIsEmpty<T> {
    @BeforeTemplate
    boolean before(ImmutableCollection<T> collection) {
      // XXX: @Stephan this one can also fit in at: CollectionIsEmpty in the @Before, ~line 25.
      // I don't know what the convention would be for that.
      return collection.asList().isEmpty();
    }

    @AfterTemplate
    boolean after(ImmutableCollection<T> collection) {
      return collection.isEmpty();
    }
  }

  /**
   * Don't call {@link ImmutableCollection#asList()} if `contains(null)` is called on the result;
   * call it directly.
   */
  static final class ImmutableCollectionAsListContainsNull<T> {
    @BeforeTemplate
    boolean before(ImmutableCollection<T> collection) {
      return collection.asList().contains(null);
    }

    @AfterTemplate
    boolean after(ImmutableCollection<T> collection) {
      return collection.contains(null);
    }
  }

  /**
   * Don't use the ternary operator to extract the first element of a possibly-empty {@link
   * Collection} as an {@link Optional}.
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
      return collection.isEmpty() ? Optional.empty() : Optional.of(collection.get(0));
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

  // XXX: collection.stream().noneMatch(e -> e.equals(other))
  // ^ This is !collection.contains(other). Do we already rewrite variations on this?
}
