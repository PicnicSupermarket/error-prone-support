package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import java.util.Optional;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link Optional}s. */
final class OptionalTemplates {
  private OptionalTemplates() {}

  /** Prefer {@link Optional#isEmpty()} over the more verbose alternative. */
  static final class OptionalIsEmpty<T> {
    @BeforeTemplate
    boolean before(Optional<T> optional) {
      return !optional.isPresent();
    }

    @AfterTemplate
    boolean after(Optional<T> optional) {
      return optional.isEmpty();
    }
  }

  /** Prefer {@link Optional#isPresent()} over the inverted alternative. */
  static final class OptionalIsPresent<T> {
    @BeforeTemplate
    boolean before(Optional<T> optional) {
      return !optional.isEmpty();
    }

    @AfterTemplate
    boolean after(Optional<T> optional) {
      return optional.isPresent();
    }
  }

  /** Prefer {@link Optional#stream()} over the Guava alternative. */
  static final class OptionalToStream<T> {
    @BeforeTemplate
    Stream<T> before(Optional<T> optional) {
      return Streams.stream(optional);
    }

    @AfterTemplate
    Stream<T> after(Optional<T> optional) {
      return optional.stream();
    }
  }

  /**
   * Flatten a stream of {@link Optional}s using {@link Optional#stream()}, rather than using one of
   * the more verbose alternatives.
   */
  static final class FlatmapOptionalToStream<T> {
    @BeforeTemplate
    Stream<T> before(Stream<Optional<T>> stream) {
      return Refaster.anyOf(
          stream.filter(Optional::isPresent).map(Optional::get), stream.flatMap(Streams::stream));
    }

    @AfterTemplate
    Stream<T> after(Stream<Optional<T>> stream) {
      return stream.flatMap(Optional::stream);
    }
  }

  /** Within a stream's map operation unconditional {@link Optional#get()} calls can be avoided. */
  // XXX: An alternative approach is to `.flatMap(Optional::stream)`. That may be a bit longer, but
  // yield nicer code. Think about it.
  abstract static class MapToOptionalGet<T, S> {
    @Placeholder
    abstract Optional<S> toOptionalFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Stream<S> before(Stream<T> stream, Optional<S> optional) {
      return stream.map(e -> toOptionalFunction(e).get());
    }

    @AfterTemplate
    Stream<S> after(Stream<T> stream, Optional<S> optional) {
      return stream.flatMap(e -> toOptionalFunction(e).stream());
    }
  }
}
