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
    return Flowable.just(1).flatMap(i -> ImmutableSet::of);
  }

  Flowable<Integer> testFlowableFilterInReactor() {
    return Flowable.just(1).filter(i -> i > 2);
  }

    ImmutableSet<Flowable<Integer>> testFlowableFirstElementInReactor() {
      return ImmutableSet.of(
              Flowable<Integer>.toMaybe(::evenFilter).firstElement(),
              Maybe.<Integer>empty().toFlowable().firstElement());
    }

  Single<Integer> testMaybeSwitchIfEmptyInReactor() {
    return Maybe.just(1)
        .switchIfEmpty(
            Single.error(
                () -> {
                  throw new IllegalStateException();
                }));
  }

  Flowable<Integer> testFlowableSwitchIfEmptyInReactor() {
    return Flowable.just(1)
        .switchIfEmpty(
            Flowable.error(
                () -> {
                  throw new IllegalStateException();
                }));
  }

  Flux<Integer> testRemoveUnnecessaryConversion() {
    Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .map(e -> e + e)
        .as(RxJava2Adapter::fluxToFlowable)
        .as(RxJava2Adapter::flowableToFlux)
        .flatMap(e -> ImmutableSet::of)
        .as(RxJava2Adapter::fluxToFlowable);

    return Flux.just(2).as(RxJava2Adapter::fluxToFlowable).as(RxJava2Adapter::flowableToFlux);
  }
}
