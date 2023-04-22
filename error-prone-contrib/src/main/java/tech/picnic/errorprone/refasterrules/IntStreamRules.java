package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import java.util.OptionalInt;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link IntStream}s. */
@OnlineDocumentation
final class IntStreamRules {
  private IntStreamRules() {}

  /** Prefer {@link IntStream#range(int, int)} over the more contrived alternative. */
  static final class IntStreamClosedOpenRange {
    @BeforeTemplate
    IntStream before(int from, int to) {
      return IntStream.rangeClosed(from, to - 1);
    }

    @AfterTemplate
    IntStream after(int from, int to) {
      return IntStream.range(from, to);
    }
  }

  /** Don't unnecessarily call {@link Streams#concat(IntStream...)}. */
  static final class ConcatOneIntStream {
    @BeforeTemplate
    IntStream before(IntStream stream) {
      return Streams.concat(stream);
    }

    @AfterTemplate
    IntStream after(IntStream stream) {
      return stream;
    }
  }

  /** Prefer {@link IntStream#concat(IntStream, IntStream)} over the Guava alternative. */
  static final class ConcatTwoIntStreams {
    @BeforeTemplate
    IntStream before(IntStream s1, IntStream s2) {
      return Streams.concat(s1, s2);
    }

    @AfterTemplate
    IntStream after(IntStream s1, IntStream s2) {
      return IntStream.concat(s1, s2);
    }
  }

  /** Avoid unnecessary nesting of {@link IntStream#filter(IntPredicate)} operations. */
  abstract static class FilterOuterIntStreamAfterFlatMap {
    @Placeholder
    abstract IntStream toIntStreamFunction(@MayOptionallyUse int element);

    @BeforeTemplate
    IntStream before(IntStream stream, IntPredicate predicate) {
      return stream.flatMap(v -> toIntStreamFunction(v).filter(predicate));
    }

    @AfterTemplate
    IntStream after(IntStream stream, IntPredicate predicate) {
      return stream.flatMap(v -> toIntStreamFunction(v)).filter(predicate);
    }
  }

