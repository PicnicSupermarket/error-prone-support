package tech.picnic.errorprone.refasterrules;

import static java.util.stream.Collectors.toUnmodifiableSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
        ImmutableSet.of(1).size() == 0,
        ImmutableSet.of(2).size() <= 0,
        ImmutableSet.of(3).size() < 1,
        ImmutableSet.of(4).size() != 0,
        ImmutableSet.of(5).size() > 0,
        ImmutableSet.of(6).size() >= 1,
        Iterables.isEmpty(ImmutableSet.of(7)),
        ImmutableSet.of(8).stream().findAny().isEmpty(),
        ImmutableSet.of(9).stream().findFirst().isEmpty(),
        ImmutableSet.of(10).asList().isEmpty());
  }

  ImmutableSet<Integer> testCollectionSize() {
    return ImmutableSet.of(Iterables.size(ImmutableSet.of(1)), ImmutableSet.of(2).asList().size());
  }

  boolean testCollectionContains() {
    return ImmutableSet.of("foo").stream().anyMatch("bar"::equals);
  }

  boolean testCollectionAddAllToCollectionExpression() {
    return Iterables.addAll(new ArrayList<>(), ImmutableSet.of("foo"));
  }

  void testCollectionAddAllToCollectionBlock() {
    ImmutableSet.of("foo").forEach(new ArrayList<>()::add);
    for (Number element : ImmutableSet.of(1)) {
      new ArrayList<Number>().add(element);
    }
    for (Integer element : ImmutableSet.of(2)) {
      new ArrayList<Number>().add(element);
    }
  }

  boolean testCollectionRemoveAllFromCollectionExpression() {
    return Iterables.removeAll(new ArrayList<>(), ImmutableSet.of("foo"));
  }

  void testCollectionRemoveAllFromCollectionBlock() {
    ImmutableSet.of("foo").forEach(new HashSet<>()::remove);
    for (Number element : ImmutableList.of(1)) {
      new HashSet<Number>().remove(element);
    }
    for (Integer element : ImmutableSet.of(2)) {
      new HashSet<Number>().remove(element);
    }
  }

  Stream<Integer> testSetStream() {
    return ImmutableSet.of(1).stream().distinct();
  }

  Set<Integer> testSetOfVarargs() {
    return Stream.of(1, 2).collect(toUnmodifiableSet());
  }

  ArrayList<String> testNewArrayListFromCollection() {
    return Lists.newArrayList(ImmutableList.of("foo"));
  }

  Stream<Integer> testImmutableCollectionStream() {
    return ImmutableSet.of(1).asList().stream();
  }

  ImmutableList<Integer> testImmutableCollectionAsList() {
    return ImmutableList.copyOf(ImmutableSet.of(1));
  }

  boolean testImmutableCollectionContains() {
    return ImmutableSet.of(1).asList().contains("foo");
  }

  Stream<Integer> testImmutableCollectionParallelStream() {
    return ImmutableSet.of(1).asList().parallelStream();
  }

  String testImmutableCollectionToString() {
    return ImmutableSet.of(1).asList().toString();
  }

  List<String> testArraysAsList() {
    return Arrays.stream(new String[0]).toList();
  }

  ImmutableSet<Object[]> testCollectionToArray() {
    return ImmutableSet.of(
        ImmutableSet.of(1).toArray(new Object[1]),
        ImmutableSet.of(2).toArray(Object[]::new),
        ImmutableSet.of(3).asList().toArray());
  }

  Integer[] testImmutableCollectionToArrayWithArray() {
    return ImmutableSet.of(1).asList().toArray(new Integer[0]);
  }

  Integer[] testImmutableCollectionToArrayWithGenerator() {
    return ImmutableSet.of(1).asList().toArray(Integer[]::new);
  }

  ImmutableSet<Iterator<Integer>> testCollectionIterator() {
    return ImmutableSet.of(
        ImmutableSet.of(1).stream().iterator(), ImmutableSet.of(2).asList().iterator());
  }

  ImmutableSet<Optional<Integer>> testOptionalFirstCollectionElement() {
    return ImmutableSet.of(
        ImmutableSet.of(0).stream().findAny(),
        ImmutableSet.of(1).isEmpty()
            ? Optional.empty()
            : Optional.of(ImmutableSet.of(1).iterator().next()),
        ImmutableList.of(2).isEmpty()
            ? Optional.empty()
            : Optional.of(ImmutableList.of(2).getFirst()),
        ImmutableSortedSet.of(3).isEmpty()
            ? Optional.empty()
            : Optional.of(ImmutableSortedSet.of(3).first()),
        !ImmutableSet.of(1).isEmpty()
            ? Optional.of(ImmutableSet.of(1).iterator().next())
            : Optional.empty(),
        !ImmutableList.of(2).isEmpty()
            ? Optional.of(ImmutableList.of(2).getFirst())
            : Optional.empty(),
        !ImmutableSortedSet.of(3).isEmpty()
            ? Optional.of(ImmutableSortedSet.of(3).first())
            : Optional.empty());
  }

  ImmutableSet<Optional<String>> testOptionalFirstQueueElement() {
    return ImmutableSet.of(
        new LinkedList<String>().stream().findFirst(),
        new LinkedList<String>().isEmpty()
            ? Optional.empty()
            : Optional.of(new LinkedList<String>().peek()),
        new LinkedList<String>().isEmpty()
            ? Optional.empty()
            : Optional.ofNullable(new LinkedList<String>().peek()),
        !new LinkedList<String>().isEmpty()
            ? Optional.of(new LinkedList<String>().peek())
            : Optional.empty(),
        !new LinkedList<String>().isEmpty()
            ? Optional.ofNullable(new LinkedList<String>().peek())
            : Optional.empty());
  }

  ImmutableSet<Optional<String>> testRemoveOptionalFirstNavigableSetElement() {
    return ImmutableSet.of(
        new TreeSet<String>().isEmpty()
            ? Optional.empty()
            : Optional.of(new TreeSet<String>().pollFirst()),
        new TreeSet<String>().isEmpty()
            ? Optional.empty()
            : Optional.ofNullable(new TreeSet<String>().pollFirst()),
        !new TreeSet<String>().isEmpty()
            ? Optional.of(new TreeSet<String>().pollFirst())
            : Optional.empty(),
        !new TreeSet<String>().isEmpty()
            ? Optional.ofNullable(new TreeSet<String>().pollFirst())
            : Optional.empty());
  }

  ImmutableSet<Optional<String>> testRemoveOptionalFirstQueueElement() {
    return ImmutableSet.of(
        new LinkedList<String>().isEmpty()
            ? Optional.empty()
            : Optional.of(new LinkedList<String>().poll()),
        new LinkedList<String>().isEmpty()
            ? Optional.empty()
            : Optional.of(new LinkedList<String>().remove()),
        new LinkedList<String>().isEmpty()
            ? Optional.empty()
            : Optional.ofNullable(new LinkedList<String>().poll()),
        new LinkedList<String>().isEmpty()
            ? Optional.empty()
            : Optional.ofNullable(new LinkedList<String>().remove()),
        !new LinkedList<String>().isEmpty()
            ? Optional.of(new LinkedList<String>().poll())
            : Optional.empty(),
        !new LinkedList<String>().isEmpty()
            ? Optional.of(new LinkedList<String>().remove())
            : Optional.empty(),
        !new LinkedList<String>().isEmpty()
            ? Optional.ofNullable(new LinkedList<String>().poll())
            : Optional.empty(),
        !new LinkedList<String>().isEmpty()
            ? Optional.ofNullable(new LinkedList<String>().remove())
            : Optional.empty());
  }

  void testCollectionForEach() {
    ImmutableSet.of(1).stream().forEach(String::valueOf);
  }

  String testCollectionIteratorNext() {
    return ImmutableSet.of("foo").stream().findFirst().orElseThrow();
  }

  ImmutableSet<String> testSequencedCollectionGetFirst() {
    return ImmutableSet.of(
        ImmutableList.of("foo").iterator().next(), ImmutableList.of("bar").get(0));
  }

  ImmutableSet<String> testSequencedCollectionGetLast() {
    return ImmutableSet.of(
        ImmutableList.of("foo").reversed().getFirst(),
        Streams.findLast(ImmutableList.of("bar").stream()).orElseThrow(),
        ImmutableList.of("baz").get(ImmutableList.of("baz").size() - 1));
  }

  void testListAddFirst() {
    new ArrayList<String>().add(0, "foo");
  }

  void testListAdd() {
    new ArrayList<String>(0).addLast("bar");
    new ArrayList<String>(1).add(new ArrayList<String>(1).size(), "qux");
  }

  String testListRemoveFirst() {
    return new ArrayList<String>().remove(0);
  }

  String testListRemoveLast() {
    return new ArrayList<String>().remove(new ArrayList<String>().size() - 1);
  }

  String testSortedSetFirst() {
    return ImmutableSortedSet.of("foo").getFirst();
  }

  String testSortedSetLast() {
    return ImmutableSortedSet.of("foo").getLast();
  }
}
