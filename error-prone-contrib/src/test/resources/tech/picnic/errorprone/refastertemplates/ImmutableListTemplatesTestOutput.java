package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.ImmutableListTemplates.EmptyImmutableList;
import tech.picnic.errorprone.refastertemplates.ImmutableListTemplates.ImmutableListBuilder;
import tech.picnic.errorprone.refastertemplates.ImmutableListTemplates.ImmutableListSortedCopyOf;
import tech.picnic.errorprone.refastertemplates.ImmutableListTemplates.ImmutableListSortedCopyOfWithCustomComparator;
import tech.picnic.errorprone.refastertemplates.ImmutableListTemplates.IterableToImmutableList;
import tech.picnic.errorprone.refastertemplates.ImmutableListTemplates.SingletonImmutableList;
import tech.picnic.errorprone.refastertemplates.ImmutableListTemplates.StreamToDistinctImmutableList;
import tech.picnic.errorprone.refastertemplates.ImmutableListTemplates.StreamToImmutableList;

@TemplateCollection(ImmutableListTemplates.class)
final class ImmutableListTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class,
        Collections.class,
        Comparator.class,
        Streams.class,
        collectingAndThen(null, null),
        naturalOrder(),
        toList());
  }

  @Template(ImmutableListBuilder.class)
  ImmutableList.Builder<String> testImmutableListBuilder() {
    return ImmutableList.builder();
  }

  @Template(EmptyImmutableList.class)
  ImmutableSet<ImmutableList<Integer>> testEmptyImmutableList() {
    return ImmutableSet.of(ImmutableList.of(), ImmutableList.of());
  }

  @Template(SingletonImmutableList.class)
  List<String> testSingletonImmutableList() {
    return ImmutableList.of("foo");
  }

  @Template(IterableToImmutableList.class)
  ImmutableSet<ImmutableList<Integer>> testIterableToImmutableList() {
    return ImmutableSet.of(
        ImmutableList.copyOf(ImmutableList.of(1)),
        ImmutableList.copyOf(ImmutableList.of(2)::iterator),
        ImmutableList.copyOf(ImmutableList.of(3).iterator()),
        ImmutableList.copyOf(ImmutableList.of(4)),
        ImmutableList.copyOf(ImmutableList.of(5)::iterator),
        ImmutableList.copyOf(ImmutableList.of(6).iterator()),
        ImmutableList.copyOf(new Integer[] {7}),
        ImmutableList.copyOf(new Integer[] {8}));
  }

  @Template(StreamToImmutableList.class)
  ImmutableSet<ImmutableList<Integer>> testStreamToImmutableList() {
    return ImmutableSet.of(
        Stream.of(1).collect(toImmutableList()), Stream.of(2).collect(toImmutableList()));
  }

  @Template(ImmutableListSortedCopyOf.class)
  ImmutableSet<ImmutableList<Integer>> testImmutableListSortedCopyOf() {
    return ImmutableSet.of(
        ImmutableList.sortedCopyOf(ImmutableSet.of(1)),
        ImmutableList.sortedCopyOf(ImmutableSet.of(2)),
        ImmutableList.sortedCopyOf(ImmutableSet.of(3)::iterator));
  }

  @Template(ImmutableListSortedCopyOfWithCustomComparator.class)
  ImmutableSet<ImmutableList<String>> testImmutableListSortedCopyOfWithCustomComparator() {
    return ImmutableSet.of(
        ImmutableList.sortedCopyOf(Comparator.comparing(String::length), ImmutableSet.of("foo")),
        ImmutableList.sortedCopyOf(
            Comparator.comparing(String::isEmpty), ImmutableSet.of("bar")::iterator));
  }

  @Template(StreamToDistinctImmutableList.class)
  ImmutableList<Integer> testStreamToDistinctImmutableList() {
    return Stream.of(1).collect(toImmutableSet()).asList();
  }
}
