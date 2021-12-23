package tech.picnic.errorprone.refastertemplates;

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
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.RxJava2AdapterTemplates.CompletableToMono;
import tech.picnic.errorprone.refastertemplates.RxJava2AdapterTemplates.FlowableToFlux;
import tech.picnic.errorprone.refastertemplates.RxJava2AdapterTemplates.FluxToFlowable;
import tech.picnic.errorprone.refastertemplates.RxJava2AdapterTemplates.FluxToObservable;
import tech.picnic.errorprone.refastertemplates.RxJava2AdapterTemplates.MaybeToMono;
import tech.picnic.errorprone.refastertemplates.RxJava2AdapterTemplates.MonoToCompletable;
import tech.picnic.errorprone.refastertemplates.RxJava2AdapterTemplates.MonoToFlowable;
import tech.picnic.errorprone.refastertemplates.RxJava2AdapterTemplates.MonoToMaybe;
import tech.picnic.errorprone.refastertemplates.RxJava2AdapterTemplates.MonoToSingle;
import tech.picnic.errorprone.refastertemplates.RxJava2AdapterTemplates.ObservableToFlux;
import tech.picnic.errorprone.refastertemplates.RxJava2AdapterTemplates.SingleToMono;

@TemplateCollection(RxJava2AdapterTemplates.class)
final class RxJava2AdapterTemplatesTest implements RefasterTemplateTestCase {
  @Template(CompletableToMono.class)
  ImmutableSet<Mono<Void>> testCompletableToMono() {
    return ImmutableSet.of(
        RxJava2Adapter.completableToMono(Completable.complete()),
        Completable.complete().to(RxJava2Adapter::completableToMono));
  }

  @Template(FlowableToFlux.class)
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

  @Template(FluxToFlowable.class)
  ImmutableSet<Publisher<String>> testFluxToFlowable() {
    return ImmutableSet.of(
        Flowable.fromPublisher(Flux.just("foo")),
        Flux.just("bar").transform(Flowable::fromPublisher),
        Flux.just("baz").as(Flowable::fromPublisher),
        RxJava2Adapter.fluxToFlowable(Flux.just("qux")),
        Flux.just("quux").transform(RxJava2Adapter::fluxToFlowable));
  }

  @Template(FluxToObservable.class)
  ImmutableSet<Observable<Integer>> testFluxToObservable() {
    return ImmutableSet.of(
        Observable.fromPublisher(Flux.just(1)),
        Flux.just(2).as(Observable::fromPublisher),
        RxJava2Adapter.fluxToObservable(Flux.just(3)));
  }

  @Template(MaybeToMono.class)
  ImmutableSet<Mono<String>> testMaybeToMono() {
    return ImmutableSet.of(
        RxJava2Adapter.maybeToMono(Maybe.just("foo")),
        Maybe.just("bar").to(RxJava2Adapter::maybeToMono));
  }

  @Template(MonoToCompletable.class)
  ImmutableSet<Completable> testMonoToCompletable() {
    return ImmutableSet.of(
        Completable.fromPublisher(Mono.empty()),
        Mono.empty().as(Completable::fromPublisher),
        RxJava2Adapter.monoToCompletable(Mono.empty()));
  }

  @Template(MonoToFlowable.class)
  ImmutableSet<Publisher<Integer>> testMonoToFlowable() {
    return ImmutableSet.of(
        Flowable.fromPublisher(Mono.just(1)),
        Mono.just(2).transform(Flowable::fromPublisher),
        Mono.just(3).as(Flowable::fromPublisher),
        RxJava2Adapter.monoToFlowable(Mono.just(4)),
        Mono.just(5).transform(RxJava2Adapter::monoToFlowable));
  }

  @Template(MonoToMaybe.class)
  Maybe<String> testMonoToMaybe() {
    return RxJava2Adapter.monoToMaybe(Mono.just("foo"));
  }

  @Template(MonoToSingle.class)
  ImmutableSet<Single<Integer>> testMonoToSingle() {
    return ImmutableSet.of(
        Single.fromPublisher(Mono.just(1)),
        Mono.just(2).as(Single::fromPublisher),
        RxJava2Adapter.monoToSingle(Mono.just(3)));
  }

  @Template(ObservableToFlux.class)
  ImmutableSet<Flux<String>> testObservableToFlux() {
    return ImmutableSet.of(
        RxJava2Adapter.observableToFlux(Observable.just("foo"), BackpressureStrategy.BUFFER),
        Observable.just("bar")
            .as(obs -> RxJava2Adapter.observableToFlux(obs, BackpressureStrategy.DROP)),
        Observable.just("baz")
            .to(obs -> RxJava2Adapter.observableToFlux(obs, BackpressureStrategy.ERROR)));
  }

  @Template(SingleToMono.class)
  ImmutableSet<Mono<Integer>> testSingleToMono() {
    return ImmutableSet.of(
        RxJava2Adapter.singleToMono(Single.just(1)),
        Single.just(2).to(RxJava2Adapter::singleToMono));
  }
}