  /** Avoid unnecessary nesting of {@link IntStream#filter(IntPredicate)} operations. */
  abstract static class FilterOuterStreamAfterFlatMapToInt<T> {
    @Placeholder(allowsIdentity = true)
    abstract IntStream toIntStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    IntStream before(Stream<T> stream, IntPredicate predicate) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v).filter(predicate));
    }

    @AfterTemplate
    IntStream after(Stream<T> stream, IntPredicate predicate) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v)).filter(predicate);
    }
  }

  /** Avoid unnecessary nesting of {@link IntStream#map(IntUnaryOperator)} operations. */
  abstract static class MapOuterIntStreamAfterFlatMap {
    @Placeholder
    abstract IntStream toIntStreamFunction(@MayOptionallyUse int element);

    @BeforeTemplate
    IntStream before(IntStream stream, IntUnaryOperator function) {
      return stream.flatMap(v -> toIntStreamFunction(v).map(function));
    }

    @AfterTemplate
    IntStream after(IntStream stream, IntUnaryOperator function) {
      return stream.flatMap(v -> toIntStreamFunction(v)).map(function);
    }
  }

  /** Avoid unnecessary nesting of {@link IntStream#map(IntUnaryOperator)} operations. */
  abstract static class MapOuterStreamAfterFlatMapToInt<T> {
    @Placeholder(allowsIdentity = true)
    abstract IntStream toIntStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    IntStream before(Stream<T> stream, IntUnaryOperator function) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v).map(function));
    }

    @AfterTemplate
    IntStream after(Stream<T> stream, IntUnaryOperator function) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v)).map(function);
    }
  }

  /** Avoid unnecessary nesting of {@link IntStream#flatMap(IntFunction)} operations. */
  abstract static class FlatMapOuterIntStreamAfterFlatMap {
    @Placeholder
    abstract IntStream toIntStreamFunction(@MayOptionallyUse int element);

    @BeforeTemplate
    IntStream before(IntStream stream, IntFunction<? extends IntStream> function) {
      return stream.flatMap(v -> toIntStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    IntStream after(IntStream stream, IntFunction<? extends IntStream> function) {
      return stream.flatMap(v -> toIntStreamFunction(v)).flatMap(function);
    }
  }

  /** Avoid unnecessary nesting of {@link IntStream#flatMap(IntFunction)} operations. */
  abstract static class FlatMapOuterStreamAfterFlatMapToInt<T> {
    @Placeholder(allowsIdentity = true)
    abstract IntStream toIntStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    IntStream before(Stream<T> stream, IntFunction<? extends IntStream> function) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    IntStream after(Stream<T> stream, IntFunction<? extends IntStream> function) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v)).flatMap(function);
    }
  }

  /**
   * Apply {@link IntStream#filter(IntPredicate)} before {@link IntStream#sorted()} to reduce the
   * number of elements to sort.
   */
  static final class IntStreamFilterSorted {
    @BeforeTemplate
    IntStream before(IntStream stream, IntPredicate predicate) {
      return stream.sorted().filter(predicate);
    }

    @AfterTemplate
    IntStream after(IntStream stream, IntPredicate predicate) {
      return stream.filter(predicate).sorted();
    }
  }

  /** In order to test whether a stream has any element, simply try to find one. */
  static final class IntStreamIsEmpty {
    @BeforeTemplate
    boolean before(IntStream stream) {
      return Refaster.anyOf(
          stream.count() == 0,
          stream.count() <= 0,
          stream.count() < 1,
          stream.findFirst().isEmpty());
    }

    @AfterTemplate
    boolean after(IntStream stream) {
      return stream.findAny().isEmpty();
    }
  }

  /** In order to test whether a stream has any element, simply try to find one. */
  static final class IntStreamIsNotEmpty {
    @BeforeTemplate
    boolean before(IntStream stream) {
      return Refaster.anyOf(
          stream.count() != 0,
          stream.count() > 0,
          stream.count() >= 1,
          stream.findFirst().isPresent());
    }

    @AfterTemplate
    boolean after(IntStream stream) {
      return stream.findAny().isPresent();
    }
  }

  static final class IntStreamMin {
    @BeforeTemplate
    OptionalInt before(IntStream stream) {
      return stream.sorted().findFirst();
    }

    @AfterTemplate
    OptionalInt after(IntStream stream) {
      return stream.min();
    }
  }

  /** Prefer {@link IntStream#noneMatch(IntPredicate)} over more contrived alternatives. */
  static final class IntStreamNoneMatch {
    @BeforeTemplate
    boolean before(IntStream stream, IntPredicate predicate) {
      return Refaster.anyOf(
          !stream.anyMatch(predicate),
          stream.allMatch(predicate.negate()),
          stream.filter(predicate).findAny().isEmpty());
    }

    @AfterTemplate
    boolean after(IntStream stream, IntPredicate predicate) {
      return stream.noneMatch(predicate);
    }
  }

  abstract static class IntStreamNoneMatch2 {
    @Placeholder
    abstract boolean test(@MayOptionallyUse int element);

    @BeforeTemplate
    boolean before(IntStream stream) {
      return stream.allMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(IntStream stream) {
      return stream.noneMatch(e -> test(e));
    }
  }

  /** Prefer {@link IntStream#anyMatch(IntPredicate)} over more contrived alternatives. */
  static final class IntStreamAnyMatch {
    @BeforeTemplate
    @SuppressWarnings("java:S4034" /* This violation will be rewritten. */)
    boolean before(IntStream stream, IntPredicate predicate) {
      return Refaster.anyOf(
          !stream.noneMatch(predicate), stream.filter(predicate).findAny().isPresent());
    }

    @AfterTemplate
    boolean after(IntStream stream, IntPredicate predicate) {
      return stream.anyMatch(predicate);
    }
  }

  static final class IntStreamAllMatch {
    @BeforeTemplate
    boolean before(IntStream stream, IntPredicate predicate) {
      return stream.noneMatch(predicate.negate());
    }

    @AfterTemplate
    boolean after(IntStream stream, IntPredicate predicate) {
      return stream.allMatch(predicate);
    }
  }

  abstract static class IntStreamAllMatch2 {
    @Placeholder
    abstract boolean test(@MayOptionallyUse int element);

    @BeforeTemplate
    boolean before(IntStream stream) {
      return stream.noneMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(IntStream stream) {
      return stream.allMatch(e -> test(e));
    }
  }
}
