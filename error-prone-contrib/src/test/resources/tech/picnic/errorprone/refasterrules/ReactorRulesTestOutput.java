package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.MoreCollectors.toOptional;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.minBy;
import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;
import static reactor.function.TupleUtils.function;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.math.MathFlux;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ReactorRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        ArrayList.class,
        Collection.class,
        HashMap.class,
        List.class,
        ImmutableCollection.class,
        ImmutableMap.class,
        assertThat(0),
        maxBy(null),
        minBy(null),
        naturalOrder(),
        toCollection(null),
        toImmutableList(),
        toOptional());
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

  Mono<Integer> testMonoJustOrEmptyObject() {
    return Mono.justOrEmpty(1);
  }

  Mono<Integer> testMonoJustOrEmptyOptional() {
    return Mono.justOrEmpty(Optional.of(1));
  }

  ImmutableSet<Mono<Integer>> testMonoDeferMonoJustOrEmpty() {
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

  Flux<Tuple2<String, Integer>> testFluxZipWithIterable() {
    return Flux.just("foo", "bar").zipWithIterable(ImmutableSet.of(1, 2));
  }

  Flux<String> testFluxZipWithIterableBiFunction() {
    return Flux.just("foo", "bar").zipWithIterable(ImmutableSet.of(1, 2), String::repeat);
  }

  Flux<String> testFluxZipWithIterableMapFunction() {
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

  ImmutableSet<Mono<String>> testMonoThenReturn() {
    return ImmutableSet.of(
        Mono.just(1).thenReturn("foo"),
        Mono.just(2).thenReturn("bar"),
        Mono.just(3).thenReturn("baz"));
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

  ImmutableSet<Flux<?>> testFluxEmpty() {
    return ImmutableSet.of(
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty(),
        Flux.empty());
  }

  Flux<Integer> testFluxJust() {
    return Flux.just(0);
  }

  ImmutableSet<Mono<?>> testMonoIdentity() {
    return ImmutableSet.of(
        Mono.just(1),
        Mono.just(2),
        Mono.just(3),
        Mono.<Void>empty(),
        Mono.<Void>empty(),
        Mono.<ImmutableList<String>>empty());
  }

  Mono<Integer> testMonoSingle() {
    return Mono.just(1).single();
  }

  ImmutableSet<Flux<Integer>> testFluxSwitchIfEmptyOfEmptyPublisher() {
    return ImmutableSet.of(Flux.just(1), Flux.just(2));
  }

  ImmutableSet<Flux<Integer>> testFluxConcatMap() {
    return ImmutableSet.of(
        Flux.just(1).concatMap(Mono::just),
        Flux.just(2).concatMap(Mono::just),
        Flux.just(3).concatMap(Mono::just),
        Flux.just(4).concatMap(Mono::just),
        Flux.just(5).map(Mono::just).concatMap(v -> Mono.empty()));
  }

  ImmutableSet<Flux<Integer>> testFluxConcatMapWithPrefetch() {
    return ImmutableSet.of(
        Flux.just(1).concatMap(Mono::just, 3),
        Flux.just(2).concatMap(Mono::just, 4),
        Flux.just(3).concatMap(Mono::just, 5),
        Flux.just(4).concatMap(Mono::just, 6),
        Flux.just(5).map(Mono::just).concatMap(v -> Mono.empty(), 7));
  }

  ImmutableSet<Flux<Integer>> testMonoFlatMapIterable() {
    return ImmutableSet.of(
        Mono.just(1).flatMapIterable(ImmutableSet::of),
        Mono.just(2).flatMapIterable(ImmutableSet::of),
        Mono.just(3).flatMapIterable(ImmutableSet::of),
        Mono.just(4).map(ImmutableSet::of).flatMapIterable(v -> ImmutableSet.of()),
        Mono.just(5).flatMapIterable(ImmutableSet::of));
  }

  Flux<Integer> testMonoFlatMapIterableIdentity() {
    return Mono.just(ImmutableSet.of(1)).flatMapIterable(identity());
  }

  ImmutableSet<Flux<Integer>> testFluxConcatMapIterable() {
    return ImmutableSet.of(
        Flux.just(1).concatMapIterable(ImmutableList::of),
        Flux.just(2).concatMapIterable(ImmutableList::of),
        Flux.just(3).concatMapIterable(ImmutableList::of),
        Flux.just(4).map(ImmutableList::of).concatMapIterable(v -> ImmutableSet.of()));
  }

  ImmutableSet<Flux<Integer>> testFluxConcatMapIterableWithPrefetch() {
    return ImmutableSet.of(
        Flux.just(1).concatMapIterable(ImmutableList::of, 5),
        Flux.just(2).concatMapIterable(ImmutableList::of, 5),
        Flux.just(3).concatMapIterable(ImmutableList::of, 5),
        Flux.just(4).map(ImmutableList::of).concatMapIterable(v -> ImmutableSet.of(), 5));
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

  ImmutableSet<Mono<Void>> testMonoThen() {
    return ImmutableSet.of(Mono.just("foo").then(), Mono.just("bar").then());
  }

  ImmutableSet<Mono<Void>> testFluxThen() {
    return ImmutableSet.of(Flux.just("foo").then(), Flux.<Void>empty().then());
  }

  Mono<Void> testMonoThenEmpty() {
    return Mono.just("foo").thenEmpty(Mono.empty());
  }

  Mono<Void> testFluxThenEmpty() {
    return Flux.just("foo").thenEmpty(Mono.empty());
  }

  ImmutableSet<Flux<String>> testMonoThenMany() {
    return ImmutableSet.of(
        Mono.just("foo").thenMany(Flux.just("bar")), Mono.just("baz").thenMany(Flux.just("qux")));
  }

  Flux<String> testMonoThenMonoFlux() {
    return Mono.just("foo").then(Mono.just("bar")).flux();
  }

  Flux<String> testFluxThenMany() {
    return Flux.just("foo").thenMany(Flux.just("bar"));
  }

  ImmutableSet<Mono<?>> testMonoThenMono() {
    return ImmutableSet.of(
        Mono.just("foo").then(Mono.just("bar")),
        Mono.just("baz").then(Mono.just("qux")),
        Mono.just("quux").then(Mono.<Void>empty()));
  }

  ImmutableSet<Mono<?>> testFluxThenMono() {
    return ImmutableSet.of(
        Flux.just("foo").then(Mono.just("bar")), Flux.just("baz").then(Mono.<Void>empty()));
  }

  ImmutableSet<Mono<Optional<String>>> testMonoSingleOptional() {
    return ImmutableSet.of(
        Mono.just("foo").singleOptional(),
        Mono.just("bar").singleOptional(),
        Mono.just("baz").singleOptional());
  }

  Mono<Number> testMonoCast() {
    return Mono.just(1).cast(Number.class);
  }

  Flux<Number> testFluxCast() {
    return Flux.just(1).cast(Number.class);
  }

  Mono<Number> testMonoOfType() {
    return Mono.just(1).ofType(Number.class);
  }

  Flux<Number> testFluxOfType() {
    return Flux.just(1).ofType(Number.class);
  }

  ImmutableSet<Mono<String>> testMonoFlatMap() {
    return ImmutableSet.of(
        Mono.just("foo").flatMap(Mono::just),
        Mono.just("bar").flatMap(Mono::just),
        Mono.just("baz").map(Mono::just).flatMap(v -> Mono.empty()));
  }

  ImmutableSet<Flux<Integer>> testMonoFlatMapMany() {
    return ImmutableSet.of(
        Mono.just(1).flatMapMany(Mono::just),
        Mono.just(2).flatMapMany(Mono::just),
        Mono.just(3).map(Mono::just).flatMapMany(v -> Flux.empty()),
        Mono.just(4).flatMapMany(Mono::just),
        Mono.just(5).flatMapMany(Mono::just),
        Mono.just(6).flatMapMany(Mono::just),
        Mono.just(7).flatMapMany(Mono::just),
        Mono.just(8).flatMapMany(Mono::just),
        Mono.just(9).flatMapMany(Mono::just),
        Mono.just(10).flatMapMany(Mono::just),
        Mono.just(11).flatMapMany(Mono::just),
        Mono.just(12).flatMapMany(Mono::just),
        Mono.just(13).flatMapMany(Mono::just),
        Mono.just(14).flatMapMany(Mono::just),
        Mono.just(15).flatMapMany(Mono::just));
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

  Flux<String> testFluxFromIterable() {
    return Flux.fromIterable(ImmutableList.of("foo"));
  }

  ImmutableSet<Mono<Integer>> testFluxCountMapMathToIntExact() {
    return ImmutableSet.of(
        Flux.just(1).count().map(Math::toIntExact),
        Flux.just(2).count().map(Math::toIntExact),
        Flux.just(3).count().map(Math::toIntExact),
        Flux.just(4).count().map(Math::toIntExact),
        Flux.just(5).count().map(Math::toIntExact),
        Flux.just(6).count().map(Math::toIntExact));
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

  Flux<Integer> testFluxTakeWhile() {
    return Flux.just(1, 2, 3).takeWhile(i -> i % 2 == 0);
  }

  Mono<List<Integer>> testFluxCollectToImmutableList() {
    return Flux.just(1).collect(toImmutableList());
  }

  Mono<ImmutableSet<Integer>> testFluxCollectToImmutableSet() {
    return Flux.just(1).collect(toImmutableSet());
  }

  Flux<Integer> testFluxSort() {
    return Flux.just(1).sort();
  }

  Mono<Integer> testFluxTransformMin() {
    return Flux.just(1).transform(MathFlux::min).singleOrEmpty();
  }

  ImmutableSet<Mono<Integer>> testFluxTransformMinWithComparator() {
    return ImmutableSet.of(
        Flux.just(1).transform(f -> MathFlux.min(f, reverseOrder())).singleOrEmpty(),
        Flux.just(2).transform(f -> MathFlux.min(f, reverseOrder())).singleOrEmpty());
  }

  Mono<Integer> testFluxTransformMax() {
    return Flux.just(1).transform(MathFlux::max).singleOrEmpty();
  }

  ImmutableSet<Mono<Integer>> testFluxTransformMaxWithComparator() {
    return ImmutableSet.of(
        Flux.just(1).transform(f -> MathFlux.max(f, reverseOrder())).singleOrEmpty(),
        Flux.just(2).transform(f -> MathFlux.max(f, reverseOrder())).singleOrEmpty());
  }

  ImmutableSet<Mono<Integer>> testMathFluxMin() {
    return ImmutableSet.of(MathFlux.min(Flux.just(1)), MathFlux.min(Flux.just(2)));
  }

  ImmutableSet<Mono<Integer>> testMathFluxMax() {
    return ImmutableSet.of(MathFlux.max(Flux.just(1)), MathFlux.max(Flux.just(2)));
  }

  ImmutableSet<Context> testContextEmpty() {
    return ImmutableSet.of(Context.empty(), Context.of(ImmutableMap.of(1, 2)));
  }

  ImmutableSet<PublisherProbe<Void>> testPublisherProbeEmpty() {
    return ImmutableSet.of(PublisherProbe.empty(), PublisherProbe.empty());
  }

  ImmutableSet<StepVerifier.FirstStep<Integer>> testStepVerifierFromMono() {
    return ImmutableSet.of(
        Mono.just(1).as(StepVerifier::create), Mono.just(2).as(StepVerifier::create));
  }

  StepVerifier.FirstStep<Integer> testStepVerifierFromFlux() {
    return Flux.just(1).as(StepVerifier::create);
  }

  ImmutableSet<StepVerifier.Step<Integer>> testStepVerifierStepIdentity() {
    return ImmutableSet.of(
        Mono.just(1).as(StepVerifier::create),
        Mono.just(2).as(StepVerifier::create),
        Mono.just(3).as(StepVerifier::create),
        Mono.just(4).as(StepVerifier::create).expectNextSequence(ImmutableList.of(5)));
  }

  ImmutableSet<StepVerifier.Step<String>> testStepVerifierStepExpectNext() {
    return ImmutableSet.of(
        Mono.just("foo").as(StepVerifier::create).expectNext("bar"),
        Mono.just("baz").as(StepVerifier::create).expectNext("qux"));
  }

  StepVerifier.Step<?> testFluxAsStepVerifierExpectNext() {
    return Flux.just(1).as(StepVerifier::create).expectNext(2);
  }

  Duration testStepVerifierLastStepVerifyComplete() {
    return Mono.empty().as(StepVerifier::create).verifyComplete();
  }

  Duration testStepVerifierLastStepVerifyError() {
    return Mono.empty().as(StepVerifier::create).verifyError();
  }

  ImmutableSet<Duration> testStepVerifierLastStepVerifyErrorClass() {
    return ImmutableSet.of(
        Mono.empty().as(StepVerifier::create).verifyError(IllegalArgumentException.class),
        Mono.empty().as(StepVerifier::create).verifyError(IllegalStateException.class),
        Mono.empty().as(StepVerifier::create).verifyError(AssertionError.class));
  }

  Duration testStepVerifierLastStepVerifyErrorMatches() {
    return Mono.empty()
        .as(StepVerifier::create)
        .verifyErrorMatches(IllegalArgumentException.class::equals);
  }

  Duration testStepVerifierLastStepVerifyErrorSatisfies() {
    return Mono.empty().as(StepVerifier::create).verifyErrorSatisfies(t -> {});
  }

  Duration testStepVerifierLastStepVerifyErrorMessage() {
    return Mono.empty().as(StepVerifier::create).verifyErrorMessage("foo");
  }

  Duration testStepVerifierLastStepVerifyTimeout() {
    return Mono.empty().as(StepVerifier::create).verifyTimeout(Duration.ZERO);
  }
}
