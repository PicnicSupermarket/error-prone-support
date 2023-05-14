package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.Streams;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import java.util.OptionalLong;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link LongStream}s. */
@OnlineDocumentation
final class LongStreamRules {
  private LongStreamRules() {}

  /** Prefer {@link LongStream#range(long, long)} over the more contrived alternative. */
  static final class LongStreamClosedOpenRange {
    @BeforeTemplate
    LongStream before(long from, long to) {
      return LongStream.rangeClosed(from, to - 1);
    }

    @AfterTemplate
    LongStream after(long from, long to) {
      return LongStream.range(from, to);
    }
  }

  /** Don't unnecessarily call {@link Streams#concat(LongStream...)}. */
  static final class ConcatOneLongStream {
    @BeforeTemplate
    LongStream before(LongStream stream) {
      return Streams.concat(stream);
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    LongStream after(LongStream stream) {
      return stream;
    }
  }

  /** Prefer {@link LongStream#concat(LongStream, LongStream)} over the Guava alternative. */
  static final class ConcatTwoLongStreams {
    @BeforeTemplate
    LongStream before(LongStream s1, LongStream s2) {
      return Streams.concat(s1, s2);
    }

    @AfterTemplate
    LongStream after(LongStream s1, LongStream s2) {
      return LongStream.concat(s1, s2);
    }
  }

  /** Avoid unnecessary nesting of {@link LongStream#filter(LongPredicate)} operations. */
  abstract static class FilterOuterLongStreamAfterFlatMap {
    @Placeholder
    abstract LongStream toLongStreamFunction(@MayOptionallyUse long element);

    @BeforeTemplate
    LongStream before(LongStream stream, LongPredicate predicate) {
      return stream.flatMap(v -> toLongStreamFunction(v).filter(predicate));
    }

    @AfterTemplate
    LongStream after(LongStream stream, LongPredicate predicate) {
      return stream.flatMap(v -> toLongStreamFunction(v)).filter(predicate);
    }
  }

  /** Avoid unnecessary nesting of {@link LongStream#filter(LongPredicate)} operations. */
  abstract static class FilterOuterStreamAfterFlatMapToLong<T> {
    @Placeholder(allowsIdentity = true)
    abstract LongStream toLongStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    LongStream before(Stream<T> stream, LongPredicate predicate) {
      return stream.flatMapToLong(v -> toLongStreamFunction(v).filter(predicate));
    }

    @AfterTemplate
    LongStream after(Stream<T> stream, LongPredicate predicate) {
      return stream.flatMapToLong(v -> toLongStreamFunction(v)).filter(predicate);
    }
  }

  /** Avoid unnecessary nesting of {@link LongStream#map(LongUnaryOperator)} operations. */
  abstract static class MapOuterLongStreamAfterFlatMap {
    @Placeholder
    abstract LongStream toLongStreamFunction(@MayOptionallyUse long element);

    @BeforeTemplate
    LongStream before(LongStream stream, LongUnaryOperator function) {
      return stream.flatMap(v -> toLongStreamFunction(v).map(function));
    }

    @AfterTemplate
    LongStream after(LongStream stream, LongUnaryOperator function) {
      return stream.flatMap(v -> toLongStreamFunction(v)).map(function);
    }
  }

  /** Avoid unnecessary nesting of {@link LongStream#map(LongUnaryOperator)} operations. */
  abstract static class MapOuterStreamAfterFlatMapToLong<T> {
    @Placeholder(allowsIdentity = true)
    abstract LongStream toLongStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    LongStream before(Stream<T> stream, LongUnaryOperator function) {
      return stream.flatMapToLong(v -> toLongStreamFunction(v).map(function));
    }

    @AfterTemplate
    LongStream after(Stream<T> stream, LongUnaryOperator function) {
      return stream.flatMapToLong(v -> toLongStreamFunction(v)).map(function);
    }
  }

  /** Avoid unnecessary nesting of {@link LongStream#flatMap(LongFunction)} operations. */
  abstract static class FlatMapOuterLongStreamAfterFlatMap {
    @Placeholder
    abstract LongStream toLongStreamFunction(@MayOptionallyUse long element);

