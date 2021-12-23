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
        ImmutableSet.of(1).isEmpty(),
        ImmutableSet.of(2).isEmpty(),
        ImmutableSet.of(3).isEmpty(),
        !ImmutableSet.of(4).isEmpty(),
        !ImmutableSet.of(5).isEmpty(),
        !ImmutableSet.of(6).isEmpty(),
        ImmutableSet.of(7).isEmpty(),
        ImmutableSet.of(8).isEmpty());
  }

  @Template(CollectionSize.class)
  ImmutableSet<Integer> testCollectionSize() {
    return ImmutableSet.of(ImmutableSet.of(1).size(), ImmutableSet.of(2).size());
  }

  @Template(CollectionAddAllToCollectionExpression.class)
  boolean testCollectionAddAllToCollectionExpression() {
    return new ArrayList<>().addAll(ImmutableSet.of("foo"));
  }

  @Template(CollectionAddAllToCollectionBlock.class)
  void testCollectionAddAllToCollectionBlock() {
    new ArrayList<>().addAll(ImmutableSet.of("foo"));
    new ArrayList<Number>().addAll(ImmutableSet.of(1));
    new ArrayList<Number>().addAll(ImmutableSet.of(2));
  }

  @Template(CollectionRemoveAllFromCollectionExpression.class)
  boolean testCollectionRemoveAllFromCollectionExpression() {
    return new ArrayList<>().removeAll(ImmutableSet.of("foo"));
  }

  @Template(CollectionRemoveAllFromCollectionBlock.class)
  void testCollectionRemoveAllFromCollectionBlock() {
    new ArrayList<>().removeAll(ImmutableSet.of("foo"));
    new ArrayList<Number>().removeAll(ImmutableSet.of(1));
    new ArrayList<Number>().removeAll(ImmutableSet.of(2));
  }

  @Template(NewArrayListFromCollection.class)
  ArrayList<String> testNewArrayListFromCollection() {
    return new ArrayList<>(ImmutableList.of("foo"));
  }

  @Template(ImmutableCollectionAsList.class)
  ImmutableList<Integer> testImmutableCollectionAsList() {
    return ImmutableSet.of(1).asList();
  }

  @Template(ImmutableCollectionStream.class)
  Stream<Integer> testImmutableCollectionStream() {
    return ImmutableSet.of(1).stream();
  }

  @Template(ImmutableCollectionContains.class)
  boolean testImmutableCollectionContains() {
    return ImmutableSet.of(1).contains("foo");
  }

  @Template(ImmutableCollectionParallelStream.class)
  Stream<Integer> testImmutableCollectionParallelStream() {
    return ImmutableSet.of(1).parallelStream();
  }

  @Template(ImmutableCollectionToString.class)
  String testImmutableCollectionToString() {
    return ImmutableSet.of(1).toString();
  }

  @Template(CollectionToArray.class)
  ImmutableSet<Object[]> testCollectionToArray() {
    return ImmutableSet.of(
        ImmutableSet.of(1).toArray(), ImmutableSet.of(2).toArray(), ImmutableSet.of(3).toArray());
  }

  @Template(ImmutableCollectionToArrayWithArray.class)
  Integer[] testImmutableCollectionToArrayWithArray() {
    return ImmutableSet.of(1).toArray(new Integer[0]);
  }

  @Template(ImmutableCollectionToArrayWithGenerator.class)
  Integer[] testImmutableCollectionToArrayWithGenerator() {
    return ImmutableSet.of(1).toArray(Integer[]::new);
  }

  @Template(ImmutableCollectionIterator.class)
  Iterator<Integer> testImmutableCollectionIterator() {
    return ImmutableSet.of(1).iterator();
  }

  @Template(OptionalFirstCollectionElement.class)
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

  @Template(OptionalFirstQueueElement.class)
  ImmutableSet<Optional<String>> testOptionalFirstQueueElement() {
    return ImmutableSet.of(
        new LinkedList<String>().stream().findFirst(),
        Optional.ofNullable(new LinkedList<String>().peek()),
        Optional.ofNullable(new LinkedList<String>().peek()),
        Optional.ofNullable(new LinkedList<String>().peek()),
        Optional.ofNullable(new LinkedList<String>().peek()));
  }

  @Template(RemoveOptionalFirstNavigableSetElement.class)
  ImmutableSet<Optional<String>> testRemoveOptionalFirstNavigableSetElement() {
    return ImmutableSet.of(
        Optional.ofNullable(new TreeSet<String>().pollFirst()),
        Optional.ofNullable(new TreeSet<String>().pollFirst()),
        Optional.ofNullable(new TreeSet<String>().pollFirst()),
        Optional.ofNullable(new TreeSet<String>().pollFirst()));
  }

  @Template(RemoveOptionalFirstQueueElement.class)
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
