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

  /** Prefer {@link LongStream#range(long, long)} over more verbose alternatives. */
  static final class LongStreamRange {
    @BeforeTemplate
    LongStream before(long startInclusive, long endExclusive) {
      return LongStream.rangeClosed(startInclusive, endExclusive - 1);
    }

    @AfterTemplate
    LongStream after(long startInclusive, long endExclusive) {
      return LongStream.range(startInclusive, endExclusive);
    }
  }

  /** Prefer the {@link LongStream} as-is over more contrived alternatives. */
  static final class LongStreamIdentity {
    @BeforeTemplate
    LongStream before(LongStream longStream) {
      return Streams.concat(longStream);
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    LongStream after(LongStream longStream) {
      return longStream;
    }
  }

  /** Prefer {@link LongStream#concat(LongStream, LongStream)} over non-JDK alternatives. */
  static final class LongStreamConcat {
    @BeforeTemplate
    LongStream before(LongStream a, LongStream b) {
      return Streams.concat(a, b);
    }

    @AfterTemplate
    LongStream after(LongStream a, LongStream b) {
      return LongStream.concat(a, b);
    }
  }

  /** Prefer {@link LongStream#filter(LongPredicate)} over more contrived alternatives. */
  abstract static class LongStreamFlatMapFilter {
    @Placeholder
    abstract LongStream toLongStreamFunction(@MayOptionallyUse long element);

    @BeforeTemplate
    LongStream before(LongStream longStream, LongPredicate longPredicate) {
      return longStream.flatMap(v -> toLongStreamFunction(v).filter(longPredicate));
    }

    @AfterTemplate
    LongStream after(LongStream longStream, LongPredicate longPredicate) {
      return longStream.flatMap(v -> toLongStreamFunction(v)).filter(longPredicate);
    }
  }

  /** Prefer {@link LongStream#filter(LongPredicate)} over more contrived alternatives. */
  abstract static class StreamFlatMapToLongFilter<T> {
    @Placeholder(allowsIdentity = true)
    abstract LongStream toLongStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    LongStream before(Stream<T> stream, LongPredicate longPredicate) {
      return stream.flatMapToLong(v -> toLongStreamFunction(v).filter(longPredicate));
    }

    @AfterTemplate
    LongStream after(Stream<T> stream, LongPredicate longPredicate) {
      return stream.flatMapToLong(v -> toLongStreamFunction(v)).filter(longPredicate);
    }
  }

  /** Prefer {@link LongStream#map(LongUnaryOperator)} over more contrived alternatives. */
  abstract static class LongStreamFlatMapMap {
    @Placeholder
    abstract LongStream toLongStreamFunction(@MayOptionallyUse long element);

    @BeforeTemplate
    LongStream before(LongStream longStream, LongUnaryOperator longUnaryOperator) {
      return longStream.flatMap(v -> toLongStreamFunction(v).map(longUnaryOperator));
    }

    @AfterTemplate
    LongStream after(LongStream longStream, LongUnaryOperator longUnaryOperator) {
      return longStream.flatMap(v -> toLongStreamFunction(v)).map(longUnaryOperator);
    }
  }

  /** Prefer {@link LongStream#map(LongUnaryOperator)} over more contrived alternatives. */
  abstract static class StreamFlatMapToLongMap<T> {
    @Placeholder(allowsIdentity = true)
    abstract LongStream toLongStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    LongStream before(Stream<T> stream, LongUnaryOperator longUnaryOperator) {
      return stream.flatMapToLong(v -> toLongStreamFunction(v).map(longUnaryOperator));
    }

    @AfterTemplate
    LongStream after(Stream<T> stream, LongUnaryOperator longUnaryOperator) {
      return stream.flatMapToLong(v -> toLongStreamFunction(v)).map(longUnaryOperator);
    }
  }

  /** Prefer {@link LongStream#flatMap(LongFunction)} over more contrived alternatives. */
  abstract static class LongStreamFlatMapFlatMap<S extends LongStream> {
    @Placeholder
    abstract LongStream toLongStreamFunction(@MayOptionallyUse long element);

    @BeforeTemplate
    LongStream before(LongStream longStream, LongFunction<S> longFunction) {
      return longStream.flatMap(v -> toLongStreamFunction(v).flatMap(longFunction));
    }

    @AfterTemplate
    LongStream after(LongStream longStream, LongFunction<S> longFunction) {
      return longStream.flatMap(v -> toLongStreamFunction(v)).flatMap(longFunction);
    }
  }

  /** Prefer {@link LongStream#flatMap(LongFunction)} over more contrived alternatives. */
  abstract static class StreamFlatMapToLongFlatMap<T, S extends LongStream> {
    @Placeholder(allowsIdentity = true)
    abstract LongStream toLongStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    LongStream before(Stream<T> stream, LongFunction<S> longFunction) {
      return stream.flatMapToLong(v -> toLongStreamFunction(v).flatMap(longFunction));
    }

    @AfterTemplate
    LongStream after(Stream<T> stream, LongFunction<S> longFunction) {
      return stream.flatMapToLong(v -> toLongStreamFunction(v)).flatMap(longFunction);
    }
  }

  /**
   * Prefer {@link LongStream#filter(LongPredicate)} before {@link LongStream#sorted()} over less
   * efficient alternatives.
   */
  static final class LongStreamFilterSorted {
    @BeforeTemplate
    LongStream before(LongStream longStream, LongPredicate longPredicate) {
      return longStream.sorted().filter(longPredicate);
    }

    @AfterTemplate
    LongStream after(LongStream longStream, LongPredicate longPredicate) {
      return longStream.filter(longPredicate).sorted();
    }
  }

  /** Prefer {@link LongStream#findAny()} over less efficient alternatives. */
  static final class LongStreamFindAnyIsEmpty {
    @BeforeTemplate
    boolean before(LongStream longStream) {
      return Refaster.anyOf(
          longStream.count() == 0,
          longStream.count() <= 0,
          longStream.count() < 1,
          longStream.findFirst().isEmpty());
    }

    @AfterTemplate
    boolean after(LongStream longStream) {
      return longStream.findAny().isEmpty();
    }
  }

  /** Prefer {@link LongStream#findAny()} over less efficient alternatives. */
  static final class LongStreamFindAnyIsPresent {
    @BeforeTemplate
    boolean before(LongStream longStream) {
      return Refaster.anyOf(
          longStream.count() != 0,
          longStream.count() > 0,
          longStream.count() >= 1,
          longStream.findFirst().isPresent());
    }

    @AfterTemplate
    boolean after(LongStream longStream) {
      return longStream.findAny().isPresent();
    }
  }

  /** Prefer {@link LongStream#min()} over less efficient alternatives. */
  static final class LongStreamMin {
    @BeforeTemplate
    OptionalLong before(LongStream longStream) {
      return longStream.sorted().findFirst();
    }

    @AfterTemplate
    OptionalLong after(LongStream longStream) {
      return longStream.min();
    }
  }

  /** Prefer {@link LongStream#noneMatch(LongPredicate)} over more contrived alternatives. */
  static final class LongStreamNoneMatchWithLongPredicate {
    @BeforeTemplate
    boolean before(LongStream longStream, LongPredicate longPredicate) {
      return Refaster.anyOf(
          !longStream.anyMatch(longPredicate),
          longStream.allMatch(longPredicate.negate()),
          longStream.filter(longPredicate).findAny().isEmpty());
    }

    @AfterTemplate
    boolean after(LongStream longStream, LongPredicate longPredicate) {
      return longStream.noneMatch(longPredicate);
    }
  }

  /** Prefer {@link LongStream#noneMatch(LongPredicate)} over less explicit alternatives. */
  abstract static class LongStreamNoneMatch {
    @Placeholder
    abstract boolean test(@MayOptionallyUse long element);

    @BeforeTemplate
    boolean before(LongStream longStream) {
      return longStream.allMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(LongStream longStream) {
      return longStream.noneMatch(e -> test(e));
    }
  }

  /** Prefer {@link LongStream#anyMatch(LongPredicate)} over more contrived alternatives. */
  static final class LongStreamAnyMatch {
    @BeforeTemplate
    @SuppressWarnings("java:S4034" /* This violation will be rewritten. */)
    boolean before(LongStream longStream, LongPredicate longPredicate) {
      return Refaster.anyOf(
          !longStream.noneMatch(longPredicate),
          longStream.filter(longPredicate).findAny().isPresent());
    }

    @AfterTemplate
    boolean after(LongStream longStream, LongPredicate longPredicate) {
      return longStream.anyMatch(longPredicate);
    }
  }

  /** Prefer {@link LongStream#allMatch(LongPredicate)} over more contrived alternatives. */
  static final class LongStreamAllMatchWithLongPredicate {
    @BeforeTemplate
    boolean before(LongStream longStream, LongPredicate longPredicate) {
      return longStream.noneMatch(longPredicate.negate());
    }

    @AfterTemplate
    boolean after(LongStream longStream, LongPredicate longPredicate) {
      return longStream.allMatch(longPredicate);
    }
  }

  /** Prefer {@link LongStream#allMatch(LongPredicate)} over less explicit alternatives. */
  abstract static class LongStreamAllMatch {
    @Placeholder
    abstract boolean test(@MayOptionallyUse long element);

    @BeforeTemplate
    boolean before(LongStream longStream) {
      return longStream.noneMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(LongStream longStream) {
      return longStream.allMatch(e -> test(e));
    }
  }

  /** Prefer {@link LongStream#takeWhile(LongPredicate)} over more verbose alternatives. */
  static final class LongStreamTakeWhile {
    @BeforeTemplate
    LongStream before(LongStream longStream, LongPredicate predicate) {
      return longStream.takeWhile(predicate).filter(predicate);
    }

    @AfterTemplate
    LongStream after(LongStream longStream, LongPredicate predicate) {
      return longStream.takeWhile(predicate);
    }
  }
}
