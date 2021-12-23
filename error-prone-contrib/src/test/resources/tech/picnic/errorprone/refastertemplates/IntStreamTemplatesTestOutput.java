package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.OptionalInt;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.ConcatOneIntStream;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.ConcatTwoIntStreams;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.FilterOuterIntStreamAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.FilterOuterStreamAfterFlatMapToInt;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.FlatMapOuterIntStreamAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.FlatMapOuterStreamAfterFlatMapToInt;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.IntStreamAllMatch;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.IntStreamAllMatchPredicate;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.IntStreamAnyMatch;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.IntStreamClosedOpenRange;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.IntStreamIsEmpty;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.IntStreamIsNotEmpty;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.IntStreamMin;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.IntStreamNoneMatch;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.IntStreamNoneMatchPredicate;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.MapOuterIntStreamAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.IntStreamTemplates.MapOuterStreamAfterFlatMapToInt;

@TemplateCollection(IntStreamTemplates.class)
final class IntStreamTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  @Template(IntStreamClosedOpenRange.class)
  IntStream testIntStreamClosedOpenRange() {
    return IntStream.range(0, 42);
  }

  @Template(ConcatOneIntStream.class)
  IntStream testConcatOneIntStream() {
    return IntStream.of(1);
  }

  @Template(ConcatTwoIntStreams.class)
  IntStream testConcatTwoIntStreams() {
    return IntStream.concat(IntStream.of(1), IntStream.of(2));
  }

  @Template(FilterOuterIntStreamAfterFlatMap.class)
  IntStream testFilterOuterIntStreamAfterFlatMap() {
    return IntStream.of(1).flatMap(v -> IntStream.of(v * v)).filter(n -> n > 1);
  }

  @Template(FilterOuterStreamAfterFlatMapToInt.class)
  IntStream testFilterOuterStreamAfterFlatMapToInt() {
    return Stream.of(1).flatMapToInt(v -> IntStream.of(v * v)).filter(n -> n > 1);
  }

  @Template(MapOuterIntStreamAfterFlatMap.class)
  IntStream testMapOuterIntStreamAfterFlatMap() {
    return IntStream.of(1).flatMap(v -> IntStream.of(v * v)).map(n -> n * 1);
  }

  @Template(MapOuterStreamAfterFlatMapToInt.class)
  IntStream testMapOuterStreamAfterFlatMapToInt() {
    return Stream.of(1).flatMapToInt(v -> IntStream.of(v * v)).map(n -> n * 1);
  }

  @Template(FlatMapOuterIntStreamAfterFlatMap.class)
  IntStream testFlatMapOuterIntStreamAfterFlatMap() {
    return IntStream.of(1).flatMap(v -> IntStream.of(v * v)).flatMap(IntStream::of);
  }

  @Template(FlatMapOuterStreamAfterFlatMapToInt.class)
  IntStream testFlatMapOuterStreamAfterFlatMapToInt() {
    return Stream.of(1).flatMapToInt(v -> IntStream.of(v * v)).flatMap(IntStream::of);
  }

  @Template(IntStreamIsEmpty.class)
  ImmutableSet<Boolean> testIntStreamIsEmpty() {
    return ImmutableSet.of(
        IntStream.of(1).findAny().isEmpty(),
        IntStream.of(2).findAny().isEmpty(),
        IntStream.of(3).findAny().isEmpty(),
        IntStream.of(4).findAny().isEmpty());
  }

  @Template(IntStreamIsNotEmpty.class)
  ImmutableSet<Boolean> testIntStreamIsNotEmpty() {
    return ImmutableSet.of(
        IntStream.of(1).findAny().isPresent(),
        IntStream.of(2).findAny().isPresent(),
        IntStream.of(3).findAny().isPresent(),
        IntStream.of(4).findAny().isPresent());
  }

  @Template(IntStreamMin.class)
  OptionalInt testIntStreamMin() {
    return IntStream.of(1).min();
  }

  @Template(IntStreamNoneMatch.class)
  boolean testIntStreamNoneMatch() {
    return IntStream.of(1).noneMatch(n -> n > 1);
  }

  @Template(IntStreamNoneMatchPredicate.class)
  ImmutableSet<Boolean> testIntStreamNoneMatchPredicate() {
    IntPredicate pred = i -> i > 0;
    return ImmutableSet.of(
        IntStream.of(1).noneMatch(n -> n > 1),
        IntStream.of(2).noneMatch(pred),
        IntStream.of(3).noneMatch(pred));
  }

  @Template(IntStreamAnyMatch.class)
  ImmutableSet<Boolean> testIntStreamAnyMatch() {
    return ImmutableSet.of(
        IntStream.of(1).anyMatch(n -> n > 1), IntStream.of(2).anyMatch(n -> n > 2));
  }

  @Template(IntStreamAllMatch.class)
  boolean testIntStreamAllMatch() {
    return IntStream.of(1).allMatch(n -> n > 1);
  }

  @Template(IntStreamAllMatchPredicate.class)
  boolean testIntStreamAllMatchPredicate() {
    IntPredicate pred = i -> i > 0;
    return IntStream.of(1).allMatch(pred);
  }
}
