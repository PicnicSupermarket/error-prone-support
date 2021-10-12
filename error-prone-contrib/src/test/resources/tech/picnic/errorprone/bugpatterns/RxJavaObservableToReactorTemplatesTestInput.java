package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;

final class RxJavaObservableToReactorTemplatesTest implements RefasterTemplateTestCase {

  Observable<Integer> testObservableAmb() {
    return Observable.amb(ImmutableList.of(Observable.just(1), Observable.just(2)));
  }

  Observable<Integer> testObservableEmpty() {
    return Observable.empty();
  }

  Observable<Integer> testObservableJust() {
    return Observable.just(1);
  }

  Observable<Integer> testObservableJustTwo() {
    return Observable.just(1, 2);
  }

  Observable<Integer> testObservableJustThree() {
    return Observable.just(1, 2, 3);
  }

  Maybe<Integer> testMaybeFirstElement() {
    return Observable.just(1).firstElement();
  }

  Observable<Integer> testObservableFilter() {
    return Observable.just(1).filter(i -> i > 1);
  }

  Completable testObservableIgnoreElements() {
    return Observable.just(1, 2).ignoreElements();
  }

  Flowable<Integer> testCompletableToFlowable() {
    return Observable.just(1).toFlowable(BackpressureStrategy.BUFFER);
  }

  void testObservableTestAssertResultItem() throws InterruptedException {
    Observable.just(1).test().await().assertResult(1);
    Observable.just(2).test().await().assertValue(2);
  }

  void testObservableTestAssertResult() throws InterruptedException {
    Observable.just(1).test().await().assertResult();
  }

  void testObservableTestAssertValue() throws InterruptedException {
    Observable.just(1).test().await().assertValue(i -> i > 2);
    Observable.just(3).test().await().assertValue(i -> i > 4).assertComplete();
  }

  void testObservableTestAssertResultValues() throws InterruptedException {
    Observable.just(1, 2, 3).test().await().assertResult(1, 2, 3);
    Observable.just(4, 5, 6).test().await().assertValues(4, 5, 6);
  }

  void testObservableTestAssertComplete() throws InterruptedException {
    Observable.just(1).test().await().assertComplete();
  }

  void testObservableTestAssertErrorClass() throws InterruptedException {
    Observable.just(1).test().await().assertError(InterruptedException.class);
  }

  void testObservableTestAssertNoErrors() throws InterruptedException {
    Observable.just(1).test().await().assertNoErrors();
  }

  void testObservableTestAssertValueCount() throws InterruptedException {
    Observable.just(1).test().await().assertValueCount(1);
  }
}
