package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.OptionalDouble;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class DoubleStreamRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  DoubleStream testDoubleStreamIdentity() {
    return DoubleStream.of(1);
  }

  DoubleStream testDoubleStreamConcat() {
    return DoubleStream.concat(DoubleStream.of(1), DoubleStream.of(2));
  }

  DoubleStream testDoubleStreamFlatMapFilter() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v)).filter(n -> n > 1);
  }

  DoubleStream testStreamFlatMapToDoubleFilter() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v)).filter(n -> n > 1);
  }

  DoubleStream testDoubleStreamFlatMapMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v)).map(n -> n * 1);
  }

  DoubleStream testStreamFlatMapToDoubleMap() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v)).map(n -> n * 1);
  }

  DoubleStream testDoubleStreamFlatMapFlatMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v)).flatMap(DoubleStream::of);
  }

  DoubleStream testStreamFlatMapToDoubleFlatMap() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v)).flatMap(DoubleStream::of);
  }

  DoubleStream testDoubleStreamFilterSorted() {
    return DoubleStream.of(1).filter(d -> d > 0).sorted();
  }

  ImmutableSet<Boolean> testDoubleStreamFindAnyIsEmpty() {
    return ImmutableSet.of(
        DoubleStream.of(1).findAny().isEmpty(),
        DoubleStream.of(2).findAny().isEmpty(),
        DoubleStream.of(3).findAny().isEmpty(),
        DoubleStream.of(4).findAny().isEmpty());
  }

  ImmutableSet<Boolean> testDoubleStreamFindAnyIsPresent() {
    return ImmutableSet.of(
        DoubleStream.of(1).findAny().isPresent(),
        DoubleStream.of(2).findAny().isPresent(),
        DoubleStream.of(3).findAny().isPresent(),
        DoubleStream.of(4).findAny().isPresent());
  }

  OptionalDouble testDoubleStreamMin() {
    return DoubleStream.of(1).min();
  }

  ImmutableSet<Boolean> testDoubleStreamNoneMatchWithDoublePredicate() {
    DoublePredicate pred = i -> i > 0;
    return ImmutableSet.of(
        DoubleStream.of(1).noneMatch(n -> n > 1),
        DoubleStream.of(2).noneMatch(pred),
        DoubleStream.of(3).noneMatch(pred));
  }

  boolean testDoubleStreamNoneMatch() {
    return DoubleStream.of(1).noneMatch(n -> n > 1);
  }

  ImmutableSet<Boolean> testDoubleStreamAnyMatch() {
    return ImmutableSet.of(
        DoubleStream.of(1).anyMatch(n -> n > 1), DoubleStream.of(2).anyMatch(n -> n > 2));
  }

  boolean testDoubleStreamAllMatchWithDoublePredicate() {
    DoublePredicate pred = i -> i > 0;
    return DoubleStream.of(1).allMatch(pred);
  }

  boolean testDoubleStreamAllMatch() {
    return DoubleStream.of(1).allMatch(n -> n > 1);
  }

  DoubleStream testDoubleStreamTakeWhile() {
    return DoubleStream.of(1, 2, 3).takeWhile(i -> i < 2);
  }
}
