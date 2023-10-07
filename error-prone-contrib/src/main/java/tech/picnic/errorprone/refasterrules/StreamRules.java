package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.minBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.summarizingDouble;
import static java.util.stream.Collectors.summarizingInt;
import static java.util.stream.Collectors.summarizingLong;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.summingLong;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Matches;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.NotMatches;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.matchers.IsLambdaExpressionOrMethodReference;
import tech.picnic.errorprone.refaster.matchers.IsRefasterAsVarargs;

/** Refaster rules related to expressions dealing with {@link Stream}s. */
@OnlineDocumentation
final class StreamRules {
  private StreamRules() {}

  /**
   * Prefer {@link Collectors#joining()} over {@link Collectors#joining(CharSequence)} with an empty
   * delimiter string.
   */
  static final class Joining {
    @BeforeTemplate
    Collector<CharSequence, ?, String> before() {
      return joining("");
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Collector<CharSequence, ?, String> after() {
      return joining();
    }
  }

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

  /**
   * Prefer {@link Arrays#stream(Object[])} over {@link Stream#of(Object[])}, as the former is
   * clearer.
   */
  static final class StreamOfArray<T> {
    @BeforeTemplate
    Stream<T> before(@NotMatches(IsRefasterAsVarargs.class) T[] array) {
      return Stream.of(array);
    }

    @AfterTemplate
    Stream<T> after(T[] array) {
      return Arrays.stream(array);
    }
  }

  /** Don't unnecessarily call {@link Streams#concat(Stream...)}. */
  static final class ConcatOneStream<T> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream) {
      return Streams.concat(stream);
    }

    @AfterTemplate
    @CanIgnoreReturnValue
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
   * Apply {@link Stream#filter(Predicate)} before {@link Stream#sorted()} to reduce the number of
   * elements to sort.
   */
  static final class StreamFilterSorted<T> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream, Predicate<? super T> predicate) {
      return stream.sorted().filter(predicate);
    }

