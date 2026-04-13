package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.Comparators.least;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.collectingAndThen;
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

import com.google.common.collect.Comparators;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.LongSummaryStatistics;
import java.util.Map;
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
import java.util.stream.StreamSupport;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;
import tech.picnic.errorprone.refaster.matchers.IsEmpty;
import tech.picnic.errorprone.refaster.matchers.IsIdentityOperation;
import tech.picnic.errorprone.refaster.matchers.IsLambdaExpressionOrMethodReference;
import tech.picnic.errorprone.refaster.matchers.IsRefasterAsVarargs;
import tech.picnic.errorprone.refaster.matchers.RequiresComputation;

/** Refaster rules related to expressions dealing with {@link Stream}s. */
@OnlineDocumentation
final class StreamRules {
  private StreamRules() {}

  /** Prefer {@link Collectors#joining()} over more verbose alternatives. */
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

  /** Prefer {@link Stream#empty()} over less explicit alternatives. */
  // XXX: We can additionally introduce a rule that maps `OptionalInt.empty().stream()` to
  // `IntStream.empty()`, and likewise for `OptionalLong` and `OptionalDouble`, but those
  // expressions are highly unlikely to be seen in the wild.
  static final class StreamEmpty<T> {
    @BeforeTemplate
    Stream<T> before(
        @Matches(IsEmpty.class) Collection<T> collection,
        @Matches(IsEmpty.class) Iterable<T> iterable,
        @Matches(IsEmpty.class) Iterator<T> iterator,
        @Matches(IsEmpty.class) T[] array) {
      return Refaster.anyOf(
          Stream.of(),
          Optional.<T>empty().stream(),
          collection.stream(),
          Streams.stream(iterable),
          Streams.stream(iterator),
          Arrays.stream(array));
    }

    @AfterTemplate
    Stream<T> after() {
      return Stream.empty();
    }
  }

  /** Prefer {@link Stream#ofNullable(Object)} over more contrived alternatives. */
  static final class StreamOfNullable<T extends @Nullable Object> {
    // XXX: Drop the `java:S2583` violation suppression once SonarCloud better supports JSpecify
    // annotations.
    @BeforeTemplate
    @SuppressWarnings(
        "java:S2583" /* SonarCloud incorrectly believes that `object` is not `@Nullable`. */)
    Stream<T> before(T t) {
      return Refaster.anyOf(
          Stream.of(t).filter(Objects::nonNull),
          Optional.ofNullable(t).stream(),
          t != null ? Stream.of(t) : Stream.empty(),
          t == null ? Stream.empty() : Stream.of(t));
    }

    @AfterTemplate
    Stream<T> after(T t) {
      return Stream.ofNullable(t);
    }
  }

  /** Prefer {@link Arrays#stream(Object[])} over less explicit alternatives. */
  static final class ArraysStream<T> {
    @BeforeTemplate
    Stream<T> before(@NotMatches(IsRefasterAsVarargs.class) T[] array) {
      return Stream.of(array);
    }

    @AfterTemplate
    Stream<T> after(T[] array) {
      return Arrays.stream(array);
    }
  }

  /** Prefer the {@link Stream} as-is over more contrived alternatives. */
  static final class StreamIdentity<T> {
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

  /** Prefer {@link Stream#concat(Stream, Stream)} over non-JDK alternatives. */
  static final class StreamConcat<T> {
    @BeforeTemplate
    Stream<T> before(Stream<T> a, Stream<T> b) {
      return Streams.concat(a, b);
    }

    @AfterTemplate
    Stream<T> after(Stream<T> a, Stream<T> b) {
      return Stream.concat(a, b);
    }
  }

  /** Prefer {@link Stream#filter(Predicate)} over more contrived alternatives. */
  abstract static class StreamFlatMapFilter<T, S2, S extends S2> {
    @Placeholder
    abstract Stream<S> toStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Stream<S> before(Stream<T> stream, Predicate<S2> predicate) {
      return stream.flatMap(v -> toStreamFunction(v).filter(predicate));
    }

    @AfterTemplate
    Stream<S> after(Stream<T> stream, Predicate<S2> predicate) {
      return stream.flatMap(v -> toStreamFunction(v)).filter(predicate);
    }
  }

