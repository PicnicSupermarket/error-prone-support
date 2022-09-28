package tech.picnic.errorprone.refasterrules.input;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssortedRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        HashSet.class,
        Iterables.class,
        Preconditions.class,
        Sets.class,
        Splitter.class,
        Streams.class,
        toImmutableSet());
  }

  int testCheckIndex() {
    return Preconditions.checkElementIndex(0, 1);
  }

  void testCheckIndexConditional() {
    if (1 < 0 || 1 >= 2) {
      throw new IndexOutOfBoundsException();
    }
  }

  ImmutableSet<BoundType> testStreamToImmutableEnumSet() {
    return Stream.of(BoundType.OPEN).collect(toImmutableSet());
  }

  ImmutableSet<String> testIteratorGetNextOrDefault() {
    return ImmutableSet.of(
        ImmutableList.of("a").iterator().hasNext()
            ? ImmutableList.of("a").iterator().next()
            : "foo",
        Streams.stream(ImmutableList.of("b").iterator()).findFirst().orElse("bar"),
        Streams.stream(ImmutableList.of("c").iterator()).findAny().orElse("baz"));
  }

  // XXX: Only the first statement is rewritten. Make smarter.
  ImmutableSet<Boolean> testLogicalImplication() {
    return ImmutableSet.of(
        toString().isEmpty() || (!toString().isEmpty() && Boolean.TRUE),
        !toString().isEmpty() || (toString().isEmpty() && Boolean.TRUE),
        3 < 4 || (3 >= 4 && Boolean.TRUE),
        3 >= 4 || (3 < 4 && Boolean.TRUE));
  }

  Stream<String> testUnboundedSingleElementStream() {
    return Streams.stream(Iterables.cycle("foo"));
  }

  ImmutableSet<Boolean> testDisjointSets() {
    return ImmutableSet.of(
        Sets.intersection(ImmutableSet.of(1), ImmutableSet.of(2)).isEmpty(),
        ImmutableSet.of(3).stream().noneMatch(ImmutableSet.of(4)::contains));
  }

  ImmutableSet<Boolean> testDisjointCollections() {
    return ImmutableSet.of(
        Collections.disjoint(ImmutableSet.copyOf(ImmutableList.of(1)), ImmutableList.of(2)),
        Collections.disjoint(new HashSet<>(ImmutableList.of(3)), ImmutableList.of(4)),
        Collections.disjoint(ImmutableList.of(5), ImmutableSet.copyOf(ImmutableList.of(6))),
        Collections.disjoint(ImmutableList.of(7), new HashSet<>(ImmutableList.of(8))));
  }

  boolean testIterableIsEmpty() {
    return !ImmutableList.of().iterator().hasNext();
  }

  ImmutableSet<Stream<String>> testSplitToStream() {
    return ImmutableSet.of(
        Streams.stream(Splitter.on(':').split("foo")),
        Splitter.on(',').splitToList(new StringBuilder("bar")).stream());
  }
}
