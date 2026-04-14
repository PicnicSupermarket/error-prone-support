package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.Streams;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import java.util.OptionalDouble;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link DoubleStream}s. */
@OnlineDocumentation
final class DoubleStreamRules {
  private DoubleStreamRules() {}

  /** Prefer using {@link DoubleStream}s as-is over more contrived alternatives. */
  static final class DoubleStreamIdentity {
    @BeforeTemplate
    DoubleStream before(DoubleStream doubleStream) {
      return Streams.concat(doubleStream);
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    DoubleStream after(DoubleStream doubleStream) {
      return doubleStream;
    }
  }

  /** Prefer {@link DoubleStream#concat(DoubleStream, DoubleStream)} over non-JDK alternatives. */
  static final class DoubleStreamConcat {
    @BeforeTemplate
    DoubleStream before(DoubleStream a, DoubleStream b) {
      return Streams.concat(a, b);
    }

    @AfterTemplate
    DoubleStream after(DoubleStream a, DoubleStream b) {
      return DoubleStream.concat(a, b);
    }
  }

  /** Prefer {@link DoubleStream#filter(DoublePredicate)} over more contrived alternatives. */
  abstract static class DoubleStreamFlatMapFilter {
    @Placeholder
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse double element);

    @BeforeTemplate
    DoubleStream before(DoubleStream doubleStream, DoublePredicate doublePredicate) {
      return doubleStream.flatMap(v -> toDoubleStreamFunction(v).filter(doublePredicate));
    }

    @AfterTemplate
    DoubleStream after(DoubleStream doubleStream, DoublePredicate doublePredicate) {
      return doubleStream.flatMap(v -> toDoubleStreamFunction(v)).filter(doublePredicate);
    }
  }

