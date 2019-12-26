package tech.picnic.errorprone.bugpatterns;

import static java.util.function.Predicate.not;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

final class StreamTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Objects.class, Streams.class, (Runnable) () -> not(null));
  }

  Stream<String> testEmptyStream() {
    return Stream.empty();
  }

  ImmutableSet<Stream<String>> testStreamOfNullable() {
    return ImmutableSet.of(Stream.ofNullable("a"), Stream.ofNullable("b"));
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
        Stream.of(4).findAny().isEmpty());
  }

  ImmutableSet<Boolean> testStreamIsNotEmpty() {
    return ImmutableSet.of(
        Stream.of(1).findAny().isPresent(),
        Stream.of(2).findAny().isPresent(),
        Stream.of(3).findAny().isPresent(),
        Stream.of(4).findAny().isPresent());
  }

  ImmutableSet<Boolean> testStreamNoneMatch() {
    Predicate<String> pred = String::isBlank;
    return ImmutableSet.of(
        Stream.of("foo").noneMatch(s -> s.length() > 1),
        Stream.of("bar").noneMatch(String::isBlank),
        Stream.of("baz").noneMatch(pred),
        Stream.of("qux").noneMatch(String::isEmpty));
  }

  boolean testStreamNoneMatch2() {
    return Stream.of("foo").noneMatch(s -> s.isBlank());
  }

  ImmutableSet<Boolean> testStreamAnyMatch() {
    return ImmutableSet.of(
        Stream.of("foo").anyMatch(s -> s.length() > 1), Stream.of("bar").anyMatch(String::isEmpty));
  }

  ImmutableSet<Boolean> testStreamAllMatch() {
    Predicate<String> pred = String::isBlank;
    return ImmutableSet.of(
        Stream.of("foo").allMatch(String::isBlank),
        Stream.of("bar").allMatch(pred),
        Stream.of("baz").allMatch(s -> s.length() > 1),
        Stream.of("qux").allMatch(pred),
        Stream.of("quux").allMatch(String::isEmpty),
        Stream.of("quuz").allMatch(pred));
  }

  ImmutableSet<Boolean> testStreamAllMatch2() {
    return ImmutableSet.of(
        Stream.of("foo").allMatch(s -> s.isBlank()),
        Stream.of("bar").allMatch(s -> s.isEmpty()),
        Stream.of("baz").allMatch(s -> s.isBlank()));
  }
}
