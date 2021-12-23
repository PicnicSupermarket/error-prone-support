package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.MoreCollectors.toOptional;

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
        Mono.defer(() -> Mono.justOrEmpty(Optional.of(1))),
        Mono.defer(() -> Mono.justOrEmpty(Optional.of(2))));
  }

  @Template(MonoDeferredError.class)
  Mono<Void> testMonoDeferredError() {
    return Mono.error(() -> new IllegalStateException());
  }

  @Template(FluxDeferredError.class)
  Flux<Void> testFluxDeferredError() {
    return Flux.error(() -> new IllegalStateException());
  }

  @Template(MonoErrorSupplier.class)
  Mono<Void> testMonoErrorSupplier() {
    return Mono.error(((Supplier<RuntimeException>) null));
  }

  @Template(FluxErrorSupplier.class)
  Flux<Void> testFluxErrorSupplier() {
    return Flux.error(((Supplier<RuntimeException>) null));
  }

  @Template(MonoThenReturn.class)
  Mono<String> testMonoThenReturn() {
    return Mono.empty().thenReturn("foo");
  }

  @Template(MonoSwitchIfEmptyOfEmptyPublisher.class)
  Mono<Integer> testMonoSwitchIfEmptyOfEmptyPublisher() {
    return Mono.just(1);
  }

  @Template(FluxSwitchIfEmptyOfEmptyPublisher.class)
  ImmutableSet<Flux<Integer>> testFluxSwitchIfEmptyOfEmptyPublisher() {
    return ImmutableSet.of(Flux.just(1), Flux.just(2));
  }

  @Template(MonoFlatMapToFlux.class)
  Flux<String> testMonoFlatMapToFlux() {
    return Mono.just("foo").flatMap(s -> Mono.just(s + s)).flux();
  }

  @Template(MonoFlux.class)
  Flux<String> testMonoFlux() {
    return Mono.just("foo").flux();
  }

  @Template(FluxIdentity.class)
  Flux<String> testFluxIdentity() {
    return Flux.just("foo");
  }

  @Template(MonoCollectToOptional.class)
  ImmutableSet<Mono<Optional<String>>> testMonoCollectToOptional() {
    return ImmutableSet.of(
        Mono.just("foo").flux().collect(toOptional()),
        Mono.just("bar").flux().collect(toOptional()));
  }

  @Template(PublisherProbeEmpty.class)
  ImmutableSet<PublisherProbe<Void>> testPublisherProbeEmpty() {
    return ImmutableSet.of(PublisherProbe.empty(), PublisherProbe.empty());
  }

  @Template(StepVerifierFromMono.class)
  StepVerifier.FirstStep<Integer> testStepVerifierFromMono() {
    return Mono.just(1).as(StepVerifier::create);
  }

  @Template(StepVerifierFromFlux.class)
  StepVerifier.FirstStep<Integer> testStepVerifierFromFlux() {
    return Flux.just(1).as(StepVerifier::create);
  }

  @Template(StepVerifierStepExpectNextEmpty.class)
  StepVerifier.Step<Integer> testStepVerifierStepExpectNextEmpty() {
    return StepVerifier.create(Mono.just(0));
  }

  @Template(StepVerifierStepExpectNext.class)
  ImmutableSet<StepVerifier.Step<String>> testStepVerifierStepExpectNext() {
    return ImmutableSet.of(
        StepVerifier.create(Mono.just("foo")).expectNext("bar"),
        StepVerifier.create(Mono.just("baz")).expectNext("qux"));
  }

  @Template(StepVerifierLastStepVerifyComplete.class)
  Duration testStepVerifierLastStepVerifyComplete() {
    return StepVerifier.create(Mono.empty()).verifyComplete();
  }

  @Template(StepVerifierLastStepVerifyError.class)
  Duration testStepVerifierLastStepVerifyError() {
    return StepVerifier.create(Mono.empty()).verifyError();
  }

  @Template(StepVerifierLastStepVerifyErrorClass.class)
  Duration testStepVerifierLastStepVerifyErrorClass() {
    return StepVerifier.create(Mono.empty()).verifyError(IllegalArgumentException.class);
  }

  @Template(StepVerifierLastStepVerifyErrorMatches.class)
  Duration testStepVerifierLastStepVerifyErrorMatches() {
    return StepVerifier.create(Mono.empty())
        .verifyErrorMatches(IllegalArgumentException.class::equals);
  }

  @Template(StepVerifierLastStepVerifyErrorSatisfies.class)
  Duration testStepVerifierLastStepVerifyErrorSatisfies() {
    return StepVerifier.create(Mono.empty()).verifyErrorSatisfies(t -> {});
  }

  @Template(StepVerifierLastStepVerifyErrorMessage.class)
  Duration testStepVerifierLastStepVerifyErrorMessage() {
    return StepVerifier.create(Mono.empty()).verifyErrorMessage("foo");
  }

  @Template(StepVerifierLastStepVerifyTimeout.class)
  Duration testStepVerifierLastStepVerifyTimeout() {
    return StepVerifier.create(Mono.empty()).verifyTimeout(Duration.ZERO);
  }
}
