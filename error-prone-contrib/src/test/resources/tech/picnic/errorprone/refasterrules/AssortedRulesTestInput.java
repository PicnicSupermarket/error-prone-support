package tech.picnic.errorprone.refasterrules;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssortedRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Iterables.class, Preconditions.class, Splitter.class, Streams.class);
  }

  int testCheckIndexExpression() {
    return Preconditions.checkElementIndex(0, 1);
  }

  void testCheckIndexBlock() {
    if (1 < 0 || 1 >= 2) {
      throw new IndexOutOfBoundsException();
    }
  }

  ImmutableSet<String> testIteratorsGetNext() {
    return ImmutableSet.of(
        ImmutableList.of("a").iterator().hasNext()
            ? ImmutableList.of("a").iterator().next()
            : "foo",
        Streams.stream(ImmutableList.of("b").iterator()).findFirst().orElse("bar"),
        Streams.stream(ImmutableList.of("c").iterator()).findAny().orElse("baz"));
  }

  // XXX: Only the first statement is rewritten. Make smarter.
  ImmutableSet<Boolean> testOr() {
    return ImmutableSet.of(
        toString().isEmpty() || (!toString().isEmpty() && Boolean.TRUE),
        !toString().isEmpty() || (toString().isEmpty() && Boolean.TRUE),
        3 < 4 || (3 >= 4 && Boolean.TRUE),
        3 >= 4 || (3 < 4 && Boolean.TRUE));
  }

  Stream<String> testStreamGenerate() {
    return Streams.stream(Iterables.cycle("foo"));
  }

  boolean testIterablesIsEmpty() {
    return !ImmutableList.of().iterator().hasNext();
  }

  ImmutableSet<Stream<String>> testSplitterSplitToStream() {
    return ImmutableSet.of(
        Streams.stream(Splitter.on(':').split("foo")),
        Splitter.on(',').splitToList(new StringBuilder("bar")).stream());
  }
}
