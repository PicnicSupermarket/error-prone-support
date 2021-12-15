package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import java.util.concurrent.TimeUnit;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaSingleToReactorTemplatesTest implements RefasterTemplateTestCase {

  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(List.class);
  }

  Single<Object> testSingleErrorThrowable() {
    return Single.error(new IllegalStateException());
  }

  Single<Integer> testSingleDefer() {
    return Single.defer(() -> Single.just(1));
  }

  Single<Object> testSingleErrorCallable() {
    return Single.error(
        () -> {
          throw new IllegalStateException();
        });
  }

  Single<Integer> testSingleFromCallable() {
    return Single.fromCallable(() -> 1);
  }

  Single<Integer> testSingleJust() {
    return Single.just(1);
  }

  Single<Object> testSingleNever() {
    return Single.never();
  }

  Single<Integer> testSingleWrap() {
    return Single.wrap(Single.just(1));
  }

  Integer testSingleBlockingGet() {
    return Single.just(1).blockingGet();
  }

  Flowable<Integer> testSingleConcatWith() {
    return Single.just(1).concatWith(Single.just(2));
  }

  Single<Integer> testSingleDoOnError() {
    return Single.just(1).doOnError(System.out::println);
  }

  Single<Integer> testSingleDoOnSuccess() {
    return Single.just(1).doOnSuccess(System.out::println);
  }

  Maybe<Integer> testSingleFilter() {
    return Single.just(1).filter(i -> i > 2);
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
    return Single.just(1).flatMap(i -> Single.just(i * 2));
  }

  Completable testSingleFlatMapCompletable() {
    return Single.just(1).flatMapCompletable(integer -> Completable.complete());
  }

  Flowable<Integer> testSingleFlatMapPublisher() {
    return Single.just(1).flatMapPublisher(i -> Flowable::just);
  }

  Completable testCompletableIgnoreElement() {
    return Single.just(1).ignoreElement();
  }

  Single<Integer> testSingleMap() {
    return Single.just(1).map(i -> i + 1);
  }

  Flowable<Integer> testSingleToFlowable() {
    return Single.just(1).toFlowable();
  }

  Single<Integer> testSingleTimeOut() {
    return Single.just(1).timeout(1000, TimeUnit.MILLISECONDS, Single.just(2));
  }

  Maybe<Integer> testSingleToMaybe() {
    return Single.just(1).toMaybe();
  }

  Single<Integer> testSingleZipWith() {
    return Single.just(1).zipWith(Single.just(2), (integer, integer2) -> integer + integer2);
  }

  void testSingleTestAssertResultItem() throws InterruptedException {
    Single.just(1).test().await().assertResult(1);
    Single.just(2).test().await().assertValue(2);
  }

  void testSingleTestAssertResult() throws InterruptedException {
    Single.just(1).test().await().assertResult();
  }

  void testSingleTestAssertValue() throws InterruptedException {
    Single.just(1).test().await().assertValue(i -> i > 2);
    Single.just(3).test().await().assertValue(i -> i > 4).assertComplete();
  }

  void testSingleTestAssertComplete() throws InterruptedException {
    Single.just(1).test().await().assertComplete();
  }

  void testSingleTestAssertErrorClass() throws InterruptedException {
    Single.just(1).test().await().assertError(InterruptedException.class);
  }

  void testSingleTestAssertNoErrors() throws InterruptedException {
    Single.just(1).test().await().assertNoErrors();
  }

  void testSingleTestAssertValueCount() throws InterruptedException {
    Single.just(1).test().await().assertValueCount(1);
  }
}
