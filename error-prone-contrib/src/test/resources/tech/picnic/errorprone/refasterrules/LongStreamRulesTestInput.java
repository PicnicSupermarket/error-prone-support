package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.OptionalLong;
import java.util.function.LongPredicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class LongStreamRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  LongStream testLongStreamRange() {
    return LongStream.rangeClosed(0, 42 - 1);
  }

  LongStream testLongStreamIdentity() {
    return Streams.concat(LongStream.of(1));
  }

  LongStream testLongStreamConcat() {
    return Streams.concat(LongStream.of(1), LongStream.of(2));
  }

  LongStream testLongStreamFlatMapFilter() {
    return LongStream.of(1).flatMap(v -> LongStream.of(v * v).filter(n -> n > 1));
  }

  LongStream testStreamFlatMapToLongFilter() {
    return Stream.of(1).flatMapToLong(v -> LongStream.of(v * v).filter(n -> n > 1));
  }

  LongStream testLongStreamFlatMapMap() {
    return LongStream.of(1).flatMap(v -> LongStream.of(v * v).map(n -> n * 1));
  }

  LongStream testStreamFlatMapToLongMap() {
    return Stream.of(1).flatMapToLong(v -> LongStream.of(v * v).map(n -> n * 1));
  }

  LongStream testLongStreamFlatMapFlatMap() {
    return LongStream.of(1).flatMap(v -> LongStream.of(v * v).flatMap(LongStream::of));
  }

  LongStream testStreamFlatMapToLongFlatMap() {
    return Stream.of(1).flatMapToLong(v -> LongStream.of(v * v).flatMap(LongStream::of));
  }

  LongStream testLongStreamFilterSorted() {
    return LongStream.of(1, 4, 3, 2).sorted().filter(l -> l % 2 == 0);
  }

  ImmutableSet<Boolean> testLongStreamFindAnyIsEmpty() {
    return ImmutableSet.of(
        LongStream.of(1).count() == 0,
        LongStream.of(2).count() <= 0,
        LongStream.of(3).count() < 1,
        LongStream.of(4).findFirst().isEmpty());
  }

  ImmutableSet<Boolean> testLongStreamFindAnyIsPresent() {
    return ImmutableSet.of(
        LongStream.of(1).count() != 0,
        LongStream.of(2).count() > 0,
        LongStream.of(3).count() >= 1,
        LongStream.of(4).findFirst().isPresent());
  }

  OptionalLong testLongStreamMin() {
    return LongStream.of(1).sorted().findFirst();
  }

  ImmutableSet<Boolean> testLongStreamNoneMatchWithLongPredicate() {
    LongPredicate pred = i -> i > 0;
    return ImmutableSet.of(
        !LongStream.of(1).anyMatch(n -> n > 1),
        LongStream.of(2).allMatch(pred.negate()),
        LongStream.of(3).filter(pred).findAny().isEmpty());
  }

  boolean testLongStreamNoneMatch() {
    return LongStream.of(1).allMatch(n -> !(n > 1));
  }

  ImmutableSet<Boolean> testLongStreamAnyMatch() {
    return ImmutableSet.of(
        !LongStream.of(1).noneMatch(n -> n > 1),
        LongStream.of(2).filter(n -> n > 2).findAny().isPresent());
  }

  boolean testLongStreamAllMatchWithLongPredicate() {
    LongPredicate pred = i -> i > 0;
    return LongStream.of(1).noneMatch(pred.negate());
  }

  boolean testLongStreamAllMatch() {
    return LongStream.of(1).noneMatch(n -> !(n > 1));
  }

  LongStream testLongStreamTakeWhile() {
    return LongStream.of(1, 2, 3).takeWhile(i -> i < 2).filter(i -> i < 2);
  }
}