  /** Prefer {@link Stream#map(Function)} over more contrived alternatives. */
  abstract static class StreamFlatMapMap<T, S2, S extends S2, R> {
    @Placeholder
    abstract Stream<S> toStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Stream<R> before(Stream<T> stream, Function<S2, R> function) {
      return stream.flatMap(v -> toStreamFunction(v).map(function));
    }

    @AfterTemplate
    Stream<R> after(Stream<T> stream, Function<S2, R> function) {
      return stream.flatMap(v -> toStreamFunction(v)).map(function);
    }
  }

  /** Prefer {@link Stream#flatMap(Function)} over more contrived alternatives. */
  abstract static class StreamFlatMapFlatMap<T, S2, S extends S2, R> {
    @Placeholder
    abstract Stream<S> toStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Stream<R> before(Stream<T> stream, Function<S2, ? extends Stream<? extends R>> function) {
      return stream.flatMap(v -> toStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    Stream<R> after(Stream<T> stream, Function<S2, ? extends Stream<? extends R>> function) {
      return stream.flatMap(v -> toStreamFunction(v)).flatMap(function);
    }
  }

  /** Prefer {@link Stream#sorted()} over more verbose alternatives. */
  static final class StreamSorted<T extends Comparable<? super T>> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream) {
      return stream.sorted(naturalOrder());
    }

    @AfterTemplate
    Stream<T> after(Stream<T> stream) {
      return stream.sorted();
    }
  }

  /**
   * Prefer {@link Stream#filter(Predicate)} before {@link Stream#sorted()} over less efficient
   * alternatives.
   */
  static final class StreamFilterSorted<S, T extends S> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream, Predicate<S> predicate) {
      return stream.sorted().filter(predicate);
    }

