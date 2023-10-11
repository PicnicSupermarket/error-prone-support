package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.minBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.summarizingDouble;
import static java.util.stream.Collectors.summarizingInt;
import static java.util.stream.Collectors.summarizingLong;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.summingLong;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class StreamRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        ImmutableList.class,
        Objects.class,
        Streams.class,
        counting(),
        filtering(null, null),
        flatMapping(null, null),
        identity(),
        mapping(null, null),
        maxBy(null),
        minBy(null),
        not(null),
        reducing(null),
        summarizingDouble(null),
        summarizingInt(null),
        summarizingLong(null),
        summingDouble(null),
        summingInt(null),
        summingLong(null));
  }

  String testJoining() {
    return Stream.of("foo").collect(joining());
  }

  Stream<String> testEmptyStream() {
    return Stream.empty();
  }

  ImmutableSet<Stream<String>> testStreamOfNullable() {
    return ImmutableSet.of(Stream.ofNullable("a"), Stream.ofNullable("b"));
  }

  Stream<String> testStreamOfArray() {
    return Arrays.stream(new String[] {"foo", "bar"});
  }

  Stream<Integer> testConcatOneStream() {
    return Stream.of(1);
  }

  Stream<Integer> testConcatTwoStreams() {
    return Stream.concat(Stream.of(1), Stream.of(2));
  }

  Stream<Integer> testFilterOuterStreamAfterFlatMap() {
    return Stream.of("foo").flatMap(v -> Stream.of(v.length())).filter(len -> len > 0);
  }

  Stream<Integer> testMapOuterStreamAfterFlatMap() {
    return Stream.of("foo").flatMap(v -> Stream.of(v.length())).map(len -> len * 0);
  }

  Stream<Integer> testFlatMapOuterStreamAfterFlatMap() {
    return Stream.of("foo").flatMap(v -> Stream.of(v.length())).flatMap(Stream::of);
  }

  Stream<Integer> testStreamFilterSorted() {
    return Stream.of(1, 4, 3, 2).filter(i -> i % 2 == 0).sorted();
  }

  Stream<Integer> testStreamFilterSortedWithComparator() {
    return Stream.of(1, 4, 3, 2).filter(i -> i % 2 == 0).sorted(reverseOrder());
  }

  ImmutableSet<Optional<Integer>> testStreamMapFirst() {
    return ImmutableSet.of(
        Stream.of("foo").findFirst().map(s -> s.length()),
        Stream.of("bar").findFirst().map(String::length));
  }

  ImmutableSet<Boolean> testStreamIsEmpty() {
    return ImmutableSet.of(
        Stream.of(1).findAny().isEmpty(),
        Stream.of(2).findAny().isEmpty(),
        Stream.of(3).findAny().isEmpty(),
        Stream.of(4).findAny().isEmpty(),
        Stream.of(5).findAny().isEmpty());
  }

  ImmutableSet<Boolean> testStreamIsNotEmpty() {
    return ImmutableSet.of(
        Stream.of(1).findAny().isPresent(),
        Stream.of(2).findAny().isPresent(),
        Stream.of(3).findAny().isPresent(),
        Stream.of(4).findAny().isPresent());
  }

  ImmutableSet<Optional<String>> testStreamMin() {
    return ImmutableSet.of(
        Stream.of("foo").min(comparingInt(String::length)),
        Stream.of("bar").min(comparingInt(String::length)),
        Stream.of("baz").min(comparingInt(String::length)));
  }

  ImmutableSet<Optional<String>> testStreamMinNaturalOrder() {
    return ImmutableSet.of(
        Stream.of("foo").min(naturalOrder()), Stream.of("bar").min(naturalOrder()));
  }

  ImmutableSet<Optional<String>> testStreamMax() {
    return ImmutableSet.of(
        Stream.of("foo").max(comparingInt(String::length)),
        Stream.of("bar").max(comparingInt(String::length)),
        Stream.of("baz").max(comparingInt(String::length)));
  }

  ImmutableSet<Optional<String>> testStreamMaxNaturalOrder() {
    return ImmutableSet.of(
        Stream.of("foo").max(naturalOrder()), Stream.of("bar").max(naturalOrder()));
  }

  ImmutableSet<Boolean> testStreamNoneMatch() {
    Predicate<String> pred = String::isBlank;
    Function<String, Boolean> toBooleanFunction = Boolean::valueOf;
    return ImmutableSet.of(
        Stream.of("foo").noneMatch(s -> s.length() > 1),
        Stream.of("bar").noneMatch(String::isBlank),
        Stream.of("baz").noneMatch(pred),
        Stream.of("qux").noneMatch(String::isEmpty),
        Stream.of("quux").noneMatch(s -> s.isBlank()),
        Stream.of("quuz").noneMatch(Boolean::valueOf),
        Stream.of("corge").map(toBooleanFunction).noneMatch(Boolean::booleanValue));
  }

  ImmutableSet<Boolean> testStreamNoneMatch2() {
    return ImmutableSet.of(
        Stream.of("foo").noneMatch(s -> s.isBlank()), Stream.of(Boolean.TRUE).noneMatch(b -> b));
  }

  ImmutableSet<Boolean> testStreamAnyMatch() {
    Function<String, Boolean> toBooleanFunction = Boolean::valueOf;
    return ImmutableSet.of(
        Stream.of("foo").anyMatch(s -> s.length() > 1),
        Stream.of("bar").anyMatch(String::isEmpty),
        Stream.of("baz").anyMatch(s -> s.isBlank()),
        Stream.of("qux").anyMatch(Boolean::valueOf),
        Stream.of("quux").map(toBooleanFunction).anyMatch(Boolean::booleanValue));
  }

  ImmutableSet<Boolean> testStreamAllMatch() {
    Predicate<String> pred = String::isBlank;
    Function<String, Boolean> toBooleanFunction = Boolean::valueOf;
    return ImmutableSet.of(
        Stream.of("foo").allMatch(String::isBlank),
        Stream.of("bar").allMatch(pred),
        Stream.of("baz").allMatch(s -> s.isBlank()),
        Stream.of("qux").allMatch(Boolean::valueOf),
        Stream.of("quux").map(toBooleanFunction).anyMatch(Boolean::booleanValue));
  }

  boolean testStreamAllMatch2() {
    return Stream.of("foo").allMatch(s -> s.isBlank());
  }

  ImmutableSet<Integer> testStreamMapToIntSum() {
    Function<String, Integer> parseIntFunction = Integer::parseInt;
    return ImmutableSet.of(
        Stream.of("1").mapToInt(Integer::parseInt).sum(),
        Stream.of(2).mapToInt(i -> i * 2).sum(),
        Stream.of("3").mapToInt(Integer::parseInt).sum(),
        Stream.of("4").map(parseIntFunction).reduce(0, Integer::sum));
  }

  ImmutableSet<Double> testStreamMapToDoubleSum() {
    Function<String, Double> parseDoubleFunction = Double::parseDouble;
    return ImmutableSet.of(
        Stream.of("1").mapToDouble(Double::parseDouble).sum(),
        Stream.of(2).mapToDouble(i -> i * 2.0).sum(),
        Stream.of("3").mapToDouble(Double::parseDouble).sum(),
        Stream.of("4").map(parseDoubleFunction).reduce(0.0, Double::sum));
  }

  ImmutableSet<Long> testStreamMapToLongSum() {
    Function<String, Long> parseLongFunction = Long::parseLong;
    return ImmutableSet.of(
        Stream.of("1").mapToLong(Long::parseLong).sum(),
        Stream.of(2).mapToLong(i -> i * 2L).sum(),
        Stream.of("3").mapToLong(Long::parseLong).sum(),
        Stream.of("4").map(parseLongFunction).reduce(0L, Long::sum));
  }

  IntSummaryStatistics testStreamMapToIntSummaryStatistics() {
    return Stream.of("1").mapToInt(Integer::parseInt).summaryStatistics();
  }

  DoubleSummaryStatistics testStreamMapToDoubleSummaryStatistics() {
    return Stream.of("1").mapToDouble(Double::parseDouble).summaryStatistics();
  }

  LongSummaryStatistics testStreamMapToLongSummaryStatistics() {
    return Stream.of("1").mapToLong(Long::parseLong).summaryStatistics();
  }

  Long testStreamCount() {
    return Stream.of(1).count();
  }

  Optional<Integer> testStreamReduce() {
    return Stream.of(1).reduce(Integer::sum);
  }

  Integer testStreamReduceWithIdentity() {
    return Stream.of(1).reduce(0, Integer::sum);
  }

  ImmutableSet<String> testStreamFilterCollect() {
    return Stream.of("1").filter(String::isEmpty).collect(toImmutableSet());
  }

  ImmutableSet<Integer> testStreamMapCollect() {
    return Stream.of("1").map(Integer::parseInt).collect(toImmutableSet());
  }

  ImmutableSet<Integer> testStreamFlatMapCollect() {
    return Stream.of(1).flatMap(n -> Stream.of(n, n)).collect(toImmutableSet());
  }

  ImmutableSet<Stream<Integer>> testStreamsConcat() {
    return ImmutableSet.of(
        Streams.concat(Stream.of(1), Stream.of(2)), Streams.concat(Stream.of(3), Stream.of(4)));
  }

  Stream<Integer> testStreamTakeWhile() {
    return Stream.of(1, 2, 3).takeWhile(i -> i < 2);
  }

  Stream<Integer> testStreamIterate() {
    return Stream.iterate(0, i -> i < 10, i -> i + 1);
  }

  Stream<Integer> testStreamOf1() {
    return Stream.of(1);
  }

  Stream<Integer> testStreamOf2() {
    return Stream.of(1, 2);
  }

  Stream<Integer> testStreamOf3() {
    return Stream.of(1, 2, 3);
  }

  Stream<Integer> testStreamOf4() {
    return Stream.of(1, 2, 3, 4);
  }

  Stream<Integer> testStreamOf5() {
    return Stream.of(1, 2, 3, 4, 5);
  }
}
