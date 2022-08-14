package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ReactorRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(assertThat(0), HashMap.class, ImmutableMap.class);
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

  Mono<Tuple2<String, Integer>> testMonoZip() {
    return Mono.just("foo").zipWith(Mono.just(1));
  }

  Mono<String> testMonoZipWithCombinator() {
    return Mono.just("foo").zipWith(Mono.just(1), String::repeat);
  }

  Flux<Tuple2<String, Integer>> testFluxZip() {
    return Flux.just("foo", "bar").zipWith(Flux.just(1, 2));
  }

  Flux<String> testFluxZipWithCombinator() {
    return Flux.just("foo", "bar").zipWith(Flux.just(1, 2), String::repeat);
  }

  Flux<String> testFluxZipWithIterable() {
    return Flux.just("foo", "bar").zipWithIterable(ImmutableSet.of(1, 2), String::repeat);
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

  Flux<Integer> testFluxTake() {
    return Flux.just(1, 2, 3).take(1);
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

  ImmutableSet<Flux<Integer>> testFluxConcatMapWithPrefetch() {
    return ImmutableSet.of(
        Flux.just(1).flatMap(Mono::just, 1, 3), Flux.just(2).flatMapSequential(Mono::just, 1, 4));
  }

  Flux<Integer> testFluxConcatMapIterable() {
    return Flux.just(1, 2).flatMapIterable(ImmutableList::of);
  }

  Flux<Integer> testFluxConcatMapIterableWithPrefetch() {
    return Flux.just(1, 2).flatMapIterable(ImmutableList::of, 3);
  }

  Flux<String> testMonoFlatMapToFlux() {
    return Mono.just("foo").flatMapMany(s -> Mono.fromSupplier(() -> s + s));
  }

  ImmutableSet<Publisher<String>> testMonoMap() {
    return ImmutableSet.of(
        Mono.just("foo").flatMap(s -> Mono.just(s)),
        Mono.just("bar").flatMapMany(s -> Mono.just(s.substring(1))),
        Mono.just("baz").flatMapMany(s -> Flux.just(s)));
  }

  ImmutableSet<Flux<String>> testFluxMap() {
    return ImmutableSet.of(
        Flux.just("fooConcat").concatMap(s -> Mono.just(s.substring(1))),
        Flux.just("fooConcat").concatMap(s -> Flux.just("foo")),
        Flux.just("fooConcat").concatMap(s -> Mono.just(s), 2),
        Flux.just("fooConcat").concatMap(s -> Flux.just(s), 2),
        Flux.just("fooConcatDelay").concatMapDelayError(s -> Mono.just(s)),
        Flux.just("fooConcatDelay").concatMapDelayError(s -> Flux.just(s)),
        Flux.just("fooConcatDelay").concatMapDelayError(s -> Mono.just(s), 2),
        Flux.just("fooConcatDelay").concatMapDelayError(s -> Flux.just(s), 2),
        Flux.just("fooFlat").flatMap(s -> Mono.just(s), 2),
        Flux.just("fooFlat").flatMap(s -> Flux.just(s), 2),
        Flux.just("fooFlat").flatMap(s -> Mono.just(s), 2, 2),
        Flux.just("fooFlat").flatMap(s -> Flux.just(s), 2, 2),
        Flux.just("fooFlatDelay").flatMapDelayError(s -> Mono.just(s), 2, 2),
        Flux.just("fooFlatDelay").flatMapDelayError(s -> Flux.just(s), 2, 2),
        Flux.just("fooSeq").flatMapSequential(s -> Mono.just(s), 2),
        Flux.just("fooSeq").flatMapSequential(s -> Flux.just(s), 2),
        Flux.just("fooSeq").flatMapSequential(s -> Mono.just(s), 2, 2),
        Flux.just("fooSeq").flatMapSequential(s -> Flux.just(s), 2, 2),
        Flux.just("fooSeqDelay").flatMapSequentialDelayError(s -> Mono.just(s), 2, 2),
        Flux.just("fooSeqDelay").flatMapSequentialDelayError(s -> Flux.just(s), 2, 2),
        Flux.just("fooSwitch").switchMap(s -> Mono.just(s)),
        Flux.just("fooSwitch").switchMap(s -> Flux.just(s)));
  }

  ImmutableSet<Publisher<String>> testMonoMapNotNull() {
    return ImmutableSet.of(
        Mono.just("foo").flatMap(s -> Mono.justOrEmpty(s)),
        Mono.just("bar").flatMapMany(s -> Mono.justOrEmpty(s.substring(1))));
  }

  ImmutableSet<Flux<String>> testFluxMapNotNull() {
    return ImmutableSet.of(
        Flux.just("fooConcat").concatMap(s -> Mono.justOrEmpty(s.substring(1))),
        Flux.just("fooConcat").concatMap(s -> Mono.justOrEmpty("foo"), 2),
        Flux.just("fooConcatDelay").concatMapDelayError(s -> Mono.justOrEmpty(s)),
        Flux.just("fooConcatDelay").concatMapDelayError(s -> Mono.justOrEmpty(s), 2),
        Flux.just("fooFlat").flatMap(s -> Mono.justOrEmpty(s), 2),
        Flux.just("fooFlat").flatMap(s -> Mono.justOrEmpty(s), 2, 2),
        Flux.just("fooFlatDelay").flatMapDelayError(s -> Mono.justOrEmpty(s), 2, 2),
        Flux.just("fooSeq").flatMapSequential(s -> Mono.justOrEmpty(s), 2),
        Flux.just("fooSeq").flatMapSequential(s -> Mono.justOrEmpty(s), 2, 2),
        Flux.just("fooSeqDelay").flatMapSequentialDelayError(s -> Mono.justOrEmpty(s), 2, 2),
        Flux.just("fooSwitch").switchMap(s -> Mono.justOrEmpty(s)));
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

  ImmutableSet<Flux<String>> testConcatMapIterableIdentity() {
    return ImmutableSet.of(
        Flux.just(ImmutableList.of("foo")).concatMap(list -> Flux.fromIterable(list)),
        Flux.just(ImmutableList.of("bar")).concatMap(Flux::fromIterable));
  }

  ImmutableSet<Flux<String>> testConcatMapIterableIdentityWithPrefetch() {
    return ImmutableSet.of(
        Flux.just(ImmutableList.of("foo")).concatMap(list -> Flux.fromIterable(list), 1),
        Flux.just(ImmutableList.of("bar")).concatMap(Flux::fromIterable, 2));
  }

  Mono<Integer> testMonoOnErrorComplete() {
    return Mono.just(1).onErrorResume(e -> Mono.empty());
  }

  ImmutableSet<Flux<Integer>> testFluxOnErrorComplete() {
    return ImmutableSet.of(
        Flux.just(1).onErrorResume(e -> Mono.empty()),
        Flux.just(2).onErrorResume(e -> Flux.empty()));
  }

  ImmutableSet<Context> testContextEmpty() {
    return ImmutableSet.of(Context.of(new HashMap<>()), Context.of(ImmutableMap.of()));
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
