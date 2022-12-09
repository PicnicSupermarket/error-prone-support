package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.MoreCollectors.toOptional;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static reactor.function.TupleUtils.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
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
        Mono.fromSupplier(() -> toString()),
        Mono.fromCallable(getClass()::getDeclaredConstructor),
        Mono.fromSupplier(this::toString));
  }

  ImmutableSet<Mono<String>> testMonoEmpty() {
    return ImmutableSet.of(Mono.empty(), Mono.empty());
  }

  Mono<Integer> testMonoJust() {
    return Mono.just(1);
  }

  Mono<Integer> testMonoJustOrEmpty() {
    return Mono.justOrEmpty(1);
  }

  ImmutableSet<Mono<Integer>> testMonoFromOptional() {
    return ImmutableSet.of(
        Mono.defer(() -> Mono.justOrEmpty(Optional.of(1))),
        Mono.defer(() -> Mono.justOrEmpty(Optional.of(2))));
  }

  Optional<Mono<String>> testOptionalMapMonoJust() {
    return Optional.of("foo").map(Mono::just);
  }

  Mono<Integer> testMonoFromOptionalSwitchIfEmpty() {
    return Mono.justOrEmpty(Optional.of(1)).switchIfEmpty(Mono.just(2));
  }

  Mono<Tuple2<String, Integer>> testMonoZip() {
    return Mono.zip(Mono.just("foo"), Mono.just(1));
  }

  Mono<String> testMonoZipWithCombinator() {
    return Mono.zip(Mono.just("foo"), Mono.just(1)).map(TupleUtils.function(String::repeat));
  }

  Flux<Tuple2<String, Integer>> testFluxZip() {
    return Flux.zip(Flux.just("foo", "bar"), Flux.just(1, 2));
  }

  Flux<String> testFluxZipWithCombinator() {
    return Flux.zip(Flux.just("foo", "bar"), Flux.just(1, 2))
        .map(TupleUtils.function(String::repeat));
  }

  Flux<String> testFluxZipWithIterable() {
    return Flux.just("foo", "bar")
        .zipWithIterable(ImmutableSet.of(1, 2))
        .map(function(String::repeat));
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

  Flux<Integer> testFluxTake() {
    return Flux.just(1, 2, 3).take(1, true);
  }

  Mono<String> testMonoDefaultIfEmpty() {
    return Mono.just("foo").defaultIfEmpty("bar");
  }

  ImmutableSet<Flux<String>> testFluxDefaultIfEmpty() {
    return ImmutableSet.of(
        Flux.just("foo").defaultIfEmpty("bar"), Flux.just("baz").defaultIfEmpty("qux"));
  }

  Mono<Integer> testMonoSwitchIfEmptyOfEmptyPublisher() {
    return Mono.just(1);
  }

  ImmutableSet<Flux<Integer>> testFluxSwitchIfEmptyOfEmptyPublisher() {
    return ImmutableSet.of(Flux.just(1), Flux.just(2));
  }

  ImmutableSet<Flux<Integer>> testFluxConcatMap() {
    return ImmutableSet.of(
        Flux.just(1).concatMap(Mono::just),
        Flux.just(2).concatMap(Mono::just),
        Flux.just(3).concatMap(Mono::just));
  }

  ImmutableSet<Flux<Integer>> testFluxConcatMapWithPrefetch() {
    return ImmutableSet.of(
        Flux.just(1).concatMap(Mono::just, 3),
        Flux.just(2).concatMap(Mono::just, 4),
        Flux.just(3).concatMap(Mono::just, 5));
  }

  Flux<Integer> testFluxConcatMapIterable() {
    return Flux.just(1, 2).concatMapIterable(ImmutableList::of);
  }

  Flux<Integer> testFluxConcatMapIterableWithPrefetch() {
    return Flux.just(1, 2).concatMapIterable(ImmutableList::of, 3);
  }

  Flux<String> testMonoFlatMapToFlux() {
    return Mono.just("foo").flatMap(s -> Mono.fromSupplier(() -> s + s)).flux();
  }

  ImmutableSet<Mono<String>> testMonoMap() {
    return ImmutableSet.of(Mono.just("foo").map(s -> s), Mono.just("bar").map(s -> s.substring(1)));
  }

  ImmutableSet<Flux<Integer>> testFluxMap() {
    return ImmutableSet.of(
        Flux.just(1).map(n -> n),
        Flux.just(1).map(n -> n * 2),
        Flux.just(1).map(n -> n),
        Flux.just(1).map(n -> n * 2),
        Flux.just(1).map(n -> n),
        Flux.just(1).map(n -> n * 2),
        Flux.just(1).map(n -> n),
        Flux.just(1).map(n -> n * 2),
        Flux.just(1).map(n -> n),
        Flux.just(1).map(n -> n * 2),
        Flux.just(1).map(n -> n),
        Flux.just(1).map(n -> n * 2),
        Flux.just(1).map(n -> n),
        Flux.just(1).map(n -> n * 2),
        Flux.just(1).map(n -> n),
        Flux.just(1).map(n -> n * 2),
        Flux.just(1).map(n -> n),
        Flux.just(1).map(n -> n * 2),
        Flux.just(1).map(n -> n),
        Flux.just(1).map(n -> n * 2),
        Flux.just(1).map(n -> n),
        Flux.just(1).map(n -> n * 2));
  }

  ImmutableSet<Mono<String>> testMonoMapNotNull() {
    return ImmutableSet.of(
        Mono.just("foo").mapNotNull(s -> s), Mono.just("bar").mapNotNull(s -> s.substring(1)));
  }

  ImmutableSet<Flux<Integer>> testFluxMapNotNull() {
    return ImmutableSet.of(
        Flux.just(1).mapNotNull(n -> n),
        Flux.just(1).mapNotNull(n -> n * 2),
        Flux.just(1).mapNotNull(n -> n),
        Flux.just(1).mapNotNull(n -> n * 2),
        Flux.just(1).mapNotNull(n -> n),
        Flux.just(1).mapNotNull(n -> n * 2),
        Flux.just(1).mapNotNull(n -> n),
        Flux.just(1).mapNotNull(n -> n * 2),
        Flux.just(1).mapNotNull(n -> n),
        Flux.just(1).mapNotNull(n -> n * 2),
        Flux.just(1).mapNotNull(n -> n),
        Flux.just(1).mapNotNull(n -> n * 2),
        Flux.just(1).mapNotNull(n -> n),
        Flux.just(1).mapNotNull(n -> n * 2),
        Flux.just(1).mapNotNull(n -> n),
        Flux.just(1).mapNotNull(n -> n * 2),
        Flux.just(1).mapNotNull(n -> n),
        Flux.just(1).mapNotNull(n -> n * 2),
        Flux.just(1).mapNotNull(n -> n),
        Flux.just(1).mapNotNull(n -> n * 2),
        Flux.just(1).mapNotNull(n -> n),
        Flux.just(1).mapNotNull(n -> n * 2));
  }

  ImmutableSet<Flux<String>> testMonoFlux() {
    return ImmutableSet.of(
        Mono.just("foo").flux(), Mono.just("bar").flux(), Mono.just("baz").flux());
  }

  Mono<Void> testMonoFluxThen() {
    return Mono.just("foo").then();
  }

  Mono<Void> testMonoVoidThen() {
    return Mono.just("foo").then();
  }

  Mono<Optional<String>> testMonoCollectToOptional() {
    return Mono.just("foo").flux().collect(toOptional());
  }

  Mono<Number> testMonoCast() {
    return Mono.just(1).cast(Number.class);
  }

  Flux<Number> testFluxCast() {
    return Flux.just(1).cast(Number.class);
  }

  Mono<String> testMonoFlatMap() {
    return Mono.just("foo").flatMap(Mono::just);
  }

  Flux<String> testMonoFlatMapMany() {
    return Mono.just("foo").flatMapMany(Mono::just);
  }

  ImmutableSet<Flux<String>> testConcatMapIterableIdentity() {
    return ImmutableSet.of(
        Flux.just(ImmutableList.of("foo")).concatMapIterable(identity()),
        Flux.just(ImmutableList.of("bar")).concatMapIterable(identity()));
  }

  ImmutableSet<Flux<String>> testConcatMapIterableIdentityWithPrefetch() {
    return ImmutableSet.of(
        Flux.just(ImmutableList.of("foo")).concatMapIterable(identity(), 1),
        Flux.just(ImmutableList.of("bar")).concatMapIterable(identity(), 2));
  }

  Mono<Integer> testMonoDoOnError() {
    return Mono.just(1).doOnError(IllegalArgumentException.class, e -> {});
  }

  Flux<Integer> testFluxDoOnError() {
    return Flux.just(1).doOnError(IllegalArgumentException.class, e -> {});
  }

  Mono<Integer> testMonoOnErrorComplete() {
    return Mono.just(1).onErrorComplete();
  }

  ImmutableSet<Flux<Integer>> testFluxOnErrorComplete() {
    return ImmutableSet.of(Flux.just(1).onErrorComplete(), Flux.just(2).onErrorComplete());
  }

  ImmutableSet<Mono<Integer>> testMonoOnErrorCompleteClass() {
    return ImmutableSet.of(
        Mono.just(1).onErrorComplete(IllegalArgumentException.class),
        Mono.just(2).onErrorComplete(IllegalStateException.class));
  }

  ImmutableSet<Flux<Integer>> testFluxOnErrorCompleteClass() {
    return ImmutableSet.of(
        Flux.just(1).onErrorComplete(IllegalArgumentException.class),
        Flux.just(2).onErrorComplete(IllegalStateException.class),
        Flux.just(3).onErrorComplete(AssertionError.class));
  }

  Mono<Integer> testMonoOnErrorCompletePredicate() {
    return Mono.just(1).onErrorComplete(e -> e.getCause() == null);
  }

  ImmutableSet<Flux<Integer>> testFluxOnErrorCompletePredicate() {
    return ImmutableSet.of(
        Flux.just(1).onErrorComplete(e -> e.getCause() == null),
        Flux.just(2).onErrorComplete(e -> e.getCause() != null));
  }

  Mono<Integer> testMonoOnErrorContinue() {
    return Mono.just(1).onErrorContinue(IllegalArgumentException.class, (e, v) -> {});
  }

  Flux<Integer> testFluxOnErrorContinue() {
    return Flux.just(1).onErrorContinue(IllegalArgumentException.class, (e, v) -> {});
  }

  Mono<Integer> testMonoOnErrorMap() {
    return Mono.just(1).onErrorMap(IllegalArgumentException.class, e -> e);
  }

  Flux<Integer> testFluxOnErrorMap() {
    return Flux.just(1).onErrorMap(IllegalArgumentException.class, e -> e);
  }

  Mono<Integer> testMonoOnErrorResume() {
    return Mono.just(1).onErrorResume(IllegalArgumentException.class, e -> Mono.just(2));
  }

  Flux<Integer> testFluxOnErrorResume() {
    return Flux.just(1).onErrorResume(IllegalArgumentException.class, e -> Flux.just(2));
  }

  Mono<Integer> testMonoOnErrorReturn() {
    return Mono.just(1).onErrorReturn(IllegalArgumentException.class, 2);
  }

  Flux<Integer> testFluxOnErrorReturn() {
    return Flux.just(1).onErrorReturn(IllegalArgumentException.class, 2);
  }

  Flux<Integer> testFluxFilterSort() {
    return Flux.just(1, 4, 3, 2).filter(i -> i % 2 == 0).sort();
  }

  Flux<Integer> testFluxFilterSortWithComparator() {
    return Flux.just(1, 4, 3, 2).filter(i -> i % 2 == 0).sort(reverseOrder());
  }

  ImmutableSet<Context> testContextEmpty() {
    return ImmutableSet.of(Context.empty(), Context.empty());
  }

  ImmutableSet<PublisherProbe<Void>> testPublisherProbeEmpty() {
    return ImmutableSet.of(PublisherProbe.empty(), PublisherProbe.empty());
  }

  StepVerifier.FirstStep<Integer> testStepVerifierFromMono() {
    return Mono.just(1).as(StepVerifier::create);
  }

  StepVerifier.FirstStep<Integer> testStepVerifierFromFlux() {
    return Flux.just(1).as(StepVerifier::create);
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

  ImmutableSet<Duration> testStepVerifierLastStepVerifyErrorClass() {
    return ImmutableSet.of(
        StepVerifier.create(Mono.empty()).verifyError(IllegalArgumentException.class),
        StepVerifier.create(Mono.empty()).verifyError(IllegalStateException.class));
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
