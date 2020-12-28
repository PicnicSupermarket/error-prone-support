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
        ImmutableSet.of(1).size() == 0,
        ImmutableSet.of(2).size() <= 0,
        ImmutableSet.of(3).size() < 1,
        ImmutableSet.of(4).size() != 0,
        ImmutableSet.of(5).size() > 0,
        ImmutableSet.of(6).size() >= 1,
        Iterables.isEmpty(ImmutableSet.of(7)));
  }

  int testCollectionSize() {
    return Iterables.size(ImmutableSet.of());
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
    ImmutableSet.of("foo").forEach(new ArrayList<>()::remove);
    for (Number element : ImmutableSet.of(1)) {
      new ArrayList<Number>().remove(element);
    }
    for (Integer element : ImmutableSet.of(2)) {
      new ArrayList<Number>().remove(element);
    }
  }

  ArrayList<String> testNewArrayListFromCollection() {
    return Lists.newArrayList(ImmutableList.of("foo"));
  }

  ImmutableList<Integer> testImmutableCollectionAsList() {
    return ImmutableList.copyOf(ImmutableSet.of(1));
  }

  Stream<Integer> testImmutableCollectionAsListToStream() {
    return ImmutableSet.of(1).asList().stream();
  }

  ImmutableSet<Boolean> testImmutableCollectionAsListIsEmpty() {
    return ImmutableSet.of(
        ImmutableSet.of(1).asList().isEmpty(), ImmutableSet.of().asList().isEmpty());
  }

  ImmutableSet<Boolean> testImmutableCollectionAsListContainsNull() {
    return ImmutableSet.of(
        ImmutableSet.of(1).asList().contains(null), ImmutableSet.of().asList().contains(null));
  }

  Stream<Integer> testImmutableCollectionAsListParallelStream() {
    return ImmutableSet.of(1).asList().parallelStream();
  }

  int testImmutableCollectionAsListSize() {
    return ImmutableSet.of(1).asList().size();
  }

  String testImmutableCollectionAsListToString() {
    return ImmutableSet.of(1).asList().toString();
  }

  ImmutableSet<Optional<Integer>> testOptionalFirstCollectionElement() {
    return ImmutableSet.of(
        ImmutableSet.of(0).stream().findAny(),
        ImmutableSet.of(1).isEmpty()
            ? Optional.empty()
            : Optional.of(ImmutableSet.of(1).iterator().next()),
        ImmutableList.of(2).isEmpty() ? Optional.empty() : Optional.of(ImmutableList.of(2).get(0)),
        ImmutableSortedSet.of(3).isEmpty()
            ? Optional.empty()
            : Optional.of(ImmutableSortedSet.of(3).first()),
        !ImmutableSet.of(1).isEmpty()
            ? Optional.of(ImmutableSet.of(1).iterator().next())
            : Optional.empty(),
        !ImmutableList.of(2).isEmpty() ? Optional.of(ImmutableList.of(2).get(0)) : Optional.empty(),
        !ImmutableSortedSet.of(3).isEmpty()
            ? Optional.of(ImmutableSortedSet.of(3).first())
            : Optional.empty());
  }

  ImmutableSet<Optional<String>> testOptionalFirstQueueElement() {
    return ImmutableSet.of(
        new LinkedList<String>().stream().findAny(),
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
}
