package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class RxJava2AdapterRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Mono<Void>> testCompletableAsRxJava2AdapterCompletableToMono() {
    return ImmutableSet.of(
        Completable.complete().as(RxJava2Adapter::completableToMono),
        Completable.never().as(RxJava2Adapter::completableToMono));
  }

  ImmutableSet<Flux<Integer>> testFlowableAsRxJava2AdapterFlowableToFlux() {
    return ImmutableSet.of(
        Flowable.just(1).as(RxJava2Adapter::flowableToFlux),
        Flowable.just(2).as(RxJava2Adapter::flowableToFlux),
        Flowable.just(3).as(RxJava2Adapter::flowableToFlux),
        Flowable.just(4).as(RxJava2Adapter::flowableToFlux),
        Flowable.just(5).as(RxJava2Adapter::flowableToFlux));
  }

  ImmutableSet<Flowable<String>> testFluxAsRxJava2AdapterFluxToFlowable() {
    return ImmutableSet.of(
        Flux.just("foo").as(RxJava2Adapter::fluxToFlowable),
        Flux.just("bar").as(RxJava2Adapter::fluxToFlowable),
        Flux.just("baz").as(RxJava2Adapter::fluxToFlowable));
  }

  ImmutableSet<Observable<Integer>> testFluxAsRxJava2AdapterFluxToObservable() {
    return ImmutableSet.of(
        Flux.just(1).as(RxJava2Adapter::fluxToObservable),
        Flux.just(2).as(RxJava2Adapter::fluxToObservable),
        Flux.just(3).as(RxJava2Adapter::fluxToObservable));
  }

  ImmutableSet<Mono<String>> testMaybeAsRxJava2AdapterMaybeToMono() {
    return ImmutableSet.of(
        Maybe.just("foo").as(RxJava2Adapter::maybeToMono),
        Maybe.just("bar").as(RxJava2Adapter::maybeToMono));
  }

  ImmutableSet<Completable> testMonoAsRxJava2AdapterMonoToCompletable() {
    return ImmutableSet.of(
        Mono.empty().as(RxJava2Adapter::monoToCompletable),
        Mono.just(1).as(RxJava2Adapter::monoToCompletable),
        Mono.just(2).as(RxJava2Adapter::monoToCompletable));
  }

  ImmutableSet<Flowable<Integer>> testMonoAsRxJava2AdapterMonoToFlowable() {
    return ImmutableSet.of(
        Mono.just(1).as(RxJava2Adapter::monoToFlowable),
        Mono.just(2).as(RxJava2Adapter::monoToFlowable),
        Mono.just(3).as(RxJava2Adapter::monoToFlowable));
  }

  Maybe<String> testMonoAsRxJava2AdapterMonoToMaybe() {
    return Mono.just("foo").as(RxJava2Adapter::monoToMaybe);
  }

  ImmutableSet<Single<Integer>> testMonoAsRxJava2AdapterMonoToSingle() {
    return ImmutableSet.of(
        Mono.just(1).as(RxJava2Adapter::monoToSingle),
        Mono.just(2).as(RxJava2Adapter::monoToSingle),
        Mono.just(3).as(RxJava2Adapter::monoToSingle));
  }

  ImmutableSet<Flux<String>> testObservableToFlowableAsRxJava2AdapterFlowableToFlux() {
    return ImmutableSet.of(
        Observable.just("foo")
            .toFlowable(BackpressureStrategy.BUFFER)
            .as(RxJava2Adapter::flowableToFlux),
        Observable.just("bar")
            .toFlowable(BackpressureStrategy.DROP)
            .as(RxJava2Adapter::flowableToFlux),
        Observable.just("baz")
            .toFlowable(BackpressureStrategy.ERROR)
            .as(RxJava2Adapter::flowableToFlux));
  }

  ImmutableSet<Mono<Integer>> testSingleAsRxJava2AdapterSingleToMono() {
    return ImmutableSet.of(
        Single.just(1).as(RxJava2Adapter::singleToMono),
        Single.just(2).as(RxJava2Adapter::singleToMono));
  }
}
