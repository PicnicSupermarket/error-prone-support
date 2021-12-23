package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.toImmutableEnumSet;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

final class AssortedTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        HashMap.class,
        HashSet.class,
        Iterables.class,
        Preconditions.class,
        Sets.class,
        Splitter.class,
        Streams.class,
        toImmutableSet());
  }

  int testCheckIndex() {
    return Objects.checkIndex(0, 1);
  }

  Map<RoundingMode, String> testCreateEnumMap() {
    return new EnumMap<>(RoundingMode.class);
  }

  String testMapGetOrNull() {
    return ImmutableMap.of(1, "foo").get("bar");
  }

  ImmutableSet<BoundType> testStreamToImmutableEnumSet() {
    return Stream.of(BoundType.OPEN).collect(toImmutableEnumSet());
  }

  ImmutableSet<String> testIteratorGetNextOrDefault() {
    return ImmutableSet.of(
        Iterators.getNext(ImmutableList.of("a").iterator(), "foo"),
        Iterators.getNext(ImmutableList.of("b").iterator(), "bar"),
        Iterators.getNext(ImmutableList.of("c").iterator(), "baz"));
  }

  // XXX: Only the first statement is rewritten. Make smarter.
  ImmutableSet<Boolean> testLogicalImplication() {
    return ImmutableSet.of(
        toString().isEmpty() || true,
        !toString().isEmpty() || (toString().isEmpty() && true),
        3 < 4 || (3 >= 4 && true),
        3 >= 4 || (3 < 4 && true));
  }

  Stream<String> testUnboundedSingleElementStream() {
    return Stream.generate(() -> "foo");
  }

  ImmutableSet<Boolean> testDisjointSets() {
    return ImmutableSet.of(
        Collections.disjoint(ImmutableSet.of(1), ImmutableSet.of(2)),
        Collections.disjoint(ImmutableSet.of(3), ImmutableSet.of(4)));
  }

  ImmutableSet<Boolean> testDisjointCollections() {
    return ImmutableSet.of(
        Collections.disjoint(ImmutableList.of(1), ImmutableList.of(2)),
        Collections.disjoint(ImmutableList.of(3), ImmutableList.of(4)),
        Collections.disjoint(ImmutableList.of(5), ImmutableList.of(6)),
        Collections.disjoint(ImmutableList.of(7), ImmutableList.of(8)));
  }

  boolean testIterableIsEmpty() {
    return Iterables.isEmpty(ImmutableList.of());
  }

  Stream<String> testMapKeyStream() {
    return ImmutableMap.of("foo", 1).keySet().stream();
  }

  Stream<Integer> testMapValueStream() {
    return ImmutableMap.of("foo", 1).values().stream();
  }

  ImmutableSet<Stream<String>> testSplitToStream() {
    return ImmutableSet.of(
        Splitter.on(':').splitToStream("foo"),
        Splitter.on(',').splitToStream(new StringBuilder("bar")));
  }
}
