package tech.picnic.errorprone.refasterrules.input;

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
  ImmutableSet<Mono<Void>> testCompletableToMono() {
    return ImmutableSet.of(
        RxJava2Adapter.completableToMono(Completable.complete()),
        Completable.complete().to(RxJava2Adapter::completableToMono));
  }

  ImmutableSet<Flux<Integer>> testFlowableToFlux() {
    return ImmutableSet.of(
        Flux.from(Flowable.just(1)),
        Flowable.just(2).to(Flux::from),
        Flowable.just(3).as(Flux::from),
        RxJava2Adapter.flowableToFlux(Flowable.just(4)),
        Flowable.just(5).to(RxJava2Adapter::flowableToFlux));
  }

  ImmutableSet<Flowable<String>> testFluxToFlowable() {
    return ImmutableSet.of(
        Flowable.fromPublisher(Flux.just("foo")),
        Flux.just("bar").as(Flowable::fromPublisher),
        RxJava2Adapter.fluxToFlowable(Flux.just("baz")));
  }

  ImmutableSet<Observable<Integer>> testFluxToObservable() {
    return ImmutableSet.of(
        Observable.fromPublisher(Flux.just(1)),
        Flux.just(2).as(Observable::fromPublisher),
        RxJava2Adapter.fluxToObservable(Flux.just(3)));
  }

  ImmutableSet<Mono<String>> testMaybeToMono() {
    return ImmutableSet.of(
        RxJava2Adapter.maybeToMono(Maybe.just("foo")),
        Maybe.just("bar").to(RxJava2Adapter::maybeToMono));
  }

  ImmutableSet<Completable> testMonoToCompletable() {
    return ImmutableSet.of(
        Completable.fromPublisher(Mono.empty()),
        Mono.empty().as(Completable::fromPublisher),
        RxJava2Adapter.monoToCompletable(Mono.empty()));
  }

  ImmutableSet<Flowable<Integer>> testMonoToFlowable() {
    return ImmutableSet.of(
        Flowable.fromPublisher(Mono.just(1)),
        Mono.just(2).as(Flowable::fromPublisher),
        RxJava2Adapter.monoToFlowable(Mono.just(3)));
  }

  Maybe<String> testMonoToMaybe() {
    return RxJava2Adapter.monoToMaybe(Mono.just("foo"));
  }

  ImmutableSet<Single<Integer>> testMonoToSingle() {
    return ImmutableSet.of(
        Single.fromPublisher(Mono.just(1)),
        Mono.just(2).as(Single::fromPublisher),
        RxJava2Adapter.monoToSingle(Mono.just(3)));
  }

  ImmutableSet<Flux<String>> testObservableToFlux() {
    return ImmutableSet.of(
        RxJava2Adapter.observableToFlux(Observable.just("foo"), BackpressureStrategy.BUFFER),
        Observable.just("bar")
            .as(obs -> RxJava2Adapter.observableToFlux(obs, BackpressureStrategy.DROP)),
        Observable.just("baz")
            .to(obs -> RxJava2Adapter.observableToFlux(obs, BackpressureStrategy.ERROR)));
  }

  ImmutableSet<Mono<Integer>> testSingleToMono() {
    return ImmutableSet.of(
        RxJava2Adapter.singleToMono(Single.just(1)),
        Single.just(2).to(RxJava2Adapter::singleToMono));
  }
}
