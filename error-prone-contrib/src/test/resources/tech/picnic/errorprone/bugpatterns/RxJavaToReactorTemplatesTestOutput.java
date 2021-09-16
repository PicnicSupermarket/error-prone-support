package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class RxJavaToReactorTemplatesTest implements RefasterTemplateTestCase {

  Flux<Integer> testFluxToFlowableToFlux() {
    Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .map(e -> e + e)
        .flatMap(e -> ImmutableSet::of)
        .as(RxJava2Adapter::fluxToFlowable);

    return Flux.just(2);
  }

  Mono<Integer> testMonoToFlowableToMono() {
    Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .map(e -> e + e)
        .filter(i -> i > 2)
        .as(RxJava2Adapter::monoToSingle);

    Mono.empty().then();

    return Mono.just(3);
  }

  Maybe<String> testRemoveRedundantCast() {
    return Maybe.just("foo");
  }

  Mono<Integer> testMonoErrorCallableSupplierUtil() {
    return Mono.just(1).switchIfEmpty(Mono.error(() -> new IllegalStateException()));
  }

  Maybe<Integer> testRemoveUtilCallable() {
    return RxJava2Adapter.monoToMaybe(
        Mono.fromSupplier(
            () -> {
              String s = "foo";
              return null;
            }));
  }

  Flowable<Object> testUnnecessaryFunctionConversion() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .flatMap(i -> ImmutableSet::of)
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Flowable<Integer> testUnnecessaryBiFunctionConversion() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.zip(Flowable.just(1), Flowable.just(2), (i1, i2) -> i1 + i2));
  }

  Single<Integer> testUnnecessaryConsumerConversion() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .doOnSuccess(System.out::println)
        .as(RxJava2Adapter::monoToSingle);
  }

  Maybe<Integer> testUnnecessaryPredicateConversion() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .filter(i -> i > 2)
        .as(RxJava2Adapter::monoToMaybe);
  }
}
