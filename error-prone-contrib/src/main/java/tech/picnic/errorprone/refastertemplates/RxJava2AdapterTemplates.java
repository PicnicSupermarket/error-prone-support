package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
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

/** Refaster templates related to expressions dealing with {@link RxJava2Adapter}. */
final class RxJava2AdapterTemplates {
  private RxJava2AdapterTemplates() {}

  /** Use the fluent API style when using {@link RxJava2Adapter#completableToMono}. */
  static final class CompletableToMono {
    @BeforeTemplate
    Mono<Void> before(Completable completable) {
      return Refaster.anyOf(
          RxJava2Adapter.completableToMono(completable),
          completable.to(RxJava2Adapter::completableToMono));
    }

    @AfterTemplate
    Mono<Void> after(Completable completable) {
      return completable.as(RxJava2Adapter::completableToMono);
    }
  }

  /** Use the fluent API style when using {@link RxJava2Adapter#flowableToFlux}. */
  static final class FlowableToFlux<T> {
    @BeforeTemplate
    Publisher<T> before(Flowable<T> flowable) {
      return Refaster.anyOf(
          RxJava2Adapter.flowableToFlux(flowable),
          flowable.compose(RxJava2Adapter::flowableToFlux),
          flowable.to(RxJava2Adapter::flowableToFlux));
    }

    @AfterTemplate
    Flux<T> after(Flowable<T> flowable) {
      return flowable.as(RxJava2Adapter::flowableToFlux);
    }
  }

  /** Use the fluent API style when using {@link RxJava2Adapter#fluxToFlowable}. */
  static final class FluxToFlowable<T> {
    @BeforeTemplate
    Publisher<T> before(Flux<T> flux) {
      return Refaster.anyOf(
          RxJava2Adapter.fluxToFlowable(flux), flux.transform(RxJava2Adapter::fluxToFlowable));
    }

    @AfterTemplate
    Flowable<T> after(Flux<T> flux) {
      return flux.as(RxJava2Adapter::fluxToFlowable);
    }
  }

  /** Use the fluent API style when using {@link RxJava2Adapter#fluxToObservable}. */
  static final class FluxToObservable<T> {
    @BeforeTemplate
    Observable<T> before(Flux<T> flux) {
      return RxJava2Adapter.fluxToObservable(flux);
    }

    @AfterTemplate
    Observable<T> after(Flux<T> flux) {
      return flux.as(RxJava2Adapter::fluxToObservable);
    }
  }

  /** Use the fluent API style when using {@link RxJava2Adapter#maybeToMono}. */
  static final class MaybeToMono<T> {
    @BeforeTemplate
    Mono<T> before(Maybe<T> maybe) {
      return Refaster.anyOf(
          RxJava2Adapter.maybeToMono(maybe), maybe.to(RxJava2Adapter::maybeToMono));
    }

    @AfterTemplate
    Mono<T> after(Maybe<T> maybe) {
      return maybe.as(RxJava2Adapter::maybeToMono);
    }
  }

  /** Use the fluent API style when using {@link RxJava2Adapter#monoToCompletable}. */
  static final class MonoToCompletable<T> {
    @BeforeTemplate
    Completable before(Mono<T> mono) {
      return RxJava2Adapter.monoToCompletable(mono);
    }

    @AfterTemplate
    Completable after(Mono<T> mono) {
      return mono.as(RxJava2Adapter::monoToCompletable);
    }
  }

  /** Use the fluent API style when using {@link RxJava2Adapter#monoToFlowable}. */
  static final class MonoToFlowable<T> {
    @BeforeTemplate
    Publisher<T> before(Mono<T> mono) {
      return Refaster.anyOf(
          RxJava2Adapter.monoToFlowable(mono), mono.transform(RxJava2Adapter::monoToFlowable));
    }

    @AfterTemplate
    Flowable<T> after(Mono<T> mono) {
      return mono.as(RxJava2Adapter::monoToFlowable);
    }
  }

  /** Use the fluent API style when using {@link RxJava2Adapter#monoToMaybe}. */
  static final class MonoToMaybe<T> {
    @BeforeTemplate
    Maybe<T> before(Mono<T> mono) {
      return RxJava2Adapter.monoToMaybe(mono);
    }

    @AfterTemplate
    Maybe<T> after(Mono<T> mono) {
      return mono.as(RxJava2Adapter::monoToMaybe);
    }
  }

  /** Use the fluent API style when using {@link RxJava2Adapter#monoToSingle}. */
  static final class MonoToSingle<T> {
    @BeforeTemplate
    Single<T> before(Mono<T> mono) {
      return RxJava2Adapter.monoToSingle(mono);
    }

    @AfterTemplate
    Single<T> after(Mono<T> mono) {
      return mono.as(RxJava2Adapter::monoToSingle);
    }
  }

  /** Use the fluent API style when using {@link RxJava2Adapter#observableToFlux}. */
  static final class ObservableToFlux<T> {
    @BeforeTemplate
    Flux<T> before(Observable<T> observable, BackpressureStrategy strategy) {
      return Refaster.anyOf(
          RxJava2Adapter.observableToFlux(observable, strategy),
          observable.as(obs -> RxJava2Adapter.observableToFlux(obs, strategy)),
          observable.to(obs -> RxJava2Adapter.observableToFlux(obs, strategy)));
    }

    @AfterTemplate
    Flux<T> after(Observable<T> observable, BackpressureStrategy strategy) {
      return observable.toFlowable(strategy).as(RxJava2Adapter::flowableToFlux);
    }
  }

  /** Use the fluent API style when using {@link RxJava2Adapter#singleToMono}. */
  static final class SingleToMono<T> {
    @BeforeTemplate
    Mono<T> before(Single<T> single) {
      return Refaster.anyOf(
          RxJava2Adapter.singleToMono(single), single.to(RxJava2Adapter::singleToMono));
    }

    @AfterTemplate
    Mono<T> after(Single<T> single) {
      return single.as(RxJava2Adapter::singleToMono);
    }
  }
}
