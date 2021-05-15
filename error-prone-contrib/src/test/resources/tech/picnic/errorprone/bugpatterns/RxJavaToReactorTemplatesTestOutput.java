package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Flowable;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import io.reactivex.Maybe;

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

//  ImmutableSet<Maybe<Integer>> testFlowableFirstElementInReactor() {
//    return ImmutableSet.of(
//        Flowable.just(1).as(RxJava2Adapter::flowableToFlux).next().as(RxJava2Adapter::monoToMaybe),
//        Flowable.empty().as(RxJava2Adapter::flowableToFlux).next().as(RxJava2Adapter::monoToMaybe));
//  }

  Flux<Integer> testRemoveUnnecessaryConversion() {
    Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .map(e -> e + e)
        .flatMap(e -> ImmutableSet::of)
        .as(RxJava2Adapter::fluxToFlowable);

    return Flux.just(2);
  }
}
