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
        .as(RxJava2Adapter::fluxToFlowable)
        .as(RxJava2Adapter::flowableToFlux)
        .flatMap(e -> ImmutableSet::of)
        .as(RxJava2Adapter::fluxToFlowable);

    return Flux.just(2).as(RxJava2Adapter::fluxToFlowable).as(RxJava2Adapter::flowableToFlux);
  }

  Maybe<String> testRemoveRedundantCast() {
    return (Maybe<String>) Maybe.just("foo");
  }

  Mono<Integer> testMonoToFlowableToMono() {
    Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .map(e -> e + e)
        .as(RxJava2Adapter::monoToSingle)
        .as(RxJava2Adapter::singleToMono)
        .filter(i -> i > 2)
        .as(RxJava2Adapter::monoToSingle);

    Mono.empty().then().as(RxJava2Adapter::monoToCompletable).as(RxJava2Adapter::completableToMono);

    return Mono.just(3).as(RxJava2Adapter::monoToMaybe).as(RxJava2Adapter::maybeToMono);
  }
}
