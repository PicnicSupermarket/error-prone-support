package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.Arrays;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class RxJava2AdapterTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<Flux<Integer>> testFluxToFlowableToFlux() {
    return ImmutableSet.of(
        RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(1))),
        RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(2))));
  }

  ImmutableSet<Mono<Void>> testCompletableToMono() {
    return ImmutableSet.of(
        RxJava2Adapter.completableToMono(Completable.complete()),
        Completable.complete().to(RxJava2Adapter::completableToMono));
  }

  ImmutableSet<Publisher<Integer>> testFlowableToFlux() {
    // The `Arrays.asList` is to avoid confusing `javac`; `ImmutableSet.of` uses varargs from the
    // seventh parameter onwards.
    return ImmutableSet.copyOf(
        Arrays.asList(
            Flux.from(Flowable.just(1)),
            Flowable.just(2).compose(Flux::from),
            Flowable.just(3).to(Flux::from),
            Flowable.just(4).as(Flux::from),
            RxJava2Adapter.flowableToFlux(Flowable.just(5)),
            Flowable.just(6).compose(RxJava2Adapter::flowableToFlux),
            Flowable.just(7).<Publisher<Integer>>to(RxJava2Adapter::flowableToFlux)));
  }

  ImmutableSet<Publisher<String>> testFluxToFlowable() {
    return ImmutableSet.of(
        Flowable.fromPublisher(Flux.just("foo")),
        Flux.just("bar").transform(Flowable::fromPublisher),
        Flux.just("baz").as(Flowable::fromPublisher),
        RxJava2Adapter.fluxToFlowable(Flux.just("qux")),
        Flux.just("quux").transform(RxJava2Adapter::fluxToFlowable));
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

  ImmutableSet<Publisher<Integer>> testMonoToFlowable() {
    return ImmutableSet.of(
        Flowable.fromPublisher(Mono.just(1)),
        Mono.just(2).transform(Flowable::fromPublisher),
        Mono.just(3).as(Flowable::fromPublisher),
        RxJava2Adapter.monoToFlowable(Mono.just(4)),
        Mono.just(5).transform(RxJava2Adapter::monoToFlowable));
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
