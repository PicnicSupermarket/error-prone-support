package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableList.toImmutableList;
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
    return new ImmutableList.Builder<>();
  }

  @Template(EmptyImmutableList.class)
  ImmutableSet<ImmutableList<Integer>> testEmptyImmutableList() {
    return ImmutableSet.of(
        ImmutableList.<Integer>builder().build(),
        Stream.<Integer>empty().collect(toImmutableList()));
  }

  @Template(SingletonImmutableList.class)
  List<String> testSingletonImmutableList() {
    return Collections.singletonList("foo");
  }

  @Template(IterableToImmutableList.class)
  ImmutableSet<ImmutableList<Integer>> testIterableToImmutableList() {
    return ImmutableSet.of(
        ImmutableList.of(1).stream().collect(toImmutableList()),
        Streams.stream(ImmutableList.of(2)::iterator).collect(toImmutableList()),
        Streams.stream(ImmutableList.of(3).iterator()).collect(toImmutableList()),
        ImmutableList.<Integer>builder().addAll(ImmutableList.of(4)).build(),
        ImmutableList.<Integer>builder().addAll(ImmutableList.of(5)::iterator).build(),
        ImmutableList.<Integer>builder().addAll(ImmutableList.of(6).iterator()).build(),
        ImmutableList.<Integer>builder().add(new Integer[] {7}).build(),
        Arrays.stream(new Integer[] {8}).collect(toImmutableList()));
  }

  @Template(StreamToImmutableList.class)
  ImmutableSet<ImmutableList<Integer>> testStreamToImmutableList() {
    return ImmutableSet.of(
        ImmutableList.copyOf(Stream.of(1).iterator()),
        Stream.of(2).collect(collectingAndThen(toList(), ImmutableList::copyOf)));
  }

  @Template(ImmutableListSortedCopyOf.class)
  ImmutableSet<ImmutableList<Integer>> testImmutableListSortedCopyOf() {
    return ImmutableSet.of(
        ImmutableList.sortedCopyOf(naturalOrder(), ImmutableSet.of(1)),
        ImmutableSet.of(2).stream().sorted().collect(toImmutableList()),
        Streams.stream(ImmutableSet.of(3)::iterator).sorted().collect(toImmutableList()));
  }

  @Template(ImmutableListSortedCopyOfWithCustomComparator.class)
  ImmutableSet<ImmutableList<String>> testImmutableListSortedCopyOfWithCustomComparator() {
    return ImmutableSet.of(
        ImmutableSet.of("foo").stream()
            .sorted(Comparator.comparing(String::length))
            .collect(toImmutableList()),
        Streams.stream(ImmutableSet.of("bar")::iterator)
            .sorted(Comparator.comparing(String::isEmpty))
            .collect(toImmutableList()));
  }

  @Template(StreamToDistinctImmutableList.class)
  ImmutableList<Integer> testStreamToDistinctImmutableList() {
    return Stream.of(1).distinct().collect(toImmutableList());
  }
}
