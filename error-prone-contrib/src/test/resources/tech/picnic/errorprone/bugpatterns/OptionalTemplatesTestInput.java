package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;

final class OptionalTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  ImmutableSet<Optional<String>> testOptionalOfNullable() {
    return ImmutableSet.of(
        toString() == null ? Optional.empty() : Optional.of(toString()),
        toString() != null ? Optional.of(toString()) : Optional.empty());
  }

  ImmutableSet<Boolean> testOptionalIsEmpty() {
    return ImmutableSet.of(!Optional.empty().isPresent(), !Optional.of("foo").isPresent());
  }

  ImmutableSet<Boolean> testOptionalIsPresent() {
    return ImmutableSet.of(!Optional.empty().isEmpty(), !Optional.of("foo").isEmpty());
  }

  String testOptionalOrElseThrow() {
    return Optional.of("foo").get();
  }

  Function<Optional<Integer>, Integer> testOptionalOrElseThrowMethodReference() {
    return Optional::get;
  }

  ImmutableSet<Optional<String>> testOptionalFirstIteratorElement() {
    return ImmutableSet.of(
        ImmutableSet.of("foo").iterator().hasNext()
            ? Optional.of(ImmutableSet.of("foo").iterator().next())
            : Optional.empty(),
        !ImmutableSet.of("foo").iterator().hasNext()
            ? Optional.empty()
            : Optional.of(ImmutableSet.of("foo").iterator().next()));
  }

  ImmutableSet<Optional<String>> testTernaryOperatorOptionalPositiveFiltering() {
    return ImmutableSet.of(
        "foo".length() > 5 ? Optional.of("foo") : Optional.empty(),
        !"bar".contains("baz") ? Optional.of("bar") : Optional.empty());
  }

  ImmutableSet<Optional<String>> testOptionalOfNullableFilterPositive(
          @Nullable String nullableString) {
    return ImmutableSet.of(
            "foo".length() > 5 ? Optional.of(nullableString) : Optional.empty(),
            !"bar".contains("baz") ? Optional.of(nullableString) : Optional.empty());
  }

  ImmutableSet<Optional<String>> testTernaryOperatorOptionalNegativeFiltering() {
    return ImmutableSet.of(
        "foo".length() > 5 ? Optional.empty() : Optional.of("foo"),
        !"bar".contains("baz") ? Optional.empty() : Optional.of("bar"));
  }

  ImmutableSet<Optional<String>> testOptionalOfNullableFilterNegative(
      @Nullable String nullableString) {
    return ImmutableSet.of(
        "foo".length() > 5 ? Optional.empty() : Optional.of(nullableString),
        !"bar".contains("baz") ? Optional.empty() : Optional.of(nullableString));
  }

  ImmutableSet<Boolean> testMapOptionalToBoolean() {
    return ImmutableSet.of(
        Optional.of("foo").map(String::isEmpty).orElse(false),
        Optional.of("bar").map(s -> s.isEmpty()).orElse(Boolean.FALSE));
  }

  ImmutableSet<Optional<String>> testMapToNullable() {
    return ImmutableSet.of(
        Optional.of(1).flatMap(n -> Optional.of(String.valueOf(n))),
        Optional.of(2).flatMap(n -> Optional.ofNullable(String.valueOf(n))));
  }

  Optional<String> testFlatMapToOptional() {
    return Optional.of(1).map(n -> Optional.of(String.valueOf(n)).orElseThrow());
  }

  String testOrOrElseThrow() {
    return Optional.of("foo").orElseGet(() -> Optional.of("bar").orElseThrow());
  }

  ImmutableSet<Object> testStreamFlatMapOptional() {
    return ImmutableSet.of(
        Stream.of(Optional.empty()).filter(Optional::isPresent).map(Optional::orElseThrow),
        Stream.of(Optional.of("foo")).flatMap(Streams::stream));
  }

  Stream<String> testStreamMapToOptionalGet() {
    return Stream.of(1).map(n -> Optional.of(String.valueOf(n)).orElseThrow());
  }

  Optional<Integer> testFilterOuterOptionalAfterFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length()).filter(len -> len > 0));
  }

  Optional<Integer> testMapOuterOptionalAfterFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length()).map(len -> len * 0));
  }

  Optional<Integer> testFlatMapOuterOptionalAfterFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length()).flatMap(Optional::of));
  }

  ImmutableSet<Optional<String>> testOptionalOrOtherOptional() {
    return ImmutableSet.of(
        Optional.of("foo").map(Optional::of).orElse(Optional.of("bar")),
        Optional.of("baz").map(Optional::of).orElseGet(() -> Optional.of("qux")),
        Stream.of(Optional.of("quux"), Optional.of("quuz")).flatMap(Optional::stream).findFirst());
  }
}
