package tech.picnic.errorprone.bugpatterns;

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

final class ImmutableListTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class,
        Collections.class,
        Comparator.class,
        Streams.class,
        (Runnable) () -> collectingAndThen(null, null),
        (Runnable) () -> naturalOrder(),
        (Runnable) () -> toList());
  }

  ImmutableList.Builder<String> testImmutableListBuilder() {
    return new ImmutableList.Builder<>();
  }

  ImmutableSet<ImmutableList<Integer>> testEmptyImmutableList() {
    return ImmutableSet.of(
        ImmutableList.<Integer>builder().build(),
        Stream.<Integer>empty().collect(toImmutableList()));
  }

  List<String> testSingletonImmutableList() {
    return Collections.singletonList("foo");
  }

  ImmutableSet<ImmutableList<Integer>> testIterableToImmutableList() {
    return ImmutableSet.of(
        ImmutableList.of(1).stream().collect(toImmutableList()),
        Streams.stream(ImmutableList.of(2)::iterator).collect(toImmutableList()),
        Streams.stream(ImmutableList.of(3).iterator()).collect(toImmutableList()),
        ImmutableList.<Integer>builder().addAll(ImmutableList.of(4)).build(),
        ImmutableList.<Integer>builder().addAll(ImmutableList.of(5)::iterator).build(),
        ImmutableList.<Integer>builder().addAll(ImmutableList.of(6).iterator()).build(),
        ImmutableList.<Integer>builder().add(new Integer[] {7}).build(),
        Stream.of(new Integer[] {8}).collect(toImmutableList()),
        Arrays.stream(new Integer[] {9}).collect(toImmutableList()));
  }

  ImmutableSet<ImmutableList<Integer>> testStreamToImmutableList() {
    return ImmutableSet.of(
        ImmutableList.copyOf(Stream.of(1).iterator()),
        ImmutableList.copyOf(Stream.of(2).iterator()),
        Stream.of(3).collect(collectingAndThen(toList(), ImmutableList::copyOf)));
  }

  ImmutableList<Integer> testImmutableListAsList() {
    return ImmutableList.of(1, 2, 3).asList();
  }

  ImmutableSet<ImmutableList<Integer>> testImmutableListSortedCopyOf() {
    return ImmutableSet.of(
        ImmutableList.sortedCopyOf(naturalOrder(), ImmutableSet.of(1)),
        ImmutableSet.of(2).stream().sorted().collect(toImmutableList()),
        Streams.stream(ImmutableSet.of(3)::iterator).sorted().collect(toImmutableList()));
  }

  ImmutableSet<ImmutableList<String>> testImmutableListSortedCopyOfWithCustomComparator() {
    return ImmutableSet.of(
        ImmutableSet.of("foo").stream()
            .sorted(Comparator.comparing(String::length))
            .collect(toImmutableList()),
        Streams.stream(ImmutableSet.of("bar")::iterator)
            .sorted(Comparator.comparing(String::isEmpty))
            .collect(toImmutableList()));
  }

  ImmutableList<Integer> testStreamToDistinctImmutableList() {
    return Stream.of(1).distinct().collect(toImmutableList());
  }
}
