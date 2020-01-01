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
        Flowable.just(1).compose(Flux::from),
        Flowable.just(2).to(Flux::from),
        Flowable.just(3).as(Flux::from),
        RxJava2Adapter.flowableToFlux(Flowable.just(4)),
        Flowable.just(5).compose(RxJava2Adapter::flowableToFlux),
        Flowable.just(6).to(RxJava2Adapter::flowableToFlux));
  }

  ImmutableSet<Publisher<String>> testFluxToFlowable() {
    return ImmutableSet.of(
        Flux.just("foo").transform(Flowable::fromPublisher),
        Flux.just("bar").as(Flowable::fromPublisher),
        RxJava2Adapter.fluxToFlowable(Flux.just("baz")),
        Flux.just("qux").transform(RxJava2Adapter::fluxToFlowable));
  }

  ImmutableSet<Observable<Integer>> testFluxToObservable() {
    return ImmutableSet.of(
        Flux.just(1).as(Observable::fromPublisher), RxJava2Adapter.fluxToObservable(Flux.just(2)));
  }

  ImmutableSet<Mono<String>> testMaybeToMono() {
    return ImmutableSet.of(
        RxJava2Adapter.maybeToMono(Maybe.just("foo")),
        Maybe.just("bar").to(RxJava2Adapter::maybeToMono));
  }

  ImmutableSet<Completable> testMonoToCompletable() {
    return ImmutableSet.of(
        Mono.empty().as(Completable::fromPublisher),
        RxJava2Adapter.monoToCompletable(Mono.empty()));
  }

  ImmutableSet<Publisher<Integer>> testMonoToFlowable() {
    return ImmutableSet.of(
        Mono.just(1).transform(Flowable::fromPublisher),
        Mono.just(2).as(Flowable::fromPublisher),
        RxJava2Adapter.monoToFlowable(Mono.just(3)),
        Mono.just(4).transform(RxJava2Adapter::monoToFlowable));
  }

  Maybe<String> testMonoToMaybe() {
    return RxJava2Adapter.monoToMaybe(Mono.just("foo"));
  }

  ImmutableSet<Single<Integer>> testMonoToSingle() {
    return ImmutableSet.of(
        Mono.just(1).as(Single::fromPublisher), RxJava2Adapter.monoToSingle(Mono.just(2)));
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