    @BeforeTemplate
    LongStream before(LongStream stream, LongFunction<? extends LongStream> function) {
      return stream.flatMap(v -> toLongStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    LongStream after(LongStream stream, LongFunction<? extends LongStream> function) {
      return stream.flatMap(v -> toLongStreamFunction(v)).flatMap(function);
    }
  }

  /** Avoid unnecessary nesting of {@link LongStream#flatMap(LongFunction)} operations. */
  abstract static class FlatMapOuterStreamAfterFlatMapToLong<T> {
    @Placeholder(allowsIdentity = true)
    abstract LongStream toLongStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    LongStream before(Stream<T> stream, LongFunction<? extends LongStream> function) {
      return stream.flatMapToLong(v -> toLongStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    LongStream after(Stream<T> stream, LongFunction<? extends LongStream> function) {
      return stream.flatMapToLong(v -> toLongStreamFunction(v)).flatMap(function);
    }
  }

  /**
   * Apply {@link LongStream#filter(LongPredicate)} before {@link LongStream#sorted()} to reduce the
   * number of elements to sort.
   */
  static final class LongStreamFilterSorted {
    @BeforeTemplate
    LongStream before(LongStream stream, LongPredicate predicate) {
      return stream.sorted().filter(predicate);
    }

    @AfterTemplate
    LongStream after(LongStream stream, LongPredicate predicate) {
      return stream.filter(predicate).sorted();
    }
  }

  /** In order to test whether a stream has any element, simply try to find one. */
  static final class LongStreamIsEmpty {
    @BeforeTemplate
    boolean before(LongStream stream) {
      return Refaster.anyOf(
          stream.count() == 0,
          stream.count() <= 0,
          stream.count() < 1,
          stream.findFirst().isEmpty());
    }

    @AfterTemplate
    boolean after(LongStream stream) {
      return stream.findAny().isEmpty();
    }
  }

  /** In order to test whether a stream has any element, simply try to find one. */
  static final class LongStreamIsNotEmpty {
    @BeforeTemplate
    boolean before(LongStream stream) {
      return Refaster.anyOf(
          stream.count() != 0,
          stream.count() > 0,
          stream.count() >= 1,
          stream.findFirst().isPresent());
    }

    @AfterTemplate
    boolean after(LongStream stream) {
      return stream.findAny().isPresent();
    }
  }

  static final class LongStreamMin {
    @BeforeTemplate
    OptionalLong before(LongStream stream) {
      return stream.sorted().findFirst();
    }

    @AfterTemplate
    OptionalLong after(LongStream stream) {
      return stream.min();
    }
  }

  /** Prefer {@link LongStream#noneMatch(LongPredicate)} over more contrived alternatives. */
  static final class LongStreamNoneMatch {
    @BeforeTemplate
    boolean before(LongStream stream, LongPredicate predicate) {
      return Refaster.anyOf(
          !stream.anyMatch(predicate),
          stream.allMatch(predicate.negate()),
          stream.filter(predicate).findAny().isEmpty());
    }

    @AfterTemplate
    boolean after(LongStream stream, LongPredicate predicate) {
      return stream.noneMatch(predicate);
    }
  }

  abstract static class LongStreamNoneMatch2 {
    @Placeholder
    abstract boolean test(@MayOptionallyUse long element);

    @BeforeTemplate
    boolean before(LongStream stream) {
      return stream.allMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(LongStream stream) {
      return stream.noneMatch(e -> test(e));
    }
  }

  /** Prefer {@link LongStream#anyMatch(LongPredicate)} over more contrived alternatives. */
  static final class LongStreamAnyMatch {
    @BeforeTemplate
    @SuppressWarnings("java:S4034" /* This violation will be rewritten. */)
    boolean before(LongStream stream, LongPredicate predicate) {
      return Refaster.anyOf(
          !stream.noneMatch(predicate), stream.filter(predicate).findAny().isPresent());
    }

    @AfterTemplate
    boolean after(LongStream stream, LongPredicate predicate) {
      return stream.anyMatch(predicate);
    }
  }

  static final class LongStreamAllMatch {
    @BeforeTemplate
    boolean before(LongStream stream, LongPredicate predicate) {
      return stream.noneMatch(predicate.negate());
    }

    @AfterTemplate
    boolean after(LongStream stream, LongPredicate predicate) {
      return stream.allMatch(predicate);
    }
  }

  abstract static class LongStreamAllMatch2 {
    @Placeholder
    abstract boolean test(@MayOptionallyUse long element);

    @BeforeTemplate
    boolean before(LongStream stream) {
      return stream.noneMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(LongStream stream) {
      return stream.allMatch(e -> test(e));
    }
  }
}
