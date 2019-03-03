package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with (arbitrary) collections. */
final class CollectionTemplates {
  private CollectionTemplates() {}

  /** Prefer {@link Collection#isEmpty()} over alternatives that consult the collection's size. */
  static final class CollectionIsEmpty<T> {
    @BeforeTemplate
    boolean before(Collection<T> collection) {
      return Refaster.anyOf(collection.size() == 0, collection.size() <= 0, collection.size() < 1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(Collection<T> collection) {
      return collection.isEmpty();
    }
  }

  /**
   * Don't call {@link Iterables#addAll(Collection, Iterable)} when the elements to be added are
   * already part of a {@link Collection}.
   */
  static final class CollectionAddAllFromCollection<T, S extends T> {
    @BeforeTemplate
    boolean before(Collection<T> addTo, Collection<S> elementsToAdd) {
      return Iterables.addAll(addTo, elementsToAdd);
    }

    @AfterTemplate
    boolean after(Collection<T> addTo, Collection<S> elementsToAdd) {
      return addTo.addAll(elementsToAdd);
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

  /**
   * Don't call {@link ImmutableCollection#asList()} if the result is going to be streamed; stream
   * directly.
   */
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
}
