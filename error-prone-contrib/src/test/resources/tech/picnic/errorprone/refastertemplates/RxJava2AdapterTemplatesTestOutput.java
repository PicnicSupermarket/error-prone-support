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
        Completable.complete().as(RxJava2Adapter::completableToMono),
        Completable.complete().as(RxJava2Adapter::completableToMono));
  }

  @Template(FlowableToFlux.class)
  ImmutableSet<Publisher<Integer>> testFlowableToFlux() {
    // The `Arrays.asList` is to avoid confusing `javac`; `ImmutableSet.of` uses varargs from the
    // seventh parameter onwards.
    return ImmutableSet.copyOf(
        Arrays.asList(
            Flowable.just(1).as(RxJava2Adapter::flowableToFlux),
            Flowable.just(2).as(RxJava2Adapter::flowableToFlux),
            Flowable.just(3).as(RxJava2Adapter::flowableToFlux),
            Flowable.just(4).as(RxJava2Adapter::flowableToFlux),
            Flowable.just(5).as(RxJava2Adapter::flowableToFlux),
            Flowable.just(6).as(RxJava2Adapter::flowableToFlux),
            Flowable.just(7).as(RxJava2Adapter::flowableToFlux)));
  }

  @Template(FluxToFlowable.class)
  ImmutableSet<Publisher<String>> testFluxToFlowable() {
    return ImmutableSet.of(
        Flux.just("foo").as(RxJava2Adapter::fluxToFlowable),
        Flux.just("bar").as(RxJava2Adapter::fluxToFlowable),
        Flux.just("baz").as(RxJava2Adapter::fluxToFlowable),
        Flux.just("qux").as(RxJava2Adapter::fluxToFlowable),
        Flux.just("quux").as(RxJava2Adapter::fluxToFlowable));
  }

  @Template(FluxToObservable.class)
  ImmutableSet<Observable<Integer>> testFluxToObservable() {
    return ImmutableSet.of(
        Flux.just(1).as(RxJava2Adapter::fluxToObservable),
        Flux.just(2).as(RxJava2Adapter::fluxToObservable),
        Flux.just(3).as(RxJava2Adapter::fluxToObservable));
  }

  @Template(MaybeToMono.class)
  ImmutableSet<Mono<String>> testMaybeToMono() {
    return ImmutableSet.of(
        Maybe.just("foo").as(RxJava2Adapter::maybeToMono),
        Maybe.just("bar").as(RxJava2Adapter::maybeToMono));
  }

  @Template(MonoToCompletable.class)
  ImmutableSet<Completable> testMonoToCompletable() {
    return ImmutableSet.of(
        Mono.empty().as(RxJava2Adapter::monoToCompletable),
        Mono.empty().as(RxJava2Adapter::monoToCompletable),
        Mono.empty().as(RxJava2Adapter::monoToCompletable));
  }

  @Template(MonoToFlowable.class)
  ImmutableSet<Publisher<Integer>> testMonoToFlowable() {
    return ImmutableSet.of(
        Mono.just(1).as(RxJava2Adapter::monoToFlowable),
        Mono.just(2).as(RxJava2Adapter::monoToFlowable),
        Mono.just(3).as(RxJava2Adapter::monoToFlowable),
        Mono.just(4).as(RxJava2Adapter::monoToFlowable),
        Mono.just(5).as(RxJava2Adapter::monoToFlowable));
  }

  @Template(MonoToMaybe.class)
  Maybe<String> testMonoToMaybe() {
    return Mono.just("foo").as(RxJava2Adapter::monoToMaybe);
  }

  @Template(MonoToSingle.class)
  ImmutableSet<Single<Integer>> testMonoToSingle() {
    return ImmutableSet.of(
        Mono.just(1).as(RxJava2Adapter::monoToSingle),
        Mono.just(2).as(RxJava2Adapter::monoToSingle),
        Mono.just(3).as(RxJava2Adapter::monoToSingle));
  }

  @Template(ObservableToFlux.class)
  ImmutableSet<Flux<String>> testObservableToFlux() {
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

  @Template(SingleToMono.class)
  ImmutableSet<Mono<Integer>> testSingleToMono() {
    return ImmutableSet.of(
        Single.just(1).as(RxJava2Adapter::singleToMono),
        Single.just(2).as(RxJava2Adapter::singleToMono));
  }
}
