package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.jspecify.annotations.Nullable;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link RxJava2Adapter}. */
@OnlineDocumentation
final class RxJava2AdapterRules {
  private RxJava2AdapterRules() {}

  /** Prefer {@link RxJava2Adapter#completableToMono} over less idiomatic alternatives. */
  static final class CompletableToMono {
    @BeforeTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> before(Completable completable) {
      return Refaster.anyOf(
          RxJava2Adapter.completableToMono(completable),
          completable.to(RxJava2Adapter::completableToMono));
    }

    @AfterTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> after(Completable completable) {
      return completable.as(RxJava2Adapter::completableToMono);
    }
  }

  /** Prefer {@link RxJava2Adapter#flowableToFlux} over less idiomatic alternatives. */
  static final class FlowableToFlux<T> {
    @BeforeTemplate
    Flux<T> before(Flowable<T> flowable) {
      return Refaster.anyOf(
          Flux.from(flowable),
          flowable.to(Flux::from),
          flowable.as(Flux::from),
          RxJava2Adapter.flowableToFlux(flowable),
          flowable.to(RxJava2Adapter::flowableToFlux));
    }

    @AfterTemplate
    Flux<T> after(Flowable<T> flowable) {
      return flowable.as(RxJava2Adapter::flowableToFlux);
    }
  }

  /** Prefer {@link RxJava2Adapter#fluxToFlowable} over less idiomatic alternatives. */
  static final class FluxToFlowable<T> {
    @BeforeTemplate
    Flowable<T> before(Flux<T> flux) {
      return Refaster.anyOf(
          Flowable.fromPublisher(flux),
          flux.as(Flowable::fromPublisher),
          RxJava2Adapter.fluxToFlowable(flux));
    }

    @AfterTemplate
    Flowable<T> after(Flux<T> flux) {
      return flux.as(RxJava2Adapter::fluxToFlowable);
    }
  }

  /** Prefer {@link RxJava2Adapter#fluxToObservable} over less idiomatic alternatives. */
  static final class FluxToObservable<T> {
    @BeforeTemplate
    Observable<T> before(Flux<T> flux) {
      return Refaster.anyOf(
          Observable.fromPublisher(flux),
          flux.as(Observable::fromPublisher),
          RxJava2Adapter.fluxToObservable(flux));
    }

    @AfterTemplate
    Observable<T> after(Flux<T> flux) {
      return flux.as(RxJava2Adapter::fluxToObservable);
    }
  }

  /** Prefer {@link RxJava2Adapter#maybeToMono} over less idiomatic alternatives. */
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

  /** Prefer {@link RxJava2Adapter#monoToCompletable} over less idiomatic alternatives. */
  static final class MonoToCompletable<T> {
    @BeforeTemplate
    Completable before(Mono<T> mono) {
      return Refaster.anyOf(
          Completable.fromPublisher(mono),
          mono.as(Completable::fromPublisher),
          RxJava2Adapter.monoToCompletable(mono));
    }

    @AfterTemplate
    Completable after(Mono<T> mono) {
      return mono.as(RxJava2Adapter::monoToCompletable);
    }
  }

  /** Prefer {@link RxJava2Adapter#monoToFlowable} over less idiomatic alternatives. */
  static final class MonoToFlowable<T> {
    @BeforeTemplate
    Flowable<T> before(Mono<T> mono) {
      return Refaster.anyOf(
          Flowable.fromPublisher(mono),
          mono.as(Flowable::fromPublisher),
          RxJava2Adapter.monoToFlowable(mono));
    }

    @AfterTemplate
    Flowable<T> after(Mono<T> mono) {
      return mono.as(RxJava2Adapter::monoToFlowable);
    }
  }

  /** Prefer {@link RxJava2Adapter#monoToMaybe} over less idiomatic alternatives. */
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

  /** Prefer {@link RxJava2Adapter#monoToSingle} over less idiomatic alternatives. */
  static final class MonoToSingle<T> {
    @BeforeTemplate
    Single<T> before(Mono<T> mono) {
      return Refaster.anyOf(
          Single.fromPublisher(mono),
          mono.as(Single::fromPublisher),
          RxJava2Adapter.monoToSingle(mono));
    }

    @AfterTemplate
    Single<T> after(Mono<T> mono) {
      return mono.as(RxJava2Adapter::monoToSingle);
    }
  }

  /**
   * Prefer chaining {@link Observable#toFlowable(BackpressureStrategy)} with {@link
   * RxJava2Adapter#flowableToFlux} over less idiomatic alternatives.
   */
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

  /** Prefer {@link RxJava2Adapter#singleToMono} over less idiomatic alternatives. */
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
