package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.OptionalInt;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class IntStreamTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  IntStream testIntStreamClosedOpenRange() {
    return IntStream.rangeClosed(0, 42 - 1);
  }

  IntStream testConcatOneIntStream() {
    return Streams.concat(IntStream.of(1));
  }

  IntStream testConcatTwoIntStreams() {
    return Streams.concat(IntStream.of(1), IntStream.of(2));
  }

  IntStream testFilterOuterIntStreamAfterFlatMap() {
    return IntStream.of(1).flatMap(v -> IntStream.of(v * v).filter(n -> n > 1));
  }

  IntStream testFilterOuterStreamAfterFlatMapToInt() {
    return Stream.of(1).flatMapToInt(v -> IntStream.of(v * v).filter(n -> n > 1));
  }

  IntStream testMapOuterIntStreamAfterFlatMap() {
    return IntStream.of(1).flatMap(v -> IntStream.of(v * v).map(n -> n * 1));
  }

  IntStream testMapOuterStreamAfterFlatMapToInt() {
    return Stream.of(1).flatMapToInt(v -> IntStream.of(v * v).map(n -> n * 1));
  }

  IntStream testFlatMapOuterIntStreamAfterFlatMap() {
    return IntStream.of(1).flatMap(v -> IntStream.of(v * v).flatMap(IntStream::of));
  }

  IntStream testFlatMapOuterStreamAfterFlatMapToInt() {
    return Stream.of(1).flatMapToInt(v -> IntStream.of(v * v).flatMap(IntStream::of));
  }

  ImmutableSet<Boolean> testIntStreamIsEmpty() {
    return ImmutableSet.of(
        IntStream.of(1).count() == 0,
        IntStream.of(2).count() <= 0,
        IntStream.of(3).count() < 1,
        IntStream.of(4).findFirst().isEmpty());
  }

  ImmutableSet<Boolean> testIntStreamIsNotEmpty() {
    return ImmutableSet.of(
        IntStream.of(1).count() != 0,
        IntStream.of(2).count() > 0,
        IntStream.of(3).count() >= 1,
        IntStream.of(4).findFirst().isPresent());
  }

  OptionalInt testIntStreamMin() {
    return IntStream.of(1).sorted().findFirst();
  }

  ImmutableSet<Boolean> testIntStreamNoneMatch() {
    IntPredicate pred = i -> i > 0;
    return ImmutableSet.of(
        !IntStream.of(1).anyMatch(n -> n > 1),
        IntStream.of(2).allMatch(pred.negate()),
        IntStream.of(3).filter(pred).findAny().isEmpty());
  }

  boolean testIntStreamNoneMatch2() {
    return IntStream.of(1).allMatch(n -> !(n > 1));
  }

  ImmutableSet<Boolean> testIntStreamAnyMatch() {
    return ImmutableSet.of(
        !IntStream.of(1).noneMatch(n -> n > 1),
        IntStream.of(2).filter(n -> n > 2).findAny().isPresent());
  }

  boolean testIntStreamAllMatch() {
    IntPredicate pred = i -> i > 0;
    return IntStream.of(1).noneMatch(pred.negate());
  }

  boolean testIntStreamAllMatch2() {
    return IntStream.of(1).noneMatch(n -> !(n > 1));
  }
}
