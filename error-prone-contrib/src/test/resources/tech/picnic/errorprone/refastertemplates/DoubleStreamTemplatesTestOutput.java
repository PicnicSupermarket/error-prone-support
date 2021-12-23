package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.OptionalDouble;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.ConcatOneDoubleStream;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.ConcatTwoDoubleStreams;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.DoubleStreamAllMatch;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.DoubleStreamAllMatchPredicate;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.DoubleStreamAnyMatch;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.DoubleStreamIsEmpty;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.DoubleStreamIsNotEmpty;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.DoubleStreamMin;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.DoubleStreamNoneMatch;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.DoubleStreamNoneMatchPredicate;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.FilterOuterDoubleStreamAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.FilterOuterStreamAfterFlatMapToDouble;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.FlatMapOuterDoubleStreamAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.FlatMapOuterStreamAfterFlatMapToDouble;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.MapOuterDoubleStreamAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.DoubleStreamTemplates.MapOuterStreamAfterFlatMapToDouble;

@TemplateCollection(DoubleStreamTemplates.class)
final class DoubleStreamTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  @Template(ConcatOneDoubleStream.class)
  DoubleStream testConcatOneDoubleStream() {
    return DoubleStream.of(1);
  }

  @Template(ConcatTwoDoubleStreams.class)
  DoubleStream testConcatTwoDoubleStreams() {
    return DoubleStream.concat(DoubleStream.of(1), DoubleStream.of(2));
  }

  @Template(FilterOuterDoubleStreamAfterFlatMap.class)
  DoubleStream testFilterOuterDoubleStreamAfterFlatMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v)).filter(n -> n > 1);
  }

  @Template(FilterOuterStreamAfterFlatMapToDouble.class)
  DoubleStream testFilterOuterStreamAfterFlatMapToDouble() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v)).filter(n -> n > 1);
  }

  @Template(MapOuterDoubleStreamAfterFlatMap.class)
  DoubleStream testMapOuterDoubleStreamAfterFlatMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v)).map(n -> n * 1);
  }

  @Template(MapOuterStreamAfterFlatMapToDouble.class)
  DoubleStream testMapOuterStreamAfterFlatMapToDouble() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v)).map(n -> n * 1);
  }

  @Template(FlatMapOuterDoubleStreamAfterFlatMap.class)
  DoubleStream testFlatMapOuterDoubleStreamAfterFlatMap() {
    return DoubleStream.of(1).flatMap(v -> DoubleStream.of(v * v)).flatMap(DoubleStream::of);
  }

  @Template(FlatMapOuterStreamAfterFlatMapToDouble.class)
  DoubleStream testFlatMapOuterStreamAfterFlatMapToDouble() {
    return Stream.of(1).flatMapToDouble(v -> DoubleStream.of(v * v)).flatMap(DoubleStream::of);
  }

  @Template(DoubleStreamIsEmpty.class)
  ImmutableSet<Boolean> testDoubleStreamIsEmpty() {
    return ImmutableSet.of(
        DoubleStream.of(1).findAny().isEmpty(),
        DoubleStream.of(2).findAny().isEmpty(),
        DoubleStream.of(3).findAny().isEmpty(),
        DoubleStream.of(4).findAny().isEmpty());
  }

  @Template(DoubleStreamIsNotEmpty.class)
  ImmutableSet<Boolean> testDoubleStreamIsNotEmpty() {
    return ImmutableSet.of(
        DoubleStream.of(1).findAny().isPresent(),
        DoubleStream.of(2).findAny().isPresent(),
        DoubleStream.of(3).findAny().isPresent(),
        DoubleStream.of(4).findAny().isPresent());
  }

  @Template(DoubleStreamMin.class)
  OptionalDouble testDoubleStreamMin() {
    return DoubleStream.of(1).min();
  }

  @Template(DoubleStreamNoneMatch.class)
  boolean testDoubleStreamNoneMatch() {
    return DoubleStream.of(1).noneMatch(n -> n > 1);
  }

  @Template(DoubleStreamNoneMatchPredicate.class)
  ImmutableSet<Boolean> testDoubleStreamNoneMatchPredicate() {
    DoublePredicate pred = i -> i > 0;
    return ImmutableSet.of(
        DoubleStream.of(1).noneMatch(n -> n > 1),
        DoubleStream.of(2).noneMatch(pred),
        DoubleStream.of(3).noneMatch(pred));
  }

  @Template(DoubleStreamAnyMatch.class)
  ImmutableSet<Boolean> testDoubleStreamAnyMatch() {
    return ImmutableSet.of(
        DoubleStream.of(1).anyMatch(n -> n > 1), DoubleStream.of(2).anyMatch(n -> n > 2));
  }

  @Template(DoubleStreamAllMatch.class)
  boolean testDoubleStreamAllMatch() {
    return DoubleStream.of(1).allMatch(n -> n > 1);
  }

  @Template(DoubleStreamAllMatchPredicate.class)
  boolean testDoubleStreamAllMatchPredicate() {
    DoublePredicate pred = i -> i > 0;
    return DoubleStream.of(1).allMatch(pred);
  }
}
