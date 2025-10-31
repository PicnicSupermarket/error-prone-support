package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.MoreCollectors.toOptional;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.minBy;
import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;
import static reactor.function.TupleUtils.function;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Matches;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.NotMatches;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import tech.picnic.errorprone.refaster.annotation.Description;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.matchers.IsEmpty;
import tech.picnic.errorprone.refaster.matchers.IsIdentityOperation;
import tech.picnic.errorprone.refaster.matchers.IsRefasterAsVarargs;
import tech.picnic.errorprone.refaster.matchers.ReturnsMono;
import tech.picnic.errorprone.refaster.matchers.ThrowsCheckedException;

/** Refaster rules related to Reactor expressions and statements. */
@OnlineDocumentation
final class ReactorRules {
  private ReactorRules() {}

  /**
   * Prefer {@link Mono#fromSupplier(Supplier)} over {@link Mono#fromCallable(Callable)} where
   * feasible.
   */
  static final class MonoFromSupplier<T> {
    @BeforeTemplate
    Mono<T> before(@NotMatches(ThrowsCheckedException.class) Callable<? extends T> supplier) {
      return Mono.fromCallable(supplier);
    }

    @AfterTemplate
    Mono<T> after(Supplier<? extends T> supplier) {
      return Mono.fromSupplier(supplier);
    }
  }

  /** Prefer {@link Mono#empty()} over more contrived alternatives. */
  static final class MonoEmpty<T> {
    @BeforeTemplate
    Mono<T> before() {
      return Refaster.anyOf(Mono.justOrEmpty(null), Mono.justOrEmpty(Optional.empty()));
    }

    @AfterTemplate
    Mono<T> after() {
      return Mono.empty();
    }
  }

