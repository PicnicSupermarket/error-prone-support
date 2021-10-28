package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.MoreCollectors.toOptional;

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
        Mono.defer(() -> Mono.justOrEmpty(Optional.of(1))),
        Mono.defer(() -> Mono.justOrEmpty(Optional.of(2))));
  }

  Mono<Void> testMonoDeferredError() {
    return Mono.error(() -> new IllegalStateException());
  }

  Flux<Void> testFluxDeferredError() {
    return Flux.error(() -> new IllegalStateException());
  }

  Mono<Void> testMonoErrorSupplier() {
    return Mono.error(((Supplier<RuntimeException>) null));
  }

  Flux<Void> testFluxErrorSupplier() {
    return Flux.error(((Supplier<RuntimeException>) null));
  }

  Mono<String> testMonoThenReturn() {
    return Mono.empty().thenReturn("foo");
  }

  Mono<Integer> testMonoSwitchIfEmptyOfEmptyPublisher() {
    return Mono.just(1);
  }

  ImmutableSet<Flux<Integer>> testFluxSwitchIfEmptyOfEmptyPublisher() {
    return ImmutableSet.of(Flux.just(1), Flux.just(2));
  }

  Flux<String> testMonoFlatMapToFlux() {
    return Mono.just("foo").flatMap(s -> Mono.just(s + s)).flux();
  }

  Flux<String> testMonoFlux() {
    return Mono.just("foo").flux();
  }

  Flux<String> testFluxIdentity() {
    return Flux.just("foo");
  }

  ImmutableSet<Mono<Optional<String>>> testMonoCollectToOptional() {
    return ImmutableSet.of(
        Mono.just("foo").flux().collect(toOptional()),
        Mono.just("bar").flux().collect(toOptional()));
  }

  ImmutableSet<PublisherProbe<Void>> testPublisherProbeEmpty() {
    return ImmutableSet.of(PublisherProbe.empty(), PublisherProbe.empty());
  }

  ImmutableSet<StepVerifier.FirstStep<Integer>> testStepVerifierCreate() {
    return ImmutableSet.of(
        Mono.just(1).as(StepVerifier::create), Flux.just(1).as(StepVerifier::create));
  }

  StepVerifier.Step<Integer> testStepVerifierStepExpectNextEmpty() {
    return StepVerifier.create(Mono.just(0));
  }

  ImmutableSet<StepVerifier.Step<String>> testStepVerifierStepExpectNext() {
    return ImmutableSet.of(
        StepVerifier.create(Mono.just("foo")).expectNext("bar"),
        StepVerifier.create(Mono.just("baz")).expectNext("qux"));
  }

  Duration testStepVerifierLastStepVerifyComplete() {
    return StepVerifier.create(Mono.empty()).verifyComplete();
  }

  Duration testStepVerifierLastStepVerifyError() {
    return StepVerifier.create(Mono.empty()).verifyError();
  }

  Duration testStepVerifierLastStepVerifyErrorClass() {
    return StepVerifier.create(Mono.empty()).verifyError(IllegalArgumentException.class);
  }

  Duration testStepVerifierLastStepVerifyErrorMatches() {
    return StepVerifier.create(Mono.empty())
        .verifyErrorMatches(IllegalArgumentException.class::equals);
  }

  Duration testStepVerifierLastStepVerifyErrorSatisfies() {
    return StepVerifier.create(Mono.empty()).verifyErrorSatisfies(t -> {});
  }

  Duration testStepVerifierLastStepVerifyErrorMessage() {
    return StepVerifier.create(Mono.empty()).verifyErrorMessage("foo");
  }

  Duration testStepVerifierLastStepVerifyTimeout() {
    return StepVerifier.create(Mono.empty()).verifyTimeout(Duration.ZERO);
  }
}
