package tech.picnic.errorprone.refasterrules;

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
    return Optional.ofNullable(null);
  }

  ImmutableSet<Optional<String>> testOptionalOfNullable() {
    return ImmutableSet.of(
        toString() == null ? Optional.empty() : Optional.of(toString()),
        toString() != null ? Optional.of(toString()) : Optional.empty());
  }

  boolean testOptionalIsEmpty() {
    return !Optional.of("foo").isPresent();
  }

  boolean testOptionalIsPresent() {
    return !Optional.of("foo").isEmpty();
  }

  String testOptionalOrElseThrowWithOptional() {
    return Optional.of("foo").get();
  }

  Function<Optional<Integer>, Integer> testOptionalOrElseThrow() {
    return Optional::get;
  }

  ImmutableSet<Boolean> testOptionalEqualsOptionalOf() {
    return ImmutableSet.of(
        Optional.of("foo").filter("bar"::equals).isPresent(),
        Optional.of("baz").stream().anyMatch("qux"::equals));
  }

  ImmutableSet<Optional<String>> testStreamsStreamFindFirst() {
    return ImmutableSet.of(
        ImmutableSet.of("foo").iterator().hasNext()
            ? Optional.of(ImmutableSet.of("foo").iterator().next())
            : Optional.empty(),
        !ImmutableSet.of("foo").iterator().hasNext()
            ? Optional.empty()
            : Optional.of(ImmutableSet.of("foo").iterator().next()));
  }

  ImmutableSet<Optional<String>> testRefasterEmitCommentBeforeOptionalOfFilter() {
    return ImmutableSet.of(
        "foo".length() > 5 ? Optional.of("foo") : Optional.empty(),
        !("bar".length() > 5) ? Optional.empty() : Optional.of("bar"));
  }

  ImmutableSet<Optional<String>> testRefasterEmitCommentBeforeOptionalOfFilterNot() {
    return ImmutableSet.of(
        "foo".length() > 5 ? Optional.empty() : Optional.of("foo"),
        !("bar".length() > 5) ? Optional.of("bar") : Optional.empty());
  }

  boolean testOptionalFilterIsPresent() {
    return Optional.of("foo").map(String::isEmpty).orElse(false);
  }

  ImmutableSet<Optional<String>> testOptionalMap() {
    return ImmutableSet.of(
        Optional.of(1).flatMap(n -> Optional.of(String.valueOf(n))),
        Optional.of(2).flatMap(n -> Optional.ofNullable(String.valueOf(n))));
  }

  Optional<String> testOptionalFlatMap() {
    return Optional.of(1).map(n -> Optional.of(String.valueOf(n)).orElseThrow());
  }

  String testOptionalOrOrElseThrow() {
    return Optional.of("foo").orElseGet(() -> Optional.of("bar").orElseThrow());
  }

  ImmutableSet<String> testOptionalOrElse() {
    return ImmutableSet.of(
        Optional.of("foo").orElseGet(() -> toString()), Optional.of("bar").orElseGet(() -> "baz"));
  }

  ImmutableSet<Object> testStreamFlatMapOptionalStream() {
    return ImmutableSet.of(
        Stream.of(Optional.empty()).filter(Optional::isPresent).map(Optional::orElseThrow),
        Stream.of(Optional.of("foo")).flatMap(Streams::stream));
  }

  Stream<String> testStreamFlatMapStream() {
    return Stream.of(1).map(n -> Optional.of(String.valueOf(n)).orElseThrow());
  }

  Optional<Integer> testOptionalFlatMapFilter() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length()).filter(len -> len > 0));
  }

  Optional<Integer> testOptionalFlatMapMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length()).map(len -> len * 0));
  }

  Optional<Integer> testOptionalFlatMapFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length()).flatMap(Optional::of));
  }

  ImmutableSet<Optional<String>> testOptionalOr() {
    return ImmutableSet.of(
        Optional.of("foo").map(Optional::of).orElse(Optional.of("bar")),
        Optional.of("baz").map(Optional::of).orElseGet(() -> Optional.of("qux")),
        Stream.of(Optional.of("quux"), Optional.of("corge")).flatMap(Optional::stream).findFirst(),
        Optional.of("grault").isPresent() ? Optional.of("grault") : Optional.of("garply"));
  }

  ImmutableSet<Optional<String>> testOptionalIdentity() {
    return ImmutableSet.of(
        Optional.of("foo").or(() -> Optional.empty()),
        Optional.of("bar").or(Optional::empty),
        Optional.of("baz").map(Optional::of).orElseGet(() -> Optional.empty()),
        Optional.of("qux").map(Optional::of).orElseGet(Optional::empty),
        Optional.of("quux").stream().findFirst(),
        Optional.of("corge").stream().findAny(),
        Optional.of("grault").stream().min(String::compareTo),
        Optional.of("garply").stream().max(String::compareTo));
  }

  ImmutableSet<Optional<String>> testOptionalFilter() {
    return ImmutableSet.of(
        Optional.of("foo").stream().filter(String::isEmpty).findFirst(),
        Optional.of("bar").stream().filter(String::isEmpty).findAny());
  }

  Optional<String> testOptionalMapWithFunction() {
    return Optional.of(1).stream().map(String::valueOf).findAny();
  }

  Stream<String> testOptionalStream() {
    return Optional.of("foo").map(Stream::of).orElseGet(Stream::empty);
  }
}
