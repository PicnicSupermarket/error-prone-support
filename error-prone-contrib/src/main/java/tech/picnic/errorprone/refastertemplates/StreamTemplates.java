package tech.picnic.errorprone.refastertemplates;


import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link Stream}s. */
final class StreamTemplates {
  private StreamTemplates() {}

  /** Prefer {@link Stream#ofNullable(Object)} over more contrived alternatives. */
  static final class StreamOfNullable<T> {
    @BeforeTemplate
    Stream<T> before(T object) {
      return Refaster.anyOf(
          Stream.of(object).filter(Objects::nonNull), Optional.ofNullable(object).stream());
    }

    @AfterTemplate
    Stream<T> after(T object) {
      return Stream.ofNullable(object);
    }
  }

  /** Don't unnecessarily call {@link Streams#concat(Stream...)}. */
  // XXX: There are int, long and double variants to this rule. Probably not worth the hassle.
  static final class ConcatOneStream<T> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream) {
      return Streams.concat(stream);
    }

    @AfterTemplate
    Stream<T> after(Stream<T> stream) {
      return stream;
    }
  }

  /** Prefer {@link Stream#concat(Stream, Stream)} over the Guava alternative. */
  // XXX: There are int, long and double variants to this rule. Worth the hassle?
  static final class ConcatTwoStreams<T> {
    @BeforeTemplate
    Stream<T> before(Stream<T> s1, Stream<T> s2) {
      return Streams.concat(s1, s2);
    }

    @AfterTemplate
    Stream<T> after(Stream<T> s1, Stream<T> s2) {
      return Stream.concat(s1, s2);
    }
  }

  /** Avoid unnecessary nesting of {@link Stream#filter(Predicate)} operations. */
  // XXX: There are int, long and double variants to this rule. Worth the hassle?
  abstract static class FilterOuterStreamAfterFlatMap<T, S> {
    @Placeholder
    abstract Stream<S> toStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Stream<S> before(Stream<T> stream, Predicate<? super S> predicate) {
      return stream.flatMap(v -> toStreamFunction(v).filter(predicate));
    }

    @AfterTemplate
    Stream<S> after(Stream<T> stream, Predicate<? super S> predicate) {
      return stream.flatMap(v -> toStreamFunction(v)).filter(predicate);
    }
  }

  /** Avoid unnecessary nesting of {@link Stream#map(Function)} operations. */
  // XXX: There are int, long and double variants to this rule. Worth the hassle?
  abstract static class MapOuterStreamAfterFlatMap<T, S, R> {
    @Placeholder
    abstract Stream<S> toStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Stream<R> before(Stream<T> stream, Function<? super S, ? extends R> function) {
      return stream.flatMap(v -> toStreamFunction(v).map(function));
    }

    @AfterTemplate
    Stream<R> after(Stream<T> stream, Function<? super S, ? extends R> function) {
      return stream.flatMap(v -> toStreamFunction(v)).map(function);
    }
  }

  /** Avoid unnecessary nesting of {@link Stream#flatMap(Function)} operations. */
  // XXX: There are int, long and double variants to this rule. Worth the hassle?
  abstract static class FlatMapOuterStreamAfterFlatMap<T, S, R> {
    @Placeholder
    abstract Stream<S> toStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Stream<R> before(
        Stream<T> stream, Function<? super S, ? extends Stream<? extends R>> function) {
      return stream.flatMap(v -> toStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    Stream<R> after(Stream<T> stream, Function<? super S, ? extends Stream<? extends R>> function) {
      return stream.flatMap(v -> toStreamFunction(v)).flatMap(function);
    }
  }
}
