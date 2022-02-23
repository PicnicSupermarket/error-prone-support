package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
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
    return ImmutableList.builder();
  }

  ImmutableSet<ImmutableList<Integer>> testEmptyImmutableList() {
    return ImmutableSet.of(ImmutableList.of(), ImmutableList.of());
  }

  List<String> testSingletonImmutableList() {
    return ImmutableList.of("foo");
  }

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

  ImmutableSet<ImmutableList<Integer>> testStreamToImmutableList() {
    return ImmutableSet.of(
        Stream.of(1).collect(toImmutableList()), Stream.of(2).collect(toImmutableList()));
  }

  ImmutableSet<ImmutableList<Integer>> testImmutableListSortedCopyOf() {
    return ImmutableSet.of(
        ImmutableList.sortedCopyOf(ImmutableSet.of(1)),
        ImmutableList.sortedCopyOf(ImmutableSet.of(2)),
        ImmutableList.sortedCopyOf(ImmutableSet.of(3)::iterator));
  }

  ImmutableSet<ImmutableList<String>> testImmutableListSortedCopyOfWithCustomComparator() {
    return ImmutableSet.of(
        ImmutableList.sortedCopyOf(Comparator.comparing(String::length), ImmutableSet.of("foo")),
        ImmutableList.sortedCopyOf(
            Comparator.comparing(String::isEmpty), ImmutableSet.of("bar")::iterator));
  }

  ImmutableList<Integer> testStreamToDistinctImmutableList() {
    return Stream.of(1).collect(toImmutableSet()).asList();
  }

  List<?> testImmutableListOf() {
    return ImmutableList.of();
  }

  List<String> testImmutableListOfTyped() {
    return ImmutableList.of();
  }

  List<String> testImmutableListOf1() {
    return ImmutableList.of("1");
  }

  List<String> testImmutableListOf2() {
    return ImmutableList.of("1", "2");
  }

  List<String> testImmutableListOf3() {
    return ImmutableList.of("1", "2", "3");
  }

  List<String> testImmutableListOf4() {
    return ImmutableList.of("1", "2", "3", "4");
  }

  List<String> testImmutableListOf5() {
    return ImmutableList.of("1", "2", "3", "4", "5");
  }
}
