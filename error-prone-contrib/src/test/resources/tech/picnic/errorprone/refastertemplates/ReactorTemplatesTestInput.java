package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.FluxDeferredError;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.FluxErrorSupplier;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.FluxIdentity;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.FluxSwitchIfEmptyOfEmptyPublisher;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.MonoCollectToOptional;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.MonoDeferredError;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.MonoErrorSupplier;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.MonoFlatMapToFlux;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.MonoFlux;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.MonoFromOptional;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.MonoSwitchIfEmptyOfEmptyPublisher;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.MonoThenReturn;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.PublisherProbeEmpty;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.StepVerifierFromFlux;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.StepVerifierFromMono;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.StepVerifierLastStepVerifyComplete;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.StepVerifierLastStepVerifyError;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.StepVerifierLastStepVerifyErrorClass;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.StepVerifierLastStepVerifyErrorMatches;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.StepVerifierLastStepVerifyErrorMessage;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.StepVerifierLastStepVerifyErrorSatisfies;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.StepVerifierLastStepVerifyTimeout;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.StepVerifierStepExpectNext;
import tech.picnic.errorprone.refastertemplates.ReactorTemplates.StepVerifierStepExpectNextEmpty;

@TemplateCollection(ReactorTemplates.class)
final class ReactorTemplatesTest implements RefasterTemplateTestCase {
  @Template(MonoFromOptional.class)
  ImmutableSet<Mono<Integer>> testMonoFromOptional() {
    return ImmutableSet.of(
        Mono.fromCallable(() -> Optional.of(1).orElse(null)),
        Mono.fromSupplier(() -> Optional.of(2).orElse(null)));
  }

  @Template(MonoDeferredError.class)
  Mono<Void> testMonoDeferredError() {
    return Mono.defer(() -> Mono.error(new IllegalStateException()));
  }

  @Template(FluxDeferredError.class)
  Flux<Void> testFluxDeferredError() {
    return Flux.defer(() -> Flux.error(new IllegalStateException()));
  }

  @Template(MonoErrorSupplier.class)
  Mono<Void> testMonoErrorSupplier() {
    return Mono.error(() -> ((Supplier<RuntimeException>) null).get());
  }

  @Template(FluxErrorSupplier.class)
  Flux<Void> testFluxErrorSupplier() {
    return Flux.error(() -> ((Supplier<RuntimeException>) null).get());
  }

  @Template(MonoThenReturn.class)
  Mono<String> testMonoThenReturn() {
    return Mono.empty().then(Mono.just("foo"));
  }

  @Template(MonoSwitchIfEmptyOfEmptyPublisher.class)
  Mono<Integer> testMonoSwitchIfEmptyOfEmptyPublisher() {
    return Mono.just(1).switchIfEmpty(Mono.empty());
  }

  @Template(FluxSwitchIfEmptyOfEmptyPublisher.class)
  ImmutableSet<Flux<Integer>> testFluxSwitchIfEmptyOfEmptyPublisher() {
    return ImmutableSet.of(
        Flux.just(1).switchIfEmpty(Mono.empty()), Flux.just(2).switchIfEmpty(Flux.empty()));
  }

  @Template(MonoFlatMapToFlux.class)
  Flux<String> testMonoFlatMapToFlux() {
    return Mono.just("foo").flatMapMany(s -> Mono.just(s + s));
  }

  @Template(MonoFlux.class)
  Flux<String> testMonoFlux() {
    return Flux.concat(Mono.just("foo"));
  }

  @Template(FluxIdentity.class)
  Flux<String> testFluxIdentity() {
    return Flux.concat(Flux.just("foo"));
  }

  @Template(MonoCollectToOptional.class)
  ImmutableSet<Mono<Optional<String>>> testMonoCollectToOptional() {
    return ImmutableSet.of(
        Mono.just("foo").map(Optional::of).defaultIfEmpty(Optional.empty()),
        Mono.just("bar").map(Optional::of).switchIfEmpty(Mono.just(Optional.empty())));
  }

  @Template(PublisherProbeEmpty.class)
  ImmutableSet<PublisherProbe<Void>> testPublisherProbeEmpty() {
    return ImmutableSet.of(PublisherProbe.of(Mono.empty()), PublisherProbe.of(Flux.empty()));
  }

  @Template(StepVerifierFromMono.class)
  StepVerifier.FirstStep<Integer> testStepVerifierFromMono() {
    return StepVerifier.create(Mono.just(1));
  }

  @Template(StepVerifierFromFlux.class)
  StepVerifier.FirstStep<Integer> testStepVerifierFromFlux() {
    return StepVerifier.create(Flux.just(1));
  }

  @Template(StepVerifierStepExpectNextEmpty.class)
  StepVerifier.Step<Integer> testStepVerifierStepExpectNextEmpty() {
    return StepVerifier.create(Mono.just(0)).expectNext();
  }

  @Template(StepVerifierStepExpectNext.class)
  ImmutableSet<StepVerifier.Step<String>> testStepVerifierStepExpectNext() {
    return ImmutableSet.of(
        StepVerifier.create(Mono.just("foo")).expectNextMatches(s -> s.equals("bar")),
        StepVerifier.create(Mono.just("baz")).expectNextMatches("qux"::equals));
  }

  @Template(StepVerifierLastStepVerifyComplete.class)
  Duration testStepVerifierLastStepVerifyComplete() {
    return StepVerifier.create(Mono.empty()).expectComplete().verify();
  }

  @Template(StepVerifierLastStepVerifyError.class)
  Duration testStepVerifierLastStepVerifyError() {
    return StepVerifier.create(Mono.empty()).expectError().verify();
  }

  @Template(StepVerifierLastStepVerifyErrorClass.class)
  Duration testStepVerifierLastStepVerifyErrorClass() {
    return StepVerifier.create(Mono.empty()).expectError(IllegalArgumentException.class).verify();
  }

  @Template(StepVerifierLastStepVerifyErrorMatches.class)
  Duration testStepVerifierLastStepVerifyErrorMatches() {
    return StepVerifier.create(Mono.empty())
        .expectErrorMatches(IllegalArgumentException.class::equals)
        .verify();
  }

  @Template(StepVerifierLastStepVerifyErrorSatisfies.class)
  Duration testStepVerifierLastStepVerifyErrorSatisfies() {
    return StepVerifier.create(Mono.empty()).expectErrorSatisfies(t -> {}).verify();
  }

  @Template(StepVerifierLastStepVerifyErrorMessage.class)
  Duration testStepVerifierLastStepVerifyErrorMessage() {
    return StepVerifier.create(Mono.empty()).expectErrorMessage("foo").verify();
  }

  @Template(StepVerifierLastStepVerifyTimeout.class)
  Duration testStepVerifierLastStepVerifyTimeout() {
    return StepVerifier.create(Mono.empty()).expectTimeout(Duration.ZERO).verify();
  }
}
