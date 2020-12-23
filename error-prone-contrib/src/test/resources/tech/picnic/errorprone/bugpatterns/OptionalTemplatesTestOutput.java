package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.Streams.stream;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

final class OptionalTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  ImmutableSet<Optional<String>> testOptionalOfNullable() {
    return ImmutableSet.of(Optional.ofNullable(toString()), Optional.ofNullable(toString()));
  }

  ImmutableSet<Boolean> testOptionalIsEmpty() {
    return ImmutableSet.of(Optional.empty().isEmpty(), Optional.of("foo").isEmpty());
  }

  ImmutableSet<Boolean> testOptionalIsPresent() {
    return ImmutableSet.of(Optional.empty().isPresent(), Optional.of("foo").isPresent());
  }

  String testOptionalOrElseThrow() {
    return Optional.of("foo").orElseThrow();
  }

  Function<Optional<Integer>, Integer> testOptionalOrElseThrowMethodReference() {
    return Optional::orElseThrow;
  }

  Stream<Object> testOptionalToStream() {
    return Stream.concat(Optional.empty().stream(), Optional.of("foo").stream());
  }

  ImmutableSet<Optional<String>> testOptionalFirstIteratorElement() {
    return ImmutableSet.of(
        stream(ImmutableSet.of("foo").iterator()).findFirst(),
        stream(ImmutableSet.of("foo").iterator()).findFirst());
  }

  ImmutableSet<Optional<String>> testTernaryOperatorOptionalPositiveFiltering() {
    return ImmutableSet.of(
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("foo")
            .filter(v -> v.length() > 5),
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("bar")
            .filter(v -> !v.contains("baz")));
  }

  ImmutableSet<Optional<String>> testTernaryOperatorOptionalNegativeFiltering() {
    return ImmutableSet.of(
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("foo")
            .filter(v -> v.length() <= 5),
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("bar")
            .filter(v -> v.contains("baz")));
  }

  ImmutableSet<Boolean> testMapOptionalToBoolean() {
    return ImmutableSet.of(
        Optional.of("foo").filter(String::isEmpty).isPresent(),
        Optional.of("bar").filter(s -> s.isEmpty()).isPresent());
  }

  ImmutableSet<Optional<String>> testMapToNullable() {
    return ImmutableSet.of(
        Optional.of(1).map(n -> String.valueOf(n)), Optional.of(2).map(n -> String.valueOf(n)));
  }

  Optional<String> testFlatMapToOptional() {
    return Optional.of(1).flatMap(n -> Optional.of(String.valueOf(n)));
  }

  String testOrOrElseThrow() {
    return Optional.of("foo").or(() -> Optional.of("bar")).orElseThrow();
  }

  ImmutableSet<Object> testStreamFlatMapOptional() {
    return ImmutableSet.of(
        Stream.of(Optional.empty()).flatMap(Optional::stream),
        Stream.of(Optional.of("foo")).flatMap(Optional::stream));
  }

  Stream<String> testStreamMapToOptionalGet() {
    return Stream.of(1).flatMap(n -> Optional.of(String.valueOf(n)).stream());
  }

  Optional<Integer> testFilterOuterOptionalAfterFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length())).filter(len -> len > 0);
  }

  Optional<Integer> testMapOuterOptionalAfterFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length())).map(len -> len * 0);
  }

  Optional<Integer> testFlatMapOuterOptionalAfterFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length())).flatMap(Optional::of);
  }

  ImmutableSet<Optional<String>> testOptionalOrOtherOptional() {
    return ImmutableSet.of(
        Optional.of("foo").or(() -> Optional.of("bar")),
        Optional.of("baz").or(() -> Optional.of("qux")),
        Optional.of("quux").or(() -> Optional.of("quuz")));
  }
}
