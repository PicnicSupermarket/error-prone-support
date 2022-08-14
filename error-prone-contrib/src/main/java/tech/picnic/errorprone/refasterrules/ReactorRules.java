package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.MoreCollectors.toOptional;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static reactor.function.TupleUtils.function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MoreCollectors;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.NotMatches;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import tech.picnic.errorprone.refaster.annotation.Description;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.Severity;
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

  /** Prefer {@link Mono#justOrEmpty(Optional)} over more verbose alternatives. */
  // XXX: If `optional` is a constant and effectively-final expression then the `Mono.defer` can be
  // dropped. Should look into Refaster support for identifying this.
  static final class MonoFromOptional<T> {
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

  /**
   * Prefer {@link Flux#zipWithIterable(Iterable)} with a chained combinator over {@link
   * Flux#zipWithIterable(Iterable, BiFunction)}, as the former generally yields more readable code.
   */
  static final class FluxZipWithIterable<T, S, R> {
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
      return mono.then(Mono.just(object));
    }

    @AfterTemplate
    Mono<S> after(Mono<T> mono, S object) {
      return mono.thenReturn(object);
    }
  }

  /**
   * Prefer {@link Flux#take(long, boolean)} over {@link Flux#take(long)}.
   *
   * <p>In Reactor versions prior to 3.5.0, {@code Flux#take(long)} makes an unbounded request
   * upstream, and is equivalent to {@code Flux#take(long, false)}. In 3.5.0, the behavior of {@code
   * Flux#take(long)} will change to that of {@code Flux#take(long, true)}.
   *
   * <p>The intent with this Refaster rule is to get the new behavior before upgrading to Reactor
   * 3.5.0.
   */
  // XXX: Drop this rule some time after upgrading to Reactor 3.6.0, or introduce a way to apply
  // this rule only when an older version of Reactor is on the classpath.
  // XXX: Once Reactor 3.6.0 is out, introduce a rule that rewrites code in the opposite direction.
  @Description(
      "Prior to Reactor 3.5.0, `take(n)` requests and unbounded number of elements upstream.")
  @Severity(WARNING)
  static final class FluxTake<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, long n) {
      return flux.take(n);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, long n) {
      return flux.take(n, /* limitRequest= */ true);
    }
  }

  /** Don't unnecessarily pass an empty publisher to {@link Mono#switchIfEmpty(Mono)}. */
  static final class MonoSwitchIfEmptyOfEmptyPublisher<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono) {
      return mono.switchIfEmpty(Mono.empty());
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono) {
      return mono;
    }
  }

  /** Don't unnecessarily pass an empty publisher to {@link Flux#switchIfEmpty(Publisher)}. */
  static final class FluxSwitchIfEmptyOfEmptyPublisher<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux) {
      return flux.switchIfEmpty(Refaster.anyOf(Mono.empty(), Flux.empty()));
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux) {
      return flux;
    }
  }

  /** Prefer {@link Flux#concatMap(Function)} over more contrived alternatives. */
  static final class FluxConcatMap<T, S> {
    @BeforeTemplate
    Flux<S> before(Flux<T> flux, Function<? super T, ? extends Publisher<? extends S>> function) {
      return Refaster.anyOf(flux.flatMap(function, 1), flux.flatMapSequential(function, 1));
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux, Function<? super T, ? extends Publisher<? extends S>> function) {
      return flux.concatMap(function);
    }
  }

  /** Prefer {@link Flux#concatMap(Function, int)} over more contrived alternatives. */
  static final class FluxConcatMapWithPrefetch<T, S> {
    @BeforeTemplate
    Flux<S> before(
        Flux<T> flux,
        Function<? super T, ? extends Publisher<? extends S>> function,
        int prefetch) {
      return Refaster.anyOf(
          flux.flatMap(function, 1, prefetch), flux.flatMapSequential(function, 1, prefetch));
    }

    @AfterTemplate
    Flux<S> after(
        Flux<T> flux,
        Function<? super T, ? extends Publisher<? extends S>> function,
        int prefetch) {
      return flux.concatMap(function, prefetch);
    }
  }

  /**
   * Prefer {@link Flux#concatMapIterable(Function)} over {@link Flux#flatMapIterable(Function)}, as
   * the former has equivalent semantics but a clearer name.
   */
  static final class FluxConcatMapIterable<T, S> {
    @BeforeTemplate
    Flux<S> before(Flux<T> flux, Function<? super T, ? extends Iterable<? extends S>> function) {
      return flux.flatMapIterable(function);
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux, Function<? super T, ? extends Iterable<? extends S>> function) {
      return flux.concatMapIterable(function);
    }
  }

  /**
   * Prefer {@link Flux#concatMapIterable(Function, int)} over {@link Flux#flatMapIterable(Function,
   * int)}, as the former has equivalent semantics but a clearer name.
   */
  static final class FluxConcatMapIterableWithPrefetch<T, S> {
    @BeforeTemplate
    Flux<S> before(
        Flux<T> flux, Function<? super T, ? extends Iterable<? extends S>> function, int prefetch) {
      return flux.flatMapIterable(function, prefetch);
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
  // XXX: Also cover `{Mono,Flux}.fromSupplier(() -> transformation(x))`. (Though it'd be more
  // accurate in some cases to use `mapNotNull` in those cases.)
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
  // XXX: Also cover `{Mono,Flux}.fromSupplier(() -> transformation(x))`. (Though it'd be more
  // accurate in some cases to use `mapNotNull` in those cases.)
  abstract static class FluxMap<T, S> {
    @Placeholder(allowsIdentity = true)
    abstract S transformation(@MayOptionallyUse T value);

    @BeforeTemplate
    Flux<S> before(Flux<T> flux, boolean delayUntilEnd, int maxConcurrency, int prefetch) {
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
      return mono.flatMap(x -> Mono.justOrEmpty(transformation(x)));
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
    Publisher<S> before(Flux<T> flux, boolean delayUntilEnd, int maxConcurrency, int prefetch) {
      return Refaster.anyOf(
          flux.concatMap(x -> Mono.justOrEmpty(transformation(x))),
          flux.concatMap(x -> Mono.justOrEmpty(transformation(x)), prefetch),
          flux.concatMapDelayError(x -> Mono.justOrEmpty(transformation(x))),
          flux.concatMapDelayError(x -> Mono.justOrEmpty(transformation(x)), prefetch),
          flux.concatMapDelayError(
              x -> Mono.justOrEmpty(transformation(x)), delayUntilEnd, prefetch),
          flux.flatMap(x -> Mono.justOrEmpty(transformation(x)), maxConcurrency),
          flux.flatMap(x -> Mono.justOrEmpty(transformation(x)), maxConcurrency, prefetch),
          flux.flatMapDelayError(
              x -> Mono.justOrEmpty(transformation(x)), maxConcurrency, prefetch),
          flux.flatMapSequential(x -> Mono.justOrEmpty(transformation(x)), maxConcurrency),
          flux.flatMapSequential(
              x -> Mono.justOrEmpty(transformation(x)), maxConcurrency, prefetch),
          flux.flatMapSequentialDelayError(
              x -> Mono.justOrEmpty(transformation(x)), maxConcurrency, prefetch),
          flux.switchMap(x -> Mono.justOrEmpty(transformation(x))));
    }

    @AfterTemplate
    Flux<S> after(Flux<T> flux) {
      return flux.mapNotNull(x -> transformation(x));
    }
  }

  /** Prefer {@link Mono#flux()}} over more contrived alternatives. */
  static final class MonoFlux<T> {
    @BeforeTemplate
    Flux<T> before(Mono<T> mono) {
      return Flux.concat(mono);
    }

    @AfterTemplate
    Flux<T> after(Mono<T> mono) {
      return mono.flux();
    }
  }

  /**
   * Prefer a collection using {@link MoreCollectors#toOptional()} over more contrived alternatives.
   */
  // XXX: Consider creating a plugin that flags/discourages `Mono<Optional<T>>` method return
  // types, just as we discourage nullable `Boolean`s and `Optional`s.
  static final class MonoCollectToOptional<T> {
    @BeforeTemplate
    Mono<Optional<T>> before(Mono<T> mono) {
      return Refaster.anyOf(
          mono.map(Optional::of).defaultIfEmpty(Optional.empty()),
          mono.map(Optional::of).switchIfEmpty(Mono.just(Optional.empty())));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Mono<Optional<T>> after(Mono<T> mono) {
      return mono.flux().collect(toOptional());
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

  /** Prefer {@link reactor.util.context.Context#empty()}} over more verbose alternatives. */
  // XXX: Consider introducing an `IsEmpty` matcher that identifies a wide range of guaranteed-empty
  // `Collection` and `Map` expressions.
  static final class ContextEmpty {
    @BeforeTemplate
    Context before() {
      return Context.of(Refaster.anyOf(new HashMap<>(), ImmutableMap.of()));
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

  /** Prefer {@link Mono#as(Function)} when creating a {@link StepVerifier}. */
  static final class StepVerifierFromMono<T> {
    @BeforeTemplate
    StepVerifier.FirstStep<? extends T> before(Mono<T> mono) {
      return StepVerifier.create(mono);
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

  /** Don't unnecessarily call {@link StepVerifier.Step#expectNext(Object[])}. */
  static final class StepVerifierStepExpectNextEmpty<T> {
    @BeforeTemplate
    @SuppressWarnings("unchecked")
    StepVerifier.Step<T> before(StepVerifier.Step<T> step) {
      return step.expectNext();
    }

    @AfterTemplate
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
}
