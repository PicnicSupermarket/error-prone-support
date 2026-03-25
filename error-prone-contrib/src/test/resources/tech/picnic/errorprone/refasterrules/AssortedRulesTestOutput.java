package tech.picnic.errorprone.refasterrules;

import static java.util.Objects.checkIndex;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssortedRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Iterables.class, Preconditions.class, Splitter.class, Streams.class);
  }

  int testCheckIndex() {
    return checkIndex(0, 1);
  }

  void testCheckIndexConditional() {
    checkIndex(1, 2);
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

  boolean testIterableIsEmpty() {
    return Iterables.isEmpty(ImmutableList.of());
  }

  ImmutableSet<Stream<String>> testSplitToStream() {
    return ImmutableSet.of(
        Splitter.on(':').splitToStream("foo"),
        Splitter.on(',').splitToStream(new StringBuilder("bar")));
  }
}
