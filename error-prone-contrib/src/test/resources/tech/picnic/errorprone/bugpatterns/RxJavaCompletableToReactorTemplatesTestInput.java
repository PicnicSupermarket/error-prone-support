package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import java.util.Arrays;

final class RxJavaCompletableReactorTemplatesTest implements RefasterTemplateTestCase {

  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Maybe.class);
  }

  Completable testCompletableAmb() {
    return Completable.amb(Arrays.asList(Completable.complete(), Completable.complete()));
  }

  Completable testCompletableComplete() {
    return Completable.complete();
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

  Flowable<Void> testCompletableToFlowable() {
    return Completable.complete().toFlowable();
  }

  Maybe<Void> testCompletableToMaybe() {
    return Completable.complete().toMaybe();
  }

  void testCompletableTestAssertResult() throws InterruptedException {
    Completable.complete().test().await().assertResult();
  }

  void testCompletableTestAssertComplete() throws InterruptedException {
    Completable.complete().test().await().assertComplete();
  }

  void testCompletableTestAssertErrorClass() throws InterruptedException {
    Completable.complete().test().await().assertError(InterruptedException.class);
  }

  void testCompletableTestAssertNoErrors() throws InterruptedException {
    Completable.complete().test().await().assertNoErrors();
  }

  void testCompletableTestAssertValueCount() throws InterruptedException {
    Completable.complete().test().await().assertValueCount(1);
  }
}
