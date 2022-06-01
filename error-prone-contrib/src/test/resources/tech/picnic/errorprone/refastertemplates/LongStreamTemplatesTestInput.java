package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.OptionalLong;
import java.util.function.LongPredicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

final class LongStreamTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  LongStream testLongStreamClosedOpenRange() {
    return LongStream.rangeClosed(0, 42 - 1);
  }

  LongStream testConcatOneLongStream() {
    return Streams.concat(LongStream.of(1));
  }

  LongStream testConcatTwoLongStreams() {
    return Streams.concat(LongStream.of(1), LongStream.of(2));
  }

  LongStream testFilterOuterLongStreamAfterFlatMap() {
    return LongStream.of(1).flatMap(v -> LongStream.of(v * v).filter(n -> n > 1));
  }

  LongStream testFilterOuterStreamAfterFlatMapToLong() {
    return Stream.of(1).flatMapToLong(v -> LongStream.of(v * v).filter(n -> n > 1));
  }

  LongStream testMapOuterLongStreamAfterFlatMap() {
    return LongStream.of(1).flatMap(v -> LongStream.of(v * v).map(n -> n * 1));
  }

  LongStream testMapOuterStreamAfterFlatMapToLong() {
    return Stream.of(1).flatMapToLong(v -> LongStream.of(v * v).map(n -> n * 1));
  }

  LongStream testFlatMapOuterLongStreamAfterFlatMap() {
    return LongStream.of(1).flatMap(v -> LongStream.of(v * v).flatMap(LongStream::of));
  }

  LongStream testFlatMapOuterStreamAfterFlatMapToLong() {
    return Stream.of(1).flatMapToLong(v -> LongStream.of(v * v).flatMap(LongStream::of));
  }

  ImmutableSet<Boolean> testLongStreamIsEmpty() {
    return ImmutableSet.of(
        LongStream.of(1).count() == 0,
        LongStream.of(2).count() <= 0,
        LongStream.of(3).count() < 1,
        LongStream.of(4).findFirst().isEmpty());
  }

  ImmutableSet<Boolean> testLongStreamIsNotEmpty() {
    return ImmutableSet.of(
        LongStream.of(1).count() != 0,
        LongStream.of(2).count() > 0,
        LongStream.of(3).count() >= 1,
        LongStream.of(4).findFirst().isPresent());
  }

  OptionalLong testLongStreamMin() {
    return LongStream.of(1).sorted().findFirst();
  }

  ImmutableSet<Boolean> testLongStreamNoneMatch() {
    LongPredicate pred = i -> i > 0;
    return ImmutableSet.of(
        !LongStream.of(1).anyMatch(n -> n > 1),
        LongStream.of(2).allMatch(pred.negate()),
        LongStream.of(3).filter(pred).findAny().isEmpty());
  }

  boolean testLongStreamNoneMatch2() {
    return LongStream.of(1).allMatch(n -> !(n > 1));
  }

  ImmutableSet<Boolean> testLongStreamAnyMatch() {
    return ImmutableSet.of(
        !LongStream.of(1).noneMatch(n -> n > 1),
        LongStream.of(2).filter(n -> n > 2).findAny().isPresent());
  }

  boolean testLongStreamAllMatch() {
    LongPredicate pred = i -> i > 0;
    return LongStream.of(1).noneMatch(pred.negate());
  }

  boolean testLongStreamAllMatch2() {
    return LongStream.of(1).noneMatch(n -> !(n > 1));
  }
}
