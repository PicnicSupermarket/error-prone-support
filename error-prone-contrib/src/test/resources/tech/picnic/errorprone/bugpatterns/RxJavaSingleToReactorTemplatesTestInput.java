package tech.picnic.errorprone.bugpatterns;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.Completable;

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

  Flowable<Integer> testFlowableToFlowable() {
    return Single.just(1).toFlowable();
  }
}
