package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.Streams;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
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

  /** Prefer {@link IntStream#range(int, int)} over more verbose alternatives. */
  static final class IntStreamRange {
    @BeforeTemplate
    IntStream before(int startInclusive, int endExclusive) {
      return IntStream.rangeClosed(startInclusive, endExclusive - 1);
    }

    @AfterTemplate
    IntStream after(int startInclusive, int endExclusive) {
      return IntStream.range(startInclusive, endExclusive);
    }
  }

  /** Prefer using {@link IntStream}s as-is over more contrived alternatives. */
  static final class IntStreamIdentity {
    @BeforeTemplate
    IntStream before(IntStream intStream) {
      return Streams.concat(intStream);
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    IntStream after(IntStream intStream) {
      return intStream;
    }
  }

  /** Prefer {@link IntStream#concat(IntStream, IntStream)} over non-JDK alternatives. */
  static final class IntStreamConcat {
    @BeforeTemplate
    IntStream before(IntStream a, IntStream b) {
      return Streams.concat(a, b);
    }

    @AfterTemplate
    IntStream after(IntStream a, IntStream b) {
      return IntStream.concat(a, b);
    }
  }

  /** Prefer {@link IntStream#filter(IntPredicate)} over more contrived alternatives. */
  abstract static class IntStreamFlatMapFilter {
    @Placeholder
    abstract IntStream toIntStreamFunction(@MayOptionallyUse int element);

    @BeforeTemplate
    IntStream before(IntStream intStream, IntPredicate intPredicate) {
      return intStream.flatMap(v -> toIntStreamFunction(v).filter(intPredicate));
    }

    @AfterTemplate
    IntStream after(IntStream intStream, IntPredicate intPredicate) {
      return intStream.flatMap(v -> toIntStreamFunction(v)).filter(intPredicate);
    }
  }

  /** Prefer {@link IntStream#filter(IntPredicate)} over more contrived alternatives. */
  abstract static class StreamFlatMapToIntFilter<T> {
    @Placeholder(allowsIdentity = true)
    abstract IntStream toIntStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    IntStream before(Stream<T> stream, IntPredicate intPredicate) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v).filter(intPredicate));
    }

    @AfterTemplate
    IntStream after(Stream<T> stream, IntPredicate intPredicate) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v)).filter(intPredicate);
    }
  }

  /** Prefer {@link IntStream#map(IntUnaryOperator)} over more contrived alternatives. */
  abstract static class IntStreamFlatMapMap {
    @Placeholder
    abstract IntStream toIntStreamFunction(@MayOptionallyUse int element);

    @BeforeTemplate
    IntStream before(IntStream intStream, IntUnaryOperator intUnaryOperator) {
      return intStream.flatMap(v -> toIntStreamFunction(v).map(intUnaryOperator));
    }

    @AfterTemplate
    IntStream after(IntStream intStream, IntUnaryOperator intUnaryOperator) {
      return intStream.flatMap(v -> toIntStreamFunction(v)).map(intUnaryOperator);
    }
  }

  /** Prefer {@link IntStream#map(IntUnaryOperator)} over more contrived alternatives. */
  abstract static class StreamFlatMapToIntMap<T> {
    @Placeholder(allowsIdentity = true)
    abstract IntStream toIntStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    IntStream before(Stream<T> stream, IntUnaryOperator intUnaryOperator) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v).map(intUnaryOperator));
    }

    @AfterTemplate
    IntStream after(Stream<T> stream, IntUnaryOperator intUnaryOperator) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v)).map(intUnaryOperator);
    }
  }

  /** Prefer {@link IntStream#flatMap(IntFunction)} over more contrived alternatives. */
  abstract static class IntStreamFlatMapFlatMap<S extends IntStream> {
    @Placeholder
    abstract IntStream toIntStreamFunction(@MayOptionallyUse int element);

    @BeforeTemplate
    IntStream before(IntStream intStream, IntFunction<S> intFunction) {
      return intStream.flatMap(v -> toIntStreamFunction(v).flatMap(intFunction));
    }

    @AfterTemplate
    IntStream after(IntStream intStream, IntFunction<S> intFunction) {
      return intStream.flatMap(v -> toIntStreamFunction(v)).flatMap(intFunction);
    }
  }

  /** Prefer {@link IntStream#flatMap(IntFunction)} over more contrived alternatives. */
  abstract static class StreamFlatMapToIntFlatMap<T, S extends IntStream> {
    @Placeholder(allowsIdentity = true)
    abstract IntStream toIntStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    IntStream before(Stream<T> stream, IntFunction<S> intFunction) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v).flatMap(intFunction));
    }

    @AfterTemplate
    IntStream after(Stream<T> stream, IntFunction<S> intFunction) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v)).flatMap(intFunction);
    }
  }

  /**
   * Prefer {@link IntStream#filter(IntPredicate)} before {@link IntStream#sorted()} over less
   * efficient alternatives.
   */
  static final class IntStreamFilterSorted {
    @BeforeTemplate
    IntStream before(IntStream intStream, IntPredicate intPredicate) {
      return intStream.sorted().filter(intPredicate);
    }

    @AfterTemplate
    IntStream after(IntStream intStream, IntPredicate intPredicate) {
      return intStream.filter(intPredicate).sorted();
    }
  }

  /** Prefer {@link IntStream#findAny()} over less efficient alternatives. */
  static final class IntStreamFindAnyIsEmpty {
    @BeforeTemplate
    boolean before(IntStream intStream) {
      return Refaster.anyOf(
          intStream.count() == 0,
          intStream.count() <= 0,
          intStream.count() < 1,
          intStream.findFirst().isEmpty());
    }

    @AfterTemplate
    boolean after(IntStream intStream) {
      return intStream.findAny().isEmpty();
    }
  }

  /** Prefer {@link IntStream#findAny()} over less efficient alternatives. */
  static final class IntStreamFindAnyIsPresent {
    @BeforeTemplate
    boolean before(IntStream intStream) {
      return Refaster.anyOf(
          intStream.count() != 0,
          intStream.count() > 0,
          intStream.count() >= 1,
          intStream.findFirst().isPresent());
    }

    @AfterTemplate
    boolean after(IntStream intStream) {
      return intStream.findAny().isPresent();
    }
  }

  /** Prefer {@link IntStream#min()} over less efficient alternatives. */
  static final class IntStreamMin {
    @BeforeTemplate
    OptionalInt before(IntStream intStream) {
      return intStream.sorted().findFirst();
    }

    @AfterTemplate
    OptionalInt after(IntStream intStream) {
      return intStream.min();
    }
  }

  /** Prefer {@link IntStream#noneMatch(IntPredicate)} over more contrived alternatives. */
  static final class IntStreamNoneMatchWithIntPredicate {
    @BeforeTemplate
    boolean before(IntStream intStream, IntPredicate intPredicate) {
      return Refaster.anyOf(
          !intStream.anyMatch(intPredicate),
          intStream.allMatch(intPredicate.negate()),
          intStream.filter(intPredicate).findAny().isEmpty());
    }

    @AfterTemplate
    boolean after(IntStream intStream, IntPredicate intPredicate) {
      return intStream.noneMatch(intPredicate);
    }
  }

  /** Prefer {@link IntStream#noneMatch(IntPredicate)} over less explicit alternatives. */
  abstract static class IntStreamNoneMatch {
    @Placeholder
    abstract boolean test(@MayOptionallyUse int element);

    @BeforeTemplate
    boolean before(IntStream intStream) {
      return intStream.allMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(IntStream intStream) {
      return intStream.noneMatch(e -> test(e));
    }
  }

  /** Prefer {@link IntStream#anyMatch(IntPredicate)} over more contrived alternatives. */
  static final class IntStreamAnyMatch {
    @BeforeTemplate
    @SuppressWarnings("java:S4034" /* This violation will be rewritten. */)
    boolean before(IntStream intStream, IntPredicate intPredicate) {
      return Refaster.anyOf(
          !intStream.noneMatch(intPredicate), intStream.filter(intPredicate).findAny().isPresent());
    }

    @AfterTemplate
    boolean after(IntStream intStream, IntPredicate intPredicate) {
      return intStream.anyMatch(intPredicate);
    }
  }

  /** Prefer {@link IntStream#allMatch(IntPredicate)} over more contrived alternatives. */
  static final class IntStreamAllMatchWithIntPredicate {
    @BeforeTemplate
    boolean before(IntStream intStream, IntPredicate intPredicate) {
      return intStream.noneMatch(intPredicate.negate());
    }

    @AfterTemplate
    boolean after(IntStream intStream, IntPredicate intPredicate) {
      return intStream.allMatch(intPredicate);
    }
  }

  /** Prefer {@link IntStream#allMatch(IntPredicate)} over less explicit alternatives. */
  abstract static class IntStreamAllMatch {
    @Placeholder
    abstract boolean test(@MayOptionallyUse int element);

    @BeforeTemplate
    boolean before(IntStream intStream) {
      return intStream.noneMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(IntStream intStream) {
      return intStream.allMatch(e -> test(e));
    }
  }

  /** Prefer {@link IntStream#takeWhile(IntPredicate)} over more verbose alternatives. */
  static final class IntStreamTakeWhile {
    @BeforeTemplate
    IntStream before(IntStream intStream, IntPredicate predicate) {
      return intStream.takeWhile(predicate).filter(predicate);
    }

    @AfterTemplate
    IntStream after(IntStream intStream, IntPredicate predicate) {
      return intStream.takeWhile(predicate);
    }
  }
}
