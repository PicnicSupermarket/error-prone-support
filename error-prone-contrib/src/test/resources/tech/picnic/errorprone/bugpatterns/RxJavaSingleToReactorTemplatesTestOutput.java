package tech.picnic.errorprone.bugpatterns;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaSingleToReactorTemplatesTest implements RefasterTemplateTestCase {

  Single<Object> testSingleErrorThrowable() {
    return RxJava2Adapter.monoToSingle(Mono.error(new IllegalStateException()));
  }

  Single<Integer> testSingleDefer() {
    return Mono.defer(() -> Single.just(1).as(RxJava2Adapter::singleToMono))
        .as(RxJava2Adapter::monoToSingle);
  }

  Single<Object> testSingleErrorCallable() {
    return RxJava2Adapter.monoToSingle(
        Mono.error(
            RxJavaReactorMigrationUtil.callableAsSupplier(
                () -> {
                  throw new IllegalStateException();
                })));
  }

  Single<Integer> testSingleFromCallable() {
    return RxJava2Adapter.monoToSingle(
        Mono.fromSupplier(RxJavaReactorMigrationUtil.callableAsSupplier(() -> 1)));
  }

  Single<Integer> testSingleJust() {
    return RxJava2Adapter.monoToSingle(Mono.just(1));
  }

  Single<Integer> testSingleWrap() {
    return Single.just(1);
  }

  Integer testSingleBlockingGet() {
    return RxJava2Adapter.singleToMono(Single.just(1)).block();
  }

  Single<Integer> testSingleDoOnError() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(System.out::println))
        .as(RxJava2Adapter::monoToSingle);
  }

  Single<Integer> testSingleDoOnSuccess() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(System.out::println))
        .as(RxJava2Adapter::monoToSingle);
  }

  Maybe<Integer> testSingleFilter() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .filter(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 2))
        .as(RxJava2Adapter::monoToMaybe);
  }

  Single<Integer> testSingleFlatMapLambda() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .flatMap(i -> Single.just(i * 2).as(RxJava2Adapter::singleToMono))
        .as(RxJava2Adapter::monoToSingle);
  }

  Completable testSingleFlatMapCompletable() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.singleToMono(Single.just(1))
            .flatMap(
                e ->
                    RxJava2Adapter.completableToMono(
                        Completable.wrap(
                            RxJavaReactorMigrationUtil.toJdkFunction(
                                    (Function<Integer, CompletableSource>)
                                        integer -> Completable.complete())
                                .apply(e))))
            .then());
  }

  Completable testSingleRemoveLambdaWithCast() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(1)))
            .flatMap(v -> Mono.justOrEmpty(null))
            .then());
  }

  Mono<Void> testSingleRemoveLambdaWithCompletable() {
    return Flux.just(1, 2)
        .collectList()
        .flatMap(u -> RxJava2Adapter.completableToMono(Completable.complete()))
        .then();
  }

  Flowable<Integer> testSingleFlatMapPublisher() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.singleToMono(Single.just(1))
            .flatMapMany(RxJavaReactorMigrationUtil.toJdkFunction(i -> Flowable::just)));
  }

  Completable testCompletableIgnoreElement() {
    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(Single.just(1)).then());
  }

  Single<Integer> testSingleMap() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .map(RxJavaReactorMigrationUtil.toJdkFunction(i -> i + 1))
        .as(RxJava2Adapter::monoToSingle);
  }

  Flowable<Integer> testSingleToFlowable() {
    return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(Single.just(1)).flux());
  }

  void testSingleTestAssertResultItem() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1))
        .as(StepVerifier::create)
        .expectNext(1)
        .verifyComplete();
    RxJava2Adapter.singleToMono(Single.just(2))
        .as(StepVerifier::create)
        .expectNext(2)
        .verifyComplete();
  }

  void testSingleTestAssertResult() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1)).as(StepVerifier::create).verifyComplete();
  }

  void testSingleTestAssertValue() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1))
        .as(StepVerifier::create)
        .expectNextMatches(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 2))
        .verifyComplete();
    RxJava2Adapter.singleToMono(Single.just(3))
        .as(StepVerifier::create)
        .expectNextMatches(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 4))
        .verifyComplete();
  }

  void testSingleTestAssertComplete() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1)).as(StepVerifier::create).verifyComplete();
  }

  void testSingleTestAssertErrorClass() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1))
        .as(StepVerifier::create)
        .expectError(InterruptedException.class)
        .verify();
  }

  void testSingleTestAssertNoErrors() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1)).as(StepVerifier::create).verifyComplete();
  }

  void testSingleTestAssertValueCount() throws InterruptedException {
    RxJava2Adapter.singleToMono(Single.just(1))
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }
}
