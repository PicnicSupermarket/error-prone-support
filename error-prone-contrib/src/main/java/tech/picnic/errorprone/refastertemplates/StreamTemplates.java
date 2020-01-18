package tech.picnic.errorprone.refastertemplates;

import static java.util.function.Predicate.not;

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

  /** Prefer {@link Stream#empty()} over less clear alternatives. */
  static final class EmptyStream<T> {
    @BeforeTemplate
    Stream<T> before() {
      return Stream.of();
    }

    @AfterTemplate
    Stream<T> after() {
      return Stream.empty();
    }
  }

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

  /**
   * Where possible, clarify that a mapping operation will be applied only to a single stream
   * element.
   */
  // XXX: Consider whether to have a similar rule for `.findAny()`. For parallel streams it
  // wouldn't be quite the same....
  static final class StreamMapFirst<T, S> {
    @BeforeTemplate
    Optional<S> before(Stream<T> stream, Function<? super T, S> function) {
      return stream.map(function).findFirst();
    }

    @AfterTemplate
    Optional<S> after(Stream<T> stream, Function<? super T, S> function) {
      return stream.findFirst().map(function);
    }
  }

  /** In order to test whether a stream has any element, simply try to find one. */
  static final class StreamIsEmpty<T> {
    @BeforeTemplate
    boolean before(Stream<T> stream) {
      return Refaster.anyOf(
          stream.count() == 0,
          stream.count() <= 0,
          stream.count() < 1,
          stream.findFirst().isEmpty());
    }

    @AfterTemplate
    boolean after(Stream<T> stream) {
      return stream.findAny().isEmpty();
    }
  }

  /** In order to test whether a stream has any element, simply try to find one. */
  static final class StreamIsNotEmpty<T> {
    @BeforeTemplate
    boolean before(Stream<T> stream) {
      return Refaster.anyOf(
          stream.count() != 0,
          stream.count() > 0,
          stream.count() >= 1,
          stream.findFirst().isPresent());
    }

    @AfterTemplate
    boolean after(Stream<T> stream) {
      return stream.findAny().isPresent();
    }
  }

  /** Prefer {@link Stream#noneMatch(Predicate)} over more contrived alternatives. */
  static final class StreamNoneMatch<T> {
    @BeforeTemplate
    boolean before(Stream<T> stream, Predicate<? super T> predicate) {
      return Refaster.anyOf(
          !stream.anyMatch(predicate),
          stream.allMatch(Refaster.anyOf(not(predicate), predicate.negate())),
          stream.filter(predicate).findAny().isEmpty());
    }

    @AfterTemplate
    boolean after(Stream<T> stream, Predicate<? super T> predicate) {
      return stream.noneMatch(predicate);
    }
  }

  abstract static class StreamNoneMatch2<T> {
    @Placeholder(allowsIdentity = true)
    abstract boolean test(@MayOptionallyUse T element);

    @BeforeTemplate
    boolean before(Stream<T> stream) {
      return stream.allMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(Stream<T> stream) {
      return stream.noneMatch(e -> test(e));
    }
  }

  /** Prefer {@link Stream#anyMatch(Predicate)} over more contrived alternatives. */
  static final class StreamAnyMatch<T> {
    @BeforeTemplate
    boolean before(Stream<T> stream, Predicate<? super T> predicate) {
      return Refaster.anyOf(
          !stream.noneMatch(predicate), stream.filter(predicate).findAny().isPresent());
    }

    @AfterTemplate
    boolean after(Stream<T> stream, Predicate<? super T> predicate) {
      return stream.anyMatch(predicate);
    }
  }

  static final class StreamAllMatch<T> {
    @BeforeTemplate
    boolean before(Stream<T> stream, Predicate<? super T> predicate) {
      return Refaster.anyOf(
          stream.noneMatch(Refaster.anyOf(not(predicate), predicate.negate())),
          !stream.anyMatch(Refaster.anyOf(not(predicate), predicate.negate())),
          stream.filter(Refaster.anyOf(not(predicate), predicate.negate())).findAny().isEmpty());
    }

    @AfterTemplate
    boolean after(Stream<T> stream, Predicate<? super T> predicate) {
      return stream.allMatch(predicate);
    }
  }

  abstract static class StreamAllMatch2<T> {
    @Placeholder(allowsIdentity = true)
    abstract boolean test(@MayOptionallyUse T element);

    @BeforeTemplate
    boolean before(Stream<T> stream) {
      return Refaster.anyOf(
          stream.noneMatch(e -> !test(e)),
          !stream.anyMatch(e -> !test(e)),
          stream.filter(e -> !test(e)).findAny().isEmpty());
    }

    @AfterTemplate
    boolean after(Stream<T> stream) {
      return stream.allMatch(e -> test(e));
    }
  }
}
