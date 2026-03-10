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
    DoubleStream before(DoubleStream stream) {
      return Streams.concat(stream);
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    DoubleStream after(DoubleStream stream) {
      return stream;
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
    DoubleStream before(DoubleStream stream, DoublePredicate predicate) {
      return stream.flatMap(v -> toDoubleStreamFunction(v).filter(predicate));
    }

    @AfterTemplate
    DoubleStream after(DoubleStream stream, DoublePredicate predicate) {
      return stream.flatMap(v -> toDoubleStreamFunction(v)).filter(predicate);
    }
  }

  /** Prefer {@link DoubleStream#filter(DoublePredicate)} over more contrived alternatives. */
  abstract static class StreamFlatMapToDoubleFilter<T> {
    @Placeholder(allowsIdentity = true)
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    DoubleStream before(Stream<T> stream, DoublePredicate predicate) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v).filter(predicate));
    }

    @AfterTemplate
    DoubleStream after(Stream<T> stream, DoublePredicate predicate) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v)).filter(predicate);
    }
  }

  /** Prefer {@link DoubleStream#map(DoubleUnaryOperator)} over more contrived alternatives. */
  abstract static class DoubleStreamFlatMapMap {
    @Placeholder
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse double element);

    @BeforeTemplate
    DoubleStream before(DoubleStream stream, DoubleUnaryOperator unaryOperator) {
      return stream.flatMap(v -> toDoubleStreamFunction(v).map(unaryOperator));
    }

    @AfterTemplate
    DoubleStream after(DoubleStream stream, DoubleUnaryOperator unaryOperator) {
      return stream.flatMap(v -> toDoubleStreamFunction(v)).map(unaryOperator);
    }
  }

  /** Prefer {@link DoubleStream#map(DoubleUnaryOperator)} over more contrived alternatives. */
  abstract static class StreamFlatMapToDoubleMap<T> {
    @Placeholder(allowsIdentity = true)
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    DoubleStream before(Stream<T> stream, DoubleUnaryOperator unaryOperator) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v).map(unaryOperator));
    }

    @AfterTemplate
    DoubleStream after(Stream<T> stream, DoubleUnaryOperator unaryOperator) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v)).map(unaryOperator);
    }
  }

  /** Prefer {@link DoubleStream#flatMap(DoubleFunction)} over more contrived alternatives. */
  abstract static class DoubleStreamFlatMapFlatMap<S extends DoubleStream> {
    @Placeholder
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse double element);

    @BeforeTemplate
    DoubleStream before(DoubleStream stream, DoubleFunction<S> function) {
      return stream.flatMap(v -> toDoubleStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    DoubleStream after(DoubleStream stream, DoubleFunction<S> function) {
      return stream.flatMap(v -> toDoubleStreamFunction(v)).flatMap(function);
    }
  }

  /** Prefer {@link DoubleStream#flatMap(DoubleFunction)} over more contrived alternatives. */
  abstract static class StreamFlatMapToDoubleFlatMap<T, S extends DoubleStream> {
    @Placeholder(allowsIdentity = true)
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    DoubleStream before(Stream<T> stream, DoubleFunction<S> function) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    DoubleStream after(Stream<T> stream, DoubleFunction<S> function) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v)).flatMap(function);
    }
  }

  /**
   * Prefer {@link DoubleStream#filter(DoublePredicate)} before {@link DoubleStream#sorted()} over
   * less efficient alternatives.
   */
  static final class DoubleStreamFilterSorted {
    @BeforeTemplate
    DoubleStream before(DoubleStream stream, DoublePredicate predicate) {
      return stream.sorted().filter(predicate);
    }

    @AfterTemplate
    DoubleStream after(DoubleStream stream, DoublePredicate predicate) {
      return stream.filter(predicate).sorted();
    }
  }

  /** Prefer {@link DoubleStream#findAny()} over less efficient alternatives. */
  static final class DoubleStreamFindAnyIsEmpty {
    @BeforeTemplate
    boolean before(DoubleStream stream) {
      return Refaster.anyOf(
          stream.count() == 0,
          stream.count() <= 0,
          stream.count() < 1,
          stream.findFirst().isEmpty());
    }

    @AfterTemplate
    boolean after(DoubleStream stream) {
      return stream.findAny().isEmpty();
    }
  }

  /** Prefer {@link DoubleStream#findAny()} over less efficient alternatives. */
  static final class DoubleStreamFindAnyIsPresent {
    @BeforeTemplate
    boolean before(DoubleStream stream) {
      return Refaster.anyOf(
          stream.count() != 0,
          stream.count() > 0,
          stream.count() >= 1,
          stream.findFirst().isPresent());
    }

    @AfterTemplate
    boolean after(DoubleStream stream) {
      return stream.findAny().isPresent();
    }
  }

  /** Prefer {@link DoubleStream#min()} over less efficient alternatives. */
  static final class DoubleStreamMin {
    @BeforeTemplate
    OptionalDouble before(DoubleStream stream) {
      return stream.sorted().findFirst();
    }

    @AfterTemplate
    OptionalDouble after(DoubleStream stream) {
      return stream.min();
    }
  }

  /** Prefer {@link DoubleStream#noneMatch(DoublePredicate)} over more contrived alternatives. */
  static final class DoubleStreamNoneMatchWithDoublePredicate {
    @BeforeTemplate
    boolean before(DoubleStream stream, DoublePredicate predicate) {
      return Refaster.anyOf(
          !stream.anyMatch(predicate),
          stream.allMatch(predicate.negate()),
          stream.filter(predicate).findAny().isEmpty());
    }

    @AfterTemplate
    boolean after(DoubleStream stream, DoublePredicate predicate) {
      return stream.noneMatch(predicate);
    }
  }

  /** Prefer {@link DoubleStream#noneMatch(DoublePredicate)} over less explicit alternatives. */
  abstract static class DoubleStreamNoneMatch {
    @Placeholder
    abstract boolean test(@MayOptionallyUse double element);

    @BeforeTemplate
    boolean before(DoubleStream stream) {
      return stream.allMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(DoubleStream stream) {
      return stream.noneMatch(e -> test(e));
    }
  }

  /** Prefer {@link DoubleStream#anyMatch(DoublePredicate)} over more contrived alternatives. */
  static final class DoubleStreamAnyMatch {
    @BeforeTemplate
    @SuppressWarnings("java:S4034" /* This violation will be rewritten. */)
    boolean before(DoubleStream stream, DoublePredicate predicate) {
      return Refaster.anyOf(
          !stream.noneMatch(predicate), stream.filter(predicate).findAny().isPresent());
    }

    @AfterTemplate
    boolean after(DoubleStream stream, DoublePredicate predicate) {
      return stream.anyMatch(predicate);
    }
  }

  /** Prefer {@link DoubleStream#allMatch(DoublePredicate)} over more contrived alternatives. */
  static final class DoubleStreamAllMatchWithDoublePredicate {
    @BeforeTemplate
    boolean before(DoubleStream stream, DoublePredicate predicate) {
      return stream.noneMatch(predicate.negate());
    }

    @AfterTemplate
    boolean after(DoubleStream stream, DoublePredicate predicate) {
      return stream.allMatch(predicate);
    }
  }

  /** Prefer {@link DoubleStream#allMatch(DoublePredicate)} over less explicit alternatives. */
  abstract static class DoubleStreamAllMatch {
    @Placeholder
    abstract boolean test(@MayOptionallyUse double element);

    @BeforeTemplate
    boolean before(DoubleStream stream) {
      return stream.noneMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(DoubleStream stream) {
      return stream.allMatch(e -> test(e));
    }
  }

  /** Prefer {@link DoubleStream#takeWhile(DoublePredicate)} over more verbose alternatives. */
  static final class DoubleStreamTakeWhile {
    @BeforeTemplate
    DoubleStream before(DoubleStream stream, DoublePredicate predicate) {
      return stream.takeWhile(predicate).filter(predicate);
    }

    @AfterTemplate
    DoubleStream after(DoubleStream stream, DoublePredicate predicate) {
      return stream.takeWhile(predicate);
    }
  }
}
