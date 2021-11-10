package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

final class ReactorTemplatesTest implements RefasterTemplateTestCase {
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

  Flux<String> testMonoFlatMapToFlux() {
    return Mono.just("foo").flatMapMany(s -> Mono.just(s + s));
  }

  Flux<String> testFluxConcatMono() {
    return Flux.concat(Mono.just("foo"));
  }

  Flux<String> testFluxConcatFlux() {
    return Flux.concat(Flux.just("foo", "bar"));
  }

  ImmutableSet<Mono<Optional<String>>> testMonoCollectToOptional() {
    return ImmutableSet.of(
        Mono.just("foo").map(Optional::of).defaultIfEmpty(Optional.empty()),
        Mono.just("bar").map(Optional::of).switchIfEmpty(Mono.just(Optional.empty())));
  }

  ImmutableSet<PublisherProbe<Void>> testPublisherProbeEmpty() {
    return ImmutableSet.of(PublisherProbe.of(Mono.empty()), PublisherProbe.of(Flux.empty()));
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

  Duration testStepVerifierLastStepVerifyErrorClass() {
    return StepVerifier.create(Mono.empty()).expectError(IllegalArgumentException.class).verify();
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
