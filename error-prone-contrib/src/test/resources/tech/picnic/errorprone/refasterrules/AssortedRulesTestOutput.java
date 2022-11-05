package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.toImmutableEnumSet;
import static java.util.Objects.checkIndex;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
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
    return checkIndex(0, 1);
  }

  void testCheckIndexConditional() {
    checkIndex(1, 2);
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
        toString().isEmpty() || Boolean.TRUE,
        !toString().isEmpty() || (toString().isEmpty() && Boolean.TRUE),
        3 < 4 || (3 >= 4 && Boolean.TRUE),
        3 >= 4 || (3 < 4 && Boolean.TRUE));
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

  ImmutableSet<Stream<String>> testSplitToStream() {
    return ImmutableSet.of(
        Splitter.on(':').splitToStream("foo"),
        Splitter.on(',').splitToStream(new StringBuilder("bar")));
  }
}
