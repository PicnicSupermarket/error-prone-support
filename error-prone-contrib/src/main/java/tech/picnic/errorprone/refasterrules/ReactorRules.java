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
import reactor.test.StepVerifier.FirstStep;
import reactor.test.publisher.PublisherProbe;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import tech.picnic.errorprone.refaster.annotation.Description;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;
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
   * Prefer {@link Mono#timeout(Duration, Mono)} over more contrived or less efficient alternatives.
   */
  static final class MonoTimeoutMonoEmptyDuration<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Duration timeout) {
      return mono.timeout(timeout).onErrorComplete(TimeoutException.class);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Duration timeout) {
      return mono.timeout(timeout, Mono.empty());
    }
  }

  /**
   * Prefer {@link Mono#timeout(Duration, Mono)} over more contrived or less efficient alternatives.
   */
  static final class MonoTimeoutMonoJustDuration<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Duration timeout, T data) {
      return mono.timeout(timeout).onErrorReturn(TimeoutException.class, data);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Duration timeout, T data) {
      return mono.timeout(timeout, Mono.just(data));
    }
  }

  /**
   * Prefer {@link Mono#timeout(Duration, Mono)} over more contrived or less efficient alternatives.
   */
  static final class MonoTimeoutDuration<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Duration timeout, Mono<T> fallback) {
      return mono.timeout(timeout).onErrorResume(TimeoutException.class, e -> fallback);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Duration timeout, Mono<T> fallback) {
      return mono.timeout(timeout, fallback);
    }
  }

  /**
   * Prefer {@link Mono#timeout(Publisher, Mono)} over more contrived or less efficient
   * alternatives.
   */
  static final class MonoTimeoutMonoEmptyPublisher<T, S> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Publisher<S> firstTimeout) {
      return mono.timeout(firstTimeout).onErrorComplete(TimeoutException.class);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Publisher<S> firstTimeout) {
      return mono.timeout(firstTimeout, Mono.empty());
    }
  }

  /**
   * Prefer {@link Mono#timeout(Publisher, Mono)} over more contrived or less efficient
   * alternatives.
   */
  static final class MonoTimeoutMonoJustPublisher<T, S> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Publisher<S> firstTimeout, T data) {
      return mono.timeout(firstTimeout).onErrorReturn(TimeoutException.class, data);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Publisher<S> firstTimeout, T data) {
      return mono.timeout(firstTimeout, Mono.just(data));
    }
  }

  /**
   * Prefer {@link Mono#timeout(Publisher, Mono)} over more contrived or less efficient
   * alternatives.
   */
  static final class MonoTimeoutPublisher<T, S> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Publisher<S> firstTimeout, Mono<T> fallback) {
      return mono.timeout(firstTimeout).onErrorResume(TimeoutException.class, e -> fallback);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Publisher<S> firstTimeout, Mono<T> fallback) {
      return mono.timeout(firstTimeout, fallback);
    }
  }

  /** Prefer {@link Mono#just(Object)} over more contrived alternatives. */
  static final class MonoJust<T> {
    @BeforeTemplate
    Mono<T> before(T data) {
      return Refaster.anyOf(Mono.justOrEmpty(Optional.of(data)), Flux.just(data).next());
    }

    @AfterTemplate
    Mono<T> after(T data) {
      return Mono.just(data);
    }
  }

  /** Prefer {@link Mono#justOrEmpty(Object)} over more contrived alternatives. */
  static final class MonoJustOrEmptyObject<T extends @Nullable Object> {
    @BeforeTemplate
    Mono<T> before(T data) {
      return Mono.justOrEmpty(Optional.ofNullable(data));
    }

    @AfterTemplate
    Mono<T> after(T data) {
      return Mono.justOrEmpty(data);
    }
  }

  /** Prefer {@link Mono#justOrEmpty(Optional)} over more verbose alternatives. */
  static final class MonoJustOrEmptyOptional<T> {
    @BeforeTemplate
    Mono<T> before(Optional<T> data) {
      return Mono.just(data).filter(Optional::isPresent).map(Optional::orElseThrow);
    }

    @AfterTemplate
    Mono<T> after(Optional<T> data) {
      return Mono.justOrEmpty(data);
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
    Mono<T> before(Optional<T> data) {
      return Refaster.anyOf(
          Mono.fromCallable(() -> data.orElse(null)), Mono.fromSupplier(() -> data.orElse(null)));
    }

    @AfterTemplate
    Mono<T> after(Optional<T> data) {
      return Mono.defer(() -> Mono.justOrEmpty(data));
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
  static final class MonoJustOrEmptySwitchIfEmpty<T> {
    @BeforeTemplate
    Mono<T> before(Optional<T> data, Mono<T> alternate) {
      return data.map(Mono::just).orElse(alternate);
    }

    @AfterTemplate
    Mono<T> after(Optional<T> data, Mono<T> alternate) {
      return Mono.justOrEmpty(data).switchIfEmpty(alternate);
    }
  }

  /**
   * Prefer {@link Mono#zip(Mono, Mono)} over a chained {@link Mono#zipWith(Mono)}, as the former
   * better conveys that the {@link Mono}s may be subscribed to concurrently, and generalizes to
   * combining three or more reactive streams.
   */
  static final class MonoZip<T, S> {
    @BeforeTemplate
    Mono<Tuple2<T, S>> before(Mono<T> p1, Mono<S> p2) {
      return p1.zipWith(p2);
    }

    @AfterTemplate
    Mono<Tuple2<T, S>> after(Mono<T> p1, Mono<S> p2) {
      return Mono.zip(p1, p2);
    }
  }

  /**
   * Prefer {@link Mono#zip(Mono, Mono)} with a chained combinator over a chained {@link
   * Mono#zipWith(Mono, BiFunction)}, as the former better conveys that the {@link Mono}s may be
   * subscribed to concurrently, and generalizes to combining three or more reactive streams.
   */
  static final class MonoZipMapFunction<T, S, R> {
    @BeforeTemplate
    Mono<R> before(Mono<T> p1, Mono<S> p2, BiFunction<T, S, R> function) {
      return p1.zipWith(p2, function);
    }

    @AfterTemplate
    Mono<R> after(Mono<T> p1, Mono<S> p2, BiFunction<T, S, R> function) {
      return Mono.zip(p1, p2).map(function(function));
    }
  }

  /**
   * Prefer {@link Flux#zip(Publisher, Publisher)} over a chained {@link Flux#zipWith(Publisher)},
   * as the former better conveys that the {@link Publisher}s may be subscribed to concurrently, and
   * generalizes to combining three or more reactive streams.
   */
  static final class FluxZip<T, S> {
    @BeforeTemplate
    Flux<Tuple2<T, S>> before(Flux<T> source1, Publisher<S> source2) {
      return source1.zipWith(source2);
    }

    @AfterTemplate
    Flux<Tuple2<T, S>> after(Flux<T> source1, Publisher<S> source2) {
      return Flux.zip(source1, source2);
    }
  }

  /**
   * Prefer {@link Flux#zip(Publisher, Publisher)} with a chained combinator over a chained {@link
   * Flux#zipWith(Publisher, BiFunction)}, as the former better conveys that the {@link Publisher}s
   * may be subscribed to concurrently, and generalizes to combining three or more reactive streams.
   */
  static final class FluxZipMapFunction<T, S, R> {
    @BeforeTemplate
    Flux<R> before(Flux<T> source1, Publisher<S> source2, BiFunction<T, S, R> function) {
      return source1.zipWith(source2, function);
    }

    @AfterTemplate
    Flux<R> after(Flux<T> source1, Publisher<S> source2, BiFunction<T, S, R> function) {
      return Flux.zip(source1, source2).map(function(function));
    }
  }

  /** Prefer {@link Flux#zipWithIterable(Iterable)} over more contrived alternatives. */
  static final class FluxZipWithIterable<T, S> {
    @BeforeTemplate
    Flux<Tuple2<T, S>> before(Flux<T> source1, Iterable<S> iterable) {
      return Flux.zip(source1, Flux.fromIterable(iterable));
    }

    @AfterTemplate
    Flux<Tuple2<T, S>> after(Flux<T> source1, Iterable<S> iterable) {
      return source1.zipWithIterable(iterable);
    }
  }

  /** Prefer {@link Flux#zipWithIterable(Iterable, BiFunction)} over more contrived alternatives. */
  static final class FluxZipWithIterableWithBiFunction<T, S, R> {
    @BeforeTemplate
    Flux<R> before(
        Flux<T> flux, Iterable<S> iterable, BiFunction<? super T, ? super S, ? extends R> zipper) {
      return flux.zipWith(Flux.fromIterable(iterable), zipper);
    }

    @AfterTemplate
    Flux<R> after(
        Flux<T> flux, Iterable<S> iterable, BiFunction<? super T, ? super S, ? extends R> zipper) {
      return flux.zipWithIterable(iterable, zipper);
    }
  }

  /**
   * Prefer {@link Flux#zipWithIterable(Iterable)} with a chained combinator over {@link
   * Flux#zipWithIterable(Iterable, BiFunction)}, as the former generally yields more readable code.
   */
  static final class FluxZipWithIterableMapFunction<T, S, R> {
    @BeforeTemplate
    Flux<R> before(Flux<T> flux, Iterable<S> iterable, BiFunction<T, S, R> function) {
      return flux.zipWithIterable(iterable, function);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Flux<R> after(Flux<T> flux, Iterable<S> iterable, BiFunction<T, S, R> function) {
      return flux.zipWithIterable(iterable).map(function(function));
    }
  }

  /**
   * Prefer {@link Mono#error(Supplier)} over unnecessarily deferring {@link Mono#error(Throwable)}.
   */
  static final class MonoErrorThrowable<T> {
    @BeforeTemplate
    Mono<T> before(Throwable error) {
      return Mono.defer(() -> Mono.error(error));
    }

    @AfterTemplate
    Mono<T> after(Throwable error) {
      return Mono.error(() -> error);
    }
  }

  /**
   * Prefer {@link Flux#error(Supplier)} over unnecessarily deferring {@link Flux#error(Throwable)}.
   */
  static final class FluxErrorThrowable<T> {
    @BeforeTemplate
    Flux<T> before(Throwable error) {
      return Flux.defer(() -> Flux.error(error));
    }

    @AfterTemplate
    Flux<T> after(Throwable error) {
      return Flux.error(() -> error);
    }
  }

  /**
   * Prefer passing {@link Mono#error(Supplier)} a direct supplier reference over a lambda or method
   * reference that invokes another supplier.
   */
  // XXX: Drop this rule once the more general rule `AssortedRules#SupplierAsSupplier` works
  // reliably.
  static final class MonoErrorSupplier<T, E extends Throwable> {
    @BeforeTemplate
    Mono<T> before(Supplier<E> errorSupplier) {
      return Mono.error(() -> errorSupplier.get());
    }

    @AfterTemplate
    Mono<T> after(Supplier<E> errorSupplier) {
      return Mono.error(errorSupplier);
    }
  }

  /**
   * Prefer passing {@link Flux#error(Supplier)} a direct supplier reference over a lambda or method
   * reference that invokes another supplier.
   */
  // XXX: Drop this rule once the more general rule `AssortedRules#SupplierAsSupplier` works
  // reliably.
  static final class FluxErrorSupplier<T, E extends Throwable> {
    @BeforeTemplate
    Flux<T> before(Supplier<E> errorSupplier) {
      return Flux.error(() -> errorSupplier.get());
    }

    @AfterTemplate
    Flux<T> after(Supplier<E> errorSupplier) {
      return Flux.error(errorSupplier);
    }
  }

  /** Prefer {@link Mono#thenReturn(Object)} over more verbose alternatives. */
  static final class MonoThenReturn<T, S> {
    @BeforeTemplate
    Mono<S> before(Mono<T> mono, S value) {
      return Refaster.anyOf(
          mono.ignoreElement().thenReturn(value),
          mono.then().thenReturn(value),
          mono.then(Mono.just(value)));
    }

    @AfterTemplate
    Mono<S> after(Mono<T> mono, S value) {
      return mono.thenReturn(value);
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
    Mono<T> before(Mono<T> mono, T defaultV) {
      return mono.switchIfEmpty(Mono.just(defaultV));
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, T defaultV) {
      return mono.defaultIfEmpty(defaultV);
    }
  }

  /** Prefer {@link Flux#defaultIfEmpty(Object)} over more contrived alternatives. */
  static final class FluxDefaultIfEmpty<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, T defaultV) {
      return flux.switchIfEmpty(Refaster.anyOf(Mono.just(defaultV), Flux.just(defaultV)));
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, T defaultV) {
      return flux.defaultIfEmpty(defaultV);
    }
  }

  /** Prefer {@link Flux#empty()} over more contrived alternatives. */
  // XXX: Using `@Matches(IsEmpty.class)`, the non-varargs overloads of most methods referenced here
  // can be rewritten as well. That would require adding a bunch more suitably-typed parameters.
  @PossibleSourceIncompatibility
  static final class FluxEmpty<T, S extends Comparable<? super S>> {
    // XXX: The methods enumerated here are not ordered entirely lexicographically, to accommodate a
    // conflict between the `InconsistentOverloads` and `RefasterMethodParameterOrder` checks.
    @BeforeTemplate
    Flux<T> before(
        Function<? super Object[], ? extends T> combinator,
        int prefetch,
        Comparator<? super T> comparator,
        @Matches(IsEmpty.class) T[] emptyArray,
        @Matches(IsEmpty.class) Iterable<T> emptyIt,
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
          Flux.fromIterable(emptyIt),
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
   * Prefer {@link Flux#timeout(Duration, Publisher)} over more contrived or less efficient
   * alternatives.
   */
  static final class FluxTimeoutFluxEmpty<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Duration timeout) {
      return flux.timeout(timeout).onErrorComplete(TimeoutException.class);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Duration timeout) {
      return flux.timeout(timeout, Flux.empty());
    }
  }

  /** Prefer {@link Flux#just(Object)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class FluxJust<T> {
    @BeforeTemplate
    Flux<Integer> before(int data) {
      return Flux.range(data, 1);
    }

    @BeforeTemplate
    Flux<T> before(T data) {
      return Refaster.anyOf(
          Mono.just(data).flux(),
          Flux.fromStream(() -> Stream.of(data)),
          Mono.just(data).repeat().take(1));
    }

    @AfterTemplate
    Flux<T> after(T data) {
      return Flux.just(data);
    }
  }

  /** Prefer {@link Flux#just(Object[])} over more contrived alternatives. */
  static final class FluxJustVarargs<T> {
    @BeforeTemplate
    Flux<T> before(@Repeated T data) {
      return Flux.fromStream(() -> Stream.of(Refaster.asVarargs(data)));
    }

    @AfterTemplate
    Flux<T> after(@Repeated T data) {
      return Flux.just(data);
    }
  }

  /** Prefer {@link Flux#fromArray(Object[])} over more ambiguous or contrived alternatives. */
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

  /**
   * Prefer using {@link Mono}s as-is over less efficient transformations to equivalent instances.
   */
  @PossibleSourceIncompatibility
  static final class MonoIdentity<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono) {
      return Refaster.anyOf(
          mono.switchIfEmpty(Mono.empty()), mono.flux().next(), mono.flux().singleOrEmpty());
    }

    @BeforeTemplate
    @SuppressWarnings("VoidMissingNullable" /* Suggestion is incompatible with Reactor API. */)
    Mono<Void> before2(Mono<Void> mono) {
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

  /**
   * Prefer using {@link Mono#single()} or {@link Mono#singleOptional()} over unnecessarily
   * transforming a {@link Mono} to a {@link Flux}.
   */
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
  static final class MonoUsingWithBoolean<D extends AutoCloseable, T> {
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
  static final class MonoUsingWithConsumer<D, T> {
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
  static final class MonoUsingWithConsumerAndBoolean<D, T> {
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
  static final class MonoUsingWhen<D, T> {
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
  static final class MonoUsingWhenWithBiFunctionAndFunction<D, T> {
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

  /**
   * Prefer using {@link Flux}s as-is over unnecessarily passing an empty publisher to {@link
   * Flux#switchIfEmpty(Publisher)}.
   */
  static final class FluxIdentity<T> {
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
        Function<? super T, ? extends P> mapper,
        @Matches(IsIdentityOperation.class)
            Function<? super P, ? extends Publisher<? extends S>> identityMapper) {
      return Refaster.anyOf(
          flux.concatMap(mapper, 0),
          flux.flatMap(mapper, 1),
          flux.flatMapSequential(mapper, 1),
          flux.map(mapper).concatMap(identityMapper));
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux, Function<? super T, ? extends P> mapper) {
      return flux.concatMap(mapper);
    }
  }

  /** Prefer {@link Flux#concatMap(Function, int)} over more contrived alternatives. */
  static final class FluxConcatMapWithInt<T, S, P extends Publisher<? extends S>> {
    @BeforeTemplate
    @SuppressWarnings("NestedPublishers")
    Flux<S> before(
        Flux<T> flux,
        Function<? super T, ? extends P> mapper,
        int prefetch,
        @Matches(IsIdentityOperation.class)
            Function<? super P, ? extends Publisher<? extends S>> identityMapper) {
      return Refaster.anyOf(
          flux.flatMap(mapper, 1, prefetch),
          flux.flatMapSequential(mapper, 1, prefetch),
          flux.map(mapper).concatMap(identityMapper, prefetch));
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux, Function<? super T, ? extends P> mapper, int prefetch) {
      return flux.concatMap(mapper, prefetch);
    }
  }

  /** Prefer {@link Mono#flatMapIterable(Function)} over more contrived alternatives. */
  static final class MonoFlatMapIterable<T, S, I extends Iterable<? extends S>> {
    @BeforeTemplate
    Flux<S> before(Mono<T> mono, Function<? super T, I> mapper) {
      return mono.map(mapper).flatMapMany(Flux::fromIterable);
    }

    @BeforeTemplate
    Flux<S> before(
        Mono<T> mono,
        Function<? super T, I> mapper,
        @Matches(IsIdentityOperation.class)
            Function<? super I, ? extends Iterable<? extends S>> identityMapper) {
      return Refaster.anyOf(
          mono.map(mapper).flatMapIterable(identityMapper), mono.flux().concatMapIterable(mapper));
    }

    @AfterTemplate
    Flux<S> after(Mono<T> mono, Function<? super T, I> mapper) {
      return mono.flatMapIterable(mapper);
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
   * Prefer {@link Flux#concatMapIterable(Function)} over alternatives with less explicit syntax or
   * semantics.
   */
  static final class FluxConcatMapIterable<T, S, I extends Iterable<? extends S>> {
    @BeforeTemplate
    Flux<S> before(
        Flux<T> flux,
        Function<? super T, I> mapper,
        @Matches(IsIdentityOperation.class)
            Function<? super I, ? extends Iterable<? extends S>> identityMapper) {
      return Refaster.anyOf(
          flux.flatMapIterable(mapper), flux.map(mapper).concatMapIterable(identityMapper));
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux, Function<? super T, ? extends Iterable<? extends S>> mapper) {
      return flux.concatMapIterable(mapper);
    }
  }

  /**
   * Prefer {@link Flux#concatMapIterable(Function, int)} over alternatives with less explicit
   * syntax or semantics.
   */
  static final class FluxConcatMapIterableWithInt<T, S, I extends Iterable<? extends S>> {
    @BeforeTemplate
    Flux<S> before(
        Flux<T> flux,
        Function<? super T, I> mapper,
        int prefetch,
        @Matches(IsIdentityOperation.class)
            Function<? super I, ? extends Iterable<? extends S>> identityMapper) {
      return Refaster.anyOf(
          flux.flatMapIterable(mapper, prefetch),
          flux.map(mapper).concatMapIterable(identityMapper, prefetch));
    }

    @AfterTemplate
    Flux<S> after(
        Flux<T> flux, Function<? super T, ? extends Iterable<? extends S>> mapper, int prefetch) {
      return flux.concatMapIterable(mapper, prefetch);
    }
  }

  /**
   * Prefer using {@link Mono#flatMap(Function)} followed by {@code .flux()} over {@link
   * Mono#flatMapMany(Function)} for explicit type conversion.
   */
  abstract static class MonoFlatMapFlux<T, S> {
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
    Flux<S> before(Flux<T> flux, int prefetch, boolean delayUntilEnd, int concurrency) {
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
          flux.flatMap(x -> Mono.just(transformation(x)), concurrency),
          flux.flatMap(x -> Flux.just(transformation(x)), concurrency),
          flux.flatMap(x -> Mono.just(transformation(x)), concurrency, prefetch),
          flux.flatMap(x -> Flux.just(transformation(x)), concurrency, prefetch),
          flux.flatMapDelayError(x -> Mono.just(transformation(x)), concurrency, prefetch),
          flux.flatMapDelayError(x -> Flux.just(transformation(x)), concurrency, prefetch),
          flux.flatMapSequential(x -> Mono.just(transformation(x)), concurrency),
          flux.flatMapSequential(x -> Flux.just(transformation(x)), concurrency),
          flux.flatMapSequential(x -> Mono.just(transformation(x)), concurrency, prefetch),
          flux.flatMapSequential(x -> Flux.just(transformation(x)), concurrency, prefetch),
          flux.flatMapSequentialDelayError(
              x -> Mono.just(transformation(x)), concurrency, prefetch),
          flux.flatMapSequentialDelayError(
              x -> Flux.just(transformation(x)), concurrency, prefetch),
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
    Flux<S> before(Flux<T> flux, int prefetch, boolean delayUntilEnd, int concurrency) {
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
              concurrency),
          flux.flatMap(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              concurrency,
              prefetch),
          flux.flatMapDelayError(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              concurrency,
              prefetch),
          flux.flatMapSequential(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              concurrency),
          flux.flatMapSequential(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              concurrency,
              prefetch),
          flux.flatMapSequentialDelayError(
              x ->
                  Refaster.anyOf(
                      Mono.justOrEmpty(transformation(x)),
                      Mono.fromSupplier(() -> transformation(x))),
              concurrency,
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
  abstract static class FluxMapNotNullOrElseNull<T, S> {
    @Placeholder(allowsIdentity = true)
    abstract Optional<S> transformation(@MayOptionallyUse T value);

    // XXX: Drop the `NullAway` suppression once https://github.com/uber/NullAway/issues/1522 is
    // resolved.
    @BeforeTemplate
    @SuppressWarnings("NullAway" /* `mapNotNull` result *is* `@Nullable`. */)
    Flux<S> before(Flux<T> flux) {
      return flux.map(v -> transformation(v)).mapNotNull(o -> o.orElse(null));
    }

    // XXX: Drop the `NullAway` suppression once https://github.com/uber/NullAway/issues/1522 is
    // resolved.
    @AfterTemplate
    @SuppressWarnings("NullAway" /* `mapNotNull` result *is* `@Nullable`. */)
    Flux<S> after(Flux<T> flux) {
      return flux.mapNotNull(x -> transformation(x).orElse(null));
    }
  }

  /** Prefer {@link Flux#mapNotNull(Function)} over more contrived alternatives. */
  static final class FluxMapNotNullOptionalOrElseNull<T> {
    @BeforeTemplate
    Flux<T> before(Flux<Optional<T>> flux) {
      return flux.filter(Optional::isPresent).map(Optional::orElseThrow);
    }

    // XXX: Drop the `NullAway` suppression once https://github.com/uber/NullAway/issues/1522 is
    // resolved.
    @AfterTemplate
    @SuppressWarnings("NullAway" /* `mapNotNull` result *is* `@Nullable`. */)
    Flux<T> after(Flux<Optional<T>> flux) {
      return flux.mapNotNull(x -> x.orElse(null));
    }
  }

  /** Prefer {@link Mono#flux()} over more contrived alternatives. */
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

  /** Prefer direct invocation of {@link Mono#then()} over more contrived alternatives. */
  static final class MonoThen<T> {
    @BeforeTemplate
    @SuppressWarnings("VoidMissingNullable" /* Suggestion is incompatible with Reactor API. */)
    Mono<Void> before(Mono<T> mono) {
      return Refaster.anyOf(
          mono.ignoreElement().then(),
          mono.flux().then(),
          Mono.when(mono),
          Mono.whenDelayError(mono));
    }

    @AfterTemplate
    @SuppressWarnings("VoidMissingNullable" /* Suggestion is incompatible with Reactor API. */)
    Mono<Void> after(Mono<T> mono) {
      return mono.then();
    }
  }

  /** Prefer {@link Flux#then()} over vacuously invoking {@link Flux#ignoreElements()}. */
  static final class FluxThen<T> {
    @BeforeTemplate
    @SuppressWarnings("VoidMissingNullable" /* Suggestion is incompatible with Reactor API. */)
    Mono<Void> before(Flux<T> flux) {
      return flux.ignoreElements().then();
    }

    @BeforeTemplate
    @SuppressWarnings("VoidMissingNullable" /* Suggestion is incompatible with Reactor API. */)
    Mono<Void> before2(Flux<Void> flux) {
      return flux.ignoreElements();
    }

    @AfterTemplate
    @SuppressWarnings("VoidMissingNullable" /* Suggestion is incompatible with Reactor API. */)
    Mono<Void> after(Flux<T> flux) {
      return flux.then();
    }
  }

  /**
   * Prefer {@link Mono#thenEmpty(Publisher)} over vacuously invoking {@link Mono#ignoreElement()}.
   */
  static final class MonoThenEmpty<T> {
    @BeforeTemplate
    @SuppressWarnings("VoidMissingNullable" /* Suggestion is incompatible with Reactor API. */)
    Mono<Void> before(Mono<T> mono, Publisher<Void> other) {
      return mono.ignoreElement().thenEmpty(other);
    }

    @AfterTemplate
    @SuppressWarnings("VoidMissingNullable" /* Suggestion is incompatible with Reactor API. */)
    Mono<Void> after(Mono<T> mono, Publisher<Void> other) {
      return mono.thenEmpty(other);
    }
  }

  /**
   * Prefer {@link Flux#thenEmpty(Publisher)} over vacuously invoking {@link Flux#ignoreElements()}.
   */
  static final class FluxThenEmpty<T> {
    @BeforeTemplate
    @SuppressWarnings("VoidMissingNullable" /* Suggestion is incompatible with Reactor API. */)
    Mono<Void> before(Flux<T> flux, Publisher<Void> other) {
      return flux.ignoreElements().thenEmpty(other);
    }

    @AfterTemplate
    @SuppressWarnings("VoidMissingNullable" /* Suggestion is incompatible with Reactor API. */)
    Mono<Void> after(Flux<T> flux, Publisher<Void> other) {
      return flux.thenEmpty(other);
    }
  }

  /** Prefer {@link Mono#thenMany(Publisher)} over applying vacuous operations first. */
  static final class MonoThenMany<T, S> {
    @BeforeTemplate
    Flux<S> before(Mono<T> mono, Publisher<S> other) {
      return Refaster.anyOf(mono.ignoreElement().thenMany(other), mono.flux().thenMany(other));
    }

    @AfterTemplate
    Flux<S> after(Mono<T> mono, Publisher<S> other) {
      return mono.thenMany(other);
    }
  }

  /**
   * Prefer explicit invocation of {@link Mono#flux()} over implicit conversions from {@link Mono}
   * to {@link Flux}.
   */
  static final class MonoThenFlux<T, S> {
    @BeforeTemplate
    Flux<S> before(Mono<T> mono, Mono<S> other) {
      return mono.thenMany(other);
    }

    @AfterTemplate
    Flux<S> after(Mono<T> mono, Mono<S> other) {
      return mono.then(other).flux();
    }
  }

  /**
   * Prefer {@link Flux#thenMany(Publisher)} over vacuously invoking {@link Flux#ignoreElements()}.
   */
  static final class FluxThenMany<T, S> {
    @BeforeTemplate
    Flux<S> before(Flux<T> flux, Publisher<S> other) {
      return flux.ignoreElements().thenMany(other);
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux, Publisher<S> other) {
      return flux.thenMany(other);
    }
  }

  /** Prefer {@link Mono#then(Mono)} over applying vacuous operations first. */
  @PossibleSourceIncompatibility
  static final class MonoThenWithMono<T, S> {
    @BeforeTemplate
    Mono<S> before(Mono<T> mono, Mono<S> other) {
      return Refaster.anyOf(mono.ignoreElement().then(other), mono.flux().then(other));
    }

    @BeforeTemplate
    @SuppressWarnings("VoidMissingNullable" /* Suggestion is incompatible with Reactor API. */)
    Mono<Void> before2(Mono<T> mono, Mono<Void> other) {
      return mono.thenEmpty(other);
    }

    @AfterTemplate
    Mono<S> after(Mono<T> mono, Mono<S> other) {
      return mono.then(other);
    }
  }

  /** Prefer {@link Flux#then(Mono)} over vacuously invoking {@link Flux#ignoreElements()}. */
  @PossibleSourceIncompatibility
  static final class FluxThenWithMono<T, S> {
    @BeforeTemplate
    Mono<S> before(Flux<T> flux, Mono<S> other) {
      return flux.ignoreElements().then(other);
    }

    @BeforeTemplate
    @SuppressWarnings("VoidMissingNullable" /* Suggestion is incompatible with Reactor API. */)
    Mono<Void> before2(Flux<T> flux, Mono<Void> other) {
      return flux.thenEmpty(other);
    }

    @AfterTemplate
    Mono<S> after(Flux<T> flux, Mono<S> other) {
      return flux.then(other);
    }
  }

  /** Prefer {@link Mono#singleOptional()} over more contrived alternatives. */
  // XXX: Consider creating a plugin that flags/discourages `Mono<Optional<T>>` method return
  // types, just as we discourage nullable `Boolean`s and `Optional`s.
  // XXX: The `mono.transform(Mono::singleOptional)` replacement is a special case of a more general
  // rule. Consider introducing an Error Prone check for this.
  static final class MonoSingleOptional<T> {
    @BeforeTemplate
    Mono<Optional<T>> before(Mono<T> mono, Optional<T> defaultV, Mono<Optional<T>> alternate) {
      return Refaster.anyOf(
          mono.flux().collect(toOptional()),
          mono.map(Optional::of).defaultIfEmpty(Optional.empty()),
          mono.singleOptional().defaultIfEmpty(defaultV),
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
  static final class MonoCastClass<T, S> {
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
  static final class FluxCastClass<T, S> {
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
        Function<? super S, ? extends P> transformer,
        @Matches(IsIdentityOperation.class)
            Function<? super P, ? extends Mono<? extends T>> identityTransformer) {
      return mono.map(transformer).flatMap(identityTransformer);
    }

    @AfterTemplate
    Mono<T> after(Mono<S> mono, Function<? super S, ? extends P> transformer) {
      return mono.flatMap(transformer);
    }
  }

  /** Prefer {@link Mono#flatMapMany(Function)} over more contrived alternatives. */
  static final class MonoFlatMapMany<S, T, P extends Publisher<? extends T>> {
    @BeforeTemplate
    @SuppressWarnings("NestedPublishers")
    Flux<T> before(
        Mono<S> mono,
        Function<? super S, P> mapper,
        @Matches(IsIdentityOperation.class)
            Function<? super P, ? extends Publisher<? extends T>> identityMapper,
        int prefetch,
        boolean delayUntilEnd,
        int concurrency) {
      return Refaster.anyOf(
          mono.map(mapper).flatMapMany(identityMapper),
          mono.flux().concatMap(mapper),
          mono.flux().concatMap(mapper, prefetch),
          mono.flux().concatMapDelayError(mapper),
          mono.flux().concatMapDelayError(mapper, prefetch),
          mono.flux().concatMapDelayError(mapper, delayUntilEnd, prefetch),
          mono.flux().flatMap(mapper, concurrency),
          mono.flux().flatMap(mapper, concurrency, prefetch),
          mono.flux().flatMapDelayError(mapper, concurrency, prefetch),
          mono.flux().flatMapSequential(mapper, concurrency),
          mono.flux().flatMapSequential(mapper, concurrency, prefetch),
          mono.flux().flatMapSequentialDelayError(mapper, concurrency, prefetch));
    }

    @BeforeTemplate
    Flux<T> before(Mono<S> mono, Function<? super S, Publisher<? extends T>> mapper) {
      return mono.flux().switchMap(mapper);
    }

    @AfterTemplate
    Flux<T> after(Mono<S> mono, Function<? super S, ? extends P> mapper) {
      return mono.flatMapMany(mapper);
    }
  }

  /**
   * Prefer {@link Flux#concatMapIterable(Function)} over alternatives that require an additional
   * subscription.
   */
  static final class FluxConcatMapIterableIdentity<T> {
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
  static final class FluxConcatMapIterableIdentityWithInt<T> {
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
  // XXX: Once the `FluxFromStream` rule is constrained using
  // `@NotMatches(IsIdentityOperation.class)`, this rule should also cover
  // `Flux.fromStream(collection.stream())`.
  static final class FluxFromIterable<T> {
    // XXX: Once the `MethodReferenceUsage` check is generally enabled, drop the second
    // `Refaster.anyOf` variant.
    @BeforeTemplate
    Flux<T> before(Collection<T> it) {
      return Flux.fromStream(
          Refaster.<Supplier<Stream<? extends T>>>anyOf(it::stream, () -> it.stream()));
    }

    @AfterTemplate
    Flux<T> after(Collection<T> it) {
      return Flux.fromIterable(it);
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
        Mono<T> mono,
        Class<? extends Throwable> exceptionType,
        Consumer<? super Throwable> onError) {
      return mono.doOnError(exceptionType::isInstance, onError);
    }

    @AfterTemplate
    Mono<T> after(
        Mono<T> mono,
        Class<? extends Throwable> exceptionType,
        Consumer<? super Throwable> onError) {
      return mono.doOnError(exceptionType, onError);
    }
  }

  /**
   * Prefer {@link Flux#doOnError(Class, Consumer)} over {@link Flux#doOnError(Predicate, Consumer)}
   * where possible.
   */
  static final class FluxDoOnError<T> {
    @BeforeTemplate
    Flux<T> before(
        Flux<T> flux,
        Class<? extends Throwable> exceptionType,
        Consumer<? super Throwable> onError) {
      return flux.doOnError(exceptionType::isInstance, onError);
    }

    @AfterTemplate
    Flux<T> after(
        Flux<T> flux,
        Class<? extends Throwable> exceptionType,
        Consumer<? super Throwable> onError) {
      return flux.doOnError(exceptionType, onError);
    }
  }

  /**
   * Prefer {@link Mono#onErrorComplete()} over more contrived alternatives, and don't chain it with
   * redundant calls to {@link Mono#doOnError}.
   */
  static final class MonoOnErrorComplete<T, E extends Throwable> {
    @BeforeTemplate
    Mono<T> before(
        Mono<T> mono,
        Consumer<? super Throwable> onError1,
        Class<E> exceptionType,
        Consumer<? super E> onError2,
        Predicate<? super Throwable> predicate) {
      return Refaster.anyOf(
          mono.onErrorResume(e -> Mono.empty()),
          mono.onErrorComplete().doOnError(onError1),
          mono.onErrorComplete().doOnError(exceptionType, onError2),
          mono.onErrorComplete().doOnError(predicate, onError1));
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono) {
      return mono.onErrorComplete();
    }
  }

  /**
   * Prefer {@link Flux#onErrorComplete()} over more contrived alternatives, and don't chain it with
   * redundant calls to {@link Flux#doOnError}.
   */
  static final class FluxOnErrorComplete<T, E extends Throwable> {
    @BeforeTemplate
    Flux<T> before(
        Flux<T> flux,
        Consumer<? super Throwable> onError1,
        Class<E> exceptionType,
        Consumer<? super E> onError2,
        Predicate<? super Throwable> predicate) {
      return Refaster.anyOf(
          flux.onErrorResume(e -> Refaster.anyOf(Mono.empty(), Flux.empty())),
          flux.onErrorComplete().doOnError(onError1),
          flux.onErrorComplete().doOnError(exceptionType, onError2),
          flux.onErrorComplete().doOnError(predicate, onError1));
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux) {
      return flux.onErrorComplete();
    }
  }

  /** Prefer {@link Mono#onErrorComplete(Class)} over more contrived alternatives. */
  static final class MonoOnErrorCompleteWithClass<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Class<? extends Throwable> type) {
      return Refaster.anyOf(
          mono.onErrorComplete(type::isInstance), mono.onErrorResume(type, e -> Mono.empty()));
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Class<? extends Throwable> type) {
      return mono.onErrorComplete(type);
    }
  }

  /** Prefer {@link Flux#onErrorComplete(Class)} over more contrived alternatives. */
  static final class FluxOnErrorCompleteWithClass<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Class<? extends Throwable> type) {
      return Refaster.anyOf(
          flux.onErrorComplete(type::isInstance),
          flux.onErrorResume(type, e -> Refaster.anyOf(Mono.empty(), Flux.empty())));
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Class<? extends Throwable> type) {
      return flux.onErrorComplete(type);
    }
  }

  /** Prefer {@link Mono#onErrorComplete(Predicate)} over more contrived alternatives. */
  static final class MonoOnErrorCompleteWithPredicate<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Predicate<? super Throwable> predicate) {
      return mono.onErrorResume(predicate, e -> Mono.empty());
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Predicate<? super Throwable> predicate) {
      return mono.onErrorComplete(predicate);
    }
  }

  /** Prefer {@link Flux#onErrorComplete(Predicate)} over more contrived alternatives. */
  static final class FluxOnErrorCompleteWithPredicate<T> {
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
        Class<? extends Throwable> type,
        BiConsumer<Throwable, Object> errorConsumer) {
      return mono.onErrorContinue(type::isInstance, errorConsumer);
    }

    @AfterTemplate
    Mono<T> after(
        Mono<T> mono,
        Class<? extends Throwable> type,
        BiConsumer<Throwable, Object> errorConsumer) {
      return mono.onErrorContinue(type, errorConsumer);
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
        Class<? extends Throwable> type,
        BiConsumer<Throwable, Object> errorConsumer) {
      return flux.onErrorContinue(type::isInstance, errorConsumer);
    }

    @AfterTemplate
    Flux<T> after(
        Flux<T> flux,
        Class<? extends Throwable> type,
        BiConsumer<Throwable, Object> errorConsumer) {
      return flux.onErrorContinue(type, errorConsumer);
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
        Class<? extends Throwable> type,
        Function<? super Throwable, ? extends Throwable> mapper) {
      return mono.onErrorMap(type::isInstance, mapper);
    }

    @AfterTemplate
    Mono<T> after(
        Mono<T> mono,
        Class<? extends Throwable> type,
        Function<? super Throwable, ? extends Throwable> mapper) {
      return mono.onErrorMap(type, mapper);
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
        Class<? extends Throwable> type,
        Function<? super Throwable, ? extends Throwable> mapper) {
      return flux.onErrorMap(type::isInstance, mapper);
    }

    @AfterTemplate
    Flux<T> after(
        Flux<T> flux,
        Class<? extends Throwable> type,
        Function<? super Throwable, ? extends Throwable> mapper) {
      return flux.onErrorMap(type, mapper);
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
        Class<? extends Throwable> type,
        Function<? super Throwable, ? extends Mono<? extends T>> fallback) {
      return mono.onErrorResume(type::isInstance, fallback);
    }

    @AfterTemplate
    Mono<T> after(
        Mono<T> mono,
        Class<? extends Throwable> type,
        Function<? super Throwable, ? extends Mono<? extends T>> fallback) {
      return mono.onErrorResume(type, fallback);
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
        Class<? extends Throwable> type,
        Function<? super Throwable, ? extends Publisher<? extends T>> fallback) {
      return flux.onErrorResume(type::isInstance, fallback);
    }

    @AfterTemplate
    Flux<T> after(
        Flux<T> flux,
        Class<? extends Throwable> type,
        Function<? super Throwable, ? extends Publisher<? extends T>> fallback) {
      return flux.onErrorResume(type, fallback);
    }
  }

  /**
   * Prefer {@link Mono#onErrorReturn(Class, Object)} over {@link Mono#onErrorReturn(Predicate,
   * Object)} where possible.
   */
  static final class MonoOnErrorReturn<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Class<? extends Throwable> type, T fallbackValue) {
      return mono.onErrorReturn(type::isInstance, fallbackValue);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Class<? extends Throwable> type, T fallbackValue) {
      return mono.onErrorReturn(type, fallbackValue);
    }
  }

  /**
   * Prefer {@link Flux#onErrorReturn(Class, Object)} over {@link Flux#onErrorReturn(Predicate,
   * Object)} where possible.
   */
  static final class FluxOnErrorReturn<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Class<? extends Throwable> type, T fallbackValue) {
      return flux.onErrorReturn(type::isInstance, fallbackValue);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Class<? extends Throwable> type, T fallbackValue) {
      return flux.onErrorReturn(type, fallbackValue);
    }
  }

  /**
   * Apply {@link Flux#filter(Predicate)} before {@link Flux#sort()} to reduce the number of
   * elements to sort.
   */
  static final class FluxFilterSort<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Predicate<? super T> p) {
      return flux.sort().filter(p);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Predicate<? super T> p) {
      return flux.filter(p).sort();
    }
  }

  /**
   * Apply {@link Flux#filter(Predicate)} before {@link Flux#sort(Comparator)} to reduce the number
   * of elements to sort.
   */
  static final class FluxFilterSortWithComparator<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Predicate<? super T> p, Comparator<? super T> sortFunction) {
      return flux.sort(sortFunction).filter(p);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Predicate<? super T> p, Comparator<? super T> sortFunction) {
      return flux.filter(p).sort(sortFunction);
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
    Flux<T> before(Flux<T> flux, Comparator<S> sortFunction) {
      return flux.sort(sortFunction).distinct();
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Comparator<S> sortFunction) {
      return flux.distinct().sort(sortFunction);
    }
  }

  /**
   * Do not unnecessarily {@link Flux#filter(Predicate) filter} the result of {@link
   * Flux#takeWhile(Predicate)} using the same {@link Predicate}.
   */
  static final class FluxTakeWhile<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Predicate<? super T> continuePredicate) {
      return flux.takeWhile(continuePredicate).filter(continuePredicate);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Predicate<? super T> continuePredicate) {
      return flux.takeWhile(continuePredicate);
    }
  }

  /**
   * Prefer {@link Flux#collect(Collector)} with {@link ImmutableList#toImmutableList()} over
   * alternatives that do not explicitly return an immutable collection.
   */
  @PossibleSourceIncompatibility
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
  static final class FluxTransformMathFluxMinSingleOrEmpty<T extends Comparable<? super T>> {
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
  static final class FluxTransformMathFluxMinSingleOrEmptyWithComparator<
      T extends Comparable<? super T>> {
    @BeforeTemplate
    Mono<T> before(Flux<T> flux, Comparator<? super T> comparator) {
      return Refaster.anyOf(
          flux.sort(comparator).next(), flux.collect(minBy(comparator)).flatMap(Mono::justOrEmpty));
    }

    @AfterTemplate
    Mono<T> after(Flux<T> flux, Comparator<? super T> comparator) {
      return flux.transform(f -> MathFlux.min(f, comparator)).singleOrEmpty();
    }
  }

  /** Prefer {@link MathFlux#max(Publisher)} over less efficient alternatives. */
  static final class FluxTransformMathFluxMaxSingleOrEmpty<T extends Comparable<? super T>> {
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
  static final class FluxTransformMathFluxMaxSingleOrEmptyWithComparator<
      T extends Comparable<? super T>> {
    @BeforeTemplate
    Mono<T> before(Flux<T> flux, Comparator<? super T> comparator) {
      return Refaster.anyOf(
          flux.sort(comparator).last(), flux.collect(maxBy(comparator)).flatMap(Mono::justOrEmpty));
    }

    @AfterTemplate
    Mono<T> after(Flux<T> flux, Comparator<? super T> comparator) {
      return flux.transform(f -> MathFlux.max(f, comparator)).singleOrEmpty();
    }
  }

  /** Prefer {@link MathFlux#min(Publisher)} over more contrived alternatives. */
  static final class MathFluxMin<T extends Comparable<? super T>> {
    @BeforeTemplate
    Mono<T> before(Publisher<T> source) {
      return Refaster.anyOf(
          MathFlux.min(source, naturalOrder()), MathFlux.max(source, reverseOrder()));
    }

    @AfterTemplate
    Mono<T> after(Publisher<T> source) {
      return MathFlux.min(source);
    }
  }

  /** Prefer {@link MathFlux#max(Publisher)} over more contrived alternatives. */
  static final class MathFluxMax<T extends Comparable<? super T>> {
    @BeforeTemplate
    Mono<T> before(Publisher<T> source) {
      return Refaster.anyOf(
          MathFlux.min(source, reverseOrder()), MathFlux.max(source, naturalOrder()));
    }

    @AfterTemplate
    Mono<T> after(Publisher<T> source) {
      return MathFlux.max(source);
    }
  }

  /** Prefer {@link reactor.util.context.Context#empty()} over more verbose alternatives. */
  // XXX: Introduce Refaster rules or a `BugChecker` that maps `(Immutable)Map.of(k, v)` to
  // `Context.of(k, v)` and likewise for multi-pair overloads.
  static final class ContextEmpty {
    @BeforeTemplate
    Context before(@Matches(IsEmpty.class) Map<?, ?> emptyMap) {
      return Context.of(emptyMap);
    }

    @AfterTemplate
    Context after() {
      return Context.empty();
    }
  }

  /** Prefer {@link PublisherProbe#empty()} over more verbose alternatives. */
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
  static final class AssertThatPublisherProbeWasSubscribedIsEqualTo<T> {
    @AlsoNegation
    @BeforeTemplate
    void before(PublisherProbe<T> probe, boolean expected) {
      if (expected) {
        probe.assertWasSubscribed();
      } else {
        probe.assertWasNotSubscribed();
      }
    }

    @AfterTemplate
    void after(PublisherProbe<T> probe, boolean expected) {
      assertThat(probe.wasSubscribed()).isEqualTo(expected);
    }
  }

  /**
   * Prefer {@link Assertions#assertThat(boolean)} to check whether a {@link PublisherProbe} was
   * {@link PublisherProbe#wasCancelled() cancelled}, over more verbose alternatives.
   */
  static final class AssertThatPublisherProbeWasCancelledIsEqualTo<T> {
    @AlsoNegation
    @BeforeTemplate
    void before(PublisherProbe<T> probe, boolean expected) {
      if (expected) {
        probe.assertWasCancelled();
      } else {
        probe.assertWasNotCancelled();
      }
    }

    @AfterTemplate
    void after(PublisherProbe<T> probe, boolean expected) {
      assertThat(probe.wasCancelled()).isEqualTo(expected);
    }
  }

  /**
   * Prefer {@link Assertions#assertThat(boolean)} to check whether a {@link PublisherProbe} was
   * {@link PublisherProbe#wasRequested() requested}, over more verbose alternatives.
   */
  static final class AssertThatPublisherProbeWasRequestedIsEqualTo<T> {
    @AlsoNegation
    @BeforeTemplate
    void before(PublisherProbe<T> probe, boolean expected) {
      if (expected) {
        probe.assertWasRequested();
      } else {
        probe.assertWasNotRequested();
      }
    }

    @AfterTemplate
    void after(PublisherProbe<T> probe, boolean expected) {
      assertThat(probe.wasRequested()).isEqualTo(expected);
    }
  }

  /** Prefer {@link Mono#as(Function)} when creating a {@link StepVerifier}. */
  static final class MonoAsStepVerifierCreate<T> {
    @BeforeTemplate
    FirstStep<T> before(Mono<T> publisher) {
      return Refaster.anyOf(
          StepVerifier.create(publisher), publisher.flux().as(StepVerifier::create));
    }

    @AfterTemplate
    FirstStep<T> after(Mono<T> publisher) {
      return publisher.as(StepVerifier::create);
    }
  }

  /** Prefer {@link Flux#as(Function)} when creating a {@link StepVerifier}. */
  static final class FluxAsStepVerifierCreate<T> {
    @BeforeTemplate
    FirstStep<T> before(Flux<T> publisher) {
      return StepVerifier.create(publisher);
    }

    @AfterTemplate
    FirstStep<T> after(Flux<T> publisher) {
      return publisher.as(StepVerifier::create);
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
  @PossibleSourceIncompatibility
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
  @PossibleSourceIncompatibility
  static final class StepVerifierVerifyWithDuration {
    @BeforeTemplate
    StepVerifier.Assertions before(StepVerifier stepVerifier, Duration duration) {
      return stepVerifier.verifyThenAssertThat(duration);
    }

    @AfterTemplate
    Duration after(StepVerifier stepVerifier, Duration duration) {
      return stepVerifier.verify(duration);
    }
  }

  /** Prefer invoking {@link StepVerifier#verifyLater()} once over multiple invocations. */
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

  /** Prefer using {@link StepVerifier.Step}s as-is over unnecessarily expecting no elements. */
  static final class StepIdentity<T> {
    @BeforeTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    StepVerifier.Step<T> before(
        StepVerifier.Step<T> step, @Matches(IsEmpty.class) Iterable<? extends T> emptyIterable) {
      return Refaster.anyOf(
          step.expectNext(), step.expectNextCount(0), step.expectNextSequence(emptyIterable));
    }

    @AfterTemplate
    @CanIgnoreReturnValue
    StepVerifier.Step<T> after(StepVerifier.Step<T> step) {
      return step;
    }
  }

  /** Prefer {@link StepVerifier.Step#expectNext(Object)} over more verbose alternatives. */
  static final class StepExpectNext<T> {
    @BeforeTemplate
    StepVerifier.Step<T> before(StepVerifier.Step<T> step, T obj) {
      return Refaster.anyOf(
          step.expectNextMatches(e -> e.equals(obj)), step.expectNextMatches(obj::equals));
    }

    @AfterTemplate
    StepVerifier.Step<T> after(StepVerifier.Step<T> step, T obj) {
      return step.expectNext(obj);
    }
  }

  /**
   * Prefer using {@link FirstStep#expectNext(Object)} over collecting a single-element {@link Flux}
   * into a list.
   */
  // XXX: This rule assumes that the matched collector does not drop elements. Consider introducing
  // a `@Matches(DoesNotDropElements.class)` or `@NotMatches(MayDropElements.class)` guard.
  @PossibleSourceIncompatibility
  static final class FluxAsStepVerifierCreateExpectNext<T, L extends List<T>> {
    @BeforeTemplate
    StepVerifier.Step<L> before(Flux<T> flux, T object, Collector<? super T, ?, L> collector) {
      return flux.collect(collector)
          .as(StepVerifier::create)
          .assertNext(list -> assertThat(list).containsExactly(object));
    }

    @AfterTemplate
    StepVerifier.Step<T> after(Flux<T> flux, T object) {
      return flux.as(StepVerifier::create).expectNext(object);
    }
  }

  /** Prefer {@link StepVerifier.LastStep#verifyComplete()} over more verbose alternatives. */
  static final class LastStepVerifyComplete {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep lastStep) {
      return lastStep.expectComplete().verify();
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep lastStep) {
      return lastStep.verifyComplete();
    }
  }

  /** Prefer {@link StepVerifier.LastStep#verifyError()} over more verbose alternatives. */
  static final class LastStepVerifyError {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep lastStep) {
      return lastStep.expectError().verify();
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep lastStep) {
      return lastStep.verifyError();
    }
  }

  /** Prefer {@link StepVerifier.LastStep#verifyError(Class)} over more verbose alternatives. */
  static final class LastStepVerifyErrorWithClass<T extends Throwable> {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep lastStep, Class<T> type) {
      return Refaster.anyOf(
          lastStep.expectError(type).verify(),
          lastStep.verifyErrorMatches(type::isInstance),
          lastStep.verifyErrorSatisfies(t -> assertThat(t).isInstanceOf(type)));
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep lastStep, Class<T> type) {
      return lastStep.verifyError(type);
    }
  }

  /**
   * Prefer {@link StepVerifier.LastStep#verifyErrorMatches(Predicate)} over more verbose
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class LastStepVerifyErrorMatches {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep lastStep, Predicate<Throwable> predicate) {
      return lastStep.expectErrorMatches(predicate).verify();
    }

    @BeforeTemplate
    @SuppressWarnings("StepVerifierVerify" /* This is a more specific template. */)
    StepVerifier.Assertions before2(
        StepVerifier.LastStep lastStep, Predicate<Throwable> predicate) {
      return lastStep.expectError().verifyThenAssertThat().hasOperatorErrorMatching(predicate);
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep lastStep, Predicate<Throwable> predicate) {
      return lastStep.verifyErrorMatches(predicate);
    }
  }

  /**
   * Prefer {@link StepVerifier.LastStep#verifyErrorSatisfies(Consumer)} over more verbose
   * alternatives.
   */
  static final class LastStepVerifyErrorSatisfies {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep lastStep, Consumer<Throwable> consumer) {
      return lastStep.expectErrorSatisfies(consumer).verify();
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep lastStep, Consumer<Throwable> consumer) {
      return lastStep.verifyErrorSatisfies(consumer);
    }
  }

  /**
   * Prefer {@link StepVerifier.LastStep#verifyErrorSatisfies(Consumer)} with AssertJ over more
   * contrived alternatives.
   */
  @PossibleSourceIncompatibility
  static final class LastStepVerifyErrorSatisfiesAssertThatIsInstanceOfHasMessage<
      T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings("StepVerifierVerify" /* This is a more specific template. */)
    StepVerifier.Assertions before(StepVerifier.LastStep lastStep, Class<T> type, String message) {
      return Refaster.anyOf(
          lastStep
              .expectError()
              .verifyThenAssertThat()
              .hasOperatorErrorOfType(type)
              .hasOperatorErrorWithMessage(message),
          lastStep.expectError(type).verifyThenAssertThat().hasOperatorErrorWithMessage(message),
          lastStep.expectErrorMessage(message).verifyThenAssertThat().hasOperatorErrorOfType(type));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Duration after(StepVerifier.LastStep lastStep, Class<T> type, String message) {
      return lastStep.verifyErrorSatisfies(
          t -> assertThat(t).isInstanceOf(type).hasMessage(message));
    }
  }

  /**
   * Prefer {@link StepVerifier.LastStep#verifyErrorMessage(String)} over more verbose alternatives.
   */
  static final class LastStepVerifyErrorMessage {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep lastStep, String str) {
      return lastStep.expectErrorMessage(str).verify();
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep lastStep, String str) {
      return lastStep.verifyErrorMessage(str);
    }
  }

  /**
   * Prefer {@link StepVerifier.LastStep#verifyTimeout(Duration)} over more verbose alternatives.
   */
  static final class LastStepVerifyTimeout {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep lastStep, Duration duration) {
      return lastStep.expectTimeout(duration).verify();
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep lastStep, Duration duration) {
      return lastStep.verifyTimeout(duration);
    }
  }

  /**
   * Prefer {@link Mono#fromFuture(Supplier)} over {@link Mono#fromFuture(CompletableFuture)}, as
   * the former may defer initiation of the asynchronous computation until subscription.
   */
  static final class MonoFromFuture<T> {
    // XXX: Constrain the `future` parameter using `@Matches(RequiresComputation.class)`.
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
  static final class MonoFromFutureWithBoolean<T> {
    // XXX: Constrain the `future` parameter using `@Matches(RequiresComputation.class)`.
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
   * Prefer suppressing {@link Mono} cancellations to upstream cache value computations over
   * propagating them, as completion of such computations may benefit concurrent or subsequent cache
   * usages.
   *
   * <p><strong>Warning:</strong> this rewrite changes cancellation propagation behavior.
   */
  static final class MonoFromFutureAsyncLoadingCacheGetTrue<K, V> {
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
   * Prefer suppressing {@link Mono} cancellations to upstream cache value computations over
   * propagating them, as completion of such computations may benefit concurrent or subsequent cache
   * usages.
   *
   * <p><strong>Warning:</strong> this rewrite changes cancellation propagation behavior.
   */
  static final class MonoFromFutureAsyncLoadingCacheGetAllTrue<K1, K2 extends K1, V> {
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
  static final class FluxFromStream<T> {
    // XXX: Constrain the `future` parameter using `@Matches(RequiresComputation.class)`.
    @BeforeTemplate
    Flux<T> before(Stream<T> s) {
      return Flux.fromStream(s);
    }

    @AfterTemplate
    Flux<T> after(Stream<T> s) {
      return Flux.fromStream(() -> s);
    }
  }

  /** Prefer fluent {@link Flux#next()} over less explicit alternatives. */
  static final class FluxNext<T> {
    @BeforeTemplate
    Mono<T> before(Flux<T> source) {
      return Mono.from(source);
    }

    @AfterTemplate
    Mono<T> after(Flux<T> source) {
      return source.next();
    }
  }
}
