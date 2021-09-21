package tech.picnic.errorprone.bugpatterns;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

final class RxJavaSingleToReactorTemplatesTest implements RefasterTemplateTestCase {

  Single<Object> testSingleErrorThrowable() {
    return Single.error(new IllegalStateException());
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

  Single<Integer> testSingleWrap() {
    return Single.wrap(Single.just(1));
  }

  Integer testSingleBlockingGet() {
    return Single.just(1).blockingGet();
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

  Single<Integer> testSingleFlatMapLambda() {
    return Single.just(1).flatMap(i -> Single.just(i * 2));
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
