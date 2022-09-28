package tech.picnic.errorprone.refasterrules.output;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.OptionalDouble;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class DoubleStreamRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  DoubleStream testConcatOneDoubleStream() {
    return DoubleStream.of(1);
  }

  DoubleStream testConcatTwoDoubleStreams() {
    return DoubleStream.concat(DoubleStream.of(1), DoubleStream.of(2));
  }

  DoubleStream testFilterOuterDoubleStreamAfterFlatMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v)).filter(n -> n > 1);
  }

  DoubleStream testFilterOuterStreamAfterFlatMapToDouble() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v)).filter(n -> n > 1);
  }

  DoubleStream testMapOuterDoubleStreamAfterFlatMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v)).map(n -> n * 1);
  }

  DoubleStream testMapOuterStreamAfterFlatMapToDouble() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v)).map(n -> n * 1);
  }

  DoubleStream testFlatMapOuterDoubleStreamAfterFlatMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v)).flatMap(DoubleStream::of);
  }

  DoubleStream testFlatMapOuterStreamAfterFlatMapToDouble() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v)).flatMap(DoubleStream::of);
  }

  ImmutableSet<Boolean> testDoubleStreamIsEmpty() {
    return ImmutableSet.of(
        DoubleStream.of(1).findAny().isEmpty(),
        DoubleStream.of(2).findAny().isEmpty(),
        DoubleStream.of(3).findAny().isEmpty(),
        DoubleStream.of(4).findAny().isEmpty());
  }

  ImmutableSet<Boolean> testDoubleStreamIsNotEmpty() {
    return ImmutableSet.of(
        DoubleStream.of(1).findAny().isPresent(),
        DoubleStream.of(2).findAny().isPresent(),
        DoubleStream.of(3).findAny().isPresent(),
        DoubleStream.of(4).findAny().isPresent());
  }

  OptionalDouble testDoubleStreamMin() {
    return DoubleStream.of(1).min();
  }

  ImmutableSet<Boolean> testDoubleStreamNoneMatch() {
    DoublePredicate pred = i -> i > 0;
    return ImmutableSet.of(
        DoubleStream.of(1).noneMatch(n -> n > 1),
        DoubleStream.of(2).noneMatch(pred),
        DoubleStream.of(3).noneMatch(pred));
  }

  boolean testDoubleStreamNoneMatch2() {
    return DoubleStream.of(1).noneMatch(n -> n > 1);
  }

  ImmutableSet<Boolean> testDoubleStreamAnyMatch() {
    return ImmutableSet.of(
        DoubleStream.of(1).anyMatch(n -> n > 1), DoubleStream.of(2).anyMatch(n -> n > 2));
  }

  boolean testDoubleStreamAllMatch() {
    DoublePredicate pred = i -> i > 0;
    return DoubleStream.of(1).allMatch(pred);
  }

  boolean testDoubleStreamAllMatch2() {
    return DoubleStream.of(1).allMatch(n -> n > 1);
  }
}
