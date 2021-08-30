package tech.picnic.errorprone.bugpatterns;

import io.reactivex.Completable;

final class RxJavaCompletableReactorTemplatesTest implements RefasterTemplateTestCase {

  Completable<String> testCompletableAmb() {
    return Completable.amb(Arrays.asList(Completable.complete(), Completable.complete()));
  }
}
