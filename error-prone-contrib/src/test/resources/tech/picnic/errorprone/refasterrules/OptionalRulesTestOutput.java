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

  String testOptionalOrElseThrowWithOptional() {
    return Optional.of("foo").orElseThrow();
  }

  Function<Optional<Integer>, Integer> testOptionalOrElseThrow() {
    return Optional::orElseThrow;
  }

  ImmutableSet<Boolean> testOptionalEqualsOptionalOf() {
    return ImmutableSet.of(
        Optional.of("foo").equals(Optional.of("bar")),
        Optional.of("baz").equals(Optional.of("qux")));
  }

  ImmutableSet<Optional<String>> testStreamsStreamFindFirst() {
    return ImmutableSet.of(
        stream(ImmutableSet.of("foo").iterator()).findFirst(),
        stream(ImmutableSet.of("foo").iterator()).findFirst());
  }

  ImmutableSet<Optional<String>> testRefasterEmitCommentBeforeOptionalOfFilter() {
    return ImmutableSet.of(
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("foo")
            .filter(v -> v.length() > 5),
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("bar")
            .filter(v -> v.length() > 5));
  }

  ImmutableSet<Optional<String>> testRefasterEmitCommentBeforeOptionalOfFilterNot() {
    return ImmutableSet.of(
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("foo")
            .filter(v -> v.length() <= 5),
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("bar")
            .filter(v -> v.length() <= 5));
  }

  boolean testOptionalFilterIsPresent() {
    return Optional.of("foo").filter(String::isEmpty).isPresent();
  }

  ImmutableSet<Optional<String>> testOptionalMap() {
    return ImmutableSet.of(
        Optional.of(1).map(n -> String.valueOf(n)), Optional.of(2).map(n -> String.valueOf(n)));
  }

  Optional<String> testOptionalFlatMap() {
    return Optional.of(1).flatMap(n -> Optional.of(String.valueOf(n)));
  }

  String testOptionalOrOrElseThrow() {
    return Optional.of("foo").or(() -> Optional.of("bar")).orElseThrow();
  }

  ImmutableSet<String> testOptionalOrElse() {
    return ImmutableSet.of(
        Optional.of("foo").orElseGet(() -> toString()), Optional.of("bar").orElse("baz"));
  }

  ImmutableSet<Object> testStreamFlatMapOptionalStream() {
    return ImmutableSet.of(
        Stream.of(Optional.empty()).flatMap(Optional::stream),
        Stream.of(Optional.of("foo")).flatMap(Optional::stream));
  }

  Stream<String> testStreamFlatMapStream() {
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

  ImmutableSet<Optional<String>> testOptionalOr() {
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

  Optional<String> testOptionalMapWithFunction() {
    return Optional.of(1).map(String::valueOf);
  }

  Stream<String> testOptionalStream() {
    return Optional.of("foo").stream();
  }
}