    @AfterTemplate
    Stream<T> after(Stream<T> stream, Predicate<? super T> predicate) {
      return stream.filter(predicate).sorted();
    }
  }

  /**
   * Apply {@link Stream#filter(Predicate)} before {@link Stream#sorted(Comparator)} to reduce the
   * number of elements to sort.
   */
  static final class StreamFilterSortedWithComparator<T> {
    @BeforeTemplate
    Stream<T> before(
        Stream<T> stream, Predicate<? super T> predicate, Comparator<? super T> comparator) {
      return stream.sorted(comparator).filter(predicate);
    }

    @AfterTemplate
    Stream<T> after(
        Stream<T> stream, Predicate<? super T> predicate, Comparator<? super T> comparator) {
      return stream.filter(predicate).sorted(comparator);
    }
  }

  /**
   * Where possible, clarify that a mapping operation will be applied only to a single stream
   * element.
   */
  // XXX: Implement a similar rule for `.findAny()`. For parallel streams this wouldn't be quite the
  // same, so such a rule requires a `Matcher` that heuristically identifies `Stream` expressions
  // with deterministic order.
  // XXX: This change is not equivalent for `null`-returning functions, as the original code throws
  // an NPE if the first element is `null`, while the latter yields an empty `Optional`.
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
  // XXX: This rule assumes that any matched `Collector` does not perform any filtering.
  // (Perhaps we could add a `@Matches` guard that validates that the collector expression does not
  // contain a `Collectors#filtering` call. That'd still not be 100% accurate, though.)
  static final class StreamIsEmpty<T> {
    @BeforeTemplate
    boolean before(Stream<T> stream, Collector<? super T, ?, ? extends Collection<?>> collector) {
      return Refaster.anyOf(
          stream.count() == 0,
          stream.count() <= 0,
          stream.count() < 1,
          stream.findFirst().isEmpty(),
          stream.collect(collector).isEmpty());
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

  static final class StreamMin<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    Optional<T> before(Stream<T> stream, Comparator<? super T> comparator) {
      return Refaster.anyOf(
          stream.max(comparator.reversed()),
          stream.sorted(comparator).findFirst(),
          stream.collect(minBy(comparator)));
    }

    @AfterTemplate
    Optional<T> after(Stream<T> stream, Comparator<? super T> comparator) {
      return stream.min(comparator);
    }
  }

  static final class StreamMinNaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    Optional<T> before(Stream<T> stream) {
      return Refaster.anyOf(stream.max(reverseOrder()), stream.sorted().findFirst());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Optional<T> after(Stream<T> stream) {
      return stream.min(naturalOrder());
    }
  }

  static final class StreamMax<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    Optional<T> before(Stream<T> stream, Comparator<? super T> comparator) {
      return Refaster.anyOf(
          stream.min(comparator.reversed()),
          Streams.findLast(stream.sorted(comparator)),
          stream.collect(maxBy(comparator)));
    }

    @AfterTemplate
    Optional<T> after(Stream<T> stream, Comparator<? super T> comparator) {
      return stream.max(comparator);
    }
  }

  static final class StreamMaxNaturalOrder<T extends Comparable<? super T>> {
    @BeforeTemplate
    Optional<T> before(Stream<T> stream) {
      return Refaster.anyOf(stream.min(reverseOrder()), Streams.findLast(stream.sorted()));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Optional<T> after(Stream<T> stream) {
      return stream.max(naturalOrder());
    }
  }

  /** Prefer {@link Stream#noneMatch(Predicate)} over more contrived alternatives. */
  static final class StreamNoneMatch<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4034" /* This violation will be rewritten. */)
    boolean before(Stream<T> stream, Predicate<? super T> predicate) {
      return Refaster.anyOf(
          !stream.anyMatch(predicate),
          stream.allMatch(Refaster.anyOf(not(predicate), predicate.negate())),
          stream.filter(predicate).findAny().isEmpty());
    }

    @BeforeTemplate
    boolean before2(
        Stream<T> stream,
        @Matches(IsLambdaExpressionOrMethodReference.class)
            Function<? super T, Boolean> predicate) {
      return stream.map(predicate).noneMatch(Refaster.anyOf(Boolean::booleanValue, b -> b));
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
    @SuppressWarnings("java:S4034" /* This violation will be rewritten. */)
    boolean before(Stream<T> stream, Predicate<? super T> predicate) {
      return Refaster.anyOf(
          !stream.noneMatch(predicate), stream.filter(predicate).findAny().isPresent());
    }

    @BeforeTemplate
    boolean before2(
        Stream<T> stream,
        @Matches(IsLambdaExpressionOrMethodReference.class)
            Function<? super T, Boolean> predicate) {
      return stream.map(predicate).anyMatch(Refaster.anyOf(Boolean::booleanValue, b -> b));
    }

    @AfterTemplate
    boolean after(Stream<T> stream, Predicate<? super T> predicate) {
      return stream.anyMatch(predicate);
    }
  }

  static final class StreamAllMatch<T> {
    @BeforeTemplate
    boolean before(Stream<T> stream, Predicate<? super T> predicate) {
      return stream.noneMatch(Refaster.anyOf(not(predicate), predicate.negate()));
    }

    @BeforeTemplate
    boolean before2(
        Stream<T> stream,
        @Matches(IsLambdaExpressionOrMethodReference.class)
            Function<? super T, Boolean> predicate) {
      return stream.map(predicate).allMatch(Refaster.anyOf(Boolean::booleanValue, b -> b));
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
      return stream.noneMatch(e -> !test(e));
    }

    @AfterTemplate
    boolean after(Stream<T> stream) {
      return stream.allMatch(e -> test(e));
    }
  }

  static final class StreamMapToIntSum<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    long before(Stream<T> stream, ToIntFunction<T> mapper) {
      return stream.collect(summingInt(mapper));
    }

    @BeforeTemplate
    int before2(
        Stream<T> stream,
        @Matches(IsLambdaExpressionOrMethodReference.class) Function<? super T, Integer> mapper) {
      return stream.map(mapper).reduce(0, Integer::sum);
    }

    @AfterTemplate
    int after(Stream<T> stream, ToIntFunction<T> mapper) {
      return stream.mapToInt(mapper).sum();
    }
  }

  static final class StreamMapToDoubleSum<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    double before(Stream<T> stream, ToDoubleFunction<T> mapper) {
      return stream.collect(summingDouble(mapper));
    }

    @BeforeTemplate
    double before2(
        Stream<T> stream,
        @Matches(IsLambdaExpressionOrMethodReference.class) Function<? super T, Double> mapper) {
      return stream.map(mapper).reduce(0.0, Double::sum);
    }

    @AfterTemplate
    double after(Stream<T> stream, ToDoubleFunction<T> mapper) {
      return stream.mapToDouble(mapper).sum();
    }
  }

  static final class StreamMapToLongSum<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    long before(Stream<T> stream, ToLongFunction<T> mapper) {
      return stream.collect(summingLong(mapper));
    }

    @BeforeTemplate
    long before2(
        Stream<T> stream,
        @Matches(IsLambdaExpressionOrMethodReference.class) Function<? super T, Long> mapper) {
      return stream.map(mapper).reduce(0L, Long::sum);
    }

    @AfterTemplate
    long after(Stream<T> stream, ToLongFunction<T> mapper) {
      return stream.mapToLong(mapper).sum();
    }
  }

  static final class StreamMapToIntSummaryStatistics<T> {
    @BeforeTemplate
    IntSummaryStatistics before(Stream<T> stream, ToIntFunction<T> mapper) {
      return stream.collect(summarizingInt(mapper));
    }

    @AfterTemplate
    IntSummaryStatistics after(Stream<T> stream, ToIntFunction<T> mapper) {
      return stream.mapToInt(mapper).summaryStatistics();
    }
  }

  static final class StreamMapToDoubleSummaryStatistics<T> {
    @BeforeTemplate
    DoubleSummaryStatistics before(Stream<T> stream, ToDoubleFunction<T> mapper) {
      return stream.collect(summarizingDouble(mapper));
    }

    @AfterTemplate
    DoubleSummaryStatistics after(Stream<T> stream, ToDoubleFunction<T> mapper) {
      return stream.mapToDouble(mapper).summaryStatistics();
    }
  }

  static final class StreamMapToLongSummaryStatistics<T> {
    @BeforeTemplate
    LongSummaryStatistics before(Stream<T> stream, ToLongFunction<T> mapper) {
      return stream.collect(summarizingLong(mapper));
    }

    @AfterTemplate
    LongSummaryStatistics after(Stream<T> stream, ToLongFunction<T> mapper) {
      return stream.mapToLong(mapper).summaryStatistics();
    }
  }

  static final class StreamCount<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    long before(Stream<T> stream) {
      return stream.collect(counting());
    }

    @AfterTemplate
    long after(Stream<T> stream) {
      return stream.count();
    }
  }

  static final class StreamReduce<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    Optional<T> before(Stream<T> stream, BinaryOperator<T> accumulator) {
      return stream.collect(reducing(accumulator));
    }

    @AfterTemplate
    Optional<T> after(Stream<T> stream, BinaryOperator<T> accumulator) {
      return stream.reduce(accumulator);
    }
  }

  static final class StreamReduceWithIdentity<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    T before(Stream<T> stream, T identity, BinaryOperator<T> accumulator) {
      return stream.collect(reducing(identity, accumulator));
    }

    @AfterTemplate
    T after(Stream<T> stream, T identity, BinaryOperator<T> accumulator) {
      return stream.reduce(identity, accumulator);
    }
  }

  static final class StreamFilterCollect<T, R> {
    @BeforeTemplate
    R before(
        Stream<T> stream, Predicate<? super T> predicate, Collector<? super T, ?, R> collector) {
      return stream.collect(filtering(predicate, collector));
    }

    @AfterTemplate
    R after(
        Stream<T> stream, Predicate<? super T> predicate, Collector<? super T, ?, R> collector) {
      return stream.filter(predicate).collect(collector);
    }
  }

  static final class StreamMapCollect<T, U, R> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    R before(
        Stream<T> stream,
        Function<? super T, ? extends U> mapper,
        Collector<? super U, ?, R> collector) {
      return stream.collect(mapping(mapper, collector));
    }

    @AfterTemplate
    R after(
        Stream<T> stream,
        Function<? super T, ? extends U> mapper,
        Collector<? super U, ?, R> collector) {
      return stream.map(mapper).collect(collector);
    }
  }

  static final class StreamFlatMapCollect<T, U, R> {
    @BeforeTemplate
    R before(
        Stream<T> stream,
        Function<? super T, ? extends Stream<? extends U>> mapper,
        Collector<? super U, ?, R> collector) {
      return stream.collect(flatMapping(mapper, collector));
    }

    @AfterTemplate
    R after(
        Stream<T> stream,
        Function<? super T, ? extends Stream<? extends U>> mapper,
        Collector<? super U, ?, R> collector) {
      return stream.flatMap(mapper).collect(collector);
    }
  }

  static final class StreamsConcat<T> {
    @BeforeTemplate
    Stream<T> before(@Repeated Stream<T> stream) {
      return Stream.of(Refaster.asVarargs(stream)).flatMap(Refaster.anyOf(identity(), s -> s));
    }

    @AfterTemplate
    Stream<T> after(@Repeated Stream<T> stream) {
      return Streams.concat(Refaster.asVarargs(stream));
    }
  }

  static final class StreamTakeWhile<T> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream, Predicate<? super T> predicate) {
      return stream.takeWhile(predicate).filter(predicate);
    }

    @AfterTemplate
    Stream<T> after(Stream<T> stream, Predicate<? super T> predicate) {
      return stream.takeWhile(predicate);
    }
  }

  /**
   * Prefer {@link Stream#iterate(Object, Predicate, UnaryOperator)} over more contrived
   * alternatives.
   */
  static final class StreamIterate<T> {
    @BeforeTemplate
    Stream<T> before(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
      return Stream.iterate(seed, next).takeWhile(hasNext);
    }

    @AfterTemplate
    Stream<T> after(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
      return Stream.iterate(seed, hasNext, next);
    }
  }

  /** Prefer {@link Stream#of(Object)} over more contrived alternatives. */
  // XXX: Generalize this and similar rules using an Error Prone check.
  static final class StreamOf1<T> {
    @BeforeTemplate
    Stream<T> before(T e1) {
      return ImmutableList.of(e1).stream();
    }

    @AfterTemplate
    Stream<T> after(T e1) {
      return Stream.of(e1);
    }
  }

  /** Prefer {@link Stream#of(Object[])} over more contrived alternatives. */
  // XXX: Generalize this and similar rules using an Error Prone check.
  static final class StreamOf2<T> {
    @BeforeTemplate
    Stream<T> before(T e1, T e2) {
      return ImmutableList.of(e1, e2).stream();
    }

    @AfterTemplate
    Stream<T> after(T e1, T e2) {
      return Stream.of(e1, e2);
    }
  }

  /** Prefer {@link Stream#of(Object[])} over more contrived alternatives. */
  // XXX: Generalize this and similar rules using an Error Prone check.
  static final class StreamOf3<T> {
    @BeforeTemplate
    Stream<T> before(T e1, T e2, T e3) {
      return ImmutableList.of(e1, e2, e3).stream();
    }

    @AfterTemplate
    Stream<T> after(T e1, T e2, T e3) {
      return Stream.of(e1, e2, e3);
    }
  }

  /** Prefer {@link Stream#of(Object[])} over more contrived alternatives. */
  // XXX: Generalize this and similar rules using an Error Prone check.
  static final class StreamOf4<T> {
    @BeforeTemplate
    Stream<T> before(T e1, T e2, T e3, T e4) {
      return ImmutableList.of(e1, e2, e3, e4).stream();
    }

    @AfterTemplate
    Stream<T> after(T e1, T e2, T e3, T e4) {
      return Stream.of(e1, e2, e3, e4);
    }
  }

  /** Prefer {@link Stream#of(Object[])} over more contrived alternatives. */
  // XXX: Generalize this and similar rules using an Error Prone check.
  static final class StreamOf5<T> {
    @BeforeTemplate
    Stream<T> before(T e1, T e2, T e3, T e4, T e5) {
      return ImmutableList.of(e1, e2, e3, e4, e5).stream();
    }

    @AfterTemplate
    Stream<T> after(T e1, T e2, T e3, T e4, T e5) {
      return Stream.of(e1, e2, e3, e4, e5);
    }
  }
}
