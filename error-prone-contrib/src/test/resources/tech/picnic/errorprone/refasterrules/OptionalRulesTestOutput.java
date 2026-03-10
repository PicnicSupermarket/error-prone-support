package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.Streams.stream;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class OptionalRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  Optional<String> testOptionalEmpty() {
    return Optional.empty();
  }

  ImmutableSet<Optional<String>> testOptionalOfNullable() {
    return ImmutableSet.of(Optional.ofNullable(toString()), Optional.ofNullable(toString()));
  }

  boolean testOptionalIsEmpty() {
    return Optional.of("foo").isEmpty();
  }

  boolean testOptionalIsPresent() {
    return Optional.of("foo").isPresent();
  }

  String testOptionalOrElseThrow() {
    return Optional.of("foo").orElseThrow();
  }

  Function<Optional<Integer>, Integer> testOptionalOrElseThrowMethodReference() {
    return Optional::orElseThrow;
  }

  ImmutableSet<Boolean> testOptionalEqualsOptional() {
    return ImmutableSet.of(
        Optional.of("foo").equals(Optional.of("bar")),
        Optional.of("baz").equals(Optional.of("qux")));
  }

  ImmutableSet<Optional<String>> testOptionalFirstIteratorElement() {
    return ImmutableSet.of(
        stream(ImmutableSet.of("foo").iterator()).findFirst(),
        stream(ImmutableSet.of("foo").iterator()).findFirst());
  }

  ImmutableSet<Optional<String>> testOptionalOfFilter() {
    return ImmutableSet.of(
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("foo")
            .filter(v -> v.length() > 5),
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("bar")
            .filter(v -> v.length() > 5));
  }

  ImmutableSet<Optional<String>> testOptionalOfFilterNegated() {
    return ImmutableSet.of(
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("foo")
            .filter(v -> v.length() <= 5),
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("bar")
            .filter(v -> v.length() <= 5));
  }

  boolean testMapOptionalToBoolean() {
    return Optional.of("foo").filter(String::isEmpty).isPresent();
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

  ImmutableSet<String> testOptionalOrElse() {
    return ImmutableSet.of(
        Optional.of("foo").orElseGet(() -> toString()), Optional.of("bar").orElse("baz"));
  }

  ImmutableSet<Object> testStreamFlatMapOptional() {
    return ImmutableSet.of(
        Stream.of(Optional.empty()).flatMap(Optional::stream),
        Stream.of(Optional.of("foo")).flatMap(Optional::stream));
  }

  Stream<String> testStreamMapToOptionalGet() {
    return Stream.of(1).flatMap(n -> Optional.of(String.valueOf(n)).stream());
  }

  Optional<Integer> testOptionalFlatMapFilter() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length())).filter(len -> len > 0);
  }

  Optional<Integer> testOptionalFlatMapMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length())).map(len -> len * 0);
  }

  Optional<Integer> testOptionalFlatMapFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length())).flatMap(Optional::of);
  }

  ImmutableSet<Optional<String>> testOptionalOrOtherOptional() {
    return ImmutableSet.of(
        Optional.of("foo").or(() -> Optional.of("bar")),
        Optional.of("baz").or(() -> Optional.of("qux")),
        Optional.of("quux").or(() -> Optional.of("corge")),
        Optional.of("grault").or(() -> Optional.of("garply")));
  }

  ImmutableSet<Optional<String>> testOptionalIdentity() {
    return ImmutableSet.of(
        Optional.of("foo"),
        Optional.of("bar"),
        Optional.of("baz"),
        Optional.of("qux"),
        Optional.of("quux"),
        Optional.of("corge"),
        Optional.of("grault"),
        Optional.of("garply"));
  }

  ImmutableSet<Optional<String>> testOptionalFilter() {
    return ImmutableSet.of(
        Optional.of("foo").filter(String::isEmpty), Optional.of("bar").filter(String::isEmpty));
  }

  Optional<String> testOptionalMap() {
    return Optional.of(1).map(String::valueOf);
  }

  Stream<String> testOptionalStream() {
    return Optional.of("foo").stream();
  }
}
