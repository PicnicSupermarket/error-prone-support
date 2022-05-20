package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.MoreCollectors.toOptional;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.MoreCollectors;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

/** Refaster templates related to Reactor expressions and statements. */
final class ReactorTemplates {
  private ReactorTemplates() {}

  /** Prefer {@link Mono#justOrEmpty(Optional)} over more verbose alternatives. */
  // XXX: If `optional` is a constant and effectively-final expression then the `Mono.defer` can be
  // dropped. Should look into Refaster support for identifying this.
  static final class MonoFromOptional<T> {
    @BeforeTemplate
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
  // XXX: Drop this rule once the more general rule `AssortedTemplates#SupplierAsSupplier` works
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
  // XXX: Drop this rule once the more general rule `AssortedTemplates#SupplierAsSupplier` works
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

  /**
   * Prefer {@link Flux#concatMapIterable(Function)} over {@link Flux#concatMapIterable(Function)},
   * as the former has equivalent semantics but a clearer name.
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
   * Don't use {@link Mono#flatMapMany(Function)} to implicitly convert a {@link Mono} to a {@link
   * Flux}.
   */
  abstract static class MonoFlatMapToFlux<T, S> {
    @Placeholder(allowsIdentity = true)
    abstract Mono<S> valueTransformation(@MayOptionallyUse T value);

    @BeforeTemplate
    Flux<S> before(Mono<T> mono) {
      return mono.flatMapMany(v -> valueTransformation(v));
    }

    @AfterTemplate
    Flux<S> after(Mono<T> mono) {
      return mono.flatMap(v -> valueTransformation(v)).flux();
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
  // XXX: Consider creating a plugin which flags/discourages `Mono<Optional<T>>` method return
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

  /** Prefer {@link PublisherProbe#empty()}} over more verbose alternatives. */
  static final class PublisherProbeEmpty<T> {
    @BeforeTemplate
    PublisherProbe<T> before() {
      return Refaster.anyOf(PublisherProbe.of(Mono.empty()), PublisherProbe.of(Flux.empty()));
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

  /** Prefer {@link StepVerifier.LastStep#verifyError()} over type check verification */
  static final class StepVerifierLastStepVerifyErrorAssert {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep step) {
      return step.verifyErrorSatisfies(t -> assertThat(t).isInstanceOf(Throwable.class));
    }

    @AfterTemplate
    Duration after(StepVerifier.LastStep step) {
      return step.verifyError(Throwable.class);
    }
  }

  /** Prefer {@link StepVerifier.LastStep#verifyError(Class)} over more verbose alternatives. */
  static final class StepVerifierLastStepVerifyErrorClass<T extends Throwable> {
    @BeforeTemplate
    Duration before(StepVerifier.LastStep step, Class<T> clazz) {
      return step.expectError(clazz).verify();
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
