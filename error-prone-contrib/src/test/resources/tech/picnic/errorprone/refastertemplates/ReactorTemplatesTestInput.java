package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class ReactorTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(assertThat(0));
  }

  ImmutableSet<Mono<?>> testMonoFromSupplier() {
    return ImmutableSet.of(
        Mono.fromCallable((Callable<?>) null),
        Mono.fromCallable(() -> getClass().getDeclaredConstructor()),
        Mono.fromCallable(() -> toString()),
        Mono.fromCallable(getClass()::getDeclaredConstructor),
        Mono.fromCallable(this::toString));
  }

  ImmutableSet<Mono<Integer>> testMonoFromOptional() {
    return ImmutableSet.of(
        Mono.fromCallable(() -> Optional.of(1).orElse(null)),
        Mono.fromSupplier(() -> Optional.of(2).orElse(null)));
  }

  Mono<Void> testMonoDeferredError() {
    return Mono.defer(() -> Mono.error(new IllegalStateException()));
  }

  Flux<Void> testFluxDeferredError() {
    return Flux.defer(() -> Flux.error(new IllegalStateException()));
  }

  Mono<Void> testMonoErrorSupplier() {
    return Mono.error(() -> ((Supplier<RuntimeException>) null).get());
  }

  Flux<Void> testFluxErrorSupplier() {
    return Flux.error(() -> ((Supplier<RuntimeException>) null).get());
  }

  Mono<String> testMonoThenReturn() {
    return Mono.empty().then(Mono.just("foo"));
  }

  Mono<Integer> testMonoSwitchIfEmptyOfEmptyPublisher() {
    return Mono.just(1).switchIfEmpty(Mono.empty());
  }

  ImmutableSet<Flux<Integer>> testFluxSwitchIfEmptyOfEmptyPublisher() {
    return ImmutableSet.of(
        Flux.just(1).switchIfEmpty(Mono.empty()), Flux.just(2).switchIfEmpty(Flux.empty()));
  }

  ImmutableSet<Flux<Integer>> testFluxConcatMap() {
    return ImmutableSet.of(
        Flux.just(1).flatMap(Mono::just, 1), Flux.just(2).flatMapSequential(Mono::just, 1));
  }

  Flux<Integer> testFluxConcatMapIterable() {
    return Flux.just(1, 2).flatMapIterable(ImmutableList::of);
  }

  Flux<String> testMonoFlatMapToFlux() {
    return Mono.just("foo").flatMapMany(s -> Mono.just(s + s));
  }

  Flux<String> testMonoFlux() {
    return Flux.concat(Mono.just("foo"));
  }

  ImmutableSet<Mono<Optional<String>>> testMonoCollectToOptional() {
    return ImmutableSet.of(
        Mono.just("foo").map(Optional::of).defaultIfEmpty(Optional.empty()),
        Mono.just("bar").map(Optional::of).switchIfEmpty(Mono.just(Optional.empty())));
  }

  Mono<Number> testMonoCast() {
    return Mono.just(1).map(Number.class::cast);
  }

  Flux<Number> testFluxCast() {
    return Flux.just(1).map(Number.class::cast);
  }

  Flux<Object> testFluxOnErrorComplete() {
    return Flux.error(new IllegalStateException()).onErrorResume(e -> Flux.empty());
  }

  Mono<Object> testMonoOnErrorComplete() {
    return Mono.error(new IllegalStateException()).onErrorResume(e -> Mono.empty());
  }

  ImmutableSet<PublisherProbe<Void>> testPublisherProbeEmpty() {
    return ImmutableSet.of(PublisherProbe.of(Mono.empty()), PublisherProbe.of(Flux.empty()));
  }

  StepVerifier.FirstStep<Integer> testStepVerifierFromMono() {
    return StepVerifier.create(Mono.just(1));
  }

  StepVerifier.FirstStep<Integer> testStepVerifierFromFlux() {
    return StepVerifier.create(Flux.just(1));
  }

  StepVerifier.Step<Integer> testStepVerifierStepExpectNextEmpty() {
    return StepVerifier.create(Mono.just(0)).expectNext();
  }

  ImmutableSet<StepVerifier.Step<String>> testStepVerifierStepExpectNext() {
    return ImmutableSet.of(
        StepVerifier.create(Mono.just("foo")).expectNextMatches(s -> s.equals("bar")),
        StepVerifier.create(Mono.just("baz")).expectNextMatches("qux"::equals));
  }

  Duration testStepVerifierLastStepVerifyComplete() {
    return StepVerifier.create(Mono.empty()).expectComplete().verify();
  }

  Duration testStepVerifierLastStepVerifyError() {
    return StepVerifier.create(Mono.empty()).expectError().verify();
  }

  ImmutableSet<Duration> testStepVerifierLastStepVerifyErrorClass() {
    return ImmutableSet.of(
        StepVerifier.create(Mono.empty()).expectError(IllegalArgumentException.class).verify(),
        StepVerifier.create(Mono.empty())
            .verifyErrorSatisfies(t -> assertThat(t).isInstanceOf(IllegalStateException.class)));
  }

  Duration testStepVerifierLastStepVerifyErrorMatches() {
    return StepVerifier.create(Mono.empty())
        .expectErrorMatches(IllegalArgumentException.class::equals)
        .verify();
  }

  Duration testStepVerifierLastStepVerifyErrorSatisfies() {
    return StepVerifier.create(Mono.empty()).expectErrorSatisfies(t -> {}).verify();
  }

  Duration testStepVerifierLastStepVerifyErrorMessage() {
    return StepVerifier.create(Mono.empty()).expectErrorMessage("foo").verify();
  }

  Duration testStepVerifierLastStepVerifyTimeout() {
    return StepVerifier.create(Mono.empty()).expectTimeout(Duration.ZERO).verify();
  }
}
