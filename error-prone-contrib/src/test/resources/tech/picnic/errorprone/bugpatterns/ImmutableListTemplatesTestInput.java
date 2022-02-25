package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

final class ImmutableListTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class,
        Collection.class,
        Collections.class,
        Comparator.class,
        Streams.class,
        collectingAndThen(null, null),
        naturalOrder(),
        toList());
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
        Arrays.stream(new Integer[] {8}).collect(toImmutableList()));
  }

  ImmutableSet<ImmutableList<Integer>> testStreamToImmutableList() {
    return ImmutableSet.of(
        ImmutableList.copyOf(Stream.of(1).iterator()),
        Stream.of(2).collect(collectingAndThen(toList(), ImmutableList::copyOf)));
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

  List<Object> testImmutableListOf() {
    return Collections.emptyList();
  }

  List<String> testImmutableListOf1() {
    return List.of("1");
  }

  List<String> testImmutableListOf2() {
    return List.of("1", "2");
  }

  List<String> testImmutableListOf3() {
    return List.of("1", "2", "3");
  }

  List<String> testImmutableListOf4() {
    return List.of("1", "2", "3", "4");
  }

  List<String> testImmutableListOf5() {
    return List.of("1", "2", "3", "4", "5");
  }
}
