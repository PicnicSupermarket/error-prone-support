package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.io.IOException;
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

  Completable testCompletableFromRunnable() {
    return Completable.fromRunnable(() -> {});
  }

  Completable testCompletableWrap() {
    return Completable.wrap(Completable.complete());
  }

  Completable testCompletableAndThenCompletable() {
    return Completable.complete().andThen(Completable.complete());
  }

  Maybe<Integer> testCompletableAndThenMaybe() {
    return Completable.complete().andThen(Maybe.just(1));
  }

  Flowable<Integer> testCompletableAndThenPublisher() {
    return Completable.complete().andThen(Flowable.just(1));
  }

  Single<Integer> testCompletableAndThenSingle() {
    return Completable.complete().andThen(Single.just(1));
  }

  void testCompletableBlockingAwait() {
    Completable.complete().blockingAwait();
  }

  Completable testCompletableDoOnError() {
    return Completable.complete().doOnError(System.out::println);
  }

  Completable testCompletableOnErrorComplete() {
    return Completable.complete().onErrorComplete();
  }

  Completable testCompletableOnErrorCompletePredicate() {
    Completable.complete().onErrorComplete(t -> t instanceof IOException);
    return Completable.complete().onErrorComplete(throwable -> true);
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

  void testCompletableTestAssertFailure() throws InterruptedException {
    Completable.complete().test().await().assertFailure(IllegalArgumentException.class);
  }

  void testCompletableTestAssertNoValues() throws InterruptedException {
    Completable.complete().test().await().assertNoValues();
  }

  void testCompletableTestAssertFailureAndMessage() throws InterruptedException {
    Completable.complete()
        .test()
        .await()
        .assertFailureAndMessage(IllegalArgumentException.class, "foo");
  }
}
