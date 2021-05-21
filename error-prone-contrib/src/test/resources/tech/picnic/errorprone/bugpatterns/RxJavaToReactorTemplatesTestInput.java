package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class RxJavaToReactorTemplatesTest implements RefasterTemplateTestCase {
  Flowable<Integer> testFlowableFilter() {
    return Flowable.just(1).filter(i -> i > 2);
  }

  Maybe<Integer> testFlowableFirstElement() {
    return Flowable.just(1).firstElement();
  }

  Flowable<Object> testFlowableFlatMap() {
    Flowable.just(1).flatMap(this::exampleMethod2);
    return Flowable.just(1).flatMap(i -> ImmutableSet::of);
  }

  Flowable<Integer> testFlowableMap() {
    return Flowable.just(1).map(i -> i + 1);
  }

  Single<Map<Boolean, Integer>> testFlowableToMap() {
    return Flowable.just(1).toMap(i -> i > 1);
  }

  Flowable<Integer> testFlowableSwitchIfEmptyPublisher() {
    return Flowable.just(1)
        .switchIfEmpty(
            Flowable.error(
                () -> {
                  throw new IllegalStateException();
                }));
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

  // XXX: This one doesn't work. Need to add suppor for this.
  Maybe<Integer> testMaybeFlatMapMethodReference() {
    return Maybe.just(1).flatMap(this::exampleMethod);
  }

  private Maybe<Integer> exampleMethod(Integer x) {
    return null;
  }

  private Flowable<Integer> exampleMethod2(Integer x) {
    return null;
  }

  Maybe<String> testCast() {
    return (Maybe<String>) Maybe.just("foo");
  }

  Maybe<String> testCast2() {
    return Maybe.just("string").cast(String.class);
  }

  Maybe<Object> testCast3() {
    return Maybe.just("string").cast(Object.class);
  }

  Completable testMaybeIgnoreElement() {
    return Maybe.just(1).ignoreElement();
  }

  Single<Integer> testMaybeSwitchIfEmpty() {
    return Maybe.just(1)
        .switchIfEmpty(
            Single.<Integer>error(
                () -> {
                  throw new IllegalStateException();
                }));
  }

  Maybe<Integer> testSingleFilter() {
    return Single.just(1).filter(i -> i > 2);
  }

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
