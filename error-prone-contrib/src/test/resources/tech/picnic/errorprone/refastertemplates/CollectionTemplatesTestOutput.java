package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class CollectionTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Iterables.class, Lists.class);
  }

  ImmutableSet<Boolean> testCollectionIsEmpty() {
    return ImmutableSet.of(
        ImmutableSet.of(1).isEmpty(),
        ImmutableSet.of(2).isEmpty(),
        ImmutableSet.of(3).isEmpty(),
        !ImmutableSet.of(4).isEmpty(),
        !ImmutableSet.of(5).isEmpty(),
        !ImmutableSet.of(6).isEmpty(),
        ImmutableSet.of(7).isEmpty(),
        ImmutableSet.of(8).isEmpty());
  }

  ImmutableSet<Integer> testCollectionSize() {
    return ImmutableSet.of(ImmutableSet.of(1).size(), ImmutableSet.of(2).size());
  }

  boolean testCollectionAddAllToCollectionExpression() {
    return new ArrayList<>().addAll(ImmutableSet.of("foo"));
  }

  void testCollectionAddAllToCollectionBlock() {
    new ArrayList<>().addAll(ImmutableSet.of("foo"));
    new ArrayList<Number>().addAll(ImmutableSet.of(1));
    new ArrayList<Number>().addAll(ImmutableSet.of(2));
  }

  boolean testCollectionRemoveAllFromCollectionExpression() {
    return new ArrayList<>().removeAll(ImmutableSet.of("foo"));
  }

  void testSetRemoveAllCollection() {
    new HashSet<>().removeAll(ImmutableSet.of("foo"));
    new HashSet<Number>().removeAll(ImmutableList.of(1));
    new HashSet<Number>().removeAll(ImmutableSet.of(2));
  }

  ArrayList<String> testNewArrayListFromCollection() {
    return new ArrayList<>(ImmutableList.of("foo"));
  }

  Stream<Integer> testImmutableCollectionStream() {
    return ImmutableSet.of(1).stream();
  }

  ImmutableList<Integer> testImmutableCollectionAsList() {
    return ImmutableSet.of(1).asList();
  }

  boolean testImmutableCollectionContains() {
    return ImmutableSet.of(1).contains("foo");
  }

  Stream<Integer> testImmutableCollectionParallelStream() {
    return ImmutableSet.of(1).parallelStream();
  }

  String testImmutableCollectionToString() {
    return ImmutableSet.of(1).toString();
  }

  ImmutableSet<Object[]> testCollectionToArray() {
    return ImmutableSet.of(
        ImmutableSet.of(1).toArray(), ImmutableSet.of(2).toArray(), ImmutableSet.of(3).toArray());
  }

  Integer[] testImmutableCollectionToArrayWithArray() {
    return ImmutableSet.of(1).toArray(new Integer[0]);
  }

  Integer[] testImmutableCollectionToArrayWithGenerator() {
    return ImmutableSet.of(1).toArray(Integer[]::new);
  }

  Iterator<Integer> testImmutableCollectionIterator() {
    return ImmutableSet.of(1).iterator();
  }

  ImmutableSet<Optional<Integer>> testOptionalFirstCollectionElement() {
    return ImmutableSet.of(
        ImmutableSet.of(0).stream().findFirst(),
        ImmutableSet.of(1).stream().findFirst(),
        ImmutableList.of(2).stream().findFirst(),
        ImmutableSortedSet.of(3).stream().findFirst(),
        ImmutableSet.of(1).stream().findFirst(),
        ImmutableList.of(2).stream().findFirst(),
        ImmutableSortedSet.of(3).stream().findFirst());
  }

  ImmutableSet<Optional<String>> testOptionalFirstQueueElement() {
    return ImmutableSet.of(
        Optional.ofNullable(new LinkedList<String>().peek()),
        Optional.ofNullable(new LinkedList<String>().peek()),
        Optional.ofNullable(new LinkedList<String>().peek()),
        Optional.ofNullable(new LinkedList<String>().peek()),
        Optional.ofNullable(new LinkedList<String>().peek()));
  }

  ImmutableSet<Optional<String>> testRemoveOptionalFirstNavigableSetElement() {
    return ImmutableSet.of(
        Optional.ofNullable(new TreeSet<String>().pollFirst()),
        Optional.ofNullable(new TreeSet<String>().pollFirst()),
        Optional.ofNullable(new TreeSet<String>().pollFirst()),
        Optional.ofNullable(new TreeSet<String>().pollFirst()));
  }

  ImmutableSet<Optional<String>> testRemoveOptionalFirstQueueElement() {
    return ImmutableSet.of(
        Optional.ofNullable(new LinkedList<String>().poll()),
        Optional.ofNullable(new LinkedList<String>().poll()),
        Optional.ofNullable(new LinkedList<String>().poll()),
        Optional.ofNullable(new LinkedList<String>().poll()),
        Optional.ofNullable(new LinkedList<String>().poll()),
        Optional.ofNullable(new LinkedList<String>().poll()),
        Optional.ofNullable(new LinkedList<String>().poll()),
        Optional.ofNullable(new LinkedList<String>().poll()));
  }
}
