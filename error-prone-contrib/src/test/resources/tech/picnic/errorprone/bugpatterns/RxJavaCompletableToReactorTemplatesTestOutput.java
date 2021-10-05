package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import java.util.Arrays;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaCompletableReactorTemplatesTest implements RefasterTemplateTestCase {

  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Maybe.class);
  }

  Completable testCompletableAmb() {
    return Mono.firstWithSignal(
            Streams.stream(Arrays.asList(Completable.complete(), Completable.complete()))
                .map(RxJava2Adapter::completableToMono)
                .collect(ImmutableList.toImmutableList()))
        .as(RxJava2Adapter::monoToCompletable);
  }

  Completable testCompletableComplete() {
    return RxJava2Adapter.monoToCompletable(Mono.empty());
  }

  Completable testCompletableDefer() {
    return RxJava2Adapter.monoToCompletable(
        Mono.defer(
            () ->
                RxJavaReactorMigrationUtil.callableAsSupplier(() -> Completable.complete())
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
        Mono.fromRunnable(RxJavaReactorMigrationUtil.toRunnable(() -> {})));
  }

  Completable testCompletableFromCallable() {
    return RxJava2Adapter.monoToCompletable(
        Mono.fromCallable(
            () -> {
              return 1;
            }));
  }

  Completable testCompletableFromPublisher() {
    return RxJava2Adapter.monoToCompletable(Mono.from(Flowable.just(1)));
  }

  Completable testCompletableWrap() {
    return Completable.complete();
  }

  Flowable<Void> testCompletableToFlowable() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.completableToMono(Completable.complete()).flux());
  }

  Maybe<Void> testCompletableToMaybe() {
    return RxJava2Adapter.monoToMaybe(RxJava2Adapter.completableToMono(Completable.complete()));
  }

  void testCompletableTestAssertResult() throws InterruptedException {
    RxJava2Adapter.completableToMono(Completable.complete())
        .as(StepVerifier::create)
        .verifyComplete();
  }

  void testCompletableTestAssertComplete() throws InterruptedException {
    RxJava2Adapter.completableToMono(Completable.complete())
        .as(StepVerifier::create)
        .verifyComplete();
  }

  void testCompletableTestAssertErrorClass() throws InterruptedException {
    RxJava2Adapter.completableToMono(Completable.complete())
        .as(StepVerifier::create)
        .verifyError(InterruptedException.class);
  }

  void testCompletableTestAssertNoErrors() throws InterruptedException {
    RxJava2Adapter.completableToMono(Completable.complete())
        .as(StepVerifier::create)
        .verifyComplete();
  }

  void testCompletableTestAssertValueCount() throws InterruptedException {
    RxJava2Adapter.completableToMono(Completable.complete())
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }
}
