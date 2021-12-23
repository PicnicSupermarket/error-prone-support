package tech.picnic.errorprone.refastertemplates;

import static java.util.Comparator.comparingInt;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Predicate.not;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.ConcatOneStream;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.ConcatTwoStreams;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.EmptyStream;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.FilterOuterStreamAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.FlatMapOuterStreamAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.MapOuterStreamAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamAllMatch;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamAllMatchPredicate;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamAnyMatch;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamIsEmpty;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamIsNotEmpty;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamMapFirst;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamMax;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamMaxNaturalOrder;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamMin;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamMinNaturalOrder;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamNoneMatch;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamNoneMatchPredicate;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamOfArray;
import tech.picnic.errorprone.refastertemplates.StreamTemplates.StreamOfNullable;

@TemplateCollection(StreamTemplates.class)
final class StreamTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Objects.class, Streams.class, not(null), reverseOrder());
  }

  @Template(EmptyStream.class)
  Stream<String> testEmptyStream() {
    return Stream.of();
  }

  @Template(StreamOfNullable.class)
  ImmutableSet<Stream<String>> testStreamOfNullable() {
    return ImmutableSet.of(
        Stream.of("a").filter(Objects::nonNull), Optional.ofNullable("b").stream());
  }

  @Template(StreamOfArray.class)
  Stream<String> testStreamOfArray() {
    return Stream.of(new String[] {"foo", "bar"});
  }

  @Template(ConcatOneStream.class)
  Stream<Integer> testConcatOneStream() {
    return Streams.concat(Stream.of(1));
  }

  @Template(ConcatTwoStreams.class)
  Stream<Integer> testConcatTwoStreams() {
    return Streams.concat(Stream.of(1), Stream.of(2));
  }

  @Template(FilterOuterStreamAfterFlatMap.class)
  Stream<Integer> testFilterOuterStreamAfterFlatMap() {
    return Stream.of("foo").flatMap(v -> Stream.of(v.length()).filter(len -> len > 0));
  }

  @Template(MapOuterStreamAfterFlatMap.class)
  Stream<Integer> testMapOuterStreamAfterFlatMap() {
    return Stream.of("foo").flatMap(v -> Stream.of(v.length()).map(len -> len * 0));
  }

  @Template(FlatMapOuterStreamAfterFlatMap.class)
  Stream<Integer> testFlatMapOuterStreamAfterFlatMap() {
    return Stream.of("foo").flatMap(v -> Stream.of(v.length()).flatMap(Stream::of));
  }

  @Template(StreamMapFirst.class)
  ImmutableSet<Optional<Integer>> testStreamMapFirst() {
    return ImmutableSet.of(
        Stream.of("foo").map(s -> s.length()).findFirst(),
        Stream.of("bar").map(String::length).findFirst());
  }

  @Template(StreamIsEmpty.class)
  ImmutableSet<Boolean> testStreamIsEmpty() {
    return ImmutableSet.of(
        Stream.of(1).count() == 0,
        Stream.of(2).count() <= 0,
        Stream.of(3).count() < 1,
        Stream.of(4).findFirst().isEmpty());
  }

  @Template(StreamIsNotEmpty.class)
  ImmutableSet<Boolean> testStreamIsNotEmpty() {
    return ImmutableSet.of(
        Stream.of(1).count() != 0,
        Stream.of(2).count() > 0,
        Stream.of(3).count() >= 1,
        Stream.of(4).findFirst().isPresent());
  }

  @Template(StreamMin.class)
  ImmutableSet<Optional<String>> testStreamMin() {
    return ImmutableSet.of(
        Stream.of("foo").max(comparingInt(String::length).reversed()),
        Stream.of("bar").sorted(comparingInt(String::length)).findFirst());
  }

  @Template(StreamMinNaturalOrder.class)
  ImmutableSet<Optional<String>> testStreamMinNaturalOrder() {
    return ImmutableSet.of(
        Stream.of("foo").max(reverseOrder()), Stream.of("bar").sorted().findFirst());
  }

  @Template(StreamMax.class)
  ImmutableSet<Optional<String>> testStreamMax() {
    return ImmutableSet.of(
        Stream.of("foo").min(comparingInt(String::length).reversed()),
        Streams.findLast(Stream.of("bar").sorted(comparingInt(String::length))));
  }

  @Template(StreamMaxNaturalOrder.class)
  ImmutableSet<Optional<String>> testStreamMaxNaturalOrder() {
    return ImmutableSet.of(
        Stream.of("foo").min(reverseOrder()), Streams.findLast(Stream.of("bar").sorted()));
  }

  @Template(StreamNoneMatch.class)
  ImmutableSet<Boolean> testStreamNoneMatch() {
    return ImmutableSet.of(
        Stream.of("foo").allMatch(s -> !s.isBlank()), Stream.of(Boolean.TRUE).allMatch(b -> !b));
  }

  @Template(StreamNoneMatchPredicate.class)
  ImmutableSet<Boolean> testStreamNoneMatchPredicate() {
    Predicate<String> pred = String::isBlank;
    return ImmutableSet.of(
        !Stream.of("foo").anyMatch(s -> s.length() > 1),
        Stream.of("bar").allMatch(not(String::isBlank)),
        Stream.of("baz").allMatch(pred.negate()),
        Stream.of("qux").filter(String::isEmpty).findAny().isEmpty());
  }

  @Template(StreamAnyMatch.class)
  ImmutableSet<Boolean> testStreamAnyMatch() {
    return ImmutableSet.of(
        !Stream.of("foo").noneMatch(s -> s.length() > 1),
        Stream.of("bar").filter(String::isEmpty).findAny().isPresent());
  }

  @Template(StreamAllMatch.class)
  boolean testStreamAllMatch() {
    return Stream.of("foo").noneMatch(s -> !s.isBlank());
  }

  @Template(StreamAllMatchPredicate.class)
  ImmutableSet<Boolean> testStreamAllMatchPredicate() {
    Predicate<String> pred = String::isBlank;
    return ImmutableSet.of(
        Stream.of("foo").noneMatch(not(String::isBlank)),
        Stream.of("bar").noneMatch(pred.negate()));
  }
}
