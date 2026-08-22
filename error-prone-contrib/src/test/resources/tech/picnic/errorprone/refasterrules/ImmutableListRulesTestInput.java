package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableListRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Arrays.class, Collections.class, Comparator.class, Streams.class, naturalOrder());
  }

  ImmutableList.Builder<String> testImmutableListBuilder() {
    return new ImmutableList.Builder<>();
  }

  ImmutableSet<ImmutableList<Integer>> testImmutableListCopyOf() {
    return ImmutableSet.of(
        ImmutableList.<Integer>builder().add(new Integer[] {1}).build(),
        Arrays.stream(new Integer[] {2}).collect(toImmutableList()),
        ImmutableList.<Integer>builder().addAll(ImmutableList.of(3).iterator()).build(),
        Streams.stream(ImmutableList.of(4).iterator()).collect(toImmutableList()),
        ImmutableList.<Integer>builder().addAll(ImmutableList.of(5)::iterator).build(),
        Streams.stream(ImmutableList.of(6)::iterator).collect(toImmutableList()),
        ImmutableList.of(7).stream().collect(toImmutableList()));
  }

  ImmutableList<Integer> testStreamCollectToImmutableList() {
    return ImmutableList.copyOf(Stream.of(1).iterator());
  }

  ImmutableSet<ImmutableList<Integer>> testImmutableListSortedCopyOf() {
    return ImmutableSet.of(
        ImmutableList.sortedCopyOf(naturalOrder(), ImmutableSet.of(1)),
        Streams.stream(ImmutableSet.of(2)::iterator).sorted().collect(toImmutableList()),
        ImmutableSet.of(3).stream().sorted().collect(toImmutableList()));
  }

  ImmutableSet<ImmutableList<String>> testImmutableListSortedCopyOfWithComparator() {
    return ImmutableSet.of(
        Streams.stream(ImmutableSet.of("foo")::iterator)
            .sorted(comparing(String::length))
            .collect(toImmutableList()),
        ImmutableSet.of("bar").stream()
            .sorted(comparing(String::isEmpty))
            .collect(toImmutableList()));
  }

  ImmutableSet<Iterator<Integer>> testImmutableListSortedCopyOfIterator() {
    return ImmutableSet.of(
        Streams.stream(ImmutableList.of(1)::iterator).sorted().iterator(),
        ImmutableList.of(2).stream().sorted().iterator());
  }

  ImmutableSet<Iterator<String>> testImmutableListSortedCopyOfIteratorWithComparator() {
    return ImmutableSet.of(
        Streams.stream(ImmutableList.of("foo")::iterator)
            .sorted(Comparator.comparing(String::length))
            .iterator(),
        ImmutableList.of("bar").stream().sorted(Comparator.comparing(String::isEmpty)).iterator());
  }

  ImmutableList<Integer> testStreamCollectToImmutableSetAsList() {
    return Stream.of(1).distinct().collect(toImmutableList());
  }

  ImmutableSet<List<Integer>> testImmutableListOf() {
    return ImmutableSet.of(
        Stream.<Integer>empty().collect(toImmutableList()), Collections.<Integer>emptyList());
  }

  ImmutableSet<List<Integer>> testImmutableListOfWithObject() {
    return ImmutableSet.of(Collections.singletonList(2), Stream.of(4).collect(toImmutableList()));
  }

  ImmutableSet<List<Integer>> testImmutableListOfVarargs() {
    return ImmutableSet.of(
        List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), Stream.of(12, 13).collect(toImmutableList()));
  }

  ImmutableList<String> testOptionalMapImmutableListOfOrElseGetImmutableListOf() {
    return Optional.of("foo").stream().collect(toImmutableList());
  }
}
