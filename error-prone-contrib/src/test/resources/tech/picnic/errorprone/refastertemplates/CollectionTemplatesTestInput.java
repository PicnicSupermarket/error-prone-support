package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.CollectionAddAllToCollectionBlock;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.CollectionAddAllToCollectionExpression;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.CollectionIsEmpty;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.CollectionRemoveAllFromCollectionBlock;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.CollectionRemoveAllFromCollectionExpression;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.CollectionSize;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.CollectionToArray;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.ImmutableCollectionAsList;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.ImmutableCollectionContains;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.ImmutableCollectionIterator;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.ImmutableCollectionParallelStream;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.ImmutableCollectionStream;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.ImmutableCollectionToArrayWithArray;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.ImmutableCollectionToArrayWithGenerator;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.ImmutableCollectionToString;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.NewArrayListFromCollection;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.OptionalFirstCollectionElement;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.OptionalFirstQueueElement;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.RemoveOptionalFirstNavigableSetElement;
import tech.picnic.errorprone.refastertemplates.CollectionTemplates.RemoveOptionalFirstQueueElement;

@TemplateCollection(CollectionTemplates.class)
final class CollectionTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Iterables.class, Lists.class);
  }

  @Template(CollectionIsEmpty.class)
  ImmutableSet<Boolean> testCollectionIsEmpty() {
    return ImmutableSet.of(
        ImmutableSet.of(1).size() == 0,
        ImmutableSet.of(2).size() <= 0,
        ImmutableSet.of(3).size() < 1,
        ImmutableSet.of(4).size() != 0,
        ImmutableSet.of(5).size() > 0,
        ImmutableSet.of(6).size() >= 1,
        Iterables.isEmpty(ImmutableSet.of(7)),
        ImmutableSet.of(8).asList().isEmpty());
  }

  @Template(CollectionSize.class)
  ImmutableSet<Integer> testCollectionSize() {
    return ImmutableSet.of(Iterables.size(ImmutableSet.of(1)), ImmutableSet.of(2).asList().size());
  }

  @Template(CollectionAddAllToCollectionExpression.class)
  boolean testCollectionAddAllToCollectionExpression() {
    return Iterables.addAll(new ArrayList<>(), ImmutableSet.of("foo"));
  }

  @Template(CollectionAddAllToCollectionBlock.class)
  void testCollectionAddAllToCollectionBlock() {
    ImmutableSet.of("foo").forEach(new ArrayList<>()::add);
    for (Number element : ImmutableSet.of(1)) {
      new ArrayList<Number>().add(element);
    }
    for (Integer element : ImmutableSet.of(2)) {
      new ArrayList<Number>().add(element);
    }
  }

  @Template(CollectionRemoveAllFromCollectionExpression.class)
  boolean testCollectionRemoveAllFromCollectionExpression() {
    return Iterables.removeAll(new ArrayList<>(), ImmutableSet.of("foo"));
  }

  @Template(CollectionRemoveAllFromCollectionBlock.class)
  void testCollectionRemoveAllFromCollectionBlock() {
    ImmutableSet.of("foo").forEach(new ArrayList<>()::remove);
    for (Number element : ImmutableSet.of(1)) {
      new ArrayList<Number>().remove(element);
    }
    for (Integer element : ImmutableSet.of(2)) {
      new ArrayList<Number>().remove(element);
    }
  }

  @Template(NewArrayListFromCollection.class)
  ArrayList<String> testNewArrayListFromCollection() {
    return Lists.newArrayList(ImmutableList.of("foo"));
  }

  @Template(ImmutableCollectionAsList.class)
  ImmutableList<Integer> testImmutableCollectionAsList() {
    return ImmutableList.copyOf(ImmutableSet.of(1));
  }

  @Template(ImmutableCollectionStream.class)
  Stream<Integer> testImmutableCollectionStream() {
    return ImmutableSet.of(1).asList().stream();
  }

  @Template(ImmutableCollectionContains.class)
  boolean testImmutableCollectionContains() {
    return ImmutableSet.of(1).asList().contains("foo");
  }

  @Template(ImmutableCollectionParallelStream.class)
  Stream<Integer> testImmutableCollectionParallelStream() {
    return ImmutableSet.of(1).asList().parallelStream();
  }

  @Template(ImmutableCollectionToString.class)
  String testImmutableCollectionToString() {
    return ImmutableSet.of(1).asList().toString();
  }

  @Template(CollectionToArray.class)
  ImmutableSet<Object[]> testCollectionToArray() {
    return ImmutableSet.of(
        ImmutableSet.of(1).toArray(new Object[1]),
        ImmutableSet.of(2).toArray(Object[]::new),
        ImmutableSet.of(3).asList().toArray());
  }

  @Template(ImmutableCollectionToArrayWithArray.class)
  Integer[] testImmutableCollectionToArrayWithArray() {
    return ImmutableSet.of(1).asList().toArray(new Integer[0]);
  }

  @Template(ImmutableCollectionToArrayWithGenerator.class)
  Integer[] testImmutableCollectionToArrayWithGenerator() {
    return ImmutableSet.of(1).asList().toArray(Integer[]::new);
  }

  @Template(ImmutableCollectionIterator.class)
  Iterator<Integer> testImmutableCollectionIterator() {
    return ImmutableSet.of(1).asList().iterator();
  }

  @Template(OptionalFirstCollectionElement.class)
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

  @Template(OptionalFirstQueueElement.class)
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

  @Template(RemoveOptionalFirstNavigableSetElement.class)
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

  @Template(RemoveOptionalFirstQueueElement.class)
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
