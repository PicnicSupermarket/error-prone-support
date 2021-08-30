package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.Streams;
import io.reactivex.Completable;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

final class RxJavaCompletableReactorTemplatesTest implements RefasterTemplateTestCase {

  Completable<String> testCompletableAmb() {
    return Mono.firstWithSignal(
            Streams.stream(
                    Completable.amb(Arrays.asList(Completable.complete(), Completable.complete())))
                .map(RxJava2Adapter::completableToMono)
                .collect(toImmutableList()))
        .as(RxJava2Adapter::monoToCompletable);
  }
}
