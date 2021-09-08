package tech.picnic.errorprone.bugpatterns;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

final class RxJavaSingleToReactorTemplatesTest implements RefasterTemplateTestCase {

  Single<Integer> testSingleJust() {
    return Mono.just(1).as(RxJava2Adapter::monoToSingle);
  }

  Maybe<Integer> testSingleFilter() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .filter(i -> i > 2)
        .as(RxJava2Adapter::monoToMaybe);
  }

  Single<Integer> testSingleFlatMapLambda() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .flatMap(i -> Single.just(i * 2).as(RxJava2Adapter::singleToMono))
        .as(RxJava2Adapter::monoToSingle);
  }

  Completable testCompletableIgnoreElement() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .ignoreElement()
        .as(RxJava2Adapter::monoToCompletable);
  }

  Single<Integer> testSingleMap() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .map(i -> i + 1)
        .as(RxJava2Adapter::monoToSingle);
  }
}
