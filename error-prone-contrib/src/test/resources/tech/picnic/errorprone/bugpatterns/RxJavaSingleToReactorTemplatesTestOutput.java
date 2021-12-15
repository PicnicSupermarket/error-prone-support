package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaSingleToReactorTemplatesTest implements RefasterTemplateTestCase {

  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(List.class);
  }

  Single<Object> testSingleErrorThrowable() {
    return RxJava2Adapter.monoToSingle(Mono.error(new IllegalStateException()));
  }

  Single<Integer> testSingleDefer() {
    return RxJava2Adapter.monoToSingle(
        Mono.defer(() -> RxJava2Adapter.singleToMono(Single.just(1))));
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

  Single<Object> testSingleNever() {
    return RxJava2Adapter.monoToSingle(Mono.never());
  }

  Single<Integer> testSingleWrap() {
    return Single.just(1);
  }

  Integer testSingleBlockingGet() {
    return RxJava2Adapter.singleToMono(Single.just(1)).block();
  }

  Flowable<Integer> testSingleConcatWith() {
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.singleToMono(Single.just(1))
            .concatWith(RxJava2Adapter.singleToMono(Single.wrap(Single.just(2)))));
  }

  Single<Integer> testSingleDoOnError() {
    return RxJava2Adapter.monoToSingle(
        RxJava2Adapter.singleToMono(Single.just(1))
            .doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(System.out::println)));
  }

  Single<Integer> testSingleDoOnSuccess() {
    return RxJava2Adapter.monoToSingle(
        RxJava2Adapter.singleToMono(Single.just(1))
            .doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(System.out::println)));
  }

  Maybe<Integer> testSingleFilter() {
    return RxJava2Adapter.monoToMaybe(
        RxJava2Adapter.singleToMono(Single.just(1))
            .filter(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 2)));
  }

  public Mono<String> testUnwrapLambdaSingle() {
    return Mono.just("1")
        .flatMap(
            v ->
                RxJava2Adapter.singleToMono(
                    (Single<String>)
                        RxJavaReactorMigrationUtil.toJdkFunction(
                                (String ident) -> RxJava2Adapter.monoToSingle(Mono.just(ident)))
                            .apply(v)));
  }

  Single<Integer> testSingleFlatMapLambda() {
    return RxJava2Adapter.monoToSingle(
        RxJava2Adapter.singleToMono(Single.just(1))
            .flatMap(i -> RxJava2Adapter.singleToMono(Single.just(i * 2))));
  }

  Completable testSingleFlatMapCompletable() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.singleToMono(Single.just(1))
            .flatMap(
                z ->
                    RxJava2Adapter.completableToMono(
                        Completable.wrap(
                            RxJavaReactorMigrationUtil.<Integer, CompletableSource>toJdkFunction(
                                    integer -> Completable.complete())
                                .apply(z))))
            .then());
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
    return RxJava2Adapter.monoToSingle(
        RxJava2Adapter.singleToMono(Single.just(1))
            .map(RxJavaReactorMigrationUtil.toJdkFunction(i -> i + 1)));
  }

  Flowable<Integer> testSingleToFlowable() {
    return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(Single.just(1)).flux());
  }

  Single<Integer> testSingleTimeOut() {
    return RxJava2Adapter.monoToSingle(
        RxJava2Adapter.singleToMono(Single.just(1))
            .timeout(
                Duration.of(100, TimeUnit.MILLISECONDS.toChronoUnit()),
                RxJava2Adapter.singleToMono(Single.just(2))));
  }

  Maybe<Integer> testSingleToMaybe() {
    return RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(Single.just(1)));
  }

  Single<Integer> testSingleZipWith() {
    return RxJava2Adapter.monoToSingle(
        RxJava2Adapter.singleToMono(Single.just(1))
            .zipWith(
                RxJava2Adapter.singleToMono(Single.wrap(Single.just(2))),
                RxJavaReactorMigrationUtil.toJdkBiFunction(
                    (integer, integer2) -> integer + integer2)));
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
        .verifyError(InterruptedException.class);
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
