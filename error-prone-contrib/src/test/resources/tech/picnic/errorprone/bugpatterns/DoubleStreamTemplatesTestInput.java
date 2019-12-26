package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

final class DoubleStreamTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  DoubleStream testConcatOneDoubleStream() {
    return Streams.concat(DoubleStream.of(1));
  }

  DoubleStream testConcatTwoDoubleStreams() {
    return Streams.concat(DoubleStream.of(1), DoubleStream.of(2));
  }

  DoubleStream testFilterOuterDoubleStreamAfterFlatMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v).filter(n -> n > 1));
  }

  DoubleStream testFilterOuterStreamAfterFlatMapToDouble() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v).filter(n -> n > 1));
  }

  DoubleStream testMapOuterDoubleStreamAfterFlatMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v).map(n -> n * 1));
  }

  DoubleStream testMapOuterStreamAfterFlatMapToDouble() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v).map(n -> n * 1));
  }

  DoubleStream testFlatMapOuterDoubleStreamAfterFlatMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v).flatMap(DoubleStream::of));
  }

  DoubleStream testFlatMapOuterStreamAfterFlatMapToDouble() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v).flatMap(DoubleStream::of));
  }

  ImmutableSet<Boolean> testDoubleStreamIsEmpty() {
    return ImmutableSet.of(
        DoubleStream.of(1).count() == 0,
        DoubleStream.of(2).count() <= 0,
        DoubleStream.of(3).count() < 1,
        DoubleStream.of(4).findFirst().isEmpty());
  }

  ImmutableSet<Boolean> testDoubleStreamIsNotEmpty() {
    return ImmutableSet.of(
        DoubleStream.of(1).count() != 0,
        DoubleStream.of(2).count() > 0,
        DoubleStream.of(3).count() >= 1,
        DoubleStream.of(4).findFirst().isPresent());
  }

  ImmutableSet<Boolean> testDoubleStreamNoneMatch() {
    DoublePredicate pred = i -> i > 0;
    return ImmutableSet.of(
        !DoubleStream.of(1).anyMatch(n -> n > 1),
        DoubleStream.of(2).allMatch(pred.negate()),
        DoubleStream.of(3).filter(pred).findAny().isEmpty());
  }

  boolean testDoubleStreamNoneMatch2() {
    return DoubleStream.of(1).allMatch(n -> !(n > 1));
  }

  ImmutableSet<Boolean> testDoubleStreamAnyMatch() {
    return ImmutableSet.of(
        !DoubleStream.of(1).noneMatch(n -> n > 1),
        DoubleStream.of(2).filter(n -> n > 2).findAny().isPresent());
  }

  ImmutableSet<Boolean> testDoubleStreamAllMatch() {
    DoublePredicate pred = i -> i > 0;
    return ImmutableSet.of(
        DoubleStream.of(1).noneMatch(pred.negate()),
        !DoubleStream.of(2).anyMatch(pred.negate()),
        DoubleStream.of(3).filter(pred.negate()).findAny().isEmpty());
  }

  ImmutableSet<Boolean> testDoubleStreamAllMatch2() {
    return ImmutableSet.of(
        DoubleStream.of(1).noneMatch(n -> !(n > 1)),
        !DoubleStream.of(2).anyMatch(n -> !(n > 2)),
        DoubleStream.of(3).filter(n -> !(n > 3)).findAny().isEmpty());
  }
}
