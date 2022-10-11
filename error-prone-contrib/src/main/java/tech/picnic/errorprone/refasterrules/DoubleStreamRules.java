package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.Streams;
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

/** Refaster rules related to expressions dealing with {@link DoubleStream}s. */
final class DoubleStreamRules {
  private DoubleStreamRules() {}

  /** Don't unnecessarily call {@link Streams#concat(DoubleStream...)}. */
  static final class ConcatOneDoubleStream {
    @BeforeTemplate
    DoubleStream before(DoubleStream stream) {
      return Streams.concat(stream);
    }

    @AfterTemplate
    DoubleStream after(DoubleStream stream) {
      return stream;
    }
  }

  /** Prefer {@link DoubleStream#concat(DoubleStream, DoubleStream)} over the Guava alternative. */
  static final class ConcatTwoDoubleStreams {
    @BeforeTemplate
    DoubleStream before(DoubleStream s1, DoubleStream s2) {
      return Streams.concat(s1, s2);
    }

    @AfterTemplate
    DoubleStream after(DoubleStream s1, DoubleStream s2) {
      return DoubleStream.concat(s1, s2);
    }
  }

  /** Avoid unnecessary nesting of {@link DoubleStream#filter(DoublePredicate)} operations. */
  abstract static class FilterOuterDoubleStreamAfterFlatMap {
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

  /** Avoid unnecessary nesting of {@link DoubleStream#filter(DoublePredicate)} operations. */
  abstract static class FilterOuterStreamAfterFlatMapToDouble<T> {
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

  /** Avoid unnecessary nesting of {@link DoubleStream#map(DoubleUnaryOperator)} operations. */
  abstract static class MapOuterDoubleStreamAfterFlatMap {
    @Placeholder
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse double element);

    @BeforeTemplate
    DoubleStream before(DoubleStream stream, DoubleUnaryOperator function) {
      return stream.flatMap(v -> toDoubleStreamFunction(v).map(function));
    }

    @AfterTemplate
    DoubleStream after(DoubleStream stream, DoubleUnaryOperator function) {
      return stream.flatMap(v -> toDoubleStreamFunction(v)).map(function);
    }
  }

  /** Avoid unnecessary nesting of {@link DoubleStream#map(DoubleUnaryOperator)} operations. */
  abstract static class MapOuterStreamAfterFlatMapToDouble<T> {
    @Placeholder(allowsIdentity = true)
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    DoubleStream before(Stream<T> stream, DoubleUnaryOperator function) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v).map(function));
    }

    @AfterTemplate
    DoubleStream after(Stream<T> stream, DoubleUnaryOperator function) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v)).map(function);
    }
  }

  /** Avoid unnecessary nesting of {@link DoubleStream#flatMap(DoubleFunction)} operations. */
  abstract static class FlatMapOuterDoubleStreamAfterFlatMap {
    @Placeholder
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse double element);

    @BeforeTemplate
    DoubleStream before(DoubleStream stream, DoubleFunction<? extends DoubleStream> function) {
      return stream.flatMap(v -> toDoubleStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    DoubleStream after(DoubleStream stream, DoubleFunction<? extends DoubleStream> function) {
      return stream.flatMap(v -> toDoubleStreamFunction(v)).flatMap(function);
    }
  }

  /** Avoid unnecessary nesting of {@link DoubleStream#flatMap(DoubleFunction)} operations. */
  abstract static class FlatMapOuterStreamAfterFlatMapToDouble<T> {
    @Placeholder(allowsIdentity = true)
    abstract DoubleStream toDoubleStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    DoubleStream before(Stream<T> stream, DoubleFunction<? extends DoubleStream> function) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    DoubleStream after(Stream<T> stream, DoubleFunction<? extends DoubleStream> function) {
      return stream.flatMapToDouble(v -> toDoubleStreamFunction(v)).flatMap(function);
    }
  }

  /** In order to test whether a stream has any element, simply try to find one. */
  static final class DoubleStreamIsEmpty {
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

  /** In order to test whether a stream has any element, simply try to find one. */
  static final class DoubleStreamIsNotEmpty {
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
  static final class DoubleStreamNoneMatch {
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

  abstract static class DoubleStreamNoneMatch2 {
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
    boolean before(DoubleStream stream, DoublePredicate predicate) {
      return Refaster.anyOf(
          !stream.noneMatch(predicate), stream.filter(predicate).findAny().isPresent());
    }

    @AfterTemplate
    boolean after(DoubleStream stream, DoublePredicate predicate) {
      return stream.anyMatch(predicate);
    }
  }

  static final class DoubleStreamAllMatch {
    @BeforeTemplate
    boolean before(DoubleStream stream, DoublePredicate predicate) {
      return stream.noneMatch(predicate.negate());
    }

    @AfterTemplate
    boolean after(DoubleStream stream, DoublePredicate predicate) {
      return stream.allMatch(predicate);
    }
  }

  abstract static class DoubleStreamAllMatch2 {
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
}
