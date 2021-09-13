package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;

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

  Maybe<Integer> testMaybeFirstElement() {
    return RxJava2Adapter.observableToFlux(
            Observable.just(1), BackpressureStrategy.BUFFER)
        .next()
        .as(RxJava2Adapter::monoToMaybe);
  }

  Completable testObservableIgnoreElements() {
    return RxJava2Adapter.observableToFlux(Observable.just(1, 2), BackpressureStrategy.BUFFER)
        .ignoreElements()
        .as(RxJava2Adapter::monoToCompletable);
  }
}