  /** Prefer {@link DoubleStream#filter(DoublePredicate)} over more contrived alternatives. */
  abstract static class StreamFlatMapToDoubleFilter<T> {
    @Placeholder(allowsIdentity = true)
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    DoubleStream before(Stream<T> stream, DoublePredicate doublePredicate) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v).filter(doublePredicate));
    }

    @AfterTemplate
    DoubleStream after(Stream<T> stream, DoublePredicate doublePredicate) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v)).filter(doublePredicate);
    }
  }

  /** Prefer {@link DoubleStream#map(DoubleUnaryOperator)} over more contrived alternatives. */
  abstract static class DoubleStreamFlatMapMap {
    @Placeholder
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse double element);

    @BeforeTemplate
    DoubleStream before(DoubleStream doubleStream, DoubleUnaryOperator doubleUnaryOperator) {
      return doubleStream.flatMap(v -> toDoubleStreamFunction(v).map(doubleUnaryOperator));
    }

    @AfterTemplate
    DoubleStream after(DoubleStream doubleStream, DoubleUnaryOperator doubleUnaryOperator) {
      return doubleStream.flatMap(v -> toDoubleStreamFunction(v)).map(doubleUnaryOperator);
    }
  }

  /** Prefer {@link DoubleStream#map(DoubleUnaryOperator)} over more contrived alternatives. */
  abstract static class StreamFlatMapToDoubleMap<T> {
    @Placeholder(allowsIdentity = true)
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    DoubleStream before(Stream<T> stream, DoubleUnaryOperator doubleUnaryOperator) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v).map(doubleUnaryOperator));
    }

    @AfterTemplate
    DoubleStream after(Stream<T> stream, DoubleUnaryOperator doubleUnaryOperator) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v)).map(doubleUnaryOperator);
    }
  }

  /** Prefer {@link DoubleStream#flatMap(DoubleFunction)} over more contrived alternatives. */
  abstract static class DoubleStreamFlatMapFlatMap<S extends DoubleStream> {
    @Placeholder
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse double element);

    @BeforeTemplate
    DoubleStream before(DoubleStream doubleStream, DoubleFunction<S> doubleFunction) {
      return doubleStream.flatMap(v -> toDoubleStreamFunction(v).flatMap(doubleFunction));
    }

    @AfterTemplate
    DoubleStream after(DoubleStream doubleStream, DoubleFunction<S> doubleFunction) {
      return doubleStream.flatMap(v -> toDoubleStreamFunction(v)).flatMap(doubleFunction);
    }
  }

  /** Prefer {@link DoubleStream#flatMap(DoubleFunction)} over more contrived alternatives. */
  abstract static class StreamFlatMapToDoubleFlatMap<T, S extends DoubleStream> {
    @Placeholder(allowsIdentity = true)
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    DoubleStream before(Stream<T> stream, DoubleFunction<S> doubleFunction) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v).flatMap(doubleFunction));
    }

    @AfterTemplate
    DoubleStream after(Stream<T> stream, DoubleFunction<S> doubleFunction) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v)).flatMap(doubleFunction);
    }
  }

  /**
   * Prefer {@link DoubleStream#filter(DoublePredicate)} before {@link DoubleStream#sorted()} over
   * less efficient alternatives.
   */
  static final class DoubleStreamFilterSorted {
    @BeforeTemplate
    DoubleStream before(DoubleStream doubleStream, DoublePredicate doublePredicate) {
      return doubleStream.sorted().filter(doublePredicate);
    }

    @AfterTemplate
    DoubleStream after(DoubleStream doubleStream, DoublePredicate doublePredicate) {
      return doubleStream.filter(doublePredicate).sorted();
    }
  }

  /** Prefer {@link DoubleStream#findAny()} over less efficient alternatives. */
  static final class DoubleStreamFindAnyIsEmpty {
    @BeforeTemplate
    boolean before(DoubleStream doubleStream) {
      return Refaster.anyOf(
          doubleStream.count() == 0,
          doubleStream.count() <= 0,
          doubleStream.count() < 1,
          doubleStream.findFirst().isEmpty());
    }

    @AfterTemplate
    boolean after(DoubleStream doubleStream) {
      return doubleStream.findAny().isEmpty();
    }
  }

  /** Prefer {@link DoubleStream#findAny()} over less efficient alternatives. */
  static final class DoubleStreamFindAnyIsPresent {
    @BeforeTemplate
    boolean before(DoubleStream doubleStream) {
      return Refaster.anyOf(
          doubleStream.count() != 0,
          doubleStream.count() > 0,
          doubleStream.count() >= 1,
          doubleStream.findFirst().isPresent());
    }

    @AfterTemplate
    boolean after(DoubleStream doubleStream) {
      return doubleStream.findAny().isPresent();
    }
  }

  /** Prefer {@link DoubleStream#min()} over less efficient alternatives. */
  static final class DoubleStreamMin {
    @BeforeTemplate
    OptionalDouble before(DoubleStream doubleStream) {
      return doubleStream.sorted().findFirst();
    }

    @AfterTemplate
    OptionalDouble after(DoubleStream doubleStream) {
      return doubleStream.min();
    }
  }

  /** Prefer {@link DoubleStream#noneMatch(DoublePredicate)} over more contrived alternatives. */
  static final class DoubleStreamNoneMatchWithDoublePredicate {
    @BeforeTemplate
    boolean before(DoubleStream doubleStream, DoublePredicate doublePredicate) {
      return Refaster.anyOf(
          !doubleStream.anyMatch(doublePredicate),
          doubleStream.allMatch(doublePredicate.negate()),
          doubleStream.filter(doublePredicate).findAny().isEmpty());
    }

    @AfterTemplate
    boolean after(DoubleStream doubleStream, DoublePredicate doublePredicate) {
      return doubleStream.noneMatch(doublePredicate);
    }
  }

  /** Prefer {@link DoubleStream#noneMatch(DoublePredicate)} over less explicit alternatives. */
  abstract static class DoubleStreamNoneMatch {
    @Placeholder
    abstract boolean test(@MayOptionallyUse double element);

    @BeforeTemplate
    boolean before(DoubleStream doubleStream) {
      return doubleStream.allMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(DoubleStream doubleStream) {
      return doubleStream.noneMatch(e -> test(e));
    }
  }

  /** Prefer {@link DoubleStream#anyMatch(DoublePredicate)} over more contrived alternatives. */
  static final class DoubleStreamAnyMatch {
    @BeforeTemplate
    @SuppressWarnings("java:S4034" /* This violation will be rewritten. */)
    boolean before(DoubleStream doubleStream, DoublePredicate doublePredicate) {
      return Refaster.anyOf(
          !doubleStream.noneMatch(doublePredicate),
          doubleStream.filter(doublePredicate).findAny().isPresent());
    }

    @AfterTemplate
    boolean after(DoubleStream doubleStream, DoublePredicate doublePredicate) {
      return doubleStream.anyMatch(doublePredicate);
    }
  }

  /** Prefer {@link DoubleStream#allMatch(DoublePredicate)} over more contrived alternatives. */
  static final class DoubleStreamAllMatchWithDoublePredicate {
    @BeforeTemplate
    boolean before(DoubleStream doubleStream, DoublePredicate doublePredicate) {
      return doubleStream.noneMatch(doublePredicate.negate());
    }

    @AfterTemplate
    boolean after(DoubleStream doubleStream, DoublePredicate doublePredicate) {
      return doubleStream.allMatch(doublePredicate);
    }
  }

  /** Prefer {@link DoubleStream#allMatch(DoublePredicate)} over less explicit alternatives. */
  abstract static class DoubleStreamAllMatch {
    @Placeholder
    abstract boolean test(@MayOptionallyUse double element);

    @BeforeTemplate
    boolean before(DoubleStream doubleStream) {
      return doubleStream.noneMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(DoubleStream doubleStream) {
      return doubleStream.allMatch(e -> test(e));
    }
  }

  /** Prefer {@link DoubleStream#takeWhile(DoublePredicate)} over more verbose alternatives. */
  static final class DoubleStreamTakeWhile {
    @BeforeTemplate
    DoubleStream before(DoubleStream doubleStream, DoublePredicate predicate) {
      return doubleStream.takeWhile(predicate).filter(predicate);
    }

    @AfterTemplate
    DoubleStream after(DoubleStream doubleStream, DoublePredicate predicate) {
      return doubleStream.takeWhile(predicate);
    }
  }
}