  /**
   * Prefer {@link Mono#timeout(Duration, Mono)} over more contrived or less performant
   * alternatives.
   */
  static final class MonoTimeoutDurationMonoEmpty<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Duration duration) {
      return mono.timeout(duration).onErrorComplete(TimeoutException.class);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Duration duration) {
      return mono.timeout(duration, Mono.empty());
    }
  }

  /**
   * Prefer {@link Mono#timeout(Duration, Mono)} over more contrived or less performant
   * alternatives.
   */
  static final class MonoTimeoutDurationMonoJust<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Duration duration, T fallbackValue) {
      return mono.timeout(duration).onErrorReturn(TimeoutException.class, fallbackValue);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Duration duration, T fallbackValue) {
      return mono.timeout(duration, Mono.just(fallbackValue));
    }
  }

  /**
   * Prefer {@link Mono#timeout(Duration, Mono)} over more contrived or less performant
   * alternatives.
   */
  static final class MonoTimeoutDuration<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Duration duration, Mono<T> fallback) {
      return mono.timeout(duration).onErrorResume(TimeoutException.class, e -> fallback);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Duration duration, Mono<T> fallback) {
      return mono.timeout(duration, fallback);
    }
  }

  /**
   * Prefer {@link Mono#timeout(Publisher, Mono)} over more contrived or less performant
   * alternatives.
   */
  static final class MonoTimeoutPublisherMonoEmpty<T, S> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Publisher<S> other) {
      return mono.timeout(other).onErrorComplete(TimeoutException.class);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Publisher<S> other) {
      return mono.timeout(other, Mono.empty());
    }
  }

  /**
   * Prefer {@link Mono#timeout(Publisher, Mono)} over more contrived or less performant
   * alternatives.
   */
  static final class MonoTimeoutPublisherMonoJust<T, S> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Publisher<S> other, T fallbackValue) {
      return mono.timeout(other).onErrorReturn(TimeoutException.class, fallbackValue);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Publisher<S> other, T fallbackValue) {
      return mono.timeout(other, Mono.just(fallbackValue));
    }
  }

  /**
   * Prefer {@link Mono#timeout(Publisher, Mono)} over more contrived or less performant
   * alternatives.
   */
  static final class MonoTimeoutPublisher<T, S> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Publisher<S> other, Mono<T> fallback) {
      return mono.timeout(other).onErrorResume(TimeoutException.class, e -> fallback);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Publisher<S> other, Mono<T> fallback) {
      return mono.timeout(other, fallback);
    }
  }

  /** Prefer {@link Mono#just(Object)} over more contrived alternatives. */
  static final class MonoJust<T> {
    @BeforeTemplate
    Mono<T> before(T value) {
      return Refaster.anyOf(Mono.justOrEmpty(Optional.of(value)), Flux.just(value).next());
    }

    @AfterTemplate
    Mono<T> after(T value) {
      return Mono.just(value);
    }
  }

  /** Prefer {@link Mono#justOrEmpty(Object)} over more contrived alternatives. */
  static final class MonoJustOrEmptyObject<T extends @Nullable Object> {
    @BeforeTemplate
    Mono<T> before(T value) {
      return Mono.justOrEmpty(Optional.ofNullable(value));
    }

    @AfterTemplate
    Mono<T> after(T value) {
      return Mono.justOrEmpty(value);
    }
  }

  /** Prefer {@link Mono#justOrEmpty(Optional)} over more verbose alternatives. */
  static final class MonoJustOrEmptyOptional<T> {
    @BeforeTemplate
    Mono<T> before(Optional<T> optional) {
      return Mono.just(optional).filter(Optional::isPresent).map(Optional::orElseThrow);
    }

    @AfterTemplate
    Mono<T> after(Optional<T> optional) {
      return Mono.justOrEmpty(optional);
    }
  }

  /**
   * Prefer {@link Mono#defer(Supplier) deferring} {@link Mono#justOrEmpty(Optional)} over more
   * verbose alternatives.
   */
  // XXX: If `optional` is a constant and effectively-final expression then the `Mono.defer` can be
  // dropped. Should look into Refaster support for identifying this.
  static final class MonoDeferMonoJustOrEmpty<T> {
    @BeforeTemplate
    @SuppressWarnings(
        "MonoFromSupplier" /* `optional` may match a checked exception-throwing expression. */)
    Mono<T> before(Optional<T> optional) {
      return Refaster.anyOf(
          Mono.fromCallable(() -> optional.orElse(null)),
          Mono.fromSupplier(() -> optional.orElse(null)));
    }

    @AfterTemplate
    Mono<T> after(Optional<T> optional) {
      return Mono.defer(() -> Mono.justOrEmpty(optional));
    }
  }

  /**
   * Try to avoid expressions of type {@code Optional<Mono<T>>}, but if you must map an {@link
   * Optional} to this type, prefer using {@link Mono#just(Object)}.
   */
  static final class OptionalMapMonoJust<T> {
    @BeforeTemplate
    Optional<Mono<T>> before(Optional<T> optional) {
      return optional.map(Mono::justOrEmpty);
    }

    @AfterTemplate
    Optional<Mono<T>> after(Optional<T> optional) {
      return optional.map(Mono::just);
    }
  }

  /**
   * Prefer a {@link Mono#justOrEmpty(Optional)} and {@link Mono#switchIfEmpty(Mono)} chain over
   * more contrived alternatives.
   *
   * <p>In particular, avoid mixing of the {@link Optional} and {@link Mono} APIs.
   */
  static final class MonoFromOptionalSwitchIfEmpty<T> {
    @BeforeTemplate
    Mono<T> before(Optional<T> optional, Mono<T> mono) {
      return optional.map(Mono::just).orElse(mono);
    }

    @AfterTemplate
    Mono<T> after(Optional<T> optional, Mono<T> mono) {
      return Mono.justOrEmpty(optional).switchIfEmpty(mono);
    }
  }

  /**
   * Prefer {@link Mono#zip(Mono, Mono)} over a chained {@link Mono#zipWith(Mono)}, as the former
   * better conveys that the {@link Mono}s may be subscribed to concurrently, and generalizes to
   * combining three or more reactive streams.
   */
  static final class MonoZip<T, S> {
    @BeforeTemplate
    Mono<Tuple2<T, S>> before(Mono<T> mono, Mono<S> other) {
      return mono.zipWith(other);
    }

    @AfterTemplate
    Mono<Tuple2<T, S>> after(Mono<T> mono, Mono<S> other) {
      return Mono.zip(mono, other);
    }
  }

  /**
   * Prefer {@link Mono#zip(Mono, Mono)} with a chained combinator over a chained {@link
   * Mono#zipWith(Mono, BiFunction)}, as the former better conveys that the {@link Mono}s may be
   * subscribed to concurrently, and generalizes to combining three or more reactive streams.
   */
  static final class MonoZipWithCombinator<T, S, R> {
    @BeforeTemplate
    Mono<R> before(Mono<T> mono, Mono<S> other, BiFunction<T, S, R> combinator) {
      return mono.zipWith(other, combinator);
    }

    @AfterTemplate
    Mono<R> after(Mono<T> mono, Mono<S> other, BiFunction<T, S, R> combinator) {
      return Mono.zip(mono, other).map(function(combinator));
    }
  }

  /**
   * Prefer {@link Flux#zip(Publisher, Publisher)} over a chained {@link Flux#zipWith(Publisher)},
   * as the former better conveys that the {@link Publisher}s may be subscribed to concurrently, and
   * generalizes to combining three or more reactive streams.
   */
  static final class FluxZip<T, S> {
    @BeforeTemplate
    Flux<Tuple2<T, S>> before(Flux<T> flux, Publisher<S> other) {
      return flux.zipWith(other);
    }

    @AfterTemplate
    Flux<Tuple2<T, S>> after(Flux<T> flux, Publisher<S> other) {
      return Flux.zip(flux, other);
    }
  }

  /**
   * Prefer {@link Flux#zip(Publisher, Publisher)} with a chained combinator over a chained {@link
   * Flux#zipWith(Publisher, BiFunction)}, as the former better conveys that the {@link Publisher}s
   * may be subscribed to concurrently, and generalizes to combining three or more reactive streams.
   */
  static final class FluxZipWithCombinator<T, S, R> {
    @BeforeTemplate
    Flux<R> before(Flux<T> flux, Publisher<S> other, BiFunction<T, S, R> combinator) {
      return flux.zipWith(other, combinator);
    }

    @AfterTemplate
    Flux<R> after(Flux<T> flux, Publisher<S> other, BiFunction<T, S, R> combinator) {
      return Flux.zip(flux, other).map(function(combinator));
    }
  }

  /** Prefer {@link Flux#zipWithIterable(Iterable)} over more contrived alternatives. */
  static final class FluxZipWithIterable<T, S> {
    @BeforeTemplate
    Flux<Tuple2<T, S>> before(Flux<T> flux, Iterable<S> iterable) {
      return Flux.zip(flux, Flux.fromIterable(iterable));
    }

    @AfterTemplate
    Flux<Tuple2<T, S>> after(Flux<T> flux, Iterable<S> iterable) {
      return flux.zipWithIterable(iterable);
    }
  }

  /** Prefer {@link Flux#zipWithIterable(Iterable, BiFunction)} over more contrived alternatives. */
  static final class FluxZipWithIterableBiFunction<T, S, R> {
    @BeforeTemplate
    Flux<R> before(
        Flux<T> flux,
        Iterable<S> iterable,
        BiFunction<? super T, ? super S, ? extends R> function) {
      return flux.zipWith(Flux.fromIterable(iterable), function);
    }

    @AfterTemplate
    Flux<R> after(
        Flux<T> flux,
        Iterable<S> iterable,
        BiFunction<? super T, ? super S, ? extends R> function) {
      return flux.zipWithIterable(iterable, function);
    }
  }

  /**
   * Prefer {@link Flux#zipWithIterable(Iterable)} with a chained combinator over {@link
   * Flux#zipWithIterable(Iterable, BiFunction)}, as the former generally yields more readable code.
   */
  static final class FluxZipWithIterableMapFunction<T, S, R> {
    @BeforeTemplate
    Flux<R> before(Flux<T> flux, Iterable<S> iterable, BiFunction<T, S, R> combinator) {
      return flux.zipWithIterable(iterable, combinator);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Flux<R> after(Flux<T> flux, Iterable<S> iterable, BiFunction<T, S, R> combinator) {
      return flux.zipWithIterable(iterable).map(function(combinator));
    }
  }

  /** Don't unnecessarily defer {@link Mono#error(Throwable)}. */
  static final class MonoDeferredError<T> {
    @BeforeTemplate
    Mono<T> before(Throwable throwable) {
      return Mono.defer(() -> Mono.error(throwable));
    }

    @AfterTemplate
    Mono<T> after(Throwable throwable) {
      return Mono.error(() -> throwable);
    }
  }

  /** Don't unnecessarily defer {@link Flux#error(Throwable)}. */
  static final class FluxDeferredError<T> {
    @BeforeTemplate
    Flux<T> before(Throwable throwable) {
      return Flux.defer(() -> Flux.error(throwable));
    }

    @AfterTemplate
    Flux<T> after(Throwable throwable) {
      return Flux.error(() -> throwable);
    }
  }

  /**
   * Don't unnecessarily pass {@link Mono#error(Supplier)} a method reference or lambda expression.
   */
  // XXX: Drop this rule once the more general rule `AssortedRules#SupplierAsSupplier` works
  // reliably.
  static final class MonoErrorSupplier<T, E extends Throwable> {
    @BeforeTemplate
    Mono<T> before(Supplier<E> supplier) {
      return Mono.error(() -> supplier.get());
    }

    @AfterTemplate
    Mono<T> after(Supplier<E> supplier) {
      return Mono.error(supplier);
    }
  }

  /**
   * Don't unnecessarily pass {@link Flux#error(Supplier)} a method reference or lambda expression.
   */
  // XXX: Drop this rule once the more general rule `AssortedRules#SupplierAsSupplier` works
  // reliably.
  static final class FluxErrorSupplier<T, E extends Throwable> {
    @BeforeTemplate
    Flux<T> before(Supplier<E> supplier) {
      return Flux.error(() -> supplier.get());
    }

    @AfterTemplate
    Flux<T> after(Supplier<E> supplier) {
      return Flux.error(supplier);
    }
  }

  /** Prefer {@link Mono#thenReturn(Object)} over more verbose alternatives. */
  static final class MonoThenReturn<T, S> {
    @BeforeTemplate
    Mono<S> before(Mono<T> mono, S object) {
      return Refaster.anyOf(
          mono.ignoreElement().thenReturn(object),
          mono.then().thenReturn(object),
          mono.then(Mono.just(object)));
    }

    @AfterTemplate
    Mono<S> after(Mono<T> mono, S object) {
      return mono.thenReturn(object);
    }
  }

  /**
   * Prefer {@link Flux#take(long)} over {@link Flux#take(long, boolean)} where relevant.
   *
   * <p>In Reactor versions prior to 3.5.0, {@code Flux#take(long)} makes an unbounded request
   * upstream, and is equivalent to {@code Flux#take(long, false)}. From version 3.5.0 onwards, the
   * behavior of {@code Flux#take(long)} instead matches {@code Flux#take(long, true)}.
   */
  @Description(
      "From Reactor 3.5.0 onwards, `take(n)` no longer requests an unbounded number of elements upstream.")
  static final class FluxTake<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, long n) {
      return flux.take(n, /* limitRequest= */ true);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, long n) {
      return flux.take(n);
    }
  }

  /** Prefer {@link Mono#defaultIfEmpty(Object)} over more contrived alternatives. */
  static final class MonoDefaultIfEmpty<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, T object) {
      return mono.switchIfEmpty(Mono.just(object));
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, T object) {
      return mono.defaultIfEmpty(object);
    }
  }

  /** Prefer {@link Flux#defaultIfEmpty(Object)} over more contrived alternatives. */
  static final class FluxDefaultIfEmpty<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, T object) {
      return flux.switchIfEmpty(Refaster.anyOf(Mono.just(object), Flux.just(object)));
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, T object) {
      return flux.defaultIfEmpty(object);
    }
  }

  /** Prefer {@link Flux#empty()} over more contrived alternatives. */
  // XXX: Using `@Matches(IsEmpty.class)`, the non-varargs overloads of most methods referenced here
  // can be rewritten as well. That would require adding a bunch more suitably-typed parameters.
  static final class FluxEmpty<T, S extends Comparable<? super S>> {
    // XXX: The methods enumerated here are not ordered entirely lexicographically, to accommodate a
    // conflict between the `InconsistentOverloads` and `RefasterMethodParameterOrder` checks.
    @BeforeTemplate
    Flux<T> before(
        Function<? super Object[], ? extends T> combinator,
        int prefetch,
        Comparator<? super T> comparator,
        @Matches(IsEmpty.class) T[] emptyArray,
        @Matches(IsEmpty.class) Iterable<T> emptyIterable,
        @Matches(IsEmpty.class) Stream<T> emptyStream) {
      return Refaster.anyOf(
          Flux.zip(combinator),
          Flux.zip(combinator, prefetch),
          Flux.concat(),
          Flux.concatDelayError(),
          Flux.firstWithSignal(),
          Flux.just(),
          Flux.merge(),
          Flux.merge(prefetch),
          Flux.mergeComparing(comparator),
          Flux.mergeComparing(prefetch, comparator),
          Flux.mergeComparingDelayError(prefetch, comparator),
          Flux.mergeDelayError(prefetch),
          Flux.mergePriority(comparator),
          Flux.mergePriority(prefetch, comparator),
          Flux.mergePriorityDelayError(prefetch, comparator),
          Flux.mergeSequential(),
          Flux.mergeSequential(prefetch),
          Flux.mergeSequentialDelayError(prefetch),
          Flux.fromArray(emptyArray),
          Flux.fromIterable(emptyIterable),
          Flux.fromStream(() -> emptyStream));
    }

    @BeforeTemplate
    Flux<T> before(Function<Object[], T> combinator, int prefetch) {
      return Refaster.anyOf(
          Flux.combineLatest(combinator), Flux.combineLatest(combinator, prefetch));
    }

    @BeforeTemplate
    Flux<S> before() {
      return Refaster.anyOf(Flux.mergeComparing(), Flux.mergePriority());
    }

    @BeforeTemplate
    Flux<Integer> before(int start) {
      return Flux.range(start, 0);
    }

    @AfterTemplate
    Flux<T> after() {
      return Flux.empty();
    }
  }

  /**
   * Prefer {@link Flux#timeout(Duration, Publisher)} over more contrived or less performant
   * alternatives.
   */
  static final class FluxTimeoutFluxEmpty<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Duration duration) {
      return flux.timeout(duration).onErrorComplete(TimeoutException.class);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Duration duration) {
      return flux.timeout(duration, Flux.empty());
    }
  }

  /** Prefer {@link Flux#just(Object)} over more contrived alternatives. */
  static final class FluxJust<T> {
    @BeforeTemplate
    Flux<Integer> before(int value) {
      return Flux.range(value, 1);
    }

    @BeforeTemplate
    Flux<T> before(T value) {
      return Refaster.anyOf(
          Mono.just(value).flux(),
          Flux.fromStream(() -> Stream.of(value)),
          Mono.just(value).repeat().take(1));
    }

    @AfterTemplate
    Flux<T> after(T value) {
      return Flux.just(value);
    }
  }

  /** Prefer {@link Flux#just(Object[])} over more contrived alternatives. */
  static final class FluxJustArray<T> {
    @BeforeTemplate
    Flux<T> before(@Repeated T values) {
      return Flux.fromStream(() -> Stream.of(Refaster.asVarargs(values)));
    }

    @AfterTemplate
    Flux<T> after(@Repeated T values) {
      return Flux.just(values);
    }
  }

  /** Prefer {@link Flux#fromArray(Object[])}} over more ambiguous or contrived alternatives. */
  static final class FluxFromArray<T> {
    @BeforeTemplate
    Flux<T> before(@NotMatches(IsRefasterAsVarargs.class) T[] array) {
      return Refaster.anyOf(Flux.just(array), Flux.fromStream(() -> Arrays.stream(array)));
    }

    @AfterTemplate
    Flux<T> after(T[] array) {
      return Flux.fromArray(array);
    }
  }

  /** Don't unnecessarily transform a {@link Mono} to an equivalent instance. */
  static final class MonoIdentity<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono) {
      return Refaster.anyOf(
          mono.switchIfEmpty(Mono.empty()), mono.flux().next(), mono.flux().singleOrEmpty());
    }

    // XXX: Consider filing a SonarCloud issue for the S2637 false positive.
    @BeforeTemplate
    @SuppressWarnings({
      "java:S2637" /* False positive: result is never `null`. */,
      "java:S4968" /* Result may be `Mono<Void>`. */,
      "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
    })
    Mono<? extends @Nullable Void> before2(Mono<@Nullable Void> mono) {
      return Refaster.anyOf(mono.ignoreElement(), mono.then());
    }

    // XXX: Replace this rule with an extension of the `IdentityConversion` rule, supporting
    // `Stream#map`, `Mono#map` and `Flux#map`. Alternatively, extend the `IsIdentityOperation`
    // matcher and use it to constrain the matched `map` argument.
    @BeforeTemplate
    Mono<ImmutableList<T>> before3(Mono<ImmutableList<T>> mono) {
      return mono.map(ImmutableList::copyOf);
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    Mono<T> after(Mono<T> mono) {
      return mono;
    }
  }

  /** Don't unnecessarily transform a {@link Mono} to a {@link Flux} to expect exactly one item. */
  static final class MonoSingle<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono) {
      return mono.flux().single();
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono) {
      return mono.single();
    }
  }

  /** Prefer {@link Mono#using(Callable, Function)} over more contrived alternatives. */
  // XXX: The `.single()` variant emits a `NoSuchElementException` if the source is empty, while the
  // replacement does not.
  static final class MonoUsing<D extends AutoCloseable, T> {
    @BeforeTemplate
    Mono<T> before(
        Callable<? extends D> resourceSupplier,
        @Matches(ReturnsMono.class)
            Function<? super D, ? extends Publisher<? extends T>> sourceSupplier) {
      return Refaster.anyOf(
          Flux.using(resourceSupplier, sourceSupplier).next(),
          Flux.using(resourceSupplier, sourceSupplier).single());
    }

    @AfterTemplate
    Mono<T> after(
        Callable<? extends D> resourceSupplier,
        Function<? super D, ? extends Mono<? extends T>> sourceSupplier) {
      return Mono.using(resourceSupplier, sourceSupplier);
    }
  }

  /** Prefer {@link Mono#using(Callable, Function, boolean)} over more contrived alternatives. */
  // XXX: The `.single()` variant emits a `NoSuchElementException` if the source is empty, while the
  // replacement does not.
  static final class MonoUsingEagerBoolean<D extends AutoCloseable, T> {
    @BeforeTemplate
    Mono<T> before(
        Callable<? extends D> resourceSupplier,
        @Matches(ReturnsMono.class)
            Function<? super D, ? extends Publisher<? extends T>> sourceSupplier,
        boolean eager) {
      return Refaster.anyOf(
          Flux.using(resourceSupplier, sourceSupplier, eager).next(),
          Flux.using(resourceSupplier, sourceSupplier, eager).single());
    }

    @AfterTemplate
    Mono<T> after(
        Callable<? extends D> resourceSupplier,
        Function<? super D, ? extends Mono<? extends T>> sourceSupplier,
        boolean eager) {
      return Mono.using(resourceSupplier, sourceSupplier, eager);
    }
  }

  /** Prefer {@link Mono#using(Callable, Function, Consumer)} over more contrived alternatives. */
  // XXX: The `.single()` variant emits a `NoSuchElementException` if the source is empty, while the
  // replacement does not.
  static final class MonoUsingResourceCleanup<D, T> {
    @BeforeTemplate
    Mono<T> before(
        Callable<? extends D> resourceSupplier,
        @Matches(ReturnsMono.class)
            Function<? super D, ? extends Publisher<? extends T>> sourceSupplier,
        Consumer<? super D> resourceCleanup) {
      return Refaster.anyOf(
          Flux.using(resourceSupplier, sourceSupplier, resourceCleanup).next(),
          Flux.using(resourceSupplier, sourceSupplier, resourceCleanup).single());
    }

    @AfterTemplate
    Mono<T> after(
        Callable<? extends D> resourceSupplier,
        Function<? super D, ? extends Mono<? extends T>> sourceSupplier,
        Consumer<? super D> resourceCleanup) {
      return Mono.using(resourceSupplier, sourceSupplier, resourceCleanup);
    }
  }

  /**
   * Prefer {@link Mono#using(Callable, Function, Consumer, boolean)} over more contrived
   * alternatives.
   */
  // XXX: The `.single()` variant emits a `NoSuchElementException` if the source is empty, while the
  // replacement does not.
  static final class MonoUsingConsumerEagerBoolean<D, T> {
    @BeforeTemplate
    Mono<T> before(
        Callable<? extends D> resourceSupplier,
        @Matches(ReturnsMono.class)
            Function<? super D, ? extends Publisher<? extends T>> sourceSupplier,
        Consumer<? super D> resourceCleanup,
        boolean eager) {
      return Refaster.anyOf(
          Flux.using(resourceSupplier, sourceSupplier, resourceCleanup, eager).next(),
          Flux.using(resourceSupplier, sourceSupplier, resourceCleanup, eager).single());
    }

    @AfterTemplate
    Mono<T> after(
        Callable<? extends D> resourceSupplier,
        Function<? super D, ? extends Mono<? extends T>> sourceSupplier,
        Consumer<? super D> resourceCleanup,
        boolean eager) {
      return Mono.using(resourceSupplier, sourceSupplier, resourceCleanup, eager);
    }
  }

  /**
   * Prefer {@link Mono#usingWhen(Publisher, Function, Function)} over more contrived alternatives.
   */
  // XXX: The `.single()` variant emits a `NoSuchElementException` if the source is empty, while the
  // replacement does not.
  static final class MonoUsingWhenAsyncCleanup<D, T> {
    @BeforeTemplate
    Mono<T> before(
        Publisher<? extends D> resourceSupplier,
        @Matches(ReturnsMono.class)
            Function<? super D, ? extends Publisher<? extends T>> resourceClosure,
        Function<? super D, ? extends Publisher<?>> asyncCleanup) {
      return Refaster.anyOf(
          Flux.usingWhen(resourceSupplier, resourceClosure, asyncCleanup).next(),
          Flux.usingWhen(resourceSupplier, resourceClosure, asyncCleanup).single());
    }

    @AfterTemplate
    Mono<T> after(
        Publisher<? extends D> resourceSupplier,
        Function<? super D, ? extends Mono<? extends T>> resourceClosure,
        Function<? super D, ? extends Publisher<?>> asyncCleanup) {
      return Mono.usingWhen(resourceSupplier, resourceClosure, asyncCleanup);
    }
  }

  /**
   * Prefer {@link Mono#usingWhen(Publisher, Function, Function, BiFunction, Function)} over more
   * contrived alternatives.
   */
  // XXX: The `.single()` variant emits a `NoSuchElementException` if the source is empty, while the
  // replacement does not.
  static final class MonoUsingWhenAsync<D, T> {
    @BeforeTemplate
    Mono<T> before(
        Publisher<? extends D> resourceSupplier,
        @Matches(ReturnsMono.class)
            Function<? super D, ? extends Publisher<? extends T>> resourceClosure,
        Function<? super D, ? extends Publisher<?>> asyncComplete,
        BiFunction<? super D, ? super Throwable, ? extends Publisher<?>> asyncError,
        Function<? super D, ? extends Publisher<?>> asyncCancel) {
      return Refaster.anyOf(
          Flux.usingWhen(resourceSupplier, resourceClosure, asyncComplete, asyncError, asyncCancel)
              .next(),
          Flux.usingWhen(resourceSupplier, resourceClosure, asyncComplete, asyncError, asyncCancel)
              .single());
    }

    @AfterTemplate
    Mono<T> after(
        Publisher<? extends D> resourceSupplier,
        Function<? super D, ? extends Mono<? extends T>> resourceClosure,
        Function<? super D, ? extends Publisher<?>> asyncComplete,
        BiFunction<? super D, ? super Throwable, ? extends Publisher<?>> asyncError,
        Function<? super D, ? extends Publisher<?>> asyncCancel) {
      return Mono.usingWhen(
          resourceSupplier, resourceClosure, asyncComplete, asyncError, asyncCancel);
    }
  }

  /** Don't unnecessarily pass an empty publisher to {@link Flux#switchIfEmpty(Publisher)}. */
  static final class FluxSwitchIfEmptyOfEmptyPublisher<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux) {
      return flux.switchIfEmpty(Refaster.anyOf(Mono.empty(), Flux.empty()));
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    Flux<T> after(Flux<T> flux) {
      return flux;
    }
  }

  /** Prefer {@link Flux#concatMap(Function)} over more contrived alternatives. */
  static final class FluxConcatMap<T, S, P extends Publisher<? extends S>> {
    @BeforeTemplate
    @SuppressWarnings("NestedPublishers")
    Flux<S> before(
        Flux<T> flux,
        Function<? super T, ? extends P> function,
        @Matches(IsIdentityOperation.class)
            Function<? super P, ? extends Publisher<? extends S>> identityOperation) {
      return Refaster.anyOf(
          flux.concatMap(function, 0),
          flux.flatMap(function, 1),
          flux.flatMapSequential(function, 1),
          flux.map(function).concatMap(identityOperation));
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux, Function<? super T, ? extends P> function) {
      return flux.concatMap(function);
    }
  }

  /** Prefer {@link Flux#concatMap(Function, int)} over more contrived alternatives. */
  static final class FluxConcatMapWithPrefetch<T, S, P extends Publisher<? extends S>> {
    @BeforeTemplate
    @SuppressWarnings("NestedPublishers")
    Flux<S> before(
        Flux<T> flux,
        Function<? super T, ? extends P> function,
        int prefetch,
        @Matches(IsIdentityOperation.class)
            Function<? super P, ? extends Publisher<? extends S>> identityOperation) {
      return Refaster.anyOf(
          flux.flatMap(function, 1, prefetch),
          flux.flatMapSequential(function, 1, prefetch),
          flux.map(function).concatMap(identityOperation, prefetch));
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux, Function<? super T, ? extends P> function, int prefetch) {
      return flux.concatMap(function, prefetch);
    }
  }

  /** Avoid contrived alternatives to {@link Mono#flatMapIterable(Function)}. */
  static final class MonoFlatMapIterable<T, S, I extends Iterable<? extends S>> {
    @BeforeTemplate
    Flux<S> before(Mono<T> mono, Function<? super T, I> function) {
      return mono.map(function).flatMapMany(Flux::fromIterable);
    }

    @BeforeTemplate
    Flux<S> before(
        Mono<T> mono,
        Function<? super T, I> function,
        @Matches(IsIdentityOperation.class)
            Function<? super I, ? extends Iterable<? extends S>> identityOperation) {
      return Refaster.anyOf(
          mono.map(function).flatMapIterable(identityOperation),
          mono.flux().concatMapIterable(function));
    }

    @AfterTemplate
    Flux<S> after(Mono<T> mono, Function<? super T, I> function) {
      return mono.flatMapIterable(function);
    }
  }

  /**
   * Prefer {@link Mono#flatMapIterable(Function)} to flatten a {@link Mono} of some {@link
   * Iterable} over less efficient alternatives.
   */
  static final class MonoFlatMapIterableIdentity<T, S extends Iterable<T>> {
    @BeforeTemplate
    Flux<T> before(Mono<S> mono) {
      return mono.flatMapMany(Flux::fromIterable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Flux<T> after(Mono<S> mono) {
      return mono.flatMapIterable(identity());
    }
  }

  /**
   * Prefer {@link Flux#concatMapIterable(Function)} over alternatives with less clear syntax or
   * semantics.
   */
  static final class FluxConcatMapIterable<T, S, I extends Iterable<? extends S>> {
    @BeforeTemplate
    Flux<S> before(
        Flux<T> flux,
        Function<? super T, I> function,
        @Matches(IsIdentityOperation.class)
            Function<? super I, ? extends Iterable<? extends S>> identityOperation) {
      return Refaster.anyOf(
          flux.flatMapIterable(function), flux.map(function).concatMapIterable(identityOperation));
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux, Function<? super T, ? extends Iterable<? extends S>> function) {
      return flux.concatMapIterable(function);
    }
  }

  /**
   * Prefer {@link Flux#concatMapIterable(Function, int)} over alternatives with less clear syntax
   * or semantics.
   */
  static final class FluxConcatMapIterableWithPrefetch<T, S, I extends Iterable<? extends S>> {
    @BeforeTemplate
    Flux<S> before(
        Flux<T> flux,
        Function<? super T, I> function,
        int prefetch,
        @Matches(IsIdentityOperation.class)
            Function<? super I, ? extends Iterable<? extends S>> identityOperation) {
      return Refaster.anyOf(
          flux.flatMapIterable(function, prefetch),
          flux.map(function).concatMapIterable(identityOperation, prefetch));
    }

    @AfterTemplate
    Flux<S> after(
        Flux<T> flux, Function<? super T, ? extends Iterable<? extends S>> function, int prefetch) {
      return flux.concatMapIterable(function, prefetch);
    }
  }

  /**
   * Don't use {@link Mono#flatMapMany(Function)} to implicitly convert a {@link Mono} to a {@link
   * Flux}.
   */
  abstract static class MonoFlatMapToFlux<T, S> {
    // XXX: It would be more expressive if this `@Placeholder` were replaced with a `Function<?
    // super T, ? extends Mono<? extends S>>` parameter, so that compatible non-lambda expression
    // arguments to `flatMapMany` are also matched. However, the type inferred for lambda and method
    // reference expressions passed to `flatMapMany` appears to always be `Function<T, Publisher<?
    // extends S>>`, which doesn't match. Find a solution.
    @Placeholder(allowsIdentity = true)
    abstract Mono<S> transformation(@MayOptionallyUse T value);

    @BeforeTemplate
    Flux<S> before(Mono<T> mono) {
      return mono.flatMapMany(v -> transformation(v));
    }

    @AfterTemplate
    Flux<S> after(Mono<T> mono) {
      return mono.flatMap(v -> transformation(v)).flux();
    }
  }

  /**
   * Prefer {@link Mono#map(Function)} over alternatives that unnecessarily require an inner
   * subscription.
   */
  abstract static class MonoMap<T, S> {
    @Placeholder(allowsIdentity = true)
    abstract S transformation(@MayOptionallyUse T value);

    @BeforeTemplate
    Mono<S> before(Mono<T> mono) {
      return mono.flatMap(x -> Mono.just(transformation(x)));
    }

    @AfterTemplate
    Mono<S> after(Mono<T> mono) {
      return mono.map(x -> transformation(x));
    }
  }

  /**
   * Prefer {@link Flux#map(Function)} over alternatives that unnecessarily require an inner
   * subscription.
   */
  abstract static class FluxMap<T, S> {
    @Placeholder(allowsIdentity = true)
    abstract S transformation(@MayOptionallyUse T value);

    @BeforeTemplate
    Flux<S> before(Flux<T> flux, int prefetch, boolean delayUntilEnd, int maxConcurrency) {
      return Refaster.anyOf(
          flux.concatMap(x -> Mono.just(transformation(x))),
          flux.concatMap(x -> Flux.just(transformation(x))),
          flux.concatMap(x -> Mono.just(transformation(x)), prefetch),
          flux.concatMap(x -> Flux.just(transformation(x)), prefetch),
          flux.concatMapDelayError(x -> Mono.just(transformation(x))),
          flux.concatMapDelayError(x -> Flux.just(transformation(x))),
          flux.concatMapDelayError(x -> Mono.just(transformation(x)), prefetch),
          flux.concatMapDelayError(x -> Flux.just(transformation(x)), prefetch),
          flux.concatMapDelayError(x -> Mono.just(transformation(x)), delayUntilEnd, prefetch),
          flux.concatMapDelayError(x -> Flux.just(transformation(x)), delayUntilEnd, prefetch),
          flux.flatMap(x -> Mono.just(transformation(x)), maxConcurrency),
          flux.flatMap(x -> Flux.just(transformation(x)), maxConcurrency),
          flux.flatMap(x -> Mono.just(transformation(x)), maxConcurrency, prefetch),
          flux.flatMap(x -> Flux.just(transformation(x)), maxConcurrency, prefetch),
          flux.flatMapDelayError(x -> Mono.just(transformation(x)), maxConcurrency, prefetch),
          flux.flatMapDelayError(x -> Flux.just(transformation(x)), maxConcurrency, prefetch),
          flux.flatMapSequential(x -> Mono.just(transformation(x)), maxConcurrency),
          flux.flatMapSequential(x -> Flux.just(transformation(x)), maxConcurrency),
          flux.flatMapSequential(x -> Mono.just(transformation(x)), maxConcurrency, prefetch),
          flux.flatMapSequential(x -> Flux.just(transformation(x)), maxConcurrency, prefetch),
          flux.flatMapSequentialDelayError(
              x -> Mono.just(transformation(x)), maxConcurrency, prefetch),
          flux.flatMapSequentialDelayError(
              x -> Flux.just(transformation(x)), maxConcurrency, prefetch),
          flux.switchMap(x -> Mono.just(transformation(x))),
          flux.switchMap(x -> Flux.just(transformation(x))));
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux) {
      return flux.map(x -> transformation(x));
    }
  }

  /**
   * Prefer {@link Mono#mapNotNull(Function)} over alternatives that unnecessarily require an inner
   * subscription.
   */
  abstract static class MonoMapNotNull<T, S> {
    @Placeholder(allowsIdentity = true)
    abstract S transformation(@MayOptionallyUse T value);

    @BeforeTemplate
    Mono<S> before(Mono<T> mono) {
      return mono.flatMap(
          x ->
              Refaster.anyOf(
                  Mono.justOrEmpty(transformation(x)), Mono.fromSupplier(() -> transformation(x))));
    }

    @AfterTemplate
    Mono<S> after(Mono<T> mono) {
      return mono.mapNotNull(x -> transformation(x));
    }
  }

  /**
   * Prefer {@link Flux#mapNotNull(Function)} over alternatives that unnecessarily require an inner
   * subscription.
   */
  abstract static class FluxMapNotNull<T, S> {
    @Placeholder(allowsIdentity = true)
    abstract S transformation(@MayOptionallyUse T value);

    @BeforeTemplate
    @SuppressWarnings("java:S138" /* Method is long, but not complex. */)
    Publisher<S> before(Flux<T> flux, int prefetch, boolean delayUntilEnd, int maxConcurrency) {
      return Refaster.anyOf(
          flux.concatMap(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x)))),
          flux.concatMap(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              prefetch),
          flux.concatMapDelayError(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x)))),
          flux.concatMapDelayError(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              prefetch),
          flux.concatMapDelayError(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              delayUntilEnd,
              prefetch),
          flux.flatMap(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              maxConcurrency),
          flux.flatMap(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              maxConcurrency,
              prefetch),
          flux.flatMapDelayError(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              maxConcurrency,
              prefetch),
          flux.flatMapSequential(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              maxConcurrency),
          flux.flatMapSequential(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              maxConcurrency,
              prefetch),
          flux.flatMapSequentialDelayError(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              maxConcurrency,
              prefetch),
          flux.switchMap(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x)))));
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux) {
      return flux.mapNotNull(x -> transformation(x));
    }
  }

  /**
   * Prefer immediately unwrapping {@link Optional} transformation results inside {@link
   * Flux#mapNotNull(Function)} over more contrived alternatives.
   */
  abstract static class FluxMapNotNullTransformationOrElse<T, S> {
    @Placeholder(allowsIdentity = true)
    abstract Optional<S> transformation(@MayOptionallyUse T value);

    @BeforeTemplate
    Flux<S> before(Flux<T> flux) {
      return flux.map(v -> transformation(v)).mapNotNull(o -> o.orElse(null));
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux) {
      return flux.mapNotNull(x -> transformation(x).orElse(null));
    }
  }

  /** Prefer {@link Flux#mapNotNull(Function)} over more contrived alternatives. */
  static final class FluxMapNotNullOrElse<T> {
    @BeforeTemplate
    Flux<T> before(Flux<Optional<T>> flux) {
      return flux.filter(Optional::isPresent).map(Optional::orElseThrow);
    }

    @AfterTemplate
    Flux<T> after(Flux<Optional<T>> flux) {
      return flux.mapNotNull(x -> x.orElse(null));
    }
  }

  /** Prefer {@link Mono#flux()}} over more contrived alternatives. */
  static final class MonoFlux<T> {
    @BeforeTemplate
    Flux<T> before(Mono<T> mono) {
      return Refaster.anyOf(
          mono.flatMapMany(Mono::just), mono.flatMapMany(Flux::just), Flux.concat(mono));
    }

    @AfterTemplate
    Flux<T> after(Mono<T> mono) {
      return mono.flux();
    }
  }

  /** Prefer direct invocation of {@link Mono#then()}} over more contrived alternatives. */
  static final class MonoThen<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> before(Mono<T> mono) {
      return Refaster.anyOf(
          mono.ignoreElement().then(),
          mono.flux().then(),
          Mono.when(mono),
          Mono.whenDelayError(mono));
    }

    @AfterTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> after(Mono<T> mono) {
      return mono.then();
    }
  }

  /** Avoid vacuous invocations of {@link Flux#ignoreElements()}. */
  static final class FluxThen<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> before(Flux<T> flux) {
      return flux.ignoreElements().then();
    }

    // XXX: Consider filing a SonarCloud issue for the S2637 false positive.
    @BeforeTemplate
    @SuppressWarnings({
      "java:S2637" /* False positive: result is never `null`. */,
      "java:S4968" /* Result may be `Mono<Void>`. */,
      "z-key-to-resolve-AnnotationUseStyle-and-TrailingComment-check-conflict"
    })
    Mono<? extends @Nullable Void> before2(Flux<@Nullable Void> flux) {
      return flux.ignoreElements();
    }

    @AfterTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> after(Flux<T> flux) {
      return flux.then();
    }
  }

  /** Avoid vacuous invocations of {@link Mono#ignoreElement()}. */
  static final class MonoThenEmpty<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> before(Mono<T> mono, Publisher<@Nullable Void> publisher) {
      return mono.ignoreElement().thenEmpty(publisher);
    }

    @AfterTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> after(Mono<T> mono, Publisher<@Nullable Void> publisher) {
      return mono.thenEmpty(publisher);
    }
  }

  /** Avoid vacuous invocations of {@link Flux#ignoreElements()}. */
  static final class FluxThenEmpty<T> {
    @BeforeTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> before(Flux<T> flux, Publisher<@Nullable Void> publisher) {
      return flux.ignoreElements().thenEmpty(publisher);
    }

    @AfterTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> after(Flux<T> flux, Publisher<@Nullable Void> publisher) {
      return flux.thenEmpty(publisher);
    }
  }

  /** Avoid vacuous operations prior to invocation of {@link Mono#thenMany(Publisher)}. */
  static final class MonoThenMany<T, S> {
    @BeforeTemplate
    Flux<S> before(Mono<T> mono, Publisher<S> publisher) {
      return Refaster.anyOf(
          mono.ignoreElement().thenMany(publisher), mono.flux().thenMany(publisher));
    }

    @AfterTemplate
    Flux<S> after(Mono<T> mono, Publisher<S> publisher) {
      return mono.thenMany(publisher);
    }
  }

  /**
   * Prefer explicit invocation of {@link Mono#flux()} over implicit conversions from {@link Mono}
   * to {@link Flux}.
   */
  static final class MonoThenMonoFlux<T, S> {
    @BeforeTemplate
    Flux<S> before(Mono<T> mono1, Mono<S> mono2) {
      return mono1.thenMany(mono2);
    }

    @AfterTemplate
    Flux<S> after(Mono<T> mono1, Mono<S> mono2) {
      return mono1.then(mono2).flux();
    }
  }

  /** Avoid vacuous invocations of {@link Flux#ignoreElements()}. */
  static final class FluxThenMany<T, S> {
    @BeforeTemplate
    Flux<S> before(Flux<T> flux, Publisher<S> publisher) {
      return flux.ignoreElements().thenMany(publisher);
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux, Publisher<S> publisher) {
      return flux.thenMany(publisher);
    }
  }

  /** Avoid vacuous operations prior to invocation of {@link Mono#then(Mono)}. */
  static final class MonoThenMono<T, S> {
    @BeforeTemplate
    Mono<S> before(Mono<T> mono1, Mono<S> mono2) {
      return Refaster.anyOf(mono1.ignoreElement().then(mono2), mono1.flux().then(mono2));
    }

    @BeforeTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> before2(Mono<T> mono1, Mono<@Nullable Void> mono2) {
      return mono1.thenEmpty(mono2);
    }

    @AfterTemplate
    Mono<S> after(Mono<T> mono1, Mono<S> mono2) {
      return mono1.then(mono2);
    }
  }

  /** Avoid vacuous invocations of {@link Flux#ignoreElements()}. */
  static final class FluxThenMono<T, S> {
    @BeforeTemplate
    Mono<S> before(Flux<T> flux, Mono<S> mono) {
      return flux.ignoreElements().then(mono);
    }

    @BeforeTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> before2(Flux<T> flux, Mono<@Nullable Void> mono) {
      return flux.thenEmpty(mono);
    }

    @AfterTemplate
    Mono<S> after(Flux<T> flux, Mono<S> mono) {
      return flux.then(mono);
    }
  }

  /** Prefer {@link Mono#singleOptional()} over more contrived alternatives. */
  // XXX: Consider creating a plugin that flags/discourages `Mono<Optional<T>>` method return
  // types, just as we discourage nullable `Boolean`s and `Optional`s.
  // XXX: The `mono.transform(Mono::singleOptional)` replacement is a special case of a more general
  // rule. Consider introducing an Error Prone check for this.
  static final class MonoSingleOptional<T> {
    @BeforeTemplate
    Mono<Optional<T>> before(Mono<T> mono, Optional<T> optional, Mono<Optional<T>> alternate) {
      return Refaster.anyOf(
          mono.flux().collect(toOptional()),
          mono.map(Optional::of),
          mono.singleOptional().defaultIfEmpty(optional),
          mono.singleOptional().switchIfEmpty(alternate),
          mono.transform(Mono::singleOptional));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Mono<Optional<T>> after(Mono<T> mono) {
      return mono.singleOptional();
    }
  }

  /** Prefer {@link Mono#cast(Class)} over {@link Mono#map(Function)} with a cast. */
  static final class MonoCast<T, S> {
    @BeforeTemplate
    Mono<S> before(Mono<T> mono) {
      return mono.map(Refaster.<S>clazz()::cast);
    }

    @AfterTemplate
    Mono<S> after(Mono<T> mono) {
      return mono.cast(Refaster.<S>clazz());
    }
  }

  /** Prefer {@link Flux#cast(Class)} over {@link Flux#map(Function)} with a cast. */
  static final class FluxCast<T, S> {
    @BeforeTemplate
    Flux<S> before(Flux<T> flux) {
      return flux.map(Refaster.<S>clazz()::cast);
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux) {
      return flux.cast(Refaster.<S>clazz());
    }
  }

  /** Prefer {@link Mono#ofType(Class)} over more contrived alternatives. */
  static final class MonoOfType<T, S> {
    @BeforeTemplate
    Mono<S> before(Mono<T> mono, Class<S> clazz) {
      return mono.filter(clazz::isInstance).cast(clazz);
    }

    @AfterTemplate
    Mono<S> after(Mono<T> mono, Class<S> clazz) {
      return mono.ofType(clazz);
    }
  }

  /** Prefer {@link Flux#ofType(Class)} over more contrived alternatives. */
  static final class FluxOfType<T, S> {
    @BeforeTemplate
    Flux<S> before(Flux<T> flux, Class<S> clazz) {
      return flux.filter(clazz::isInstance).cast(clazz);
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux, Class<S> clazz) {
      return flux.ofType(clazz);
    }
  }

  /** Prefer {@link Mono#flatMap(Function)} over more contrived alternatives. */
  static final class MonoFlatMap<S, T, P extends Mono<? extends T>> {
    @BeforeTemplate
    @SuppressWarnings("NestedPublishers")
    Mono<T> before(
        Mono<S> mono,
        Function<? super S, ? extends P> function,
        @Matches(IsIdentityOperation.class)
            Function<? super P, ? extends Mono<? extends T>> identityOperation) {
      return mono.map(function).flatMap(identityOperation);
    }

    @AfterTemplate
    Mono<T> after(Mono<S> mono, Function<? super S, ? extends P> function) {
      return mono.flatMap(function);
    }
  }

  /** Prefer {@link Mono#flatMapMany(Function)} over more contrived alternatives. */
  static final class MonoFlatMapMany<S, T, P extends Publisher<? extends T>> {
    @BeforeTemplate
    @SuppressWarnings("NestedPublishers")
    Flux<T> before(
        Mono<S> mono,
        Function<? super S, P> function,
        @Matches(IsIdentityOperation.class)
            Function<? super P, ? extends Publisher<? extends T>> identityOperation,
        int prefetch,
        boolean delayUntilEnd,
        int maxConcurrency) {
      return Refaster.anyOf(
          mono.map(function).flatMapMany(identityOperation),
          mono.flux().concatMap(function),
          mono.flux().concatMap(function, prefetch),
          mono.flux().concatMapDelayError(function),
          mono.flux().concatMapDelayError(function, prefetch),
          mono.flux().concatMapDelayError(function, delayUntilEnd, prefetch),
          mono.flux().flatMap(function, maxConcurrency),
          mono.flux().flatMap(function, maxConcurrency, prefetch),
          mono.flux().flatMapDelayError(function, maxConcurrency, prefetch),
          mono.flux().flatMapSequential(function, maxConcurrency),
          mono.flux().flatMapSequential(function, maxConcurrency, prefetch),
          mono.flux().flatMapSequentialDelayError(function, maxConcurrency, prefetch));
    }

    @BeforeTemplate
    Flux<T> before(Mono<S> mono, Function<? super S, Publisher<? extends T>> function) {
      return mono.flux().switchMap(function);
    }

    @AfterTemplate
    Flux<T> after(Mono<S> mono, Function<? super S, ? extends P> function) {
      return mono.flatMapMany(function);
    }
  }

  /**
   * Prefer {@link Flux#concatMapIterable(Function)} over alternatives that require an additional
   * subscription.
   */
  static final class ConcatMapIterableIdentity<T> {
    @BeforeTemplate
    Flux<T> before(Flux<? extends Iterable<T>> flux) {
      return Refaster.anyOf(
          flux.concatMap(list -> Flux.fromIterable(list)), flux.concatMap(Flux::fromIterable));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Flux<T> after(Flux<? extends Iterable<T>> flux) {
      return flux.concatMapIterable(identity());
    }
  }

  /**
   * Prefer {@link Flux#concatMapIterable(Function, int)} over alternatives that require an
   * additional subscription.
   */
  static final class ConcatMapIterableIdentityWithPrefetch<T> {
    @BeforeTemplate
    Flux<T> before(Flux<? extends Iterable<T>> flux, int prefetch) {
      return Refaster.anyOf(
          flux.concatMap(list -> Flux.fromIterable(list), prefetch),
          flux.concatMap(Flux::fromIterable, prefetch));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Flux<T> after(Flux<? extends Iterable<T>> flux, int prefetch) {
      return flux.concatMapIterable(identity(), prefetch);
    }
  }

  /** Prefer {@link Flux#fromIterable(Iterable)} over less efficient alternatives. */
  // XXX: Once the `FluxFromStreamSupplier` rule is constrained using
  // `@NotMatches(IsIdentityOperation.class)`, this rule should also cover
  // `Flux.fromStream(collection.stream())`.
  static final class FluxFromIterable<T> {
    // XXX: Once the `MethodReferenceUsage` check is generally enabled, drop the second
    // `Refaster.anyOf` variant.
    @BeforeTemplate
    Flux<T> before(Collection<T> collection) {
      return Flux.fromStream(
          Refaster.<Supplier<Stream<? extends T>>>anyOf(
              collection::stream, () -> collection.stream()));
    }

    @AfterTemplate
    Flux<T> after(Collection<T> collection) {
      return Flux.fromIterable(collection);
    }
  }

  /**
   * Prefer {@link Flux#count()} followed by a conversion from {@code long} to {@code int} over
   * collecting into a list and counting its elements.
   */
  static final class FluxCountMapMathToIntExact<T> {
    @BeforeTemplate
    Mono<Integer> before(Flux<T> flux) {
      return Refaster.anyOf(
          flux.collect(toImmutableList())
              .map(
                  Refaster.anyOf(
                      Collection::size,
                      List::size,
                      ImmutableCollection::size,
                      ImmutableList::size)),
          flux.collect(toCollection(ArrayList::new))
              .map(Refaster.anyOf(Collection::size, List::size)));
    }

    @AfterTemplate
    Mono<Integer> after(Flux<T> flux) {
      return flux.count().map(Math::toIntExact);
    }
  }

  /**
   * Prefer {@link Mono#doOnError(Class, Consumer)} over {@link Mono#doOnError(Predicate, Consumer)}
   * where possible.
   */
  static final class MonoDoOnError<T> {
    @BeforeTemplate
    Mono<T> before(
        Mono<T> mono, Class<? extends Throwable> clazz, Consumer<? super Throwable> onError) {
      return mono.doOnError(clazz::isInstance, onError);
    }

    @AfterTemplate
    Mono<T> after(
        Mono<T> mono, Class<? extends Throwable> clazz, Consumer<? super Throwable> onError) {
      return mono.doOnError(clazz, onError);
    }
  }

  /** Calling {@link Mono#doOnError(Consumer)} after {@link Mono#onErrorComplete()} is redundant. */
  static final class MonoDoOnErrorOnErrorComplete<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Consumer<? super Throwable> onError) {
      return mono.onErrorComplete().doOnError(onError);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Consumer<? super Throwable> onError) {
      return mono.doOnError(onError).onErrorComplete();
    }
  }

  /**
   * Calling {@link Mono#doOnError(Class, Consumer)} after {@link Mono#onErrorComplete()} is
   * redundant.
   */
  static final class MonoDoOnErrorClassOnErrorComplete<T> {
    @BeforeTemplate
    Mono<T> before(
        Mono<T> mono, Class<? extends Throwable> clazz, Consumer<? super Throwable> onError) {
      return mono.onErrorComplete().doOnError(clazz, onError);
    }

    @AfterTemplate
    Mono<T> after(
        Mono<T> mono, Class<? extends Throwable> clazz, Consumer<? super Throwable> onError) {
      return mono.doOnError(clazz, onError).onErrorComplete();
    }
  }

  /**
   * Prefer {@link Flux#doOnError(Class, Consumer)} over {@link Flux#doOnError(Predicate, Consumer)}
   * where possible.
   */
  static final class FluxDoOnError<T> {
    @BeforeTemplate
    Flux<T> before(
        Flux<T> flux, Class<? extends Throwable> clazz, Consumer<? super Throwable> onError) {
      return flux.doOnError(clazz::isInstance, onError);
    }

    @AfterTemplate
    Flux<T> after(
        Flux<T> flux, Class<? extends Throwable> clazz, Consumer<? super Throwable> onError) {
      return flux.doOnError(clazz, onError);
    }
  }

  /** Calling {@link Flux#doOnError(Consumer)} after {@link Flux#onErrorComplete()} is redundant. */
  static final class FluxDoOnErrorOnErrorComplete<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Consumer<? super Throwable> onError) {
      return flux.onErrorComplete().doOnError(onError);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Consumer<? super Throwable> onError) {
      return flux.doOnError(onError).onErrorComplete();
    }
  }

  /**
   * Calling {@link Flux#doOnError(Class, Consumer)} after {@link Flux#onErrorComplete()} is
   * redundant.
   */
  static final class FluxDoOnErrorOnClassErrorComplete<T> {
    @BeforeTemplate
    Flux<T> before(
        Flux<T> flux, Class<? extends Throwable> clazz, Consumer<? super Throwable> onError) {
      return flux.onErrorComplete().doOnError(clazz, onError);
    }

    @AfterTemplate
    Flux<T> after(
        Flux<T> flux, Class<? extends Throwable> clazz, Consumer<? super Throwable> onError) {
      return flux.doOnError(clazz, onError).onErrorComplete();
    }
  }

  /** Prefer {@link Mono#onErrorComplete()} over more contrived alternatives. */
  static final class MonoOnErrorComplete<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono) {
      return mono.onErrorResume(e -> Mono.empty());
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono) {
      return mono.onErrorComplete();
    }
  }

  /** Prefer {@link Flux#onErrorComplete()} over more contrived alternatives. */
  static final class FluxOnErrorComplete<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux) {
      return flux.onErrorResume(e -> Refaster.anyOf(Mono.empty(), Flux.empty()));
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux) {
      return flux.onErrorComplete();
    }
  }

  /** Prefer {@link Mono#onErrorComplete(Class)}} over more contrived alternatives. */
  static final class MonoOnErrorCompleteClass<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Class<? extends Throwable> clazz) {
      return Refaster.anyOf(
          mono.onErrorComplete(clazz::isInstance), mono.onErrorResume(clazz, e -> Mono.empty()));
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Class<? extends Throwable> clazz) {
      return mono.onErrorComplete(clazz);
    }
  }

  /** Prefer {@link Flux#onErrorComplete(Class)}} over more contrived alternatives. */
  static final class FluxOnErrorCompleteClass<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Class<? extends Throwable> clazz) {
      return Refaster.anyOf(
          flux.onErrorComplete(clazz::isInstance),
          flux.onErrorResume(clazz, e -> Refaster.anyOf(Mono.empty(), Flux.empty())));
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Class<? extends Throwable> clazz) {
      return flux.onErrorComplete(clazz);
    }
  }

  /** Prefer {@link Mono#onErrorComplete(Predicate)}} over more contrived alternatives. */
  static final class MonoOnErrorCompletePredicate<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Predicate<? super Throwable> predicate) {
      return mono.onErrorResume(predicate, e -> Mono.empty());
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Predicate<? super Throwable> predicate) {
      return mono.onErrorComplete(predicate);
    }
  }

  /** Prefer {@link Flux#onErrorComplete(Predicate)}} over more contrived alternatives. */
  static final class FluxOnErrorCompletePredicate<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Predicate<? super Throwable> predicate) {
      return flux.onErrorResume(predicate, e -> Refaster.anyOf(Mono.empty(), Flux.empty()));
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Predicate<? super Throwable> predicate) {
      return flux.onErrorComplete(predicate);
    }
  }

  /**
   * Prefer {@link Mono#onErrorContinue(Class, BiConsumer)} over {@link
   * Mono#onErrorContinue(Predicate, BiConsumer)} where possible.
   */
  static final class MonoOnErrorContinue<T> {
    @BeforeTemplate
    Mono<T> before(
        Mono<T> mono,
        Class<? extends Throwable> clazz,
        BiConsumer<Throwable, Object> errorConsumer) {
      return mono.onErrorContinue(clazz::isInstance, errorConsumer);
    }

    @AfterTemplate
    Mono<T> after(
        Mono<T> mono,
        Class<? extends Throwable> clazz,
        BiConsumer<Throwable, Object> errorConsumer) {
      return mono.onErrorContinue(clazz, errorConsumer);
    }
  }

  /**
   * Prefer {@link Flux#onErrorContinue(Class, BiConsumer)} over {@link
   * Flux#onErrorContinue(Predicate, BiConsumer)} where possible.
   */
  static final class FluxOnErrorContinue<T> {
    @BeforeTemplate
    Flux<T> before(
        Flux<T> flux,
        Class<? extends Throwable> clazz,
        BiConsumer<Throwable, Object> errorConsumer) {
      return flux.onErrorContinue(clazz::isInstance, errorConsumer);
    }

    @AfterTemplate
    Flux<T> after(
        Flux<T> flux,
        Class<? extends Throwable> clazz,
        BiConsumer<Throwable, Object> errorConsumer) {
      return flux.onErrorContinue(clazz, errorConsumer);
    }
  }

  /**
   * Prefer {@link Mono#onErrorMap(Class, Function)} over {@link Mono#onErrorMap(Predicate,
   * Function)} where possible.
   */
  static final class MonoOnErrorMap<T> {
    @BeforeTemplate
    Mono<T> before(
        Mono<T> mono,
        Class<? extends Throwable> clazz,
        Function<? super Throwable, ? extends Throwable> mapper) {
      return mono.onErrorMap(clazz::isInstance, mapper);
    }

    @AfterTemplate
    Mono<T> after(
        Mono<T> mono,
        Class<? extends Throwable> clazz,
        Function<? super Throwable, ? extends Throwable> mapper) {
      return mono.onErrorMap(clazz, mapper);
    }
  }

  /**
   * Prefer {@link Flux#onErrorMap(Class, Function)} over {@link Flux#onErrorMap(Predicate,
   * Function)} where possible.
   */
  static final class FluxOnErrorMap<T> {
    @BeforeTemplate
    Flux<T> before(
        Flux<T> flux,
        Class<? extends Throwable> clazz,
        Function<? super Throwable, ? extends Throwable> mapper) {
      return flux.onErrorMap(clazz::isInstance, mapper);
    }

    @AfterTemplate
    Flux<T> after(
        Flux<T> flux,
        Class<? extends Throwable> clazz,
        Function<? super Throwable, ? extends Throwable> mapper) {
      return flux.onErrorMap(clazz, mapper);
    }
  }

  /**
   * Prefer {@link Mono#onErrorResume(Class, Function)} over {@link Mono#onErrorResume(Predicate,
   * Function)} where possible.
   */
  static final class MonoOnErrorResume<T> {
    @BeforeTemplate
    Mono<T> before(
        Mono<T> mono,
        Class<? extends Throwable> clazz,
        Function<? super Throwable, ? extends Mono<? extends T>> fallback) {
      return mono.onErrorResume(clazz::isInstance, fallback);
    }

    @AfterTemplate
    Mono<T> after(
        Mono<T> mono,
        Class<? extends Throwable> clazz,
        Function<? super Throwable, ? extends Mono<? extends T>> fallback) {
      return mono.onErrorResume(clazz, fallback);
    }
  }

  /**
   * Prefer {@link Flux#onErrorResume(Class, Function)} over {@link Flux#onErrorResume(Predicate,
   * Function)} where possible.
   */
  static final class FluxOnErrorResume<T> {
    @BeforeTemplate
    Flux<T> before(
        Flux<T> flux,
        Class<? extends Throwable> clazz,
        Function<? super Throwable, ? extends Publisher<? extends T>> fallback) {
      return flux.onErrorResume(clazz::isInstance, fallback);
    }

    @AfterTemplate
    Flux<T> after(
        Flux<T> flux,
        Class<? extends Throwable> clazz,
        Function<? super Throwable, ? extends Publisher<? extends T>> fallback) {
      return flux.onErrorResume(clazz, fallback);
    }
  }

  /**
   * Prefer {@link Mono#onErrorReturn(Class, Object)} over {@link Mono#onErrorReturn(Predicate,
   * Object)} where possible.
   */
  static final class MonoOnErrorReturn<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Class<? extends Throwable> clazz, T fallbackValue) {
      return mono.onErrorReturn(clazz::isInstance, fallbackValue);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Class<? extends Throwable> clazz, T fallbackValue) {
      return mono.onErrorReturn(clazz, fallbackValue);
    }
  }

  /**
   * Prefer {@link Flux#onErrorReturn(Class, Object)} over {@link Flux#onErrorReturn(Predicate,
   * Object)} where possible.
   */
  static final class FluxOnErrorReturn<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Class<? extends Throwable> clazz, T fallbackValue) {
      return flux.onErrorReturn(clazz::isInstance, fallbackValue);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Class<? extends Throwable> clazz, T fallbackValue) {
      return flux.onErrorReturn(clazz, fallbackValue);
    }
  }

  /**
   * Apply {@link Flux#filter(Predicate)} before {@link Flux#sort()} to reduce the number of
   * elements to sort.
   */
  static final class FluxFilterSort<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Predicate<? super T> predicate) {
      return flux.sort().filter(predicate);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Predicate<? super T> predicate) {
      return flux.filter(predicate).sort();
    }
  }

  /**
   * Apply {@link Flux#filter(Predicate)} before {@link Flux#sort(Comparator)} to reduce the number
   * of elements to sort.
   */
  static final class FluxFilterSortWithComparator<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Predicate<? super T> predicate, Comparator<? super T> comparator) {
      return flux.sort(comparator).filter(predicate);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Predicate<? super T> predicate, Comparator<? super T> comparator) {
      return flux.filter(predicate).sort(comparator);
    }
  }

  /**
   * Apply {@link Flux#distinct()} before {@link Flux#sort()} to reduce the number of elements to
   * sort.
   */
  static final class FluxDistinctSort<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux) {
      return flux.sort().distinct();
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux) {
      return flux.distinct().sort();
    }
  }

  /**
   * Apply {@link Flux#distinct()} before {@link Flux#sort(Comparator)} to reduce the number of
   * elements to sort.
   */
  static final class FluxDistinctSortWithComparator<S, T extends S> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Comparator<S> comparator) {
      return flux.sort(comparator).distinct();
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Comparator<S> comparator) {
      return flux.distinct().sort(comparator);
    }
  }

  /**
   * Do not unnecessarily {@link Flux#filter(Predicate) filter} the result of {@link
   * Flux#takeWhile(Predicate)} using the same {@link Predicate}.
   */
  static final class FluxTakeWhile<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Predicate<? super T> predicate) {
      return flux.takeWhile(predicate).filter(predicate);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Predicate<? super T> predicate) {
      return flux.takeWhile(predicate);
    }
  }

  /**
   * Prefer {@link Flux#collect(Collector)} with {@link ImmutableList#toImmutableList()} over
   * alternatives that do not explicitly return an immutable collection.
   */
  static final class FluxCollectToImmutableList<T> {
    @BeforeTemplate
    Mono<List<T>> before(Flux<T> flux) {
      return flux.collectList();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Mono<ImmutableList<T>> after(Flux<T> flux) {
      return flux.collect(toImmutableList());
    }
  }

  /**
   * Prefer {@link Flux#collect(Collector)} with {@link ImmutableSet#toImmutableSet()} over more
   * contrived alternatives.
   */
  static final class FluxCollectToImmutableSet<T> {
    @BeforeTemplate
    Mono<ImmutableSet<T>> before(Flux<T> flux) {
      return flux.collect(toImmutableList()).map(ImmutableSet::copyOf);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Mono<ImmutableSet<T>> after(Flux<T> flux) {
      return flux.collect(toImmutableSet());
    }
  }

  /** Prefer {@link Flux#sort()} over more verbose alternatives. */
  static final class FluxSort<T extends Comparable<? super T>> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux) {
      return flux.sort(naturalOrder());
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux) {
      return flux.sort();
    }
  }

  /** Prefer {@link MathFlux#min(Publisher)} over less efficient alternatives. */
  static final class FluxTransformMin<T extends Comparable<? super T>> {
    @BeforeTemplate
    Mono<T> before(Flux<T> flux) {
      return flux.sort().next();
    }

    @AfterTemplate
    Mono<T> after(Flux<T> flux) {
      return flux.transform(MathFlux::min).singleOrEmpty();
    }
  }

  /**
   * Prefer {@link MathFlux#min(Publisher, Comparator)} over less efficient or more verbose
   * alternatives.
   */
  static final class FluxTransformMinWithComparator<T extends Comparable<? super T>> {
    @BeforeTemplate
    Mono<T> before(Flux<T> flux, Comparator<? super T> cmp) {
      return Refaster.anyOf(
          flux.sort(cmp).next(), flux.collect(minBy(cmp)).flatMap(Mono::justOrEmpty));
    }

    @AfterTemplate
    Mono<T> after(Flux<T> flux, Comparator<? super T> cmp) {
      return flux.transform(f -> MathFlux.min(f, cmp)).singleOrEmpty();
    }
  }

  /** Prefer {@link MathFlux#max(Publisher)} over less efficient alternatives. */
  static final class FluxTransformMax<T extends Comparable<? super T>> {
    @BeforeTemplate
    Mono<T> before(Flux<T> flux) {
      return flux.sort().last();
    }

    @AfterTemplate
    Mono<T> after(Flux<T> flux) {
      return flux.transform(MathFlux::max).singleOrEmpty();
    }
  }

  /**
   * Prefer {@link MathFlux#max(Publisher, Comparator)} over less efficient or more verbose
   * alternatives.
   */
  static final class FluxTransformMaxWithComparator<T extends Comparable<? super T>> {
    @BeforeTemplate
    Mono<T> before(Flux<T> flux, Comparator<? super T> cmp) {
      return Refaster.anyOf(
          flux.sort(cmp).last(), flux.collect(maxBy(cmp)).flatMap(Mono::justOrEmpty));
    }

    @AfterTemplate
    Mono<T> after(Flux<T> flux, Comparator<? super T> cmp) {
      return flux.transform(f -> MathFlux.max(f, cmp)).singleOrEmpty();
    }
  }

  /** Prefer {@link MathFlux#min(Publisher)} over more contrived alternatives. */
  static final class MathFluxMin<T extends Comparable<? super T>> {
    @BeforeTemplate
    Mono<T> before(Publisher<T> publisher) {
      return Refaster.anyOf(
          MathFlux.min(publisher, naturalOrder()), MathFlux.max(publisher, reverseOrder()));
    }

    @AfterTemplate
    Mono<T> after(Publisher<T> publisher) {
      return MathFlux.min(publisher);
    }
  }

  /** Prefer {@link MathFlux#max(Publisher)} over more contrived alternatives. */
  static final class MathFluxMax<T extends Comparable<? super T>> {
    @BeforeTemplate
    Mono<T> before(Publisher<T> publisher) {
      return Refaster.anyOf(
          MathFlux.min(publisher, reverseOrder()), MathFlux.max(publisher, naturalOrder()));
    }

    @AfterTemplate
    Mono<T> after(Publisher<T> publisher) {
      return MathFlux.max(publisher);
    }
  }

  /** Prefer {@link reactor.util.context.Context#empty()}} over more verbose alternatives. */
  // XXX: Introduce Refaster rules or a `BugChecker` that maps `(Immutable)Map.of(k, v)` to
  // `Context.of(k, v)` and likewise for multi-pair overloads.
  static final class ContextEmpty {
    @BeforeTemplate
    Context before(@Matches(IsEmpty.class) Map<?, ?> map) {
      return Context.of(map);
    }

    @AfterTemplate
    Context after() {
      return Context.empty();
    }
  }

  /** Prefer {@link PublisherProbe#empty()}} over more verbose alternatives. */
  static final class PublisherProbeEmpty<T> {
    @BeforeTemplate
    PublisherProbe<T> before() {
      return PublisherProbe.of(Refaster.anyOf(Mono.empty(), Flux.empty()));
    }

    @AfterTemplate
    PublisherProbe<T> after() {
      return PublisherProbe.empty();
    }
  }

  /** Prefer {@link PublisherProbe#assertWasSubscribed()} over more verbose alternatives. */
  static final class PublisherProbeAssertWasSubscribed<T> {
    @BeforeTemplate
    void before(PublisherProbe<T> probe) {
      Refaster.anyOf(
          assertThat(probe.wasSubscribed()).isTrue(),
          assertThat(probe.subscribeCount()).isNotNegative(),
          assertThat(probe.subscribeCount()).isNotEqualTo(0),
          assertThat(probe.subscribeCount()).isPositive());
    }

    @AfterTemplate
    void after(PublisherProbe<T> probe) {
      probe.assertWasSubscribed();
    }
  }

  /** Prefer {@link PublisherProbe#assertWasNotSubscribed()} over more verbose alternatives. */
  static final class PublisherProbeAssertWasNotSubscribed<T> {
    @BeforeTemplate
    void before(PublisherProbe<T> probe) {
      Refaster.anyOf(
          assertThat(probe.wasSubscribed()).isFalse(),
          assertThat(probe.subscribeCount()).isEqualTo(0),
          assertThat(probe.subscribeCount()).isNotPositive());
    }

    @AfterTemplate
    void after(PublisherProbe<T> probe) {
      probe.assertWasNotSubscribed();
    }
  }

  /** Prefer {@link PublisherProbe#assertWasCancelled()} over more verbose alternatives. */
  static final class PublisherProbeAssertWasCancelled<T> {
    @BeforeTemplate
    void before(PublisherProbe<T> probe) {
      assertThat(probe.wasCancelled()).isTrue();
    }

    @AfterTemplate
    void after(PublisherProbe<T> probe) {
      probe.assertWasCancelled();
    }
  }

  /** Prefer {@link PublisherProbe#assertWasNotCancelled()} over more verbose alternatives. */
  static final class PublisherProbeAssertWasNotCancelled<T> {
    @BeforeTemplate
    void before(PublisherProbe<T> probe) {
      assertThat(probe.wasCancelled()).isFalse();
    }

    @AfterTemplate
    void after(PublisherProbe<T> probe) {
      probe.assertWasNotCancelled();
    }
  }

  /** Prefer {@link PublisherProbe#assertWasRequested()} over more verbose alternatives. */
  static final class PublisherProbeAssertWasRequested<T> {
    @BeforeTemplate
    void before(PublisherProbe<T> probe) {
      assertThat(probe.wasRequested()).isTrue();
    }

    @AfterTemplate
    void after(PublisherProbe<T> probe) {
      probe.assertWasRequested();
    }
  }

  /** Prefer {@link PublisherProbe#assertWasNotRequested()} over more verbose alternatives. */
  static final class PublisherProbeAssertWasNotRequested<T> {
    @BeforeTemplate
    void before(PublisherProbe<T> probe) {
      assertThat(probe.wasRequested()).isFalse();
    }

    @AfterTemplate
    void after(PublisherProbe<T> probe) {
      probe.assertWasNotRequested();
    }
  }

  /**
   * Prefer {@link Assertions#assertThat(boolean)} to check whether a {@link PublisherProbe} was
   * {@link PublisherProbe#wasSubscribed() subscribed to}, over more verbose alternatives.
   */
  static final class AssertThatPublisherProbeWasSubscribed<T> {
    @AlsoNegation
    @BeforeTemplate
    void before(PublisherProbe<T> probe, boolean wasSubscribed) {
      if (wasSubscribed) {
        probe.assertWasSubscribed();
      } else {
        probe.assertWasNotSubscribed();
      }
    }

    @AfterTemplate
    void after(PublisherProbe<T> probe, boolean wasSubscribed) {
      assertThat(probe.wasSubscribed()).isEqualTo(wasSubscribed);
    }
  }

  /**
   * Prefer {@link Assertions#assertThat(boolean)} to check whether a {@link PublisherProbe} was
   * {@link PublisherProbe#wasCancelled() cancelled}, over more verbose alternatives.
   */
  static final class AssertThatPublisherProbeWasCancelled<T> {
    @AlsoNegation
    @BeforeTemplate
    void before(PublisherProbe<T> probe, boolean wasCancelled) {
      if (wasCancelled) {
        probe.assertWasCancelled();
      } else {
        probe.assertWasNotCancelled();
      }
    }

    @AfterTemplate
    void after(PublisherProbe<T> probe, boolean wasCancelled) {
      assertThat(probe.wasCancelled()).isEqualTo(wasCancelled);
    }
  }

  /**
   * Prefer {@link Assertions#assertThat(boolean)} to check whether a {@link PublisherProbe} was
   * {@link PublisherProbe#wasRequested() requested}, over more verbose alternatives.
   */
  static final class AssertThatPublisherProbeWasRequested<T> {
    @AlsoNegation
    @BeforeTemplate
    void before(PublisherProbe<T> probe, boolean wasRequested) {
      if (wasRequested) {
        probe.assertWasRequested();
      } else {
        probe.assertWasNotRequested();
      }
    }

    @AfterTemplate
    void after(PublisherProbe<T> probe, boolean wasRequested) {
      assertThat(probe.wasRequested()).isEqualTo(wasRequested);
    }
  }

  /** Prefer {@link Mono#as(Function)} when creating a {@link StepVerifier}. */
  static final class StepVerifierFromMono<T> {
    @BeforeTemplate
    StepVerifier.FirstStep<? extends T> before(Mono<T> mono) {
      return Refaster.anyOf(StepVerifier.create(mono), mono.flux().as(StepVerifier::create));
    }

    @AfterTemplate
    StepVerifier.FirstStep<? extends T> after(Mono<T> mono) {
      return mono.as(StepVerifier::create);
    }
  }

  /** Prefer {@link Flux#as(Function)} when creating a {@link StepVerifier}. */
  static final class StepVerifierFromFlux<T> {
    @BeforeTemplate
    StepVerifier.FirstStep<? extends T> before(Flux<T> flux) {
      return StepVerifier.create(flux);
    }

    @AfterTemplate
    StepVerifier.FirstStep<? extends T> after(Flux<T> flux) {
      return flux.as(StepVerifier::create);
    }
  }

  /**
   * Prefer {@link StepVerifier#verify()} over a dangling {@link
   * StepVerifier#verifyThenAssertThat()}.
   */
  // XXX: Application of this rule (and several others in this class) will cause invalid code if the
  // result of the rewritten expression is dereferenced. Consider introducing a bug checker that
  // identifies rules that change the return type of an expression and annotates them accordingly.
  // The associated annotation can then be used to instruct an annotation processor to generate
  // corresponding `void` rules that match only statements. This would allow the `Refaster` check to
  // conditionally skip "not fully safe" rules. This allows conditionally flagging more dubious
  // code, at the risk of compilation failures. With this rule, for example, we want to explicitly
  // nudge users towards `StepVerifier.Step#assertNext(Consumer)` or
  // `StepVerifier.Step#expectNext(Object)`, together with `Step#verifyComplete()`.
  static final class StepVerifierVerify {
    @BeforeTemplate
    StepVerifier.Assertions before(StepVerifier stepVerifier) {
      return stepVerifier.verifyThenAssertThat();
    }

    @AfterTemplate
    Duration after(StepVerifier stepVerifier) {
      return stepVerifier.verify();
    }
  }

  /**
   * Prefer {@link StepVerifier#verify(Duration)} over a dangling {@link
   * StepVerifier#verifyThenAssertThat(Duration)}.
   */
  static final class StepVerifierVerifyDuration {
    @BeforeTemplate
    StepVerifier.Assertions before(StepVerifier stepVerifier, Duration duration) {
      return stepVerifier.verifyThenAssertThat(duration);
    }

    @AfterTemplate
    Duration after(StepVerifier stepVerifier, Duration duration) {
      return stepVerifier.verify(duration);
    }
  }

  /** Don't unnecessarily invoke {@link StepVerifier#verifyLater()} multiple times. */
  static final class StepVerifierVerifyLater {
    @BeforeTemplate
    StepVerifier before(StepVerifier stepVerifier) {
      return stepVerifier.verifyLater().verifyLater();
    }

    @AfterTemplate
    StepVerifier after(StepVerifier stepVerifier) {
      return stepVerifier.verifyLater();
    }
  }

  /** Don't unnecessarily have {@link StepVerifier.Step} expect no elements. */
  static final class StepVerifierStepIdentity<T> {
    @BeforeTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    StepVerifier.Step<T> before(
        StepVerifier.Step<T> step, @Matches(IsEmpty.class) Iterable<? extends T> iterable) {
      return Refaster.anyOf(
          step.expectNext(), step.expectNextCount(0), step.expectNextSequence(iterable));
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    StepVerifier.Step<T> after(StepVerifier.Step<T> step) {
      return step;
    }
  }

  /** Prefer {@link StepVerifier.Step#expectNext(Object)} over more verbose alternatives. */
  static final class StepVerifierStepExpectNext<T> {
    @BeforeTemplate
    StepVerifier.Step<T> before(StepVerifier.Step<T> step, T object) {
      return Refaster.anyOf(
          step.expectNextMatches(e -> e.equals(object)), step.expectNextMatches(object::equals));
    }

    @AfterTemplate
    StepVerifier.Step<T> after(StepVerifier.Step<T> step, T object) {
      return step.expectNext(object);
    }
  }

  /** Avoid list collection when verifying that a {@link Flux} emits exactly one value. */
  // XXX: This rule assumes that the matched collector does not drop elements. Consider introducing
  // a `@Matches(DoesNotDropElements.class)` or `@NotMatches(MayDropElements.class)` guard.
  static final class FluxAsStepVerifierExpectNext<T, L extends List<T>> {
    @BeforeTemplate
    StepVerifier.Step<L> before(Flux<T> flux, T object, Collector<? super T, ?, L> listCollector) {
      return flux.collect(listCollector)
          .as(StepVerifier::create)
          .assertNext(list -> assertThat(list).containsExactly(object));
    }

    @AfterTemplate
    StepVerifier.Step<T> after(Flux<T> flux, T object) {
      return flux.as(StepVerifier::create).expectNext(object);
    }
  }

  /** Prefer {@link StepVerifier.LastStep#verifyComplete()} over more verbose alternatives. */
  static final class StepVerifierLastStepVerifyComplete {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep step) {
      return step.expectComplete().verify();
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep step) {
      return step.verifyComplete();
    }
  }

  /** Prefer {@link StepVerifier.LastStep#verifyError()} over more verbose alternatives. */
  static final class StepVerifierLastStepVerifyError {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep step) {
      return step.expectError().verify();
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep step) {
      return step.verifyError();
    }
  }

  /** Prefer {@link StepVerifier.LastStep#verifyError(Class)} over more verbose alternatives. */
  static final class StepVerifierLastStepVerifyErrorClass<T extends Throwable> {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep step, Class<T> clazz) {
      return Refaster.anyOf(
          step.expectError(clazz).verify(),
          step.verifyErrorMatches(clazz::isInstance),
          step.verifyErrorSatisfies(t -> assertThat(t).isInstanceOf(clazz)));
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep step, Class<T> clazz) {
      return step.verifyError(clazz);
    }
  }

  /**
   * Prefer {@link StepVerifier.LastStep#verifyErrorMatches(Predicate)} over more verbose
   * alternatives.
   */
  static final class StepVerifierLastStepVerifyErrorMatches {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep step, Predicate<Throwable> predicate) {
      return step.expectErrorMatches(predicate).verify();
    }

    @BeforeTemplate
    @SuppressWarnings("StepVerifierVerify" /* This is a more specific template. */)
    StepVerifier.Assertions before2(StepVerifier.LastStep step, Predicate<Throwable> predicate) {
      return step.expectError().verifyThenAssertThat().hasOperatorErrorMatching(predicate);
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep step, Predicate<Throwable> predicate) {
      return step.verifyErrorMatches(predicate);
    }
  }

  /**
   * Prefer {@link StepVerifier.LastStep#verifyErrorSatisfies(Consumer)} over more verbose
   * alternatives.
   */
  static final class StepVerifierLastStepVerifyErrorSatisfies {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep step, Consumer<Throwable> consumer) {
      return step.expectErrorSatisfies(consumer).verify();
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep step, Consumer<Throwable> consumer) {
      return step.verifyErrorSatisfies(consumer);
    }
  }

  /**
   * Prefer {@link StepVerifier.LastStep#verifyErrorSatisfies(Consumer)} with AssertJ over more
   * contrived alternatives.
   */
  static final class StepVerifierLastStepVerifyErrorSatisfiesAssertJ<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings("StepVerifierVerify" /* This is a more specific template. */)
    StepVerifier.Assertions before(StepVerifier.LastStep step, Class<T> clazz, String message) {
      return Refaster.anyOf(
          step.expectError()
              .verifyThenAssertThat()
              .hasOperatorErrorOfType(clazz)
              .hasOperatorErrorWithMessage(message),
          step.expectError(clazz).verifyThenAssertThat().hasOperatorErrorWithMessage(message),
          step.expectErrorMessage(message).verifyThenAssertThat().hasOperatorErrorOfType(clazz));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Duration after(StepVerifier.LastStep step, Class<T> clazz, String message) {
      return step.verifyErrorSatisfies(t -> assertThat(t).isInstanceOf(clazz).hasMessage(message));
    }
  }

  /**
   * Prefer {@link StepVerifier.LastStep#verifyErrorMessage(String)} over more verbose alternatives.
   */
  static final class StepVerifierLastStepVerifyErrorMessage {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep step, String message) {
      return step.expectErrorMessage(message).verify();
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep step, String message) {
      return step.verifyErrorMessage(message);
    }
  }

  /**
   * Prefer {@link StepVerifier.LastStep#verifyTimeout(Duration)} over more verbose alternatives.
   */
  static final class StepVerifierLastStepVerifyTimeout {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep step, Duration duration) {
      return step.expectTimeout(duration).verify();
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep step, Duration duration) {
      return step.verifyTimeout(duration);
    }
  }

  /**
   * Prefer {@link Mono#fromFuture(Supplier)} over {@link Mono#fromFuture(CompletableFuture)}, as
   * the former may defer initiation of the asynchronous computation until subscription.
   */
  static final class MonoFromFutureSupplier<T> {
    // XXX: Constrain the `future` parameter using `@NotMatches(IsIdentityOperation.class)` once
    // `IsIdentityOperation` no longer matches nullary method invocations.
    @BeforeTemplate
    Mono<T> before(CompletableFuture<T> future) {
      return Mono.fromFuture(future);
    }

    @AfterTemplate
    Mono<T> after(CompletableFuture<T> future) {
      return Mono.fromFuture(() -> future);
    }
  }

  /**
   * Prefer {@link Mono#fromFuture(Supplier, boolean)} over {@link
   * Mono#fromFuture(CompletableFuture, boolean)}, as the former may defer initiation of the
   * asynchronous computation until subscription.
   */
  static final class MonoFromFutureSupplierBoolean<T> {
    // XXX: Constrain the `future` parameter using `@NotMatches(IsIdentityOperation.class)` once
    // `IsIdentityOperation` no longer matches nullary method invocations.
    @BeforeTemplate
    Mono<T> before(CompletableFuture<T> future, boolean suppressCancel) {
      return Mono.fromFuture(future, suppressCancel);
    }

    @AfterTemplate
    Mono<T> after(CompletableFuture<T> future, boolean suppressCancel) {
      return Mono.fromFuture(() -> future, suppressCancel);
    }
  }

  /**
   * Don't propagate {@link Mono} cancellations to an upstream cache value computation, as
   * completion of such computations may benefit concurrent or subsequent cache usages.
   */
  static final class MonoFromFutureAsyncLoadingCacheGet<K, V> {
    @BeforeTemplate
    Mono<V> before(AsyncLoadingCache<K, V> cache, K key) {
      return Mono.fromFuture(() -> cache.get(key));
    }

    @AfterTemplate
    Mono<V> after(AsyncLoadingCache<K, V> cache, K key) {
      return Mono.fromFuture(() -> cache.get(key), /* suppressCancel= */ true);
    }
  }

  /**
   * Don't propagate {@link Mono} cancellations to upstream cache value computations, as completion
   * of such computations may benefit concurrent or subsequent cache usages.
   */
  static final class MonoFromFutureAsyncLoadingCacheGetAll<K1, K2 extends K1, V> {
    @BeforeTemplate
    Mono<Map<K1, V>> before(AsyncLoadingCache<K1, V> cache, Iterable<K2> keys) {
      return Mono.fromFuture(() -> cache.getAll(keys));
    }

    @AfterTemplate
    Mono<Map<K1, V>> after(AsyncLoadingCache<K1, V> cache, Iterable<K2> keys) {
      return Mono.fromFuture(() -> cache.getAll(keys), /* suppressCancel= */ true);
    }
  }

  /**
   * Prefer {@link Flux#fromStream(Supplier)} over {@link Flux#fromStream(Stream)}, as the former
   * yields a {@link Flux} that is more likely to behave as expected when subscribed to more than
   * once.
   */
  static final class FluxFromStreamSupplier<T> {
    // XXX: Constrain the `stream` parameter using `@NotMatches(IsIdentityOperation.class)` once
    // `IsIdentityOperation` no longer matches nullary method invocations.
    @BeforeTemplate
    Flux<T> before(Stream<T> stream) {
      return Flux.fromStream(stream);
    }

    @AfterTemplate
    Flux<T> after(Stream<T> stream) {
      return Flux.fromStream(() -> stream);
    }
  }

  /** Prefer fluent {@link Flux#next()} over less explicit alternatives. */
  static final class FluxNext<T> {
    @BeforeTemplate
    Mono<T> before(Flux<T> flux) {
      return Mono.from(flux);
    }

    @AfterTemplate
    Mono<T> after(Flux<T> flux) {
      return flux.next();
    }
  }
}
