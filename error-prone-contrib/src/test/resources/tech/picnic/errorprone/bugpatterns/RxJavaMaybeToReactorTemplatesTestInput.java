package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.concurrent.CompletableFuture;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

final class RxJavaMaybeToReactorTemplatesTest implements RefasterTemplateTestCase {

  Maybe<String> testMaybeAmb() {
    return Maybe.amb(ImmutableList.of(Maybe.just("foo"), Maybe.just("bar")));
  }

  // XXX: Template turned off for now.
  Maybe<String> testMaybeAmbArray() {
    return Maybe.ambArray(Maybe.just("foo"), Maybe.just("bar"));
  }

  Flowable<Integer> testMaybeConcatArray() {
    return Flowable.empty();
  }

  Mono<String> testMaybeDefer() {
    return Maybe.defer(() -> Maybe.just("test")).as(RxJava2Adapter::maybeToMono);
  }

  Maybe<Integer> testMaybeEmpty() {
    return Maybe.empty();
  }

  Maybe<Object> testMaybeErrorThrowable() {
    return Maybe.error(new IllegalStateException());
  }

  Maybe<Object> testMaybeErrorCallable() {
    return Maybe.error(
        () -> {
          throw new IllegalStateException();
        });
  }

  Maybe<Object> testMaybeFromAction() {
    return Maybe.fromAction(
        () -> {
          String s = "foo";
        });
  }

  Maybe<Object> testMaybeFromCallable() {
    return Maybe.fromCallable(
        () -> {
          String s = "foo";
          return null;
        });
  }

  Maybe<Integer> testMaybeFromFuture() {
    return Maybe.fromFuture(new CompletableFuture<>());
  }

  Maybe<Integer> testMaybeFromRunnable() {
    return Maybe.fromRunnable(
        () -> {
          int i = 1 + 1;
        });
  }

  Maybe<Integer> testMaybeFromSingle() {
    return Maybe.fromSingle(Single.just(1));
  }

  Maybe<Integer> testMaybeJust() {
    return Maybe.just(1);
  }

  Maybe<Integer> testMaybeWrap() {
    return Maybe.wrap(Maybe.just(1));
  }

  Maybe<String> testMaybeAmbWith() {
    return Maybe.just("foo").ambWith(Maybe.just("bar"));
  }

  Maybe<String> testMaybeCastPositive() {
    return Maybe.just("string").cast(String.class);
  }

  @SuppressWarnings("MaybeJust")
  Maybe<Object> testMaybeCastNegative() {
    return Maybe.just("string").cast(Object.class);
  }

  Maybe<Integer> testMaybeDoOnError() {
    return Maybe.just(1).doOnError(System.out::println);
  }

  Maybe<Integer> testMaybeDoOnSuccess() {
    return Maybe.just(1).doOnSuccess(System.out::println);
  }

  Maybe<Integer> testMaybeFilter() {
    return Maybe.just(1).filter(i -> i > 1);
  }

  Maybe<Integer> testMaybeFlatMapFunction() {
    Maybe.just(1).flatMap(this::exampleMethod);

    return Maybe.just(1).flatMap(exampleFunction());
  }

  private io.reactivex.functions.Function<Integer, Maybe<Integer>> exampleFunction() {
    return null;
  }

  Maybe<Integer> testMaybeFlatMapLambda() {
    return Maybe.just(1).flatMap(i -> Maybe.just(i * 2));
  }

  Maybe<Integer> testMaybeFlatMapMethodReference() {
    return Maybe.just(1).flatMap(this::exampleMethod);
  }

  private Maybe<Integer> exampleMethod(Integer x) {
    return null;
  }

  // XXX: To be removed if template works.
  @SuppressWarnings("MaybeJust")
  Maybe<Integer> testMaybeFlatMapSingleElement() {
    return Maybe.just(1).flatMapSingleElement(Single::just);
  }

  Completable testMaybeIgnoreElement() {
    return Maybe.just(1).ignoreElement();
  }

  Maybe<String> testMaybeMap() {
    return Maybe.just(1).map(String::valueOf);
  }

  Single<Integer> testMaybeSwitchIfEmpty() {
    return Maybe.just(1)
        .switchIfEmpty(
            Single.<Integer>error(
                () -> {
                  throw new IllegalStateException();
                }));
  }

  Flowable<Integer> testMaybeToFlowable() {
    return Maybe.just(1).toFlowable();
  }

  @SuppressWarnings("MaybeJust")
  private Maybe<Integer> getMaybe() {
    return Maybe.just(3);
  }

  void MaybeTestAssertResultItem() throws InterruptedException {
    Maybe.just(1).test().await().assertResult(1);
    Maybe.just(2).test().await().assertValue(2);
  }

  void MaybeTestAssertResult() throws InterruptedException {
    Maybe.just(1).test().await().assertResult();
  }

  void MaybeTestAssertValue() throws InterruptedException {
    Maybe.just(1).test().await().assertValue(i -> i > 2);
    Maybe.just(3).test().await().assertValue(i -> i > 4).assertComplete();
  }

  void testMaybeTestAssertComplete() throws InterruptedException {
    Maybe.just(1).test().await().assertComplete();
  }

  void testMaybeTestAssertErrorClass() throws InterruptedException {
    Maybe.just(1).test().await().assertError(InterruptedException.class);
  }

  void testMaybeTestAssertNoErrors() throws InterruptedException {
    Maybe.just(1).test().await().assertNoErrors();
  }

  void testMaybeTestAssertValueCount() throws InterruptedException {
    Maybe.just(1).test().await().assertValueCount(1);
  }
}
