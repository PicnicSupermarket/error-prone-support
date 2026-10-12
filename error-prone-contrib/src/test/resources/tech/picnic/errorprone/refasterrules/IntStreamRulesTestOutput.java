package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.OptionalInt;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class IntStreamRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  IntStream testIntStreamRange() {
    return IntStream.range(0, 42);
  }

  IntStream testIntStreamIdentity() {
    return IntStream.of(1);
  }

  IntStream testIntStreamConcat() {
    return IntStream.concat(IntStream.of(1), IntStream.of(2));
  }

  IntStream testIntStreamFlatMapFilter() {
    return IntStream.of(1).flatMap(v -> IntStream.of(v * v)).filter(n -> n > 1);
  }

  IntStream testStreamFlatMapToIntFilter() {
    return Stream.of(1).flatMapToInt(v -> IntStream.of(v * v)).filter(n -> n > 1);
  }

  IntStream testIntStreamFlatMapMap() {
    return IntStream.of(1).flatMap(v -> IntStream.of(v * v)).map(n -> n * 1);
  }

  IntStream testStreamFlatMapToIntMap() {
    return Stream.of(1).flatMapToInt(v -> IntStream.of(v * v)).map(n -> n * 1);
  }

  IntStream testIntStreamFlatMapFlatMap() {
    return IntStream.of(1).flatMap(v -> IntStream.of(v * v)).flatMap(IntStream::of);
  }

  IntStream testStreamFlatMapToIntFlatMap() {
    return Stream.of(1).flatMapToInt(v -> IntStream.of(v * v)).flatMap(IntStream::of);
  }

  IntStream testIntStreamFilterSorted() {
    return IntStream.of(1).filter(i -> i > 0).sorted();
  }

  ImmutableSet<Boolean> testIntStreamFindAnyIsEmpty() {
    return ImmutableSet.of(
        IntStream.of(1).findAny().isEmpty(),
        IntStream.of(2).findAny().isEmpty(),
        IntStream.of(3).findAny().isEmpty(),
        IntStream.of(4).findAny().isEmpty());
  }

  ImmutableSet<Boolean> testIntStreamFindAnyIsPresent() {
    return ImmutableSet.of(
        IntStream.of(1).findAny().isPresent(),
        IntStream.of(2).findAny().isPresent(),
        IntStream.of(3).findAny().isPresent(),
        IntStream.of(4).findAny().isPresent());
  }

  OptionalInt testIntStreamMin() {
    return IntStream.of(1).min();
  }

  ImmutableSet<Boolean> testIntStreamNoneMatchWithIntPredicate() {
    IntPredicate pred = i -> i > 0;
    return ImmutableSet.of(
        IntStream.of(1).noneMatch(n -> n > 1),
        IntStream.of(2).noneMatch(pred),
        IntStream.of(3).noneMatch(pred));
  }

  boolean testIntStreamNoneMatch() {
    return IntStream.of(1).noneMatch(n -> n > 1);
  }

  ImmutableSet<Boolean> testIntStreamAnyMatch() {
    return ImmutableSet.of(
        IntStream.of(1).anyMatch(n -> n > 1), IntStream.of(2).anyMatch(n -> n > 2));
  }

  boolean testIntStreamAllMatchWithIntPredicate() {
    IntPredicate pred = i -> i > 0;
    return IntStream.of(1).allMatch(pred);
  }

  boolean testIntStreamAllMatch() {
    return IntStream.of(1).allMatch(n -> n > 1);
  }

  IntStream testIntStreamTakeWhile() {
    return IntStream.of(1).takeWhile(i -> i > 0);
  }
}
