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

  Maybe<String> testMaybeAmbArray() {
    return Maybe.ambArray(Maybe.just("foo"), Maybe.just("bar"));
  }

  Flowable<Integer> testMaybeConcatArray() {
    return Maybe.concatArray(Maybe.just(1), Maybe.just(2), Maybe.empty());
  }

  Mono<String> testMaybeDefer() {
    return Maybe.defer(() -> Maybe.just("test")).as(RxJava2Adapter::maybeToMono);
  }

  Maybe<Integer> testMaybeEmpty() {
    return Maybe.empty();
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

  Maybe<Integer> testMaybeWrap() {
    return Maybe.wrap(Maybe.just(1));
  }

  Maybe<String> testMaybeAmbWith() {
    return Maybe.just("foo").ambWith(Maybe.just("bar"));
  }

  Maybe<String> testMaybeCastPositive() {
    return Maybe.just("string").cast(String.class);
  }

  Maybe<Object> testMaybeCastNegative() {
    return Maybe.just("string").cast(Object.class);
  }

  // XXX: This should be fixed later with `Refaster.canBeCoercedTo(...)`
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

  Completable testMaybeIgnoreElement() {
    return Maybe.just(1).ignoreElement();
  }

  Maybe<String> testMaybeMap() {
    return Maybe.just(1, 2).map(String::valueOf);
  }

  Single<Integer> testMaybeSwitchIfEmpty() {
    return Maybe.just(1)
        .switchIfEmpty(
            Single.<Integer>error(
                () -> {
                  throw new IllegalStateException();
                }));
  }
}
