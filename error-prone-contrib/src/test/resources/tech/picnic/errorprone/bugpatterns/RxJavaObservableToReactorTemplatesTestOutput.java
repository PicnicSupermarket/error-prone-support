package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.Streams;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import java.util.concurrent.TimeUnit;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;

final class RxJavaObservableToReactorTemplatesTest implements RefasterTemplateTestCase {

  Completable<Integer> testObservableAmb() {
    return RxJava2Adapter.fluxToObservable(
        Flux.<T>firstWithSignal(
            Streams.stream(Observable.timer(100, TimeUnit.NANOSECONDS).map(i -> 1))
                .map(e -> e.toFlowable(BackpressureStrategy.BUFFER))
                .map(RxJava2Adapter::flowableToFlux)
                .collect(toImmutableList())));
  }
}
