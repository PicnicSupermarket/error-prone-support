package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class RxJava2AdapterTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<Mono<Void>> testCompletableToMono() {
    return ImmutableSet.of(
        RxJava2Adapter.completableToMono(Completable.complete()),
        Completable.complete().to(RxJava2Adapter::completableToMono));
  }

  ImmutableSet<Publisher<Integer>> testFlowableToFlux() {
    return ImmutableSet.of(
        RxJava2Adapter.flowableToFlux(Flowable.just(1)),
        Flowable.just(2).compose(RxJava2Adapter::flowableToFlux),
        Flowable.just(3).to(RxJava2Adapter::flowableToFlux));
  }

  ImmutableSet<Publisher<String>> testFluxToFlowable() {
    return ImmutableSet.of(
        RxJava2Adapter.fluxToFlowable(Flux.just("foo")),
        Flux.just("bar").transform(RxJava2Adapter::fluxToFlowable));
  }

  Observable<Integer> testFluxToObservable() {
    return RxJava2Adapter.fluxToObservable(Flux.just(1));
  }

  ImmutableSet<Mono<String>> testMaybeToMono() {
    return ImmutableSet.of(
        RxJava2Adapter.maybeToMono(Maybe.just("foo")),
        Maybe.just("bar").to(RxJava2Adapter::maybeToMono));
  }

  Completable testMonoToCompletable() {
    return RxJava2Adapter.monoToCompletable(Mono.empty());
  }

  ImmutableSet<Publisher<Integer>> testMonoToFlowable() {
    return ImmutableSet.of(
        RxJava2Adapter.monoToFlowable(Mono.just(1)),
        Mono.just(2).transform(RxJava2Adapter::monoToFlowable));
  }

  Maybe<String> testMonoToMaybe() {
    return RxJava2Adapter.monoToMaybe(Mono.just("foo"));
  }

  Single<Integer> testMonoToSingle() {
    return RxJava2Adapter.monoToSingle(Mono.just(1));
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
