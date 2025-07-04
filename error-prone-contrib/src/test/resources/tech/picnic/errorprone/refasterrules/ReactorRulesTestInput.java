package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.MoreCollectors.toOptional;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.minBy;
import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
        assertThat(false),
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
        Mono.fromCallable(() -> toString()),
        Mono.fromCallable(getClass()::getDeclaredConstructor),
        Mono.fromCallable(this::toString));
  }

  ImmutableSet<Mono<String>> testMonoEmpty() {
    return ImmutableSet.of(Mono.justOrEmpty(null), Mono.justOrEmpty(Optional.empty()));
  }

  Mono<Integer> testMonoJust() {
    return Mono.justOrEmpty(Optional.of(1));
  }

  Mono<Integer> testMonoJustOrEmptyObject() {
    return Mono.justOrEmpty(Optional.ofNullable(1));
  }

  Mono<Integer> testMonoJustOrEmptyOptional() {
    return Mono.just(Optional.of(1)).filter(Optional::isPresent).map(Optional::orElseThrow);
  }

  ImmutableSet<Mono<Integer>> testMonoDeferMonoJustOrEmpty() {
    return ImmutableSet.of(
        Mono.fromCallable(() -> Optional.of(1).orElse(null)),
        Mono.fromSupplier(() -> Optional.of(2).orElse(null)));
  }

  Optional<Mono<String>> testOptionalMapMonoJust() {
    return Optional.of("foo").map(Mono::justOrEmpty);
  }

  Mono<Integer> testMonoFromOptionalSwitchIfEmpty() {
    return Optional.of(1).map(Mono::just).orElse(Mono.just(2));
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

  Flux<Tuple2<String, Integer>> testFluxZipWithIterable() {
    return Flux.zip(Flux.just("foo", "bar"), Flux.fromIterable(ImmutableSet.of(1, 2)));
  }

  Flux<String> testFluxZipWithIterableBiFunction() {
    return Flux.just("foo", "bar")
        .zipWith(Flux.fromIterable(ImmutableSet.of(1, 2)), String::repeat);
  }

  Flux<String> testFluxZipWithIterableMapFunction() {
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

  ImmutableSet<Mono<String>> testMonoThenReturn() {
    return ImmutableSet.of(
        Mono.just(1).ignoreElement().thenReturn("foo"),
        Mono.just(2).then().thenReturn("bar"),
        Mono.just(3).then(Mono.just("baz")));
  }

  Flux<Integer> testFluxTake() {
    return Flux.just(1, 2, 3).take(1, true);
  }

  Mono<String> testMonoDefaultIfEmpty() {
    return Mono.just("foo").switchIfEmpty(Mono.just("bar"));
  }

  ImmutableSet<Flux<String>> testFluxDefaultIfEmpty() {
    return ImmutableSet.of(
        Flux.just("foo").switchIfEmpty(Mono.just("bar")),
        Flux.just("baz").switchIfEmpty(Flux.just("qux")));
  }

  ImmutableSet<Flux<?>> testFluxEmpty() {
    return ImmutableSet.of(
        Flux.concat(),
        Flux.concatDelayError(),
        Flux.firstWithSignal(),
        Flux.just(),
        Flux.merge(),
        Flux.merge(1),
        Flux.mergeComparing((a, b) -> 0),
        Flux.mergeComparing(1, (a, b) -> 0),
        Flux.mergeComparingDelayError(1, (a, b) -> 0),
        Flux.mergeDelayError(1),
        Flux.mergePriority((a, b) -> 0),
        Flux.mergePriority(1, (a, b) -> 0),
        Flux.mergePriorityDelayError(1, (a, b) -> 0),
        Flux.mergeSequential(),
        Flux.mergeSequential(1),
        Flux.mergeSequentialDelayError(1),
        Flux.zip(v -> v),
        Flux.zip(v -> v, 1),
        Flux.combineLatest(v -> v),
        Flux.combineLatest(v -> v, 1),
        Flux.mergeComparing(),
        Flux.mergePriority(),
        Flux.range(0, 0));
  }

  ImmutableSet<Flux<Integer>> testFluxJust() {
    return ImmutableSet.of(
        Flux.range(0, 1),
        Mono.just(2).flux(),
        Mono.just(3).repeat().take(1),
        Flux.fromIterable(ImmutableList.of(4)),
        Flux.fromIterable(ImmutableSet.of(5)));
  }

  ImmutableSet<Mono<?>> testMonoIdentity() {
    return ImmutableSet.of(
        Mono.just(1).switchIfEmpty(Mono.empty()),
        Mono.just(2).flux().next(),
        Mono.just(3).flux().singleOrEmpty(),
        Mono.<Void>empty().ignoreElement(),
        Mono.<Void>empty().then(),
        Mono.<ImmutableList<String>>empty().map(ImmutableList::copyOf));
  }

  Mono<Integer> testMonoSingle() {
    return Mono.just(1).flux().single();
  }

  ImmutableSet<Flux<Integer>> testFluxSwitchIfEmptyOfEmptyPublisher() {
    return ImmutableSet.of(
        Flux.just(1).switchIfEmpty(Mono.empty()), Flux.just(2).switchIfEmpty(Flux.empty()));
  }

  ImmutableSet<Flux<Integer>> testFluxConcatMap() {
    return ImmutableSet.of(
        Flux.just(1).concatMap(Mono::just, 0),
        Flux.just(2).flatMap(Mono::just, 1),
        Flux.just(3).flatMapSequential(Mono::just, 1),
        Flux.just(4).map(Mono::just).concatMap(identity()),
        Flux.just(5).map(Mono::just).concatMap(v -> v),
        Flux.just(6).map(Mono::just).concatMap(v -> Mono.empty()));
  }

  ImmutableSet<Flux<Integer>> testFluxConcatMapWithPrefetch() {
    return ImmutableSet.of(
        Flux.just(1).flatMap(Mono::just, 1, 3),
        Flux.just(2).flatMapSequential(Mono::just, 1, 4),
        Flux.just(3).map(Mono::just).concatMap(identity(), 5),
        Flux.just(4).map(Mono::just).concatMap(v -> v, 6),
        Flux.just(5).map(Mono::just).concatMap(v -> Mono.empty(), 7));
  }

  ImmutableSet<Flux<Integer>> testMonoFlatMapIterable() {
    return ImmutableSet.of(
        Mono.just(1).map(ImmutableSet::of).flatMapMany(Flux::fromIterable),
        Mono.just(2).map(ImmutableSet::of).flatMapIterable(identity()),
        Mono.just(3).map(ImmutableSet::of).flatMapIterable(v -> v),
        Mono.just(4).map(ImmutableSet::of).flatMapIterable(v -> ImmutableSet.of()),
        Mono.just(5).flux().concatMapIterable(ImmutableSet::of));
  }

  Flux<Integer> testMonoFlatMapIterableIdentity() {
    return Mono.just(ImmutableSet.of(1)).flatMapMany(Flux::fromIterable);
  }

  ImmutableSet<Flux<Integer>> testFluxConcatMapIterable() {
    return ImmutableSet.of(
        Flux.just(1).flatMapIterable(ImmutableList::of),
        Flux.just(2).map(ImmutableList::of).concatMapIterable(identity()),
        Flux.just(3).map(ImmutableList::of).concatMapIterable(v -> v),
        Flux.just(4).map(ImmutableList::of).concatMapIterable(v -> ImmutableSet.of()));
  }

  ImmutableSet<Flux<Integer>> testFluxConcatMapIterableWithPrefetch() {
    return ImmutableSet.of(
        Flux.just(1).flatMapIterable(ImmutableList::of, 5),
        Flux.just(2).map(ImmutableList::of).concatMapIterable(identity(), 5),
        Flux.just(3).map(ImmutableList::of).concatMapIterable(v -> v, 5),
        Flux.just(4).map(ImmutableList::of).concatMapIterable(v -> ImmutableSet.of(), 5));
  }

  Flux<String> testMonoFlatMapToFlux() {
    return Mono.just("foo").flatMapMany(s -> Mono.fromSupplier(() -> s + s));
  }

  ImmutableSet<Mono<String>> testMonoMap() {
    return ImmutableSet.of(
        Mono.just("foo").flatMap(s -> Mono.just(s)),
        Mono.just("bar").flatMap(s -> Mono.just(s.substring(1))));
  }

  ImmutableSet<Flux<Integer>> testFluxMap() {
    return ImmutableSet.of(
        Flux.just(1).concatMap(n -> Mono.just(n)),
        Flux.just(1).concatMap(n -> Flux.just(n * 2)),
        Flux.just(1).concatMap(n -> Mono.just(n), 3),
        Flux.just(1).concatMap(n -> Flux.just(n * 2), 3),
        Flux.just(1).concatMapDelayError(n -> Mono.just(n)),
        Flux.just(1).concatMapDelayError(n -> Flux.just(n * 2)),
        Flux.just(1).concatMapDelayError(n -> Mono.just(n), 3),
        Flux.just(1).concatMapDelayError(n -> Flux.just(n * 2), 3),
        Flux.just(1).flatMap(n -> Mono.just(n), 3),
        Flux.just(1).flatMap(n -> Flux.just(n * 2), 3),
        Flux.just(1).flatMap(n -> Mono.just(n), 3, 4),
        Flux.just(1).flatMap(n -> Flux.just(n * 2), 3, 4),
        Flux.just(1).flatMapDelayError(n -> Mono.just(n), 3, 4),
        Flux.just(1).flatMapDelayError(n -> Flux.just(n * 2), 3, 4),
        Flux.just(1).flatMapSequential(n -> Mono.just(n), 3),
        Flux.just(1).flatMapSequential(n -> Flux.just(n * 2), 3),
        Flux.just(1).flatMapSequential(n -> Mono.just(n), 3, 4),
        Flux.just(1).flatMapSequential(n -> Flux.just(n * 2), 3, 4),
        Flux.just(1).flatMapSequentialDelayError(n -> Mono.just(n), 3, 4),
        Flux.just(1).flatMapSequentialDelayError(n -> Flux.just(n * 2), 3, 4),
        Flux.just(1).switchMap(n -> Mono.just(n)),
        Flux.just(1).switchMap(n -> Flux.just(n * 2)));
  }

  ImmutableSet<Mono<String>> testMonoMapNotNull() {
    return ImmutableSet.of(
        Mono.just("foo").flatMap(s -> Mono.justOrEmpty(s)),
        Mono.just("bar").flatMap(s -> Mono.fromSupplier(() -> s.substring(1))));
  }

  ImmutableSet<Flux<Integer>> testFluxMapNotNull() {
    return ImmutableSet.of(
        Flux.just(1).concatMap(n -> Mono.justOrEmpty(n)),
        Flux.just(1).concatMap(n -> Mono.fromSupplier(() -> n * 2)),
        Flux.just(1).concatMap(n -> Mono.justOrEmpty(n), 3),
        Flux.just(1).concatMap(n -> Mono.fromSupplier(() -> n * 2), 3),
        Flux.just(1).concatMapDelayError(n -> Mono.justOrEmpty(n)),
        Flux.just(1).concatMapDelayError(n -> Mono.fromSupplier(() -> n * 2)),
        Flux.just(1).concatMapDelayError(n -> Mono.justOrEmpty(n), 3),
        Flux.just(1).concatMapDelayError(n -> Mono.fromSupplier(() -> n * 2), 3),
        Flux.just(1).flatMap(n -> Mono.justOrEmpty(n), 3),
        Flux.just(1).flatMap(n -> Mono.fromSupplier(() -> n * 2), 3),
        Flux.just(1).flatMap(n -> Mono.justOrEmpty(n), 3, 4),
        Flux.just(1).flatMap(n -> Mono.fromSupplier(() -> n * 2), 3, 4),
        Flux.just(1).flatMapDelayError(n -> Mono.justOrEmpty(n), 3, 4),
        Flux.just(1).flatMapDelayError(n -> Mono.fromSupplier(() -> n * 2), 3, 4),
        Flux.just(1).flatMapSequential(n -> Mono.justOrEmpty(n), 3),
        Flux.just(1).flatMapSequential(n -> Mono.fromSupplier(() -> n * 2), 3),
        Flux.just(1).flatMapSequential(n -> Mono.justOrEmpty(n), 3, 4),
        Flux.just(1).flatMapSequential(n -> Mono.fromSupplier(() -> n * 2), 3, 4),
        Flux.just(1).flatMapSequentialDelayError(n -> Mono.justOrEmpty(n), 3, 4),
        Flux.just(1).flatMapSequentialDelayError(n -> Mono.fromSupplier(() -> n * 2), 3, 4),
        Flux.just(1).switchMap(n -> Mono.justOrEmpty(n)),
        Flux.just(1).switchMap(n -> Mono.fromSupplier(() -> n * 2)));
  }

  Flux<String> testFluxMapNotNullTransformationOrElse() {
    return Flux.just(1).map(x -> Optional.of(x.toString())).mapNotNull(x -> x.orElse(null));
  }

  Flux<Integer> testFluxMapNotNullOrElse() {
    return Flux.just(Optional.of(1)).filter(Optional::isPresent).map(Optional::orElseThrow);
  }

  ImmutableSet<Flux<String>> testMonoFlux() {
    return ImmutableSet.of(
        Mono.just("foo").flatMapMany(Mono::just),
        Mono.just("bar").flatMapMany(Flux::just),
        Flux.concat(Mono.just("baz")));
  }

  ImmutableSet<Mono<Void>> testMonoThen() {
    return ImmutableSet.of(
        Mono.just("foo").ignoreElement().then(),
        Mono.just("bar").flux().then(),
        Mono.when(Mono.just("baz")),
        Mono.whenDelayError(Mono.just("qux")));
  }

  ImmutableSet<Mono<Void>> testFluxThen() {
    return ImmutableSet.of(
        Flux.just("foo").ignoreElements().then(), Flux.<Void>empty().ignoreElements());
  }

  Mono<Void> testMonoThenEmpty() {
    return Mono.just("foo").ignoreElement().thenEmpty(Mono.empty());
  }

  Mono<Void> testFluxThenEmpty() {
    return Flux.just("foo").ignoreElements().thenEmpty(Mono.empty());
  }

  ImmutableSet<Flux<String>> testMonoThenMany() {
    return ImmutableSet.of(
        Mono.just("foo").ignoreElement().thenMany(Flux.just("bar")),
        Mono.just("baz").ignoreElement().thenMany(Flux.just("qux")));
  }

  Flux<String> testMonoThenMonoFlux() {
    return Mono.just("foo").thenMany(Mono.just("bar"));
  }

  Flux<String> testFluxThenMany() {
    return Flux.just("foo").ignoreElements().thenMany(Flux.just("bar"));
  }

  ImmutableSet<Mono<?>> testMonoThenMono() {
    return ImmutableSet.of(
        Mono.just("foo").ignoreElement().then(Mono.just("bar")),
        Mono.just("baz").flux().then(Mono.just("qux")),
        Mono.just("quux").thenEmpty(Mono.<Void>empty()));
  }

  ImmutableSet<Mono<?>> testFluxThenMono() {
    return ImmutableSet.of(
        Flux.just("foo").ignoreElements().then(Mono.just("bar")),
        Flux.just("baz").thenEmpty(Mono.<Void>empty()));
  }

  ImmutableSet<Mono<Optional<String>>> testMonoSingleOptional() {
    return ImmutableSet.of(
        Mono.just("foo").flux().collect(toOptional()),
        Mono.just("bar").map(Optional::of),
        Mono.just("baz").singleOptional().defaultIfEmpty(Optional.empty()),
        Mono.just("quux").singleOptional().switchIfEmpty(Mono.just(Optional.empty())),
        Mono.just("quuz").transform(Mono::singleOptional));
  }

  Mono<Number> testMonoCast() {
    return Mono.just(1).map(Number.class::cast);
  }

  Flux<Number> testFluxCast() {
    return Flux.just(1).map(Number.class::cast);
  }

  Mono<Number> testMonoOfType() {
    return Mono.just(1).filter(Number.class::isInstance).cast(Number.class);
  }

  Flux<Number> testFluxOfType() {
    return Flux.just(1).filter(Number.class::isInstance).cast(Number.class);
  }

  ImmutableSet<Mono<String>> testMonoFlatMap() {
    return ImmutableSet.of(
        Mono.just("foo").map(Mono::just).flatMap(identity()),
        Mono.just("bar").map(Mono::just).flatMap(v -> v),
        Mono.just("baz").map(Mono::just).flatMap(v -> Mono.empty()));
  }

  ImmutableSet<Flux<Integer>> testMonoFlatMapMany() {
    return ImmutableSet.of(
        Mono.just(1).map(Mono::just).flatMapMany(identity()),
        Mono.just(2).map(Mono::just).flatMapMany(v -> v),
        Mono.just(3).map(Mono::just).flatMapMany(v -> Flux.empty()),
        Mono.just(4).flux().concatMap(Mono::just),
        Mono.just(5).flux().concatMap(Mono::just, 2),
        Mono.just(6).flux().concatMapDelayError(Mono::just),
        Mono.just(7).flux().concatMapDelayError(Mono::just, 2),
        Mono.just(8).flux().concatMapDelayError(Mono::just, false, 2),
        Mono.just(9).flux().flatMap(Mono::just, 2),
        Mono.just(10).flux().flatMap(Mono::just, 2, 3),
        Mono.just(11).flux().flatMapDelayError(Mono::just, 2, 3),
        Mono.just(12).flux().flatMapSequential(Mono::just, 2),
        Mono.just(13).flux().flatMapSequential(Mono::just, 2, 3),
        Mono.just(14).flux().flatMapSequentialDelayError(Mono::just, 2, 3),
        Mono.just(15).flux().switchMap(Mono::just));
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

  ImmutableSet<Flux<String>> testFluxFromIterable() {
    return ImmutableSet.of(
        Flux.fromStream(ImmutableList.of("foo")::stream),
        Flux.fromStream(() -> ImmutableList.of("bar").stream()));
  }

  ImmutableSet<Mono<Integer>> testFluxCountMapMathToIntExact() {
    return ImmutableSet.of(
        Flux.just(1).collect(toImmutableList()).map(Collection::size),
        Flux.just(2).collect(toImmutableList()).map(List::size),
        Flux.just(3).collect(toImmutableList()).map(ImmutableCollection::size),
        Flux.just(4).collect(toImmutableList()).map(ImmutableList::size),
        Flux.just(5).collect(toCollection(ArrayList::new)).map(Collection::size),
        Flux.just(6).collect(toCollection(ArrayList::new)).map(List::size));
  }

  Mono<Integer> testMonoDoOnError() {
    return Mono.just(1).doOnError(IllegalArgumentException.class::isInstance, e -> {});
  }

  Flux<Integer> testFluxDoOnError() {
    return Flux.just(1).doOnError(IllegalArgumentException.class::isInstance, e -> {});
  }

  Mono<Integer> testMonoOnErrorComplete() {
    return Mono.just(1).onErrorResume(e -> Mono.empty());
  }

  ImmutableSet<Flux<Integer>> testFluxOnErrorComplete() {
    return ImmutableSet.of(
        Flux.just(1).onErrorResume(e -> Mono.empty()),
        Flux.just(2).onErrorResume(e -> Flux.empty()));
  }

  ImmutableSet<Mono<Integer>> testMonoOnErrorCompleteClass() {
    return ImmutableSet.of(
        Mono.just(1).onErrorComplete(IllegalArgumentException.class::isInstance),
        Mono.just(2).onErrorResume(IllegalStateException.class, e -> Mono.empty()));
  }

  ImmutableSet<Flux<Integer>> testFluxOnErrorCompleteClass() {
    return ImmutableSet.of(
        Flux.just(1).onErrorComplete(IllegalArgumentException.class::isInstance),
        Flux.just(2).onErrorResume(IllegalStateException.class, e -> Mono.empty()),
        Flux.just(3).onErrorResume(AssertionError.class, e -> Flux.empty()));
  }

  Mono<Integer> testMonoOnErrorCompletePredicate() {
    return Mono.just(1).onErrorResume(e -> e.getCause() == null, e -> Mono.empty());
  }

  ImmutableSet<Flux<Integer>> testFluxOnErrorCompletePredicate() {
    return ImmutableSet.of(
        Flux.just(1).onErrorResume(e -> e.getCause() == null, e -> Mono.empty()),
        Flux.just(2).onErrorResume(e -> e.getCause() != null, e -> Flux.empty()));
  }

  Mono<Integer> testMonoOnErrorContinue() {
    return Mono.just(1).onErrorContinue(IllegalArgumentException.class::isInstance, (e, v) -> {});
  }

  Flux<Integer> testFluxOnErrorContinue() {
    return Flux.just(1).onErrorContinue(IllegalArgumentException.class::isInstance, (e, v) -> {});
  }

  Mono<Integer> testMonoOnErrorMap() {
    return Mono.just(1).onErrorMap(IllegalArgumentException.class::isInstance, e -> e);
  }

  Flux<Integer> testFluxOnErrorMap() {
    return Flux.just(1).onErrorMap(IllegalArgumentException.class::isInstance, e -> e);
  }

  Mono<Integer> testMonoOnErrorResume() {
    return Mono.just(1)
        .onErrorResume(IllegalArgumentException.class::isInstance, e -> Mono.just(2));
  }

  Flux<Integer> testFluxOnErrorResume() {
    return Flux.just(1)
        .onErrorResume(IllegalArgumentException.class::isInstance, e -> Flux.just(2));
  }

  Mono<Integer> testMonoOnErrorReturn() {
    return Mono.just(1).onErrorReturn(IllegalArgumentException.class::isInstance, 2);
  }

  Flux<Integer> testFluxOnErrorReturn() {
    return Flux.just(1).onErrorReturn(IllegalArgumentException.class::isInstance, 2);
  }

  Flux<Integer> testFluxFilterSort() {
    return Flux.just(1, 4, 3, 2).sort().filter(i -> i % 2 == 0);
  }

  Flux<Integer> testFluxFilterSortWithComparator() {
    return Flux.just(1, 4, 3, 2).sort(reverseOrder()).filter(i -> i % 2 == 0);
  }

  Flux<Integer> testFluxTakeWhile() {
    return Flux.just(1, 2, 3).takeWhile(i -> i % 2 == 0).filter(i -> i % 2 == 0);
  }

  Mono<List<Integer>> testFluxCollectToImmutableList() {
    return Flux.just(1).collectList();
  }

  Mono<ImmutableSet<Integer>> testFluxCollectToImmutableSet() {
    return Flux.just(1).collect(toImmutableList()).map(ImmutableSet::copyOf);
  }

  Flux<Integer> testFluxSort() {
    return Flux.just(1).sort(naturalOrder());
  }

  Mono<Integer> testFluxTransformMin() {
    return Flux.just(1).sort().next();
  }

  ImmutableSet<Mono<Integer>> testFluxTransformMinWithComparator() {
    return ImmutableSet.of(
        Flux.just(1).sort(reverseOrder()).next(),
        Flux.just(2).collect(minBy(reverseOrder())).flatMap(Mono::justOrEmpty));
  }

  Mono<Integer> testFluxTransformMax() {
    return Flux.just(1).sort().last();
  }

  ImmutableSet<Mono<Integer>> testFluxTransformMaxWithComparator() {
    return ImmutableSet.of(
        Flux.just(1).sort(reverseOrder()).last(),
        Flux.just(2).collect(maxBy(reverseOrder())).flatMap(Mono::justOrEmpty));
  }

  ImmutableSet<Mono<Integer>> testMathFluxMin() {
    return ImmutableSet.of(
        MathFlux.min(Flux.just(1), naturalOrder()), MathFlux.max(Flux.just(2), reverseOrder()));
  }

  ImmutableSet<Mono<Integer>> testMathFluxMax() {
    return ImmutableSet.of(
        MathFlux.min(Flux.just(1), reverseOrder()), MathFlux.max(Flux.just(2), naturalOrder()));
  }

  ImmutableSet<Context> testContextEmpty() {
    return ImmutableSet.of(Context.of(ImmutableMap.of()), Context.of(ImmutableMap.of(1, 2)));
  }

  ImmutableSet<PublisherProbe<Void>> testPublisherProbeEmpty() {
    return ImmutableSet.of(PublisherProbe.of(Mono.empty()), PublisherProbe.of(Flux.empty()));
  }

  void testPublisherProbeAssertWasSubscribed() {
    assertThat(PublisherProbe.of(Mono.just(1)).wasSubscribed()).isTrue();
    assertThat(PublisherProbe.of(Mono.just(2)).subscribeCount()).isNotNegative();
    assertThat(PublisherProbe.of(Mono.just(3)).subscribeCount()).isNotEqualTo(0);
    assertThat(PublisherProbe.of(Mono.just(4)).subscribeCount()).isPositive();
  }

  void testPublisherProbeAssertWasNotSubscribed() {
    assertThat(PublisherProbe.of(Mono.just(1)).wasSubscribed()).isFalse();
    assertThat(PublisherProbe.of(Mono.just(2)).subscribeCount()).isEqualTo(0);
    assertThat(PublisherProbe.of(Mono.just(3)).subscribeCount()).isNotPositive();
  }

  void testPublisherProbeAssertWasCancelled() {
    assertThat(PublisherProbe.empty().wasCancelled()).isTrue();
  }

  void testPublisherProbeAssertWasNotCancelled() {
    assertThat(PublisherProbe.empty().wasCancelled()).isFalse();
  }

  void testPublisherProbeAssertWasRequested() {
    assertThat(PublisherProbe.empty().wasRequested()).isTrue();
  }

  void testPublisherProbeAssertWasNotRequested() {
    assertThat(PublisherProbe.empty().wasRequested()).isFalse();
  }

  @SuppressWarnings("SimplifyBooleanExpression")
  void testAssertThatPublisherProbeWasSubscribed() {
    if (true) {
      PublisherProbe.of(Mono.just(1)).assertWasSubscribed();
    } else {
      PublisherProbe.of(Mono.just(1)).assertWasNotSubscribed();
    }
    if (!false) {
      PublisherProbe.of(Mono.just(2)).assertWasNotSubscribed();
    } else {
      PublisherProbe.of(Mono.just(2)).assertWasSubscribed();
    }
  }

  @SuppressWarnings("SimplifyBooleanExpression")
  void testAssertThatPublisherProbeWasCancelled() {
    if (true) {
      PublisherProbe.of(Mono.just(1)).assertWasCancelled();
    } else {
      PublisherProbe.of(Mono.just(1)).assertWasNotCancelled();
    }
    if (!false) {
      PublisherProbe.of(Mono.just(2)).assertWasNotCancelled();
    } else {
      PublisherProbe.of(Mono.just(2)).assertWasCancelled();
    }
  }

  @SuppressWarnings("SimplifyBooleanExpression")
  void testAssertThatPublisherProbeWasRequested() {
    if (true) {
      PublisherProbe.of(Mono.just(1)).assertWasRequested();
    } else {
      PublisherProbe.of(Mono.just(1)).assertWasNotRequested();
    }
    if (!false) {
      PublisherProbe.of(Mono.just(2)).assertWasNotRequested();
    } else {
      PublisherProbe.of(Mono.just(2)).assertWasRequested();
    }
  }

  ImmutableSet<StepVerifier.FirstStep<Integer>> testStepVerifierFromMono() {
    return ImmutableSet.of(
        StepVerifier.create(Mono.just(1)), Mono.just(2).flux().as(StepVerifier::create));
  }

  StepVerifier.FirstStep<Integer> testStepVerifierFromFlux() {
    return StepVerifier.create(Flux.just(1));
  }

  Object testStepVerifierVerify() {
    return Mono.empty().as(StepVerifier::create).expectError().verifyThenAssertThat();
  }

  Object testStepVerifierVerifyDuration() {
    return Mono.empty().as(StepVerifier::create).expectError().verifyThenAssertThat(Duration.ZERO);
  }

  StepVerifier testStepVerifierVerifyLater() {
    return Mono.empty().as(StepVerifier::create).expectError().verifyLater().verifyLater();
  }

  ImmutableSet<StepVerifier.Step<Integer>> testStepVerifierStepIdentity() {
    return ImmutableSet.of(
        Mono.just(1).as(StepVerifier::create).expectNext(),
        Mono.just(2).as(StepVerifier::create).expectNextCount(0L),
        Mono.just(3).as(StepVerifier::create).expectNextSequence(ImmutableList.of()),
        Mono.just(4).as(StepVerifier::create).expectNextSequence(ImmutableList.of(5)));
  }

  ImmutableSet<StepVerifier.Step<String>> testStepVerifierStepExpectNext() {
    return ImmutableSet.of(
        Mono.just("foo").as(StepVerifier::create).expectNextMatches(s -> s.equals("bar")),
        Mono.just("baz").as(StepVerifier::create).expectNextMatches("qux"::equals));
  }

  StepVerifier.Step<?> testFluxAsStepVerifierExpectNext() {
    return Flux.just(1)
        .collect(toImmutableList())
        .as(StepVerifier::create)
        .assertNext(list -> assertThat(list).containsExactly(2));
  }

  Duration testStepVerifierLastStepVerifyComplete() {
    return Mono.empty().as(StepVerifier::create).expectComplete().verify();
  }

  Duration testStepVerifierLastStepVerifyError() {
    return Mono.empty().as(StepVerifier::create).expectError().verify();
  }

  ImmutableSet<Duration> testStepVerifierLastStepVerifyErrorClass() {
    return ImmutableSet.of(
        Mono.empty().as(StepVerifier::create).expectError(IllegalArgumentException.class).verify(),
        Mono.empty()
            .as(StepVerifier::create)
            .verifyErrorMatches(IllegalStateException.class::isInstance),
        Mono.empty()
            .as(StepVerifier::create)
            .verifyErrorSatisfies(t -> assertThat(t).isInstanceOf(AssertionError.class)));
  }

  ImmutableSet<?> testStepVerifierLastStepVerifyErrorMatches() {
    return ImmutableSet.of(
        Mono.empty()
            .as(StepVerifier::create)
            .expectErrorMatches(IllegalArgumentException.class::equals)
            .verify(),
        Mono.empty()
            .as(StepVerifier::create)
            .expectError()
            .verifyThenAssertThat()
            .hasOperatorErrorMatching(IllegalStateException.class::equals));
  }

  Duration testStepVerifierLastStepVerifyErrorSatisfies() {
    return Mono.empty().as(StepVerifier::create).expectErrorSatisfies(t -> {}).verify();
  }

  ImmutableSet<?> testStepVerifierLastStepVerifyErrorSatisfiesAssertJ() {
    return ImmutableSet.of(
        Mono.empty()
            .as(StepVerifier::create)
            .expectError()
            .verifyThenAssertThat()
            .hasOperatorErrorOfType(IllegalArgumentException.class)
            .hasOperatorErrorWithMessage("foo"),
        Mono.empty()
            .as(StepVerifier::create)
            .expectError(IllegalStateException.class)
            .verifyThenAssertThat()
            .hasOperatorErrorWithMessage("bar"),
        Mono.empty()
            .as(StepVerifier::create)
            .expectErrorMessage("baz")
            .verifyThenAssertThat()
            .hasOperatorErrorOfType(AssertionError.class));
  }

  Duration testStepVerifierLastStepVerifyErrorMessage() {
    return Mono.empty().as(StepVerifier::create).expectErrorMessage("foo").verify();
  }

  Duration testStepVerifierLastStepVerifyTimeout() {
    return Mono.empty().as(StepVerifier::create).expectTimeout(Duration.ZERO).verify();
  }

  Mono<Void> testMonoFromFutureSupplier() {
    return Mono.fromFuture(CompletableFuture.completedFuture(null));
  }

  Mono<Void> testMonoFromFutureSupplierBoolean() {
    return Mono.fromFuture(CompletableFuture.completedFuture(null), true);
  }

  Mono<String> testMonoFromFutureAsyncLoadingCacheGet() {
    return Mono.fromFuture(() -> ((AsyncLoadingCache<Integer, String>) null).get(0));
  }

  Mono<Map<Integer, String>> testMonoFromFutureAsyncLoadingCacheGetAll() {
    return Mono.fromFuture(
        () -> ((AsyncLoadingCache<Integer, String>) null).getAll(ImmutableSet.of()));
  }

  Flux<Integer> testFluxFromStreamSupplier() {
    return Flux.fromStream(Stream.of(1));
  }
}