    @AfterTemplate
    Stream<T> after(Stream<T> stream, Predicate<S> predicate) {
      return stream.filter(predicate).sorted();
    }
  }

  /**
   * Prefer {@link Stream#filter(Predicate)} before {@link Stream#sorted(Comparator)} over less
   * efficient alternatives.
   */
  static final class StreamFilterSortedWithComparator<S, T extends S> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream, Predicate<S> predicate, Comparator<S> cmp) {
      return stream.sorted(cmp).filter(predicate);
    }

    @AfterTemplate
    Stream<T> after(Stream<T> stream, Predicate<S> predicate, Comparator<S> cmp) {
      return stream.filter(predicate).sorted(cmp);
    }
  }

  /**
   * Prefer {@link Stream#distinct()} before {@link Stream#sorted()} over less efficient
   * alternatives.
   */
  static final class StreamDistinctSorted<T> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream) {
      return stream.sorted().distinct();
    }

    @AfterTemplate
    Stream<T> after(Stream<T> stream) {
      return stream.distinct().sorted();
    }
  }

  /**
   * Prefer {@link Stream#distinct()} before {@link Stream#sorted(Comparator)} over less efficient
   * alternatives.
   */
  static final class StreamDistinctSortedWithComparator<S, T extends S> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream, Comparator<S> cmp) {
      return stream.sorted(cmp).distinct();
    }

    @AfterTemplate
    Stream<T> after(Stream<T> stream, Comparator<S> cmp) {
      return stream.distinct().sorted(cmp);
    }
  }

  /** Prefer {@link Comparators#least(int, Comparator)} over less efficient alternatives. */
  // XXX: For ordered streams the replacement code is not equivalent to the original code, as the
  // latter uses a stable sort operation, while the former breaks ties arbitrarily.
  static final class StreamCollectLeastStream<S, T extends S> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream, int k, Comparator<S> comparator) {
      return stream.sorted(comparator).limit(k);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Stream<T> after(Stream<T> stream, int k, Comparator<S> comparator) {
      return stream.collect(least(k, comparator)).stream();
    }
  }

  /** Prefer {@link Comparators#least(int, Comparator)} over less efficient alternatives. */
  // XXX: For ordered streams the replacement code is not equivalent to the original code, as the
  // latter uses a stable sort operation, while the former breaks ties arbitrarily.
  static final class StreamCollectLeastNaturalOrderStream<T extends Comparable<? super T>> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream, int k) {
      return stream.sorted().limit(k);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Stream<T> after(Stream<T> stream, int k) {
      return stream.collect(least(k, naturalOrder())).stream();
    }
  }

  /** Prefer {@code stream.findFirst().map(function)} over less efficient alternatives. */
  // XXX: Implement a similar rule for `.findAny()`. For parallel streams this wouldn't be quite the
  // same, so such a rule requires a `Matcher` that heuristically identifies `Stream` expressions
  // with deterministic order.
  // XXX: This change is not equivalent for `null`-returning functions, as the original code throws
  // an NPE if the first element is `null`, while the latter yields an empty `Optional`.
  static final class StreamFindFirstMap<T2, T extends T2, S> {
    @BeforeTemplate
    Optional<S> before(Stream<T> stream, Function<T2, S> mapper) {
      return stream.map(mapper).findFirst();
    }

    @AfterTemplate
    Optional<S> after(Stream<T> stream, Function<T2, S> mapper) {
      return stream.findFirst().map(mapper);
    }
  }

  /** Prefer {@link Stream#findAny()} over less efficient alternatives. */
  // XXX: This rule assumes that any matched `Collector` does not perform any filtering.
  // (Perhaps we could add a `@Matches` guard that validates that the collector expression does not
  // contain a `Collectors#filtering` call. That'd still not be 100% accurate, though.)
  @PossibleSourceIncompatibility
  static final class StreamFindAnyIsEmpty<T, K, V, C extends Collection<K>, M extends Map<K, V>> {
    @BeforeTemplate
    Boolean before(Stream<T> stream, Collector<? super T, ?, ? extends C> downstream) {
      return Refaster.anyOf(
          stream.count() == 0,
          stream.count() <= 0,
          stream.count() < 1,
          stream.findFirst().isEmpty(),
          stream.collect(downstream).isEmpty(),
          stream.collect(collectingAndThen(downstream, C::isEmpty)));
    }

    @BeforeTemplate
    Boolean before2(Stream<T> stream, Collector<? super T, ?, ? extends M> downstream) {
      return stream.collect(collectingAndThen(downstream, M::isEmpty));
    }

    @AfterTemplate
    boolean after(Stream<T> stream) {
      return stream.findAny().isEmpty();
    }
  }

  /** Prefer {@link Stream#findAny()} over less efficient alternatives. */
  static final class StreamFindAnyIsPresent<T> {
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

  /** Prefer {@link Stream#findFirst()} over more contrived alternatives. */
  // XXX: By dropping `.limit(n)` for any `n`, this rule assumes that consuming the stream does not
  // have side-effects.
  static final class StreamFindFirst<T> {
    @BeforeTemplate
    Optional<T> before(Stream<T> stream, long l) {
      return Refaster.anyOf(stream.limit(l).findFirst(), stream.limit(l).findAny());
    }

    @AfterTemplate
    Optional<T> after(Stream<T> stream) {
      return stream.findFirst();
    }
  }

  /**
   * Prefer {@code stream.map(map::get).filter(Objects::nonNull)} over less efficient alternatives.
   */
  static final class StreamMapMapGetFilterObjectsNonNull<T, K, V> {
    @BeforeTemplate
    Stream<V> before(Stream<T> stream, Map<K, V> map) {
      return stream.filter(map::containsKey).map(map::get);
    }

    @AfterTemplate
    Stream<V> after(Stream<T> stream, Map<K, V> map) {
      return stream.map(map::get).filter(Objects::nonNull);
    }
  }

  /** Prefer {@link Stream#min(Comparator)} over less efficient alternatives. */
  static final class StreamMin<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    Optional<T> before(Stream<T> stream, Comparator<S> comparator) {
      return Refaster.anyOf(
          stream.max(comparator.reversed()),
          stream.sorted(comparator).findFirst(),
          stream.collect(minBy(comparator)));
    }

    @AfterTemplate
    Optional<T> after(Stream<T> stream, Comparator<S> comparator) {
      return stream.min(comparator);
    }
  }

  /** Prefer {@link Stream#min(Comparator)} over less efficient alternatives. */
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

  /** Prefer {@link Stream#max(Comparator)} over less efficient alternatives. */
  static final class StreamMax<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    Optional<T> before(Stream<T> stream, Comparator<S> comparator) {
      return Refaster.anyOf(
          stream.min(comparator.reversed()),
          Streams.findLast(stream.sorted(comparator)),
          stream.collect(maxBy(comparator)));
    }

    @AfterTemplate
    Optional<T> after(Stream<T> stream, Comparator<S> comparator) {
      return stream.max(comparator);
    }
  }

  /** Prefer {@link Stream#max(Comparator)} over less efficient alternatives. */
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
  static final class StreamNoneMatchWithPredicate<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("java:S4034" /* This violation will be rewritten. */)
    boolean before(Stream<T> stream, Predicate<S> target) {
      return Refaster.anyOf(
          !stream.anyMatch(target),
          stream.allMatch(Refaster.anyOf(not(target), target.negate())),
          stream.filter(target).findAny().isEmpty());
    }

    // XXX: Consider extending `@Matches(IsIdentityOperation.class)` such that it can replace this
    // template's `Refaster.anyOf` usage.
    @BeforeTemplate
    boolean before2(
        Stream<T> stream,
        @Matches(IsLambdaExpressionOrMethodReference.class) Function<S, Boolean> target) {
      return stream.map(target).noneMatch(Refaster.anyOf(Boolean::booleanValue, b -> b));
    }

    @AfterTemplate
    boolean after(Stream<T> stream, Predicate<S> target) {
      return stream.noneMatch(target);
    }
  }

  /** Prefer {@link Stream#noneMatch(Predicate)} over less explicit alternatives. */
  abstract static class StreamNoneMatch<T> {
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
  static final class StreamAnyMatch<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("java:S4034" /* This violation will be rewritten. */)
    boolean before(Stream<T> stream, Predicate<S> predicate) {
      return Refaster.anyOf(
          !stream.noneMatch(predicate), stream.filter(predicate).findAny().isPresent());
    }

    // XXX: Consider extending `@Matches(IsIdentityOperation.class)` such that it can replace this
    // template's `Refaster.anyOf` usage.
    @BeforeTemplate
    boolean before2(
        Stream<T> stream,
        @Matches(IsLambdaExpressionOrMethodReference.class) Function<S, Boolean> predicate) {
      return stream.map(predicate).anyMatch(Refaster.anyOf(Boolean::booleanValue, b -> b));
    }

    @AfterTemplate
    boolean after(Stream<T> stream, Predicate<S> predicate) {
      return stream.anyMatch(predicate);
    }
  }

  /** Prefer {@link Stream#allMatch(Predicate)} over more contrived alternatives. */
  static final class StreamAllMatchWithPredicate<S, T extends S> {
    @BeforeTemplate
    boolean before(Stream<T> stream, Predicate<S> target) {
      return stream.noneMatch(Refaster.anyOf(not(target), target.negate()));
    }

    // XXX: Consider extending `@Matches(IsIdentityOperation.class)` such that it can replace this
    // template's `Refaster.anyOf` usage.
    @BeforeTemplate
    boolean before2(
        Stream<T> stream,
        @Matches(IsLambdaExpressionOrMethodReference.class) Function<S, Boolean> target) {
      return stream.map(target).allMatch(Refaster.anyOf(Boolean::booleanValue, b -> b));
    }

    @AfterTemplate
    boolean after(Stream<T> stream, Predicate<S> target) {
      return stream.allMatch(target);
    }
  }

  /** Prefer {@link Stream#allMatch(Predicate)} over less explicit alternatives. */
  abstract static class StreamAllMatch<T> {
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

  /** Prefer {@code stream.mapToInt(mapper).sum()} over less efficient alternatives. */
  @PossibleSourceIncompatibility
  static final class StreamMapToIntSum<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    Integer before(Stream<T> stream, ToIntFunction<T> mapper) {
      return stream.collect(summingInt(mapper));
    }

    @BeforeTemplate
    Integer before2(
        Stream<T> stream,
        @Matches(IsLambdaExpressionOrMethodReference.class) Function<S, Integer> mapper) {
      return stream.map(mapper).reduce(0, Integer::sum);
    }

    @AfterTemplate
    int after(Stream<T> stream, ToIntFunction<T> mapper) {
      return stream.mapToInt(mapper).sum();
    }
  }

  /** Prefer {@code stream.mapToDouble(mapper).sum()} over less efficient alternatives. */
  @PossibleSourceIncompatibility
  static final class StreamMapToDoubleSum<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    Double before(Stream<T> stream, ToDoubleFunction<T> mapper) {
      return stream.collect(summingDouble(mapper));
    }

    @BeforeTemplate
    Double before2(
        Stream<T> stream,
        @Matches(IsLambdaExpressionOrMethodReference.class) Function<S, Double> mapper) {
      return stream.map(mapper).reduce(0.0, Double::sum);
    }

    @AfterTemplate
    double after(Stream<T> stream, ToDoubleFunction<T> mapper) {
      return stream.mapToDouble(mapper).sum();
    }
  }

  /** Prefer {@code stream.mapToLong(mapper).sum()} over less efficient alternatives. */
  @PossibleSourceIncompatibility
  static final class StreamMapToLongSum<S, T extends S> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    Long before(Stream<T> stream, ToLongFunction<T> mapper) {
      return stream.collect(summingLong(mapper));
    }

    @BeforeTemplate
    Long before2(
        Stream<T> stream,
        @Matches(IsLambdaExpressionOrMethodReference.class) Function<S, Long> mapper) {
      return stream.map(mapper).reduce(0L, Long::sum);
    }

    @AfterTemplate
    long after(Stream<T> stream, ToLongFunction<T> mapper) {
      return stream.mapToLong(mapper).sum();
    }
  }

  /**
   * Prefer {@code stream.mapToInt(mapper).summaryStatistics()} over less efficient alternatives.
   */
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

  /**
   * Prefer {@code stream.mapToDouble(mapper).summaryStatistics()} over less efficient alternatives.
   */
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

  /**
   * Prefer {@code stream.mapToLong(mapper).summaryStatistics()} over less efficient alternatives.
   */
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

  /** Prefer {@link Stream#count()} over less efficient alternatives. */
  @PossibleSourceIncompatibility
  static final class StreamCount<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    Long before(Stream<T> stream) {
      return stream.collect(counting());
    }

    @AfterTemplate
    long after(Stream<T> stream) {
      return stream.count();
    }
  }

  /** Prefer {@link Stream#reduce(BinaryOperator)} over less efficient alternatives. */
  static final class StreamReduce<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    Optional<T> before(Stream<T> stream, BinaryOperator<T> op) {
      return stream.collect(reducing(op));
    }

    @AfterTemplate
    Optional<T> after(Stream<T> stream, BinaryOperator<T> op) {
      return stream.reduce(op);
    }
  }

  /** Prefer {@link Stream#reduce(Object, BinaryOperator)} over less efficient alternatives. */
  static final class StreamReduceWithObject<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    T before(Stream<T> stream, T identity, BinaryOperator<T> op) {
      return stream.collect(reducing(identity, op));
    }

    @AfterTemplate
    T after(Stream<T> stream, T identity, BinaryOperator<T> op) {
      return stream.reduce(identity, op);
    }
  }

  /** Prefer {@link Stream#filter(Predicate)} over more contrived alternatives. */
  static final class StreamFilterCollect<S, T extends S, R> {
    @BeforeTemplate
    R before(Stream<T> stream, Predicate<S> predicate, Collector<S, ?, R> downstream) {
      return stream.collect(filtering(predicate, downstream));
    }

    @AfterTemplate
    R after(Stream<T> stream, Predicate<S> predicate, Collector<S, ?, R> downstream) {
      return stream.filter(predicate).collect(downstream);
    }
  }

  /** Prefer {@link Stream#map(Function)} over more contrived alternatives. */
  static final class StreamMapCollect<S, T extends S, U, R> {
    @BeforeTemplate
    @SuppressWarnings("java:S4266" /* This violation will be rewritten. */)
    R before(Stream<T> stream, Function<S, U> mapper, Collector<U, ?, R> downstream) {
      return stream.collect(mapping(mapper, downstream));
    }

    @AfterTemplate
    R after(Stream<T> stream, Function<S, U> mapper, Collector<U, ?, R> downstream) {
      return stream.map(mapper).collect(downstream);
    }
  }

  /** Prefer {@link Stream#flatMap(Function)} over more contrived alternatives. */
  static final class StreamFlatMapCollect<S, T extends S, U, R> {
    @BeforeTemplate
    R before(
        Stream<T> stream,
        Function<S, ? extends Stream<? extends U>> mapper,
        Collector<U, ?, R> downstream) {
      return stream.collect(flatMapping(mapper, downstream));
    }

    @AfterTemplate
    R after(
        Stream<T> stream,
        Function<S, ? extends Stream<? extends U>> mapper,
        Collector<U, ?, R> downstream) {
      return stream.flatMap(mapper).collect(downstream);
    }
  }

  /** Prefer {@link Streams#concat(Stream...)} over more contrived alternatives. */
  static final class StreamsConcat<T> {
    @BeforeTemplate
    Stream<T> before(
        @Repeated Stream<T> streams,
        @Matches(IsIdentityOperation.class)
            Function<? super Stream<T>, ? extends Stream<? extends T>> function) {
      return Stream.of(Refaster.asVarargs(streams)).flatMap(function);
    }

    @AfterTemplate
    Stream<T> after(@Repeated Stream<T> streams) {
      return Streams.concat(Refaster.asVarargs(streams));
    }
  }

  /** Prefer {@link Stream#takeWhile(Predicate)} over more verbose alternatives. */
  static final class StreamTakeWhile<S, T extends S> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream, Predicate<S> predicate) {
      return stream.takeWhile(predicate).filter(predicate);
    }

    @AfterTemplate
    Stream<T> after(Stream<T> stream, Predicate<S> predicate) {
      return stream.takeWhile(predicate);
    }
  }

  /**
   * Prefer {@link Stream#iterate(Object, Predicate, UnaryOperator)} over more contrived
   * alternatives.
   */
  static final class StreamIterate<S, T extends S> {
    @BeforeTemplate
    Stream<T> before(T seed, Predicate<S> hasNext, UnaryOperator<T> next) {
      return Stream.iterate(seed, next).takeWhile(hasNext);
    }

    @AfterTemplate
    Stream<T> after(T seed, Predicate<S> hasNext, UnaryOperator<T> next) {
      return Stream.iterate(seed, hasNext, next);
    }
  }

  /** Prefer {@link Stream#of(Object)} over more contrived alternatives. */
  // XXX: Generalize this and similar rules using an Error Prone check.
  static final class StreamOf1<T> {
    @BeforeTemplate
    Stream<T> before(T t) {
      return ImmutableList.of(t).stream();
    }

    @AfterTemplate
    Stream<T> after(T t) {
      return Stream.of(t);
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

  /** Prefer {@link Streams#stream(Iterable)} over more contrived alternatives. */
  static final class StreamsStream<T> {
    @BeforeTemplate
    Stream<T> before(Iterable<T> iterable) {
      return StreamSupport.stream(iterable.spliterator(), /* parallel= */ false);
    }

    @AfterTemplate
    Stream<T> after(Iterable<T> iterable) {
      return Streams.stream(iterable);
    }
  }

  /** Prefer {@link Collection#parallelStream()} over more contrived alternatives. */
  static final class CollectionParallelStream<T> {
    @BeforeTemplate
    Stream<T> before(Collection<T> collection) {
      return StreamSupport.stream(collection.spliterator(), /* parallel= */ true);
    }

    @AfterTemplate
    Stream<T> after(Collection<T> collection) {
      return collection.parallelStream();
    }
  }

  /** Prefer {@link Collections#nCopies(int, Object)} over more contrived alternatives. */
  static final class CollectionsNCopiesStream<T> {
    @BeforeTemplate
    Stream<T> before(int n, @NotMatches(RequiresComputation.class) T o) {
      return Stream.generate(() -> o).limit(n);
    }

    @AfterTemplate
    Stream<T> after(int n, T o) {
      return Collections.nCopies(n, o).stream();
    }
  }
}
