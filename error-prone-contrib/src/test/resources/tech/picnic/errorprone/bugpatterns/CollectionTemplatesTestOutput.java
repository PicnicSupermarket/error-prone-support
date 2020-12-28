package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

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
        ImmutableSet.of(7).isEmpty());
  }

  int testCollectionSize() {
    return ImmutableSet.of().size();
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

  void testCollectionRemoveAllFromCollectionBlock() {
    new ArrayList<>().removeAll(ImmutableSet.of("foo"));
    new ArrayList<Number>().removeAll(ImmutableSet.of(1));
    new ArrayList<Number>().removeAll(ImmutableSet.of(2));
  }

  ArrayList<String> testNewArrayListFromCollection() {
    return new ArrayList<>(ImmutableList.of("foo"));
  }

  Stream<Integer> testImmutableCollectionAsListToStream() {
    return ImmutableSet.of(1).stream();
  }

  ImmutableList<Integer> testImmutableCollectionAsList() {
    return ImmutableSet.of(1).asList();
  }

  ImmutableSet<Boolean> testImmutableCollectionAsListIsEmpty() {
    return ImmutableSet.of(ImmutableSet.of(1).isEmpty(), ImmutableSet.of().isEmpty());
  }

  ImmutableSet<Boolean> testImmutableCollectionAsListContainsNull() {
    return ImmutableSet.of(ImmutableSet.of(1).contains(null), ImmutableSet.of().contains(null));
  }

  Stream<Integer> testImmutableCollectionAsListParallelStream() {
    return ImmutableSet.of(1).parallelStream();
  }

  int testImmutableCollectionAsListSize() {
    return ImmutableSet.of(1).size();
  }

  String testImmutableCollectionAsListToString() {
    return ImmutableSet.of(1).toString();
  }

  ImmutableSet<Object[]> testImmutableCollectionAsListToNewArrayObject() {
    return ImmutableSet.of(
        ImmutableSet.of(1).toArray(Object[]::new),
        ImmutableSet.of().toArray(Object[]::new),
        ImmutableSet.of(1).toArray(Object[]::new),
        ImmutableSet.of().toArray(Object[]::new));
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
        new LinkedList<String>().stream().findFirst(),
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
