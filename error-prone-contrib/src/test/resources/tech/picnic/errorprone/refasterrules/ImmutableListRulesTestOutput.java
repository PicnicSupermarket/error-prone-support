package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
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
    return ImmutableList.builder();
  }

  ImmutableSet<ImmutableList<Integer>> testImmutableListCopyOf() {
    return ImmutableSet.of(
        ImmutableList.copyOf(new Integer[] {1}),
        ImmutableList.copyOf(new Integer[] {2}),
        ImmutableList.copyOf(ImmutableList.of(3).iterator()),
        ImmutableList.copyOf(ImmutableList.of(4).iterator()),
        ImmutableList.copyOf(ImmutableList.of(5)::iterator),
        ImmutableList.copyOf(ImmutableList.of(6)::iterator),
        ImmutableList.copyOf(ImmutableList.of(7)));
  }

  ImmutableList<Integer> testStreamCollectToImmutableList() {
    return Stream.of(1).collect(toImmutableList());
  }

  ImmutableSet<ImmutableList<Integer>> testImmutableListSortedCopyOf() {
    return ImmutableSet.of(
        ImmutableList.sortedCopyOf(ImmutableSet.of(1)),
        ImmutableList.sortedCopyOf(ImmutableSet.of(2)::iterator),
        ImmutableList.sortedCopyOf(ImmutableSet.of(3)));
  }

  ImmutableSet<ImmutableList<String>> testImmutableListSortedCopyOfWithComparator() {
    return ImmutableSet.of(
        ImmutableList.sortedCopyOf(comparing(String::length), ImmutableSet.of("foo")::iterator),
        ImmutableList.sortedCopyOf(comparing(String::isEmpty), ImmutableSet.of("bar")));
  }

  ImmutableSet<Iterator<Integer>> testImmutableListSortedCopyOfIterator() {
    return ImmutableSet.of(
        ImmutableList.sortedCopyOf(ImmutableList.of(1)::iterator).iterator(),
        ImmutableList.sortedCopyOf(ImmutableList.of(2)).iterator());
  }

  ImmutableSet<Iterator<String>> testImmutableListSortedCopyOfIteratorWithComparator() {
    return ImmutableSet.of(
        ImmutableList.sortedCopyOf(
                Comparator.comparing(String::length), ImmutableList.of("foo")::iterator)
            .iterator(),
        ImmutableList.sortedCopyOf(Comparator.comparing(String::isEmpty), ImmutableList.of("bar"))
            .iterator());
  }

  ImmutableList<Integer> testStreamCollectToImmutableSetAsList() {
    return Stream.of(1).collect(toImmutableSet()).asList();
  }

  ImmutableSet<List<Integer>> testImmutableListOf() {
    return ImmutableSet.of(ImmutableList.of(), ImmutableList.of());
  }

  ImmutableSet<List<Integer>> testImmutableListOfWithObject() {
    return ImmutableSet.of(ImmutableList.of(2), ImmutableList.of(4));
  }

  ImmutableSet<List<Integer>> testImmutableListOfVarargs() {
    return ImmutableSet.of(
        ImmutableList.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), ImmutableList.of(12, 13));
  }

  ImmutableList<String> testOptionalMapImmutableListOfOrElseGetImmutableListOf() {
    return Optional.of("foo").map(ImmutableList::of).orElseGet(ImmutableList::of);
  }
}
