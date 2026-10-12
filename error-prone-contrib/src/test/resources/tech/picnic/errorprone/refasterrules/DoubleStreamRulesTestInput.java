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
    return Streams.concat(DoubleStream.of(1));
  }

  DoubleStream testDoubleStreamConcat() {
    return Streams.concat(DoubleStream.of(1), DoubleStream.of(2));
  }

  DoubleStream testDoubleStreamFlatMapFilter() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v).filter(n -> n > 1));
  }

  DoubleStream testStreamFlatMapToDoubleFilter() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v).filter(n -> n > 1));
  }

  DoubleStream testDoubleStreamFlatMapMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v).map(n -> n * 1));
  }

  DoubleStream testStreamFlatMapToDoubleMap() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v).map(n -> n * 1));
  }

  DoubleStream testDoubleStreamFlatMapFlatMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v).flatMap(DoubleStream::of));
  }

  DoubleStream testStreamFlatMapToDoubleFlatMap() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v).flatMap(DoubleStream::of));
  }

  DoubleStream testDoubleStreamFilterSorted() {
    return DoubleStream.of(1).sorted().filter(d -> d > 0);
  }

  ImmutableSet<Boolean> testDoubleStreamFindAnyIsEmpty() {
    return ImmutableSet.of(
        DoubleStream.of(1).count() == 0,
        DoubleStream.of(2).count() <= 0,
        DoubleStream.of(3).count() < 1,
        DoubleStream.of(4).findFirst().isEmpty());
  }

  ImmutableSet<Boolean> testDoubleStreamFindAnyIsPresent() {
    return ImmutableSet.of(
        DoubleStream.of(1).count() != 0,
        DoubleStream.of(2).count() > 0,
        DoubleStream.of(3).count() >= 1,
        DoubleStream.of(4).findFirst().isPresent());
  }

  OptionalDouble testDoubleStreamMin() {
    return DoubleStream.of(1).sorted().findFirst();
  }

  ImmutableSet<Boolean> testDoubleStreamNoneMatchWithDoublePredicate() {
    DoublePredicate pred = i -> i > 0;
    return ImmutableSet.of(
        !DoubleStream.of(1).anyMatch(n -> n > 1),
        DoubleStream.of(2).allMatch(pred.negate()),
        DoubleStream.of(3).filter(pred).findAny().isEmpty());
  }

  boolean testDoubleStreamNoneMatch() {
    return DoubleStream.of(1).allMatch(n -> !(n > 1));
  }

  ImmutableSet<Boolean> testDoubleStreamAnyMatch() {
    return ImmutableSet.of(
        !DoubleStream.of(1).noneMatch(n -> n > 1),
        DoubleStream.of(2).filter(n -> n > 2).findAny().isPresent());
  }

  boolean testDoubleStreamAllMatchWithDoublePredicate() {
    DoublePredicate pred = i -> i > 0;
    return DoubleStream.of(1).noneMatch(pred.negate());
  }

  boolean testDoubleStreamAllMatch() {
    return DoubleStream.of(1).noneMatch(n -> !(n > 1));
  }

  DoubleStream testDoubleStreamTakeWhile() {
    return DoubleStream.of(1, 2, 3).takeWhile(i -> i < 2).filter(i -> i < 2);
  }
}
