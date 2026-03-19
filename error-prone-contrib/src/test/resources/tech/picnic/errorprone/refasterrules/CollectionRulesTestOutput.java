package tech.picnic.errorprone.refasterrules;

import static java.util.Collections.disjoint;
import static java.util.stream.Collectors.toUnmodifiableSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class CollectionRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Iterables.class, Lists.class, Streams.class, toUnmodifiableSet());
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
        ImmutableSet.of(8).isEmpty(),
        ImmutableSet.of(9).isEmpty(),
        ImmutableSet.of(10).isEmpty(),
        Sets.intersection(ImmutableSet.of(11), ImmutableSet.of(12)).isEmpty());
  }

  ImmutableSet<Integer> testCollectionSize() {
    return ImmutableSet.of(ImmutableSet.of(1).size(), ImmutableSet.of(2).size());
  }

  boolean testCollectionContains() {
    return ImmutableSet.of("foo").contains("bar");
  }

  ImmutableSet<Boolean> testDisjoint() {
    return ImmutableSet.of(
        disjoint(ImmutableSet.of(1), ImmutableSet.of(2)),
        disjoint(ImmutableSet.of(3), ImmutableSet.of(4)),
        disjoint(ImmutableList.of(5), ImmutableList.of(6)),
        disjoint(ImmutableList.of(7), ImmutableList.of(8)),
        disjoint(ImmutableList.of(9), ImmutableList.of(10)),
        disjoint(ImmutableList.of(11), ImmutableList.of(12)));
  }

  boolean testCollectionAddAllExpression() {
    return new ArrayList<>().addAll(ImmutableSet.of("foo"));
  }

  void testCollectionAddAllBlock() {
    new ArrayList<>().addAll(ImmutableSet.of("foo"));
    new ArrayList<Number>().addAll(ImmutableSet.of(1));
    new ArrayList<Number>().addAll(ImmutableSet.of(2));
  }

  boolean testCollectionRemoveAllExpression() {
    return new ArrayList<>().removeAll(ImmutableSet.of("foo"));
  }

  void testCollectionRemoveAllBlock() {
    new HashSet<>().removeAll(ImmutableSet.of("foo"));
    new HashSet<Number>().removeAll(ImmutableList.of(1));
    new HashSet<Number>().removeAll(ImmutableSet.of(2));
  }

  Stream<Integer> testSetStream() {
    return ImmutableSet.of(1).stream();
  }

  Set<Integer> testSetOf() {
    return Set.of(1, 2);
  }

  ArrayList<String> testNewArrayList() {
    return new ArrayList<>(ImmutableList.of("foo"));
  }

  ImmutableList<Integer> testImmutableCollectionAsList() {
    return ImmutableSet.of(1).asList();
  }

  Stream<Integer> testImmutableCollectionStream() {
    return ImmutableSet.of(1).stream();
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

  List<String> testArraysAsList() {
    return Arrays.asList(new String[0]);
  }

  ImmutableSet<Object[]> testCollectionToArray() {
    return ImmutableSet.of(
        ImmutableSet.of(1).toArray(), ImmutableSet.of(2).toArray(), ImmutableSet.of(3).toArray());
  }

  Integer[] testImmutableCollectionToArrayObject() {
    return ImmutableSet.of(1).toArray(new Integer[0]);
  }

  Integer[] testImmutableCollectionToArrayIntFunction() {
    return ImmutableSet.of(1).toArray(Integer[]::new);
  }

  ImmutableSet<Iterator<Integer>> testCollectionIterator() {
    return ImmutableSet.of(ImmutableSet.of(1).iterator(), ImmutableSet.of(2).iterator());
  }

  ImmutableSet<Optional<Integer>> testCollectionStreamFindFirst() {
    return ImmutableSet.of(
        ImmutableSet.of(1).stream().findFirst(),
        ImmutableSet.of(2).stream().findFirst(),
        ImmutableList.of(3).stream().findFirst(),
        ImmutableSortedSet.of(4).stream().findFirst(),
        ImmutableSet.of(5).stream().findFirst(),
        ImmutableList.of(6).stream().findFirst(),
        ImmutableSortedSet.of(7).stream().findFirst());
  }

  ImmutableSet<Optional<String>> testOptionalOfNullableQueuePeek() {
    return ImmutableSet.of(
        Optional.ofNullable(new LinkedList<String>().peek()),
        Optional.ofNullable(new LinkedList<String>().peek()),
        Optional.ofNullable(new LinkedList<String>().peek()),
        Optional.ofNullable(new LinkedList<String>().peek()),
        Optional.ofNullable(new LinkedList<String>().peek()));
  }

  ImmutableSet<Optional<String>> testOptionalOfNullableNavigableSetPollFirst() {
    return ImmutableSet.of(
        Optional.ofNullable(new TreeSet<String>().pollFirst()),
        Optional.ofNullable(new TreeSet<String>().pollFirst()),
        Optional.ofNullable(new TreeSet<String>().pollFirst()),
        Optional.ofNullable(new TreeSet<String>().pollFirst()));
  }

  ImmutableSet<Optional<String>> testOptionalOfNullableQueuePoll() {
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

  void testCollectionForEach() {
    ImmutableSet.of(1).forEach(String::valueOf);
  }

  String testCollectionIteratorNext() {
    return ImmutableSet.of("foo").iterator().next();
  }

  ImmutableSet<String> testSequencedCollectionGetFirst() {
    return ImmutableSet.of(ImmutableList.of("foo").getFirst(), ImmutableList.of("bar").getFirst());
  }

  ImmutableSet<String> testSequencedCollectionGetLast() {
    return ImmutableSet.of(
        ImmutableList.of("foo").getLast(),
        ImmutableList.of("bar").getLast(),
        ImmutableList.of("baz").getLast());
  }

  void testListAddFirst() {
    new ArrayList<String>().addFirst("foo");
  }

  void testListAdd() {
    new ArrayList<String>(0).add("foo");
    new ArrayList<String>(1).add("bar");
  }

  String testListRemoveFirst() {
    return new ArrayList<String>().removeFirst();
  }

  String testListRemoveLast() {
    return new ArrayList<String>().removeLast();
  }

  String testSortedSetFirst() {
    return ImmutableSortedSet.of("foo").first();
  }

  String testSortedSetLast() {
    return ImmutableSortedSet.of("foo").last();
  }
}
