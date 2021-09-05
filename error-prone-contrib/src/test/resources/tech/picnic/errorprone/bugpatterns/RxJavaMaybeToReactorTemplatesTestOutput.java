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
import tech.picnic.errorprone.refastertemplates.RxJavaToReactorTemplates;

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
    return Maybe.concatArray(Maybe.just(1), Maybe.just(2), Maybe.empty());
  }

  Mono<String> testMaybeDefer() {
    return Mono.defer(() -> Maybe.just("test").as(RxJava2Adapter::maybeToMono));
  }

  Maybe<Integer> testMaybeEmpty() {
    return RxJava2Adapter.monoToMaybe(Mono.empty());
  }

  Maybe<Object> testMaybeFromCallable() {
    return RxJava2Adapter.monoToMaybe(
        Mono.fromSupplier(
            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.callableAsSupplier(
                () -> {
                  String s = "foo";
                  return null;
                })));
  }

  Maybe<Integer> testMaybeFromFuture() {
    return RxJava2Adapter.monoToMaybe(Mono.fromFuture(new CompletableFuture<>()));
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

  // XXX: This should be fixed later with `Refaster.canBeCoercedTo(...)`
  Maybe<Integer> testMaybeFlatMapFunction() {
    Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .flatMap(
            v ->
                RxJava2Adapter.maybeToMono(
                    Maybe.wrap(
                        (Maybe<Integer>)
                            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkFunction(
                                    this::exampleMethod)
                                .apply(v))))
        .as(RxJava2Adapter::monoToMaybe);

    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .flatMap(
            v ->
                RxJava2Adapter.maybeToMono(
                    Maybe.wrap(
                        (Maybe<Integer>)
                            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkFunction(
                                    exampleFunction())
                                .apply(v))))
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
                            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkFunction(
                                    this::exampleMethod)
                                .apply(v))))
        .as(RxJava2Adapter::monoToMaybe);
  }

  Completable testMaybeIgnoreElement() {
    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .ignoreElement()
        .as(RxJava2Adapter::monoToCompletable);
  }

  Single<Integer> testMaybeSwitchIfEmpty() {
    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .switchIfEmpty(
            Single.<Integer>error(
                    () -> {
                      throw new IllegalStateException();
                    })
                .as(RxJava2Adapter::singleToMono))
        .as(RxJava2Adapter::monoToSingle);
  }
}
