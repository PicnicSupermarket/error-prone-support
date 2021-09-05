package tech.picnic.errorprone.bugpatterns;

import io.reactivex.Observable;
import java.util.concurrent.TimeUnit;

final class RxJavaObservableToReactorTemplatesTest implements RefasterTemplateTestCase {

  Completable<Integer> testObservableAmb() {
    return Observable.amb(Observable.timer(100, TimeUnit.NANOSECONDS).map(i -> 1));
  }

  Completable<Integer> testObservableEmpty() {
    return Observable.empty();
  }
}
