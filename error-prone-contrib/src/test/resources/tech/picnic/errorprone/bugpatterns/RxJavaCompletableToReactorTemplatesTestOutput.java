package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import java.util.Arrays;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.refastertemplates.RxJavaToReactorTemplates;

final class RxJavaCompletableReactorTemplatesTest implements RefasterTemplateTestCase {

  Completable testCompletableAmb() {
    return Mono.firstWithSignal(
            Streams.stream(Arrays.asList(Completable.complete(), Completable.complete()))
                .map(RxJava2Adapter::completableToMono)
                .collect(ImmutableList.toImmutableList()))
        .as(RxJava2Adapter::monoToCompletable);
  }

  Completable testCompletableDefer() {
    return RxJava2Adapter.monoToCompletable(
        Mono.defer(
            () ->
                RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.callableAsSupplier(
                        () -> Completable.complete())
                    .get()
                    .as(RxJava2Adapter::completableToMono)));
  }

  Completable testCompletableErrorThrowable() {
    return RxJava2Adapter.monoToCompletable(Mono.error(new IllegalStateException()));
  }

  Completable testCompletableErrorCallable() {
    return RxJava2Adapter.monoToCompletable(
        Mono.error(
            () -> {
              throw new IllegalStateException();
            }));
  }

  Completable testCompletableFromAction() {
    return RxJava2Adapter.monoToCompletable(
        Mono.fromRunnable(
            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toRunnable(() -> {})));
  }
}
