package tech.picnic.errorprone.bugpatterns;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import java.util.Arrays;

final class RxJavaCompletableReactorTemplatesTest implements RefasterTemplateTestCase {

  Completable testCompletableAmb() {
    return Completable.amb(Arrays.asList(Completable.complete(), Completable.complete()));
  }

  Completable testCompletableDefer() {
    return Completable.defer(() -> Completable.complete());
  }

  Completable testCompletableErrorThrowable() {
    return Completable.error(new IllegalStateException());
  }

  Completable testCompletableErrorCallable() {
    return Completable.error(
        () -> {
          throw new IllegalStateException();
        });
  }

  Completable testCompletableFromAction() {
    return Completable.fromAction(() -> {});
  }

  Completable testCompletableFromCallable() {
    return Completable.fromCallable(
        () -> {
          return 1;
        });
  }

  Completable testCompletableFromPublisher() {
    return Completable.fromPublisher(Flowable.just(1));
  }

  Completable testCompletableWrap() {
    return Completable.wrap(Completable.complete());
  }
}
