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
import reactor.test.StepVerifier;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaMaybeToReactorTemplatesTest implements RefasterTemplateTestCase {

  Maybe<String> testMaybeAmb() {
    return RxJava2Adapter.monoToMaybe(
        Mono.firstWithSignal(
            Streams.stream(ImmutableList.of(Maybe.just("foo"), Maybe.just("bar")))
                .map(RxJava2Adapter::maybeToMono)
                .collect(ImmutableList.toImmutableList())));
  }

  // XXX: Template turned off for now.
  Maybe<String> testMaybeAmbArray() {
    return Maybe.ambArray(
        RxJava2Adapter.monoToMaybe(Mono.just("foo")), RxJava2Adapter.monoToMaybe(Mono.just("bar")));
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

  @SuppressWarnings("MaybeJust")
  Maybe<Object> testMaybeCastNegative() {
    return Maybe.just("string").cast(Object.class);
  }

  Maybe<Integer> testMaybeDoOnError() {
    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(System.out::println))
        .as(RxJava2Adapter::monoToMaybe);
  }

  Maybe<Integer> testMaybeDoOnSuccess() {
    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(System.out::println))
        .as(RxJava2Adapter::monoToMaybe);
  }

  Maybe<Integer> testMaybeFilter() {
    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .filter(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 1))
        .as(RxJava2Adapter::monoToMaybe);
  }

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
    return RxJava2Adapter.monoToMaybe(
        RxJava2Adapter.maybeToMono(Maybe.just(1))
            .flatMap(
                e ->
                    RxJava2Adapter.singleToMono(
                        Single.wrap(
                            RxJavaReactorMigrationUtil.toJdkFunction(Single::just).apply(e)))));
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

  Flowable<Integer> testMaybeToFlowable() {
    return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(Maybe.just(1)).flux());
  }

  @SuppressWarnings("MaybeJust")
  private Maybe<Integer> getMaybe() {
    return Maybe.just(3);
  }

  void MaybeTestAssertResultItem() throws InterruptedException {
    RxJava2Adapter.maybeToMono(Maybe.just(1))
        .as(StepVerifier::create)
        .expectNext(1)
        .verifyComplete();
    RxJava2Adapter.maybeToMono(Maybe.just(2))
        .as(StepVerifier::create)
        .expectNext(2)
        .verifyComplete();
  }

  void MaybeTestAssertResult() throws InterruptedException {
    RxJava2Adapter.maybeToMono(Maybe.just(1)).as(StepVerifier::create).verifyComplete();
  }

  void MaybeTestAssertValue() throws InterruptedException {
    RxJava2Adapter.maybeToMono(Maybe.just(1))
        .as(StepVerifier::create)
        .expectNextMatches(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 2))
        .verifyComplete();
    RxJava2Adapter.maybeToMono(Maybe.just(3))
        .as(StepVerifier::create)
        .expectNextMatches(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 4))
        .verifyComplete();
  }

  void testMaybeTestAssertComplete() throws InterruptedException {
    RxJava2Adapter.maybeToMono(Maybe.just(1)).as(StepVerifier::create).verifyComplete();
  }

  void testMaybeTestAssertErrorClass() throws InterruptedException {
    RxJava2Adapter.maybeToMono(Maybe.just(1))
        .as(StepVerifier::create)
        .expectError(InterruptedException.class)
        .verify();
  }

  void testMaybeTestAssertNoErrors() throws InterruptedException {
    RxJava2Adapter.maybeToMono(Maybe.just(1)).as(StepVerifier::create).verifyComplete();
  }

  void testMaybeTestAssertValueCount() throws InterruptedException {
    RxJava2Adapter.maybeToMono(Maybe.just(1))
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();
  }
}
