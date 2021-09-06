package tech.picnic.errorprone.bugpatterns;

import io.reactivex.Maybe;
import io.reactivex.Single;

final class RxJavaObservableToReactorTemplatesTest implements RefasterTemplateTestCase {

  Single<Integer> testSingleJust() {
    return Single.just(1);
  }

  Maybe<Integer> testSingleFilter() {
    return Single.just(1).filter(i -> i > 2);
  }

  Single<Integer> testSingleFlatMapLambda() {
    return Single.just(1).flatMap(i -> Single.just(i * 2));
  }

  Single<Integer> testSingleMap() {
    return Single.just(1).map(i -> i + 1);
  }
}
