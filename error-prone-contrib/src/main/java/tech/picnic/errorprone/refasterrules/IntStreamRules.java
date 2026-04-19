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
    IntStream before(IntStream stream) {
      return Streams.concat(stream);
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    IntStream after(IntStream stream) {
      return stream;
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
    IntStream before(IntStream stream, IntPredicate predicate) {
      return stream.flatMap(v -> toIntStreamFunction(v).filter(predicate));
    }

    @AfterTemplate
    IntStream after(IntStream stream, IntPredicate predicate) {
      return stream.flatMap(v -> toIntStreamFunction(v)).filter(predicate);
    }
  }

  /** Prefer {@link IntStream#filter(IntPredicate)} over more contrived alternatives. */
  abstract static class StreamFlatMapToIntFilter<T> {
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

  /** Prefer {@link IntStream#map(IntUnaryOperator)} over more contrived alternatives. */
  abstract static class IntStreamFlatMapMap {
    @Placeholder
    abstract IntStream toIntStreamFunction(@MayOptionallyUse int element);

    @BeforeTemplate
    IntStream before(IntStream stream, IntUnaryOperator unaryOperator) {
      return stream.flatMap(v -> toIntStreamFunction(v).map(unaryOperator));
    }

    @AfterTemplate
    IntStream after(IntStream stream, IntUnaryOperator unaryOperator) {
      return stream.flatMap(v -> toIntStreamFunction(v)).map(unaryOperator);
    }
  }

  /** Prefer {@link IntStream#map(IntUnaryOperator)} over more contrived alternatives. */
  abstract static class StreamFlatMapToIntMap<T> {
    @Placeholder(allowsIdentity = true)
    abstract IntStream toIntStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    IntStream before(Stream<T> stream, IntUnaryOperator unaryOperator) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v).map(unaryOperator));
    }

    @AfterTemplate
    IntStream after(Stream<T> stream, IntUnaryOperator unaryOperator) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v)).map(unaryOperator);
    }
  }

  /** Prefer {@link IntStream#flatMap(IntFunction)} over more contrived alternatives. */
  abstract static class IntStreamFlatMapFlatMap<S extends IntStream> {
    @Placeholder
    abstract IntStream toIntStreamFunction(@MayOptionallyUse int element);

    @BeforeTemplate
    IntStream before(IntStream stream, IntFunction<S> function) {
      return stream.flatMap(v -> toIntStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    IntStream after(IntStream stream, IntFunction<S> function) {
      return stream.flatMap(v -> toIntStreamFunction(v)).flatMap(function);
    }
  }

  /** Prefer {@link IntStream#flatMap(IntFunction)} over more contrived alternatives. */
  abstract static class StreamFlatMapToIntFlatMap<T, S extends IntStream> {
    @Placeholder(allowsIdentity = true)
    abstract IntStream toIntStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    IntStream before(Stream<T> stream, IntFunction<S> function) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    IntStream after(Stream<T> stream, IntFunction<S> function) {
      return stream.flatMapToInt(v -> toIntStreamFunction(v)).flatMap(function);
    }
  }

  /**
   * Prefer {@link IntStream#filter(IntPredicate)} before {@link IntStream#sorted()} over less
   * efficient alternatives.
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

  /** Prefer {@link IntStream#findAny()} over less efficient alternatives. */
  static final class IntStreamFindAnyIsEmpty {
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

  /** Prefer {@link IntStream#findAny()} over less efficient alternatives. */
  static final class IntStreamFindAnyIsPresent {
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

  /** Prefer {@link IntStream#min()} over less efficient alternatives. */
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
  static final class IntStreamNoneMatchWithIntPredicate {
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

  /** Prefer {@link IntStream#noneMatch(IntPredicate)} over less explicit alternatives. */
  abstract static class IntStreamNoneMatch {
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

  /** Prefer {@link IntStream#allMatch(IntPredicate)} over more contrived alternatives. */
  static final class IntStreamAllMatchWithIntPredicate {
    @BeforeTemplate
    boolean before(IntStream stream, IntPredicate predicate) {
      return stream.noneMatch(predicate.negate());
    }

    @AfterTemplate
    boolean after(IntStream stream, IntPredicate predicate) {
      return stream.allMatch(predicate);
    }
  }

  /** Prefer {@link IntStream#allMatch(IntPredicate)} over less explicit alternatives. */
  abstract static class IntStreamAllMatch {
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

  /** Prefer {@link IntStream#takeWhile(IntPredicate)} over more verbose alternatives. */
  static final class IntStreamTakeWhile {
    @BeforeTemplate
    IntStream before(IntStream stream, IntPredicate predicate) {
      return stream.takeWhile(predicate).filter(predicate);
    }

    @AfterTemplate
    IntStream after(IntStream stream, IntPredicate predicate) {
      return stream.takeWhile(predicate);
    }
  }
}
