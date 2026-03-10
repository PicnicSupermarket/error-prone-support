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
  static final class CompletableAsRxJava2AdapterCompletableToMono {
    @BeforeTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> before(Completable source) {
      return Refaster.anyOf(
          RxJava2Adapter.completableToMono(source), source.to(RxJava2Adapter::completableToMono));
    }

    @AfterTemplate
    @SuppressWarnings("java:S4968" /* Result may be `Mono<Void>`. */)
    Mono<? extends @Nullable Void> after(Completable source) {
      return source.as(RxJava2Adapter::completableToMono);
    }
  }

  /** Prefer {@link RxJava2Adapter#flowableToFlux} over less idiomatic alternatives. */
  static final class FlowableAsRxJava2AdapterFlowableToFlux<T> {
    @BeforeTemplate
    Flux<T> before(Flowable<T> source) {
      return Refaster.anyOf(
          Flux.from(source),
          source.to(Flux::from),
          source.as(Flux::from),
          RxJava2Adapter.flowableToFlux(source),
          source.to(RxJava2Adapter::flowableToFlux));
    }

    @AfterTemplate
    Flux<T> after(Flowable<T> source) {
      return source.as(RxJava2Adapter::flowableToFlux);
    }
  }

  /** Prefer {@link RxJava2Adapter#fluxToFlowable} over less idiomatic alternatives. */
  static final class FluxAsRxJava2AdapterFluxToFlowable<T> {
    @BeforeTemplate
    Flowable<T> before(Flux<T> source) {
      return Refaster.anyOf(
          Flowable.fromPublisher(source),
          source.as(Flowable::fromPublisher),
          RxJava2Adapter.fluxToFlowable(source));
    }

    @AfterTemplate
    Flowable<T> after(Flux<T> source) {
      return source.as(RxJava2Adapter::fluxToFlowable);
    }
  }

  /** Prefer {@link RxJava2Adapter#fluxToObservable} over less idiomatic alternatives. */
  static final class FluxAsRxJava2AdapterFluxToObservable<T> {
    @BeforeTemplate
    Observable<T> before(Flux<T> publisher) {
      return Refaster.anyOf(
          Observable.fromPublisher(publisher),
          publisher.as(Observable::fromPublisher),
          RxJava2Adapter.fluxToObservable(publisher));
    }

    @AfterTemplate
    Observable<T> after(Flux<T> publisher) {
      return publisher.as(RxJava2Adapter::fluxToObservable);
    }
  }

  /** Prefer {@link RxJava2Adapter#maybeToMono} over less idiomatic alternatives. */
  static final class MaybeAsRxJava2AdapterMaybeToMono<T> {
    @BeforeTemplate
    Mono<T> before(Maybe<T> source) {
      return Refaster.anyOf(
          RxJava2Adapter.maybeToMono(source), source.to(RxJava2Adapter::maybeToMono));
    }

    @AfterTemplate
    Mono<T> after(Maybe<T> source) {
      return source.as(RxJava2Adapter::maybeToMono);
    }
  }

  /** Prefer {@link RxJava2Adapter#monoToCompletable} over less idiomatic alternatives. */
  static final class MonoAsRxJava2AdapterMonoToCompletable<T> {
    @BeforeTemplate
    Completable before(Mono<T> publisher) {
      return Refaster.anyOf(
          Completable.fromPublisher(publisher),
          publisher.as(Completable::fromPublisher),
          RxJava2Adapter.monoToCompletable(publisher));
    }

    @AfterTemplate
    Completable after(Mono<T> publisher) {
      return publisher.as(RxJava2Adapter::monoToCompletable);
    }
  }

  /** Prefer {@link RxJava2Adapter#monoToFlowable} over less idiomatic alternatives. */
  static final class MonoAsRxJava2AdapterMonoToFlowable<T> {
    @BeforeTemplate
    Flowable<T> before(Mono<T> source) {
      return Refaster.anyOf(
          Flowable.fromPublisher(source),
          source.as(Flowable::fromPublisher),
          RxJava2Adapter.monoToFlowable(source));
    }

    @AfterTemplate
    Flowable<T> after(Mono<T> source) {
      return source.as(RxJava2Adapter::monoToFlowable);
    }
  }

  /** Prefer {@link RxJava2Adapter#monoToMaybe} over less idiomatic alternatives. */
  static final class MonoAsRxJava2AdapterMonoToMaybe<T> {
    @BeforeTemplate
    Maybe<T> before(Mono<T> source) {
      return RxJava2Adapter.monoToMaybe(source);
    }

    @AfterTemplate
    Maybe<T> after(Mono<T> source) {
      return source.as(RxJava2Adapter::monoToMaybe);
    }
  }

  /** Prefer {@link RxJava2Adapter#monoToSingle} over less idiomatic alternatives. */
  static final class MonoAsRxJava2AdapterMonoToSingle<T> {
    @BeforeTemplate
    Single<T> before(Mono<T> publisher) {
      return Refaster.anyOf(
          Single.fromPublisher(publisher),
          publisher.as(Single::fromPublisher),
          RxJava2Adapter.monoToSingle(publisher));
    }

    @AfterTemplate
    Single<T> after(Mono<T> publisher) {
      return publisher.as(RxJava2Adapter::monoToSingle);
    }
  }

  /**
   * Prefer chaining {@link Observable#toFlowable(BackpressureStrategy)} with {@link
   * RxJava2Adapter#flowableToFlux} over less idiomatic alternatives.
   */
  static final class ObservableToFlowableAsRxJava2AdapterFlowableToFlux<T> {
    @BeforeTemplate
    Flux<T> before(Observable<T> source, BackpressureStrategy strategy) {
      return Refaster.anyOf(
          RxJava2Adapter.observableToFlux(source, strategy),
          source.as(obs -> RxJava2Adapter.observableToFlux(obs, strategy)),
          source.to(obs -> RxJava2Adapter.observableToFlux(obs, strategy)));
    }

    @AfterTemplate
    Flux<T> after(Observable<T> source, BackpressureStrategy strategy) {
      return source.toFlowable(strategy).as(RxJava2Adapter::flowableToFlux);
    }
  }

  /** Prefer {@link RxJava2Adapter#singleToMono} over less idiomatic alternatives. */
  static final class SingleAsRxJava2AdapterSingleToMono<T> {
    @BeforeTemplate
    Mono<T> before(Single<T> source) {
      return Refaster.anyOf(
          RxJava2Adapter.singleToMono(source), source.to(RxJava2Adapter::singleToMono));
    }

    @AfterTemplate
    Mono<T> after(Single<T> source) {
      return source.as(RxJava2Adapter::singleToMono);
    }
  }
}
