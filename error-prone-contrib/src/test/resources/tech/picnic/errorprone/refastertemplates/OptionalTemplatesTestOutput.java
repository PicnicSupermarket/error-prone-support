package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.Streams.stream;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.FilterOuterOptionalAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.FlatMapOuterOptionalAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.FlatMapToOptional;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.MapOptionalToBoolean;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.MapOuterOptionalAfterFlatMap;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.MapToNullable;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.OptionalFirstIteratorElement;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.OptionalIsEmpty;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.OptionalIsPresent;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.OptionalOfNullable;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.OptionalOrElseThrow;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.OptionalOrElseThrowMethodReference;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.OptionalOrOtherOptional;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.OrOrElseThrow;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.StreamFlatMapOptional;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.StreamMapToOptionalGet;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.TernaryOperatorOptionalNegativeFiltering;
import tech.picnic.errorprone.refastertemplates.OptionalTemplates.TernaryOperatorOptionalPositiveFiltering;

@TemplateCollection(OptionalTemplates.class)
final class OptionalTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Streams.class);
  }

  @Template(OptionalOfNullable.class)
  ImmutableSet<Optional<String>> testOptionalOfNullable() {
    return ImmutableSet.of(Optional.ofNullable(toString()), Optional.ofNullable(toString()));
  }

  @Template(OptionalIsEmpty.class)
  ImmutableSet<Boolean> testOptionalIsEmpty() {
    return ImmutableSet.of(Optional.empty().isEmpty(), Optional.of("foo").isEmpty());
  }

  @Template(OptionalIsPresent.class)
  ImmutableSet<Boolean> testOptionalIsPresent() {
    return ImmutableSet.of(Optional.empty().isPresent(), Optional.of("foo").isPresent());
  }

  @Template(OptionalOrElseThrow.class)
  String testOptionalOrElseThrow() {
    return Optional.of("foo").orElseThrow();
  }

  @Template(OptionalOrElseThrowMethodReference.class)
  Function<Optional<Integer>, Integer> testOptionalOrElseThrowMethodReference() {
    return Optional::orElseThrow;
  }

  @Template(OptionalFirstIteratorElement.class)
  ImmutableSet<Optional<String>> testOptionalFirstIteratorElement() {
    return ImmutableSet.of(
        stream(ImmutableSet.of("foo").iterator()).findFirst(),
        stream(ImmutableSet.of("foo").iterator()).findFirst());
  }

  @Template(TernaryOperatorOptionalPositiveFiltering.class)
  ImmutableSet<Optional<String>> testTernaryOperatorOptionalPositiveFiltering() {
    return ImmutableSet.of(
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("foo")
            .filter(v -> v.length() > 5),
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("bar")
            .filter(v -> !v.contains("baz")));
  }

  @Template(TernaryOperatorOptionalNegativeFiltering.class)
  ImmutableSet<Optional<String>> testTernaryOperatorOptionalNegativeFiltering() {
    return ImmutableSet.of(
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("foo")
            .filter(v -> v.length() <= 5),
        /* Or Optional.ofNullable (can't auto-infer). */ Optional.of("bar")
            .filter(v -> v.contains("baz")));
  }

  @Template(MapOptionalToBoolean.class)
  ImmutableSet<Boolean> testMapOptionalToBoolean() {
    return ImmutableSet.of(
        Optional.of("foo").filter(String::isEmpty).isPresent(),
        Optional.of("bar").filter(s -> s.isEmpty()).isPresent());
  }

  @Template(MapToNullable.class)
  ImmutableSet<Optional<String>> testMapToNullable() {
    return ImmutableSet.of(
        Optional.of(1).map(n -> String.valueOf(n)), Optional.of(2).map(n -> String.valueOf(n)));
  }

  @Template(FlatMapToOptional.class)
  Optional<String> testFlatMapToOptional() {
    return Optional.of(1).flatMap(n -> Optional.of(String.valueOf(n)));
  }

  @Template(OrOrElseThrow.class)
  String testOrOrElseThrow() {
    return Optional.of("foo").or(() -> Optional.of("bar")).orElseThrow();
  }

  @Template(StreamFlatMapOptional.class)
  ImmutableSet<Object> testStreamFlatMapOptional() {
    return ImmutableSet.of(
        Stream.of(Optional.empty()).flatMap(Optional::stream),
        Stream.of(Optional.of("foo")).flatMap(Optional::stream));
  }

  @Template(StreamMapToOptionalGet.class)
  Stream<String> testStreamMapToOptionalGet() {
    return Stream.of(1).flatMap(n -> Optional.of(String.valueOf(n)).stream());
  }

  @Template(FilterOuterOptionalAfterFlatMap.class)
  Optional<Integer> testFilterOuterOptionalAfterFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length())).filter(len -> len > 0);
  }

  @Template(MapOuterOptionalAfterFlatMap.class)
  Optional<Integer> testMapOuterOptionalAfterFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length())).map(len -> len * 0);
  }

  @Template(FlatMapOuterOptionalAfterFlatMap.class)
  Optional<Integer> testFlatMapOuterOptionalAfterFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length())).flatMap(Optional::of);
  }

  @Template(OptionalOrOtherOptional.class)
  ImmutableSet<Optional<String>> testOptionalOrOtherOptional() {
    return ImmutableSet.of(
        Optional.of("foo").or(() -> Optional.of("bar")),
        Optional.of("baz").or(() -> Optional.of("qux")),
        Optional.of("quux").or(() -> Optional.of("quuz")));
  }
}
