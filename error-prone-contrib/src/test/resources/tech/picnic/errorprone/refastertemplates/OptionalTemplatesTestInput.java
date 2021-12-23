package tech.picnic.errorprone.refastertemplates;

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
    return ImmutableSet.of(
        toString() == null ? Optional.empty() : Optional.of(toString()),
        toString() != null ? Optional.of(toString()) : Optional.empty());
  }

  @Template(OptionalIsEmpty.class)
  ImmutableSet<Boolean> testOptionalIsEmpty() {
    return ImmutableSet.of(!Optional.empty().isPresent(), !Optional.of("foo").isPresent());
  }

  @Template(OptionalIsPresent.class)
  ImmutableSet<Boolean> testOptionalIsPresent() {
    return ImmutableSet.of(!Optional.empty().isEmpty(), !Optional.of("foo").isEmpty());
  }

  @Template(OptionalOrElseThrow.class)
  String testOptionalOrElseThrow() {
    return Optional.of("foo").get();
  }

  @Template(OptionalOrElseThrowMethodReference.class)
  Function<Optional<Integer>, Integer> testOptionalOrElseThrowMethodReference() {
    return Optional::get;
  }

  @Template(OptionalFirstIteratorElement.class)
  ImmutableSet<Optional<String>> testOptionalFirstIteratorElement() {
    return ImmutableSet.of(
        ImmutableSet.of("foo").iterator().hasNext()
            ? Optional.of(ImmutableSet.of("foo").iterator().next())
            : Optional.empty(),
        !ImmutableSet.of("foo").iterator().hasNext()
            ? Optional.empty()
            : Optional.of(ImmutableSet.of("foo").iterator().next()));
  }

  @Template(TernaryOperatorOptionalPositiveFiltering.class)
  ImmutableSet<Optional<String>> testTernaryOperatorOptionalPositiveFiltering() {
    return ImmutableSet.of(
        "foo".length() > 5 ? Optional.of("foo") : Optional.empty(),
        !"bar".contains("baz") ? Optional.of("bar") : Optional.empty());
  }

  @Template(TernaryOperatorOptionalNegativeFiltering.class)
  ImmutableSet<Optional<String>> testTernaryOperatorOptionalNegativeFiltering() {
    return ImmutableSet.of(
        "foo".length() > 5 ? Optional.empty() : Optional.of("foo"),
        !"bar".contains("baz") ? Optional.empty() : Optional.of("bar"));
  }

  @Template(MapOptionalToBoolean.class)
  ImmutableSet<Boolean> testMapOptionalToBoolean() {
    return ImmutableSet.of(
        Optional.of("foo").map(String::isEmpty).orElse(false),
        Optional.of("bar").map(s -> s.isEmpty()).orElse(Boolean.FALSE));
  }

  @Template(MapToNullable.class)
  ImmutableSet<Optional<String>> testMapToNullable() {
    return ImmutableSet.of(
        Optional.of(1).flatMap(n -> Optional.of(String.valueOf(n))),
        Optional.of(2).flatMap(n -> Optional.ofNullable(String.valueOf(n))));
  }

  @Template(FlatMapToOptional.class)
  Optional<String> testFlatMapToOptional() {
    return Optional.of(1).map(n -> Optional.of(String.valueOf(n)).orElseThrow());
  }

  @Template(OrOrElseThrow.class)
  String testOrOrElseThrow() {
    return Optional.of("foo").orElseGet(() -> Optional.of("bar").orElseThrow());
  }

  @Template(StreamFlatMapOptional.class)
  ImmutableSet<Object> testStreamFlatMapOptional() {
    return ImmutableSet.of(
        Stream.of(Optional.empty()).filter(Optional::isPresent).map(Optional::orElseThrow),
        Stream.of(Optional.of("foo")).flatMap(Streams::stream));
  }

  @Template(StreamMapToOptionalGet.class)
  Stream<String> testStreamMapToOptionalGet() {
    return Stream.of(1).map(n -> Optional.of(String.valueOf(n)).orElseThrow());
  }

  @Template(FilterOuterOptionalAfterFlatMap.class)
  Optional<Integer> testFilterOuterOptionalAfterFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length()).filter(len -> len > 0));
  }

  @Template(MapOuterOptionalAfterFlatMap.class)
  Optional<Integer> testMapOuterOptionalAfterFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length()).map(len -> len * 0));
  }

  @Template(FlatMapOuterOptionalAfterFlatMap.class)
  Optional<Integer> testFlatMapOuterOptionalAfterFlatMap() {
    return Optional.of("foo").flatMap(v -> Optional.of(v.length()).flatMap(Optional::of));
  }

  @Template(OptionalOrOtherOptional.class)
  ImmutableSet<Optional<String>> testOptionalOrOtherOptional() {
    return ImmutableSet.of(
        Optional.of("foo").map(Optional::of).orElse(Optional.of("bar")),
        Optional.of("baz").map(Optional::of).orElseGet(() -> Optional.of("qux")),
        Stream.of(Optional.of("quux"), Optional.of("quuz")).flatMap(Optional::stream).findFirst());
  }
}
