package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class RxJavaToReactorTemplatesTest implements RefasterTemplateTestCase {
  Flowable<Object> testFlowableFlatMapInReactor() { // look at the return type...
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .flatMap(i -> ImmutableSet::of)
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Flowable<Integer> testFlowableFilterInReactor() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .filter(i -> i > 2)
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Maybe<Integer> testFlowableFirstElementInReactor() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .next()
        .as(RxJava2Adapter::monoToMaybe);
  }

  Single<Integer> testMaybeSwitchIfEmptyInReactor() {
    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .switchIfEmpty(
            Mono.error(
                () -> {
                  throw new IllegalStateException();
                }))
        .as(RxJava2Adapter::monoToSingle);
  }

  Flowable<Integer> testFlowableSwitchIfEmptyInReactor() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .switchIfEmpty(
            Flux.error(
                () -> {
                  throw new IllegalStateException();
                }))
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Flux<Integer> testRemoveUnnecessaryConversion() {
    Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .map(e -> e + e)
        .flatMap(e -> ImmutableSet::of)
        .as(RxJava2Adapter::fluxToFlowable);

    return Flux.just(2);
  }
}
