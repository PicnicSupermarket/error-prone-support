package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import java.util.List;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaFlowableToReactorTemplatesTest implements RefasterTemplateTestCase {

  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(CompletableSource.class, MaybeSource.class, Functions.class);
  }

  Flowable<Integer> testFlowableAmbArray() {
    return RxJava2Adapter.fluxToFlowable(Flux.firstWithSignal(Flowable.just(1), Flowable.just(2)));
  }

  Flowable<Integer> testFlowableCombineLatest() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.combineLatest(
            Flowable.just(1),
            Flowable.just(2),
            RxJavaReactorMigrationUtil.toJdkBiFunction(Integer::sum)));
  }

  Flowable<Integer> testFlowableConcatWithPublisher() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1)).concatWith(Flowable.just(2)));
  }

  Flowable<Integer> testFlowableDefer() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.defer(RxJavaReactorMigrationUtil.callableAsSupplier(() -> Flowable.just(1))));
  }

  Flowable<Object> testFlowableEmpty() {
    return RxJava2Adapter.fluxToFlowable(Flux.empty());
  }

  Flowable<Object> testFlowableErrorThrowable() {
    return RxJava2Adapter.fluxToFlowable(Flux.error(new IllegalStateException()));
  }

  Flowable<Object> testFlowableErrorCallable() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.error(
            RxJavaReactorMigrationUtil.callableAsSupplier(
                () -> {
                  throw new IllegalStateException();
                })));
  }

  Flowable<Integer> testFlowableFromArray() {
    return Flowable.fromArray(1, 2, 3);
  }

  Flowable<Integer> testFlowableFromCallable() {
    return RxJava2Adapter.monoToFlowable(Mono.fromCallable(() -> 1));
  }

  Flowable<Integer> testFlowableFromIterable() {
    return RxJava2Adapter.fluxToFlowable(Flux.fromIterable(ImmutableList.of(1, 2, 3)));
  }

  Flowable<Integer> testFlowableFromPublisher() {
    return RxJava2Adapter.fluxToFlowable(Flux.from(Flowable.just(1)));
  }

  Flowable<Integer> testFlowableFilter() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .filter(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 2)));
  }

  Flowable<Integer> testFlowableDistinct() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1)).distinct());
  }

  Maybe<Integer> testFlowableFirstElement() {
    return RxJava2Adapter.monoToMaybe(RxJava2Adapter.flowableToFlux(Flowable.just(1)).next());
  }

  Single<Integer> testFlowableFirstOrError() {
    return RxJava2Adapter.monoToSingle(
        RxJava2Adapter.flowableToFlux(Flowable.just(1)).next().single());
  }

  Completable testFlowableFlatMapCompletable() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .flatMap(
                y ->
                    RxJava2Adapter.completableToMono(
                        Completable.wrap(
                            RxJavaReactorMigrationUtil.toJdkFunction(
                                    (Function<Integer, Completable>)
                                        integer2 -> Completable.complete())
                                .apply(y))))
            .then());
  }

  Completable testFlowableUnwrapLambda() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(1)))
            .flatMap(v -> Mono.empty())
            .then());
  }

  Flowable<Object> testFlowableFlatMap() {
    RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .flatMap(RxJavaReactorMigrationUtil.toJdkFunction(this::exampleMethod2)));
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .flatMap(RxJavaReactorMigrationUtil.toJdkFunction(i -> ImmutableSet::of)));
  }

  private Maybe<Integer> exampleMethod(Integer x) {
    return null;
  }

  private Flowable<Integer> exampleMethod2(Integer x) {
    return null;
  }

  ImmutableList<Flowable<Integer>> testFlowableJust() {
    return ImmutableList.of(
        RxJava2Adapter.fluxToFlowable(Flux.just(1)),
        RxJava2Adapter.fluxToFlowable(Flux.just(1, 2)),
        RxJava2Adapter.fluxToFlowable(Flux.just(1, 2, 3)),
        RxJava2Adapter.fluxToFlowable(Flux.just(1, 2, 3, 4)),
        RxJava2Adapter.fluxToFlowable(Flux.just(1, 2, 3, 4, 5)));
  }

  Flowable<Integer> testFlowableMergePublisherPublisher() {
    return RxJava2Adapter.fluxToFlowable(Flux.merge(Flowable.just(1), Flowable.just(2)));
  }

  Flowable<Integer> testFlowableRange() {
    return RxJava2Adapter.fluxToFlowable(Flux.range(1, 10));
  }

  Flowable<?> testFlowableRangeLong() {
    return RxJava2Adapter.fluxToFlowable(Flux.range(1, 10));
  }

  Flowable<Integer> testFlowableZip() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.zip(
            Flowable.just(1),
            Flowable.just(2),
            RxJavaReactorMigrationUtil.toJdkBiFunction((i1, i2) -> i1 + i2)));
  }

  Single<Boolean> testFlowableAll() {
    return RxJava2Adapter.monoToSingle(
        RxJava2Adapter.flowableToFlux(Flowable.just(true, true))
            .all(RxJavaReactorMigrationUtil.toJdkPredicate(Boolean::booleanValue)));
  }

  Single<Boolean> testFlowableAny() {
    return RxJava2Adapter.monoToSingle(
        RxJava2Adapter.flowableToFlux(Flowable.just(true, true))
            .any(RxJavaReactorMigrationUtil.toJdkPredicate(Boolean::booleanValue)));
  }

  Object testFlowableBlockingFirst() {
    return RxJava2Adapter.flowableToFlux(Flowable.just(1)).blockFirst();
  }

  Flowable<Integer> testFlowableConcatMap() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .concatMap(RxJavaReactorMigrationUtil.toJdkFunction(e -> Flowable::just)));
  }

  Completable testFlowableConcatMapCompletable() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .concatMap(
                e ->
                    RxJava2Adapter.completableToMono(
                        Completable.wrap(
                            RxJavaReactorMigrationUtil.toJdkFunction(c -> Completable.complete())
                                .apply(e))))
            .then());
  }

  Flowable<Integer> testFlowableConcatMapMaybe() {
    return RxJava2Adapter.fluxToFlowable(Flux.just(1))
        .concatMapMaybe(integer -> Maybe.just(integer));
  }

  Flowable<Integer> testFlowableConcatMapMaybeDelayError() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .concatMapDelayError(
                e ->
                    Maybe.wrap(
                            RxJavaReactorMigrationUtil.toJdkFunction(
                                    (Function<Integer, MaybeSource<Integer>>) Maybe::just)
                                .apply(e))
                        .toFlowable()));
  }

  Flowable<Integer> testFlowableFlatMapMaybe() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .flatMap(
                e ->
                    RxJava2Adapter.maybeToMono(
                        Maybe.wrap(
                            RxJavaReactorMigrationUtil.toJdkFunction(
                                    (Function<Integer, MaybeSource<Integer>>) Maybe::just)
                                .apply(e)))));
  }

  Flowable<Integer> testFlowableFlatMapMaybeSecond() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(
                Flowable.zip(Flowable.just(1), Flowable.just(2), (i1, i2) -> Maybe.just(i1 + i2)))
            .flatMap(
                e ->
                    RxJava2Adapter.maybeToMono(
                        Maybe.wrap(
                            RxJavaReactorMigrationUtil.toJdkFunction(Functions.identity())
                                .apply(e)))));
  }

  Flowable<Integer> testFlowableMap() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .map(RxJavaReactorMigrationUtil.toJdkFunction(i -> i + 1)));
  }

  Flowable<Integer> testFlowableMergeWith() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .mergeWith(RxJava2Adapter.singleToMono(Single.wrap(Single.just(1)))));
  }

  Flowable<Integer> testFlowableOnErrorResumeNext() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .onErrorResume(
                RxJavaReactorMigrationUtil.toJdkFunction((Throwable throwable) -> Flux.just(1))));
  }

  Single<Integer> testFlowableSingleDefault() {
    return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(Flowable.just(1)).single(2));
  }

  Maybe<Integer> testFlowableSingleElement() {
    return RxJava2Adapter.monoToMaybe(
        RxJava2Adapter.flowableToFlux(Flowable.just(1)).singleOrEmpty());
  }

  Single<Integer> testFlowableSingleOrError() {
    return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(Flowable.just(1)).single());
  }

  Flowable<Integer> testFlowableSorted() {
    return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(Flowable.just(1)).sort());
  }

  Flowable<Integer> testFlowableSortedComparator() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1)).sort((i1, i2) -> 0));
  }

  Flowable<Integer> testFlowableSwitchIfEmptyPublisher() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .switchIfEmpty(
                Flowable.error(
                    () -> {
                      throw new IllegalStateException();
                    })));
  }

  Single<List<Integer>> testFlowableToList() {
    return RxJava2Adapter.monoToSingle(
        RxJava2Adapter.flowableToFlux(Flowable.just(1, 2)).collectList());
  }

  Single<Map<Boolean, Integer>> testFlowableToMap() {
    return RxJava2Adapter.monoToSingle(
        RxJava2Adapter.flowableToFlux(Flowable.just(1)).collectMap(i -> i > 1));
  }

  Observable<Integer> testFlowableToObservable() {
    return RxJava2Adapter.fluxToFlowable(Flux.just(1)).toObservable();
  }

  void testFlowableTestAssertResultItem() throws InterruptedException {
    RxJava2Adapter.flowableToFlux(Flowable.just(1))
        .as(StepVerifier::create)
        .expectNext(1)
        .verifyComplete();
    RxJava2Adapter.flowableToFlux(Flowable.just(2))
        .as(StepVerifier::create)
        .expectNext(2)
        .verifyComplete();
  }

  void testFlowableTestAssertResult() throws InterruptedException {
    RxJava2Adapter.flowableToFlux(Flowable.just(1)).as(StepVerifier::create).verifyComplete();
  }

  void testFlowableTestAssertValue() throws InterruptedException {
    RxJava2Adapter.flowableToFlux(Flowable.just(1))
        .as(StepVerifier::create)
        .expectNextMatches(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 2))
        .verifyComplete();
    RxJava2Adapter.flowableToFlux(Flowable.just(3))
        .as(StepVerifier::create)
        .expectNextMatches(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 4))
        .verifyComplete();
  }

  void testFlowableTestAssertResultValues() throws InterruptedException {
    RxJava2Adapter.flowableToFlux(Flowable.just(1, 2, 3))
        .as(StepVerifier::create)
        .expectNext(1, 2, 3)
        .verifyComplete();
    RxJava2Adapter.flowableToFlux(Flowable.just(4, 5, 6))
        .as(StepVerifier::create)
        .expectNext(4, 5, 6)
        .verifyComplete();
  }

  void testFlowableTestAssertComplete() throws InterruptedException {
    RxJava2Adapter.flowableToFlux(Flowable.just(1)).as(StepVerifier::create).verifyComplete();
  }

  void testFlowableTestAssertErrorClass() throws InterruptedException {
    RxJava2Adapter.flowableToFlux(Flowable.just(1))
        .as(StepVerifier::create)
        .verifyError(InterruptedException.class);
  }

  void testFlowableTestAssertNoErrors() throws InterruptedException {
    RxJava2Adapter.flowableToFlux(Flowable.just(1)).as(StepVerifier::create).verifyComplete();
  }

  void testFlowableTestAssertValueCount() throws InterruptedException {
    RxJava2Adapter.flowableToFlux(Flowable.just(1))
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }
}
