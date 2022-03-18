package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
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
    return RxJava2Adapter.monoToCompletable(
        Mono.firstWithSignal(
            Streams.stream(Arrays.asList(Completable.complete(), Completable.complete()))
                .map(RxJava2Adapter::completableToMono)
                .collect(ImmutableList.toImmutableList())));
  }

  Completable testCompletableComplete() {
    return RxJava2Adapter.monoToCompletable(Mono.empty());
  }

  Completable testCompletableDefer() {
    return RxJava2Adapter.monoToCompletable(
        Mono.defer(
            () ->
                RxJava2Adapter.completableToMono(
                    RxJavaReactorMigrationUtil.callableAsSupplier(() -> Completable.complete())
                        .get())));
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

  Completable testCompletableFromRunnable() {
    return RxJava2Adapter.monoToCompletable(Mono.fromRunnable(() -> {}));
  }

  Completable testCompletableWrap() {
    return Completable.complete();
  }

  Completable testCompletableAndThenCompletable() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.completableToMono(Completable.complete())
            .then(RxJava2Adapter.completableToMono(Completable.wrap(Completable.complete()))));
  }

  Maybe<Integer> testCompletableAndThenMaybe() {
    return RxJava2Adapter.monoToMaybe(
        RxJava2Adapter.completableToMono(Completable.complete())
            .then(RxJava2Adapter.maybeToMono(Maybe.wrap(Maybe.just(1)))));
  }

  Flowable<Integer> testCompletableAndThenPublisher() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.completableToMono(Completable.complete()).thenMany(Flowable.just(1)));
  }

  Single<Integer> testCompletableAndThenSingle() {
    return RxJava2Adapter.monoToSingle(
        RxJava2Adapter.completableToMono(Completable.complete())
            .then(RxJava2Adapter.singleToMono(Single.wrap(Single.just(1)))));
  }

  void testCompletableBlockingAwait() {
    RxJava2Adapter.completableToMono(Completable.complete()).block();
  }

  Completable testCompletableDoOnError() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.completableToMono(Completable.complete())
            .doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(System.out::println)));
  }

  Completable testCompletableOnErrorComplete() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.completableToMono(Completable.complete()).onErrorStop());
  }

  Completable testCompletableOnErrorCompletePredicate() {
    RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.completableToMono(Completable.complete())
            .onErrorResume(
                RxJavaReactorMigrationUtil.toJdkPredicate(t -> t instanceof IOException),
                t -> Mono.empty()));
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.completableToMono(Completable.complete())
            .onErrorResume(
                RxJavaReactorMigrationUtil.toJdkPredicate(throwable -> true), t -> Mono.empty()));
  }

  Completable testCompletableTimeoutLongTimeUnit() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.completableToMono(Completable.complete())
            .timeout(Duration.of(1000, TimeUnit.MILLISECONDS.toChronoUnit())));
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

  void testCompletableTestAssertFailure() throws InterruptedException {
    RxJava2Adapter.completableToMono(Completable.complete())
        .as(StepVerifier::create)
        .verifyError(IllegalArgumentException.class);
  }

  void testCompletableTestAssertNoValues() throws InterruptedException {
    RxJava2Adapter.completableToMono(Completable.complete())
        .as(StepVerifier::create)
        .verifyComplete();
  }

  void testCompletableTestAssertFailureAndMessage() throws InterruptedException {
    RxJava2Adapter.completableToMono(Completable.complete())
        .as(StepVerifier::create)
        .expectErrorSatisfies(
            t ->
                Assertions.assertThat(t)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("foo"))
        .verify();
  }
}
