package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaObservableToReactorTemplatesTest implements RefasterTemplateTestCase {

  Observable<Integer> testObservableAmb() {
    return RxJava2Adapter.fluxToObservable(
        Flux.firstWithSignal(
            Streams.stream(ImmutableList.of(Observable.just(1), Observable.just(2)))
                .map(e -> e.toFlowable(BackpressureStrategy.BUFFER))
                .map(RxJava2Adapter::flowableToFlux)
                .collect(ImmutableList.toImmutableList())));
  }

  Observable<Integer> testObservableEmpty() {
    return RxJava2Adapter.fluxToObservable(Flux.empty());
  }

  Observable<Integer> testObservableJust() {
    return RxJava2Adapter.fluxToObservable(Flux.just(1));
  }

  Observable<Integer> testObservableJustTwo() {
    return RxJava2Adapter.fluxToObservable(Flux.just(1, 2));
  }

  Maybe<Integer> testMaybeFirstElement() {
    return RxJava2Adapter.observableToFlux(Observable.just(1), BackpressureStrategy.BUFFER)
        .next()
        .as(RxJava2Adapter::monoToMaybe);
  }

  Observable<Integer> testObservableFilter() {
    return RxJava2Adapter.observableToFlux(Observable.just(1), BackpressureStrategy.BUFFER)
        .filter(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 1))
        .as(RxJava2Adapter::fluxToObservable);
  }

  Completable testObservableIgnoreElements() {
    return RxJava2Adapter.observableToFlux(Observable.just(1, 2), BackpressureStrategy.BUFFER)
        .ignoreElements()
        .as(RxJava2Adapter::monoToCompletable);
  }

  void testObservableTestAssertResultItem() throws InterruptedException {
    RxJava2Adapter.observableToFlux(Observable.just(1), BackpressureStrategy.BUFFER)
        .as(StepVerifier::create)
        .expectNext(1)
        .verifyComplete();
    RxJava2Adapter.observableToFlux(Observable.just(2), BackpressureStrategy.BUFFER)
        .as(StepVerifier::create)
        .expectNext(2)
        .verifyComplete();
  }

  void testObservableTestAssertResult() throws InterruptedException {
    RxJava2Adapter.observableToFlux(Observable.just(1), BackpressureStrategy.BUFFER)
        .as(StepVerifier::create)
        .verifyComplete();
  }

  void testObservableTestAssertValue() throws InterruptedException {
    RxJava2Adapter.observableToFlux(Observable.just(1), BackpressureStrategy.BUFFER)
        .as(StepVerifier::create)
        .expectNextMatches(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 2))
        .verifyComplete();
    RxJava2Adapter.observableToFlux(Observable.just(3), BackpressureStrategy.BUFFER)
        .as(StepVerifier::create)
        .expectNextMatches(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 4))
        .verifyComplete();
  }

  void testObservableTestAssertResultValues() throws InterruptedException {
    RxJava2Adapter.observableToFlux(Observable.just(1, 2, 3), BackpressureStrategy.BUFFER)
        .as(StepVerifier::create)
        .expectNext(1, 2, 3)
        .verifyComplete();
    RxJava2Adapter.observableToFlux(Observable.just(4, 5, 6), BackpressureStrategy.BUFFER)
        .as(StepVerifier::create)
        .expectNext(4, 5, 6)
        .verifyComplete();
  }

  void testObservableTestAssertComplete() throws InterruptedException {
    RxJava2Adapter.observableToFlux(Observable.just(1), BackpressureStrategy.BUFFER)
        .as(StepVerifier::create)
        .verifyComplete();
  }

  void testObservableTestAssertErrorClass() throws InterruptedException {
    RxJava2Adapter.observableToFlux(Observable.just(1), BackpressureStrategy.BUFFER)
        .as(StepVerifier::create)
        .expectError(InterruptedException.class)
        .verify();
  }

  void testObservableTestAssertNoErrors() throws InterruptedException {
    RxJava2Adapter.observableToFlux(Observable.just(1), BackpressureStrategy.BUFFER)
        .as(StepVerifier::create)
        .verifyComplete();
  }

  void testObservableTestAssertValueCount() throws InterruptedException {
    RxJava2Adapter.observableToFlux(Observable.just(1), BackpressureStrategy.BUFFER)
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }
}
