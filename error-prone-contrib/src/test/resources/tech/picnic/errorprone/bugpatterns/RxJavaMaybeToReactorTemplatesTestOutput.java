package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.concurrent.CompletableFuture;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaMaybeToReactorTemplatesTest implements RefasterTemplateTestCase {

  Maybe<String> testMaybeAmb() {
    return RxJava2Adapter.monoToMaybe(
        Mono.firstWithSignal(
            Streams.stream(ImmutableList.of(Maybe.just("foo"), Maybe.just("bar")))
                .map(RxJava2Adapter::maybeToMono)
                .collect(ImmutableList.toImmutableList())));
  }

  Maybe<String> testMaybeAmbArray() {
    return Maybe.ambArray(Maybe.just("foo"), Maybe.just("bar"));
  }

  Flowable<Integer> testMaybeConcatArray() {
    return Flowable.empty();
  }

  Mono<String> testMaybeDefer() {
    return Mono.defer(() -> Maybe.just("test").as(RxJava2Adapter::maybeToMono));
  }

  Maybe<Integer> testMaybeEmpty() {
    return RxJava2Adapter.monoToMaybe(Mono.empty());
  }

  Maybe<Object> testMaybeErrorThrowable() {
    return RxJava2Adapter.monoToMaybe(Mono.error(new IllegalStateException()));
  }

  Maybe<Object> testMaybeErrorCallable() {
    return RxJava2Adapter.monoToMaybe(
        Mono.error(
            RxJavaReactorMigrationUtil.callableAsSupplier(
                () -> {
                  throw new IllegalStateException();
                })));
  }

  Maybe<Object> testMaybeFromAction() {
    return RxJava2Adapter.monoToMaybe(
        Mono.fromRunnable(
            RxJavaReactorMigrationUtil.toRunnable(
                () -> {
                  String s = "foo";
                })));
  }

  Maybe<Object> testMaybeFromCallable() {
    return RxJava2Adapter.monoToMaybe(
        Mono.fromSupplier(
            RxJavaReactorMigrationUtil.callableAsSupplier(
                () -> {
                  String s = "foo";
                  return null;
                })));
  }

  Maybe<Integer> testMaybeFromFuture() {
    return RxJava2Adapter.monoToMaybe(Mono.fromFuture(new CompletableFuture<>()));
  }

  Maybe<Integer> testMaybeFromRunnable() {
    return RxJava2Adapter.monoToMaybe(
        Mono.fromRunnable(
            () -> {
              int i = 1 + 1;
            }));
  }

  Maybe<Integer> testMaybeFromSingle() {
    return RxJava2Adapter.monoToMaybe(
        Mono.from(RxJava2Adapter.singleToMono(Single.wrap(Single.just(1)))));
  }

  Maybe<Integer> testMaybeJust() {
    return RxJava2Adapter.monoToMaybe(Mono.just(1));
  }

  Maybe<Integer> testMaybeWrap() {
    return Maybe.just(1);
  }

  Maybe<String> testMaybeAmbWith() {
    return Maybe.just("foo")
        .as(RxJava2Adapter::maybeToMono)
        .or(Maybe.just("bar").as(RxJava2Adapter::maybeToMono))
        .as(RxJava2Adapter::monoToMaybe);
  }

  Maybe<String> testMaybeCastPositive() {
    return Maybe.just("string");
  }

  Maybe<Object> testMaybeCastNegative() {
    return Maybe.just("string").cast(Object.class);
  }

  Maybe<Integer> testMaybeDoOnError() {
    return Maybe.just(1).doOnError(System::out::println);
  }

  Maybe<Integer> testMaybeDoOnSuccess() {
    return Maybe.just(1).doOnSuccess(System::out::println);
  }

  Maybe<Integer> testMaybeFilter() {
    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .filter(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 1))
        .as(RxJava2Adapter::monoToMaybe);
  }

  // XXX: This should be fixed later with `Refaster.canBeCoercedTo(...)`
  Maybe<Integer> testMaybeFlatMapFunction() {
    Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .flatMap(
            v ->
                RxJava2Adapter.maybeToMono(
                    Maybe.wrap(
                        (Maybe<Integer>)
                            RxJavaReactorMigrationUtil.toJdkFunction(this::exampleMethod)
                                .apply(v))))
        .as(RxJava2Adapter::monoToMaybe);

    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .flatMap(
            v ->
                RxJava2Adapter.maybeToMono(
                    Maybe.wrap(
                        (Maybe<Integer>)
                            RxJavaReactorMigrationUtil.toJdkFunction(exampleFunction()).apply(v))))
        .as(RxJava2Adapter::monoToMaybe);
  }

  private io.reactivex.functions.Function<Integer, Maybe<Integer>> exampleFunction() {
    return null;
  }

  Maybe<Integer> testMaybeFlatMapLambda() {
    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .flatMap(i -> Maybe.just(i * 2).as(RxJava2Adapter::maybeToMono))
        .as(RxJava2Adapter::monoToMaybe);
  }

  Maybe<Integer> testMaybeFlatMapMethodReference() {
    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .flatMap(
            v ->
                RxJava2Adapter.maybeToMono(
                    Maybe.wrap(
                        (Maybe<Integer>)
                            RxJavaReactorMigrationUtil.toJdkFunction(this::exampleMethod)
                                .apply(v))))
        .as(RxJava2Adapter::monoToMaybe);
  }

  private Maybe<Integer> exampleMethod(Integer x) {
    return null;
  }

  Maybe<Integer> testMaybeFlatMapSingleElement() {
    return Maybe.just(1).flatMapSingleElement(Single::just);
  }

  Completable testMaybeIgnoreElement() {
    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .ignoreElement()
        .as(RxJava2Adapter::monoToCompletable);
  }

  Maybe<String> testMaybeMap() {
    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .map(RxJavaReactorMigrationUtil.toJdkFunction(String::valueOf))
        .as(RxJava2Adapter::monoToMaybe);
  }

  Single<Integer> testMaybeSwitchIfEmpty() {
    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .switchIfEmpty(
            RxJava2Adapter.singleToMono(
                Single.wrap(
                    Single.<Integer>error(
                        () -> {
                          throw new IllegalStateException();
                        }))))
        .as(RxJava2Adapter::monoToSingle);
  }
}
