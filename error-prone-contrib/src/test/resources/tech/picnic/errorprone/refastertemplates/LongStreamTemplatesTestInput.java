package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.OptionalLong;
import java.util.function.LongPredicate;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.ConcatOneLongStream;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.ConcatTwoLongStreams;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.FilterOuterLongStreamAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.FilterOuterStreamAfterFlatMapToLong;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.FlatMapOuterLongStreamAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.FlatMapOuterStreamAfterFlatMapToLong;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.LongStreamAllMatch;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.LongStreamAllMatchPredicate;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.LongStreamAnyMatch;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.LongStreamClosedOpenRange;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.LongStreamIsEmpty;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.LongStreamIsNotEmpty;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.LongStreamMin;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.LongStreamNoneMatch;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.LongStreamNoneMatchPredicate;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.MapOuterLongStreamAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.LongStreamTemplates.MapOuterStreamAfterFlatMapToLong;

@TemplateCollection(LongStreamTemplates.class)
final class LongStreamTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  @Template(LongStreamClosedOpenRange.class)
  LongStream testLongStreamClosedOpenRange() {
    return LongStream.rangeClosed(0, 42 - 1);
  }

  @Template(ConcatOneLongStream.class)
  LongStream testConcatOneLongStream() {
    return Streams.concat(LongStream.of(1));
  }

  @Template(ConcatTwoLongStreams.class)
  LongStream testConcatTwoLongStreams() {
    return Streams.concat(LongStream.of(1), LongStream.of(2));
  }

  @Template(FilterOuterLongStreamAfterFlatMap.class)
  LongStream testFilterOuterLongStreamAfterFlatMap() {
    return LongStream.of(1).flatMap(v -> LongStream.of(v * v).filter(n -> n > 1));
  }

  @Template(FilterOuterStreamAfterFlatMapToLong.class)
  LongStream testFilterOuterStreamAfterFlatMapToLong() {
    return Stream.of(1).flatMapToLong(v -> LongStream.of(v * v).filter(n -> n > 1));
  }

  @Template(MapOuterLongStreamAfterFlatMap.class)
  LongStream testMapOuterLongStreamAfterFlatMap() {
    return LongStream.of(1).flatMap(v -> LongStream.of(v * v).map(n -> n * 1));
  }

  @Template(MapOuterStreamAfterFlatMapToLong.class)
  LongStream testMapOuterStreamAfterFlatMapToLong() {
    return Stream.of(1).flatMapToLong(v -> LongStream.of(v * v).map(n -> n * 1));
  }

  @Template(FlatMapOuterLongStreamAfterFlatMap.class)
  LongStream testFlatMapOuterLongStreamAfterFlatMap() {
    return LongStream.of(1).flatMap(v -> LongStream.of(v * v).flatMap(LongStream::of));
  }

  @Template(FlatMapOuterStreamAfterFlatMapToLong.class)
  LongStream testFlatMapOuterStreamAfterFlatMapToLong() {
    return Stream.of(1).flatMapToLong(v -> LongStream.of(v * v).flatMap(LongStream::of));
  }

  @Template(LongStreamIsEmpty.class)
  ImmutableSet<Boolean> testLongStreamIsEmpty() {
    return ImmutableSet.of(
        LongStream.of(1).count() == 0,
        LongStream.of(2).count() <= 0,
        LongStream.of(3).count() < 1,
        LongStream.of(4).findFirst().isEmpty());
  }

  @Template(LongStreamIsNotEmpty.class)
  ImmutableSet<Boolean> testLongStreamIsNotEmpty() {
    return ImmutableSet.of(
        LongStream.of(1).count() != 0,
        LongStream.of(2).count() > 0,
        LongStream.of(3).count() >= 1,
        LongStream.of(4).findFirst().isPresent());
  }

  @Template(LongStreamMin.class)
  OptionalLong testLongStreamMin() {
    return LongStream.of(1).sorted().findFirst();
  }

  @Template(LongStreamNoneMatch.class)
  boolean testLongStreamNoneMatch() {
    return LongStream.of(1).allMatch(n -> !(n > 1));
  }

  @Template(LongStreamNoneMatchPredicate.class)
  ImmutableSet<Boolean> testLongStreamNoneMatchPredicate() {
    LongPredicate pred = i -> i > 0;
    return ImmutableSet.of(
        !LongStream.of(1).anyMatch(n -> n > 1),
        LongStream.of(2).allMatch(pred.negate()),
        LongStream.of(3).filter(pred).findAny().isEmpty());
  }

  @Template(LongStreamAnyMatch.class)
  ImmutableSet<Boolean> testLongStreamAnyMatch() {
    return ImmutableSet.of(
        !LongStream.of(1).noneMatch(n -> n > 1),
        LongStream.of(2).filter(n -> n > 2).findAny().isPresent());
  }

  @Template(LongStreamAllMatch.class)
  boolean testLongStreamAllMatch() {
    return LongStream.of(1).noneMatch(n -> !(n > 1));
  }

  @Template(LongStreamAllMatchPredicate.class)
  boolean testLongStreamAllMatchPredicate() {
    LongPredicate pred = i -> i > 0;
    return LongStream.of(1).noneMatch(pred.negate());
  }
}
