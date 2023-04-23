package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.minBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.summingLong;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class StreamRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Objects.class, Streams.class, not(null));
  }

  String testJoining() {
    return Stream.of("foo").collect(joining(""));
  }

  Stream<String> testEmptyStream() {
    return Stream.of();
  }

  ImmutableSet<Stream<String>> testStreamOfNullable() {
    return ImmutableSet.of(
        Stream.of("a").filter(Objects::nonNull), Optional.ofNullable("b").stream());
  }

  Stream<String> testStreamOfArray() {
    return Stream.of(new String[] {"foo", "bar"});
  }

  Stream<Integer> testConcatOneStream() {
    return Streams.concat(Stream.of(1));
  }

  Stream<Integer> testConcatTwoStreams() {
    return Streams.concat(Stream.of(1), Stream.of(2));
  }

  Stream<Integer> testFilterOuterStreamAfterFlatMap() {
    return Stream.of("foo").flatMap(v -> Stream.of(v.length()).filter(len -> len > 0));
  }

  Stream<Integer> testMapOuterStreamAfterFlatMap() {
    return Stream.of("foo").flatMap(v -> Stream.of(v.length()).map(len -> len * 0));
  }

  Stream<Integer> testFlatMapOuterStreamAfterFlatMap() {
    return Stream.of("foo").flatMap(v -> Stream.of(v.length()).flatMap(Stream::of));
  }

  Stream<Integer> testStreamFilterSorted() {
    return Stream.of(1, 4, 3, 2).sorted().filter(i -> i % 2 == 0);
  }

  Stream<Integer> testStreamFilterSortedWithComparator() {
    return Stream.of(1, 4, 3, 2).sorted(reverseOrder()).filter(i -> i % 2 == 0);
  }

  ImmutableSet<Optional<Integer>> testStreamMapFirst() {
    return ImmutableSet.of(
        Stream.of("foo").map(s -> s.length()).findFirst(),
        Stream.of("bar").map(String::length).findFirst());
  }

  ImmutableSet<Boolean> testStreamIsEmpty() {
    return ImmutableSet.of(
        Stream.of(1).count() == 0,
        Stream.of(2).count() <= 0,
        Stream.of(3).count() < 1,
        Stream.of(4).findFirst().isEmpty());
  }

  ImmutableSet<Boolean> testStreamIsNotEmpty() {
    return ImmutableSet.of(
        Stream.of(1).count() != 0,
        Stream.of(2).count() > 0,
        Stream.of(3).count() >= 1,
        Stream.of(4).findFirst().isPresent());
  }

  ImmutableSet<Optional<String>> testStreamMin() {
    return ImmutableSet.of(
        Stream.of("foo").max(comparingInt(String::length).reversed()),
        Stream.of("bar").sorted(comparingInt(String::length)).findFirst(),
        Stream.of("baz").collect(minBy(comparingInt(String::length))));
  }

  ImmutableSet<Optional<String>> testStreamMinNaturalOrder() {
    return ImmutableSet.of(
        Stream.of("foo").max(reverseOrder()), Stream.of("bar").sorted().findFirst());
  }

  ImmutableSet<Optional<String>> testStreamMax() {
    return ImmutableSet.of(
        Stream.of("foo").min(comparingInt(String::length).reversed()),
        Streams.findLast(Stream.of("bar").sorted(comparingInt(String::length))),
        Stream.of("baz").collect(maxBy(comparingInt(String::length))));
  }

  ImmutableSet<Optional<String>> testStreamMaxNaturalOrder() {
    return ImmutableSet.of(
        Stream.of("foo").min(reverseOrder()), Streams.findLast(Stream.of("bar").sorted()));
  }

  ImmutableSet<Boolean> testStreamNoneMatch() {
    Predicate<String> pred = String::isBlank;
    return ImmutableSet.of(
        !Stream.of("foo").anyMatch(s -> s.length() > 1),
        Stream.of("bar").allMatch(not(String::isBlank)),
        Stream.of("baz").allMatch(pred.negate()),
        Stream.of("qux").filter(String::isEmpty).findAny().isEmpty());
  }

  ImmutableSet<Boolean> testStreamNoneMatch2() {
    return ImmutableSet.of(
        Stream.of("foo").allMatch(s -> !s.isBlank()), Stream.of(Boolean.TRUE).allMatch(b -> !b));
  }

  ImmutableSet<Boolean> testStreamAnyMatch() {
    return ImmutableSet.of(
        !Stream.of("foo").noneMatch(s -> s.length() > 1),
        Stream.of("bar").filter(String::isEmpty).findAny().isPresent());
  }

  ImmutableSet<Boolean> testStreamAllMatch() {
    Predicate<String> pred = String::isBlank;
    return ImmutableSet.of(
        Stream.of("foo").noneMatch(not(String::isBlank)),
        Stream.of("bar").noneMatch(pred.negate()));
  }

  boolean testStreamAllMatch2() {
    return Stream.of("foo").noneMatch(s -> !s.isBlank());
  }

  ImmutableSet<Integer> testStreamMapToIntSum() {
    Function<String, Integer> parseIntFunction = Integer::parseInt;
    return ImmutableSet.of(
        Stream.of(1).map(i -> i * 2).reduce(0, Integer::sum),
        Stream.of("2").map(Integer::parseInt).reduce(0, Integer::sum),
        Stream.of("3").map(parseIntFunction).reduce(0, Integer::sum));
  }

  Integer testStreamCollectSummingInt() {
    return Stream.of("1").collect(summingInt(Integer::parseInt));
  }

  ImmutableSet<Double> testStreamMapToDoubleSum() {
    Function<String, Double> parseDoubleFunction = Double::parseDouble;
    return ImmutableSet.of(
        Stream.of(1).map(i -> i * 2.0).reduce(0.0, Double::sum),
        Stream.of("2").map(Double::parseDouble).reduce(0.0, Double::sum),
        Stream.of("3").map(parseDoubleFunction).reduce(0.0, Double::sum));
  }

  Double testStreamCollectSummingDouble() {
    return Stream.of("1").collect(summingDouble(Double::parseDouble));
  }

  ImmutableSet<Long> testStreamMapToLongSum() {
    Function<String, Long> parseLongFunction = Long::parseLong;
    return ImmutableSet.of(
        Stream.of(1).map(i -> i * 2L).reduce(0L, Long::sum),
        Stream.of("2").map(Long::parseLong).reduce(0L, Long::sum),
        Stream.of("3").map(parseLongFunction).reduce(0L, Long::sum));
  }

  Long testStreamCollectSummingLong() {
    return Stream.of("1").collect(summingLong(Long::parseLong));
  }

  Long testStreamCount() {
    return Stream.of(1).collect(counting());
  }

  Integer testStreamReduce() {
    return Stream.of(1).collect(reducing(0, Integer::sum));
  }

  Optional<Integer> testStreamReduceOptional() {
    return Stream.of(1).collect(reducing(Integer::sum));
  }

  ImmutableSet<Integer> testStreamMap() {
    return Stream.of("1").collect(mapping(Integer::parseInt, toImmutableSet()));
  }
}
