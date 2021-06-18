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
import tech.picnic.errorprone.refastertemplates.RxJavaToReactorTemplates;

final class RxJavaToReactorTemplatesTest implements RefasterTemplateTestCase {
  Maybe<String> testRemoveRedundantCast() {
    return Maybe.just("foo");
  }

  Maybe<String> testMaybeCastPositive() {
    return Maybe.just("string");
  }

  Maybe<Object> testMaybeCastNegative() {
    return Maybe.just("string").cast(Object.class);
  }

  Maybe<Integer> testMaybeWrap() {
    return Maybe.just(1);
  }

  // XXX: Discuss with Stephan, look at the Publisher which is of type Flowable, that won't work...
  Flowable<Integer> testFlowableConcatWithPublisher() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .concatWith(Flowable.just(2))
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Flowable<Integer> testFlowableFilter() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .filter(i -> i > 2)
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Maybe<Integer> testFlowableFirstElement() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .next()
        .as(RxJava2Adapter::monoToMaybe);
  }

  Flowable<Object> testFlowableFlatMap() {
    Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .flatMap(this::exampleMethod2)
        .as(RxJava2Adapter::fluxToFlowable);
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .flatMap(i -> ImmutableSet::of)
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Flowable<Integer> testFlowableMap() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .map(i -> i + 1)
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Single<Map<Boolean, Integer>> testFlowableToMap() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .collectMap(i -> i > 1)
        .as(RxJava2Adapter::monoToSingle);
  }

  Flowable<Integer> testFlowableSwitchIfEmptyPublisher() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .switchIfEmpty(
            Flowable.error(
                () -> {
                  throw new IllegalStateException();
                }))
        .as(RxJava2Adapter::fluxToFlowable);
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
                            RxJavaToReactorTemplates.MyUtil.convert(this::exampleMethod).apply(v))))
        .as(RxJava2Adapter::monoToMaybe);

    return Maybe.just(1)
        .as(RxJava2Adapter::maybeToMono)
        .flatMap(
            v ->
                RxJava2Adapter.maybeToMono(
                    Maybe.wrap(
                        (Maybe<Integer>)
                            RxJavaToReactorTemplates.MyUtil.convert(exampleFunction()).apply(v))))
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
                            RxJavaToReactorTemplates.MyUtil.convert(this::exampleMethod).apply(v))))
        .as(RxJava2Adapter::monoToMaybe);
  }

  private Maybe<Integer> exampleMethod(Integer x) {
    return null;
  }

  private Flowable<Integer> exampleMethod2(Integer x) {
    return null;
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

  Maybe<Integer> testSingleFilter() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .filter(i -> i > 2)
        .as(RxJava2Adapter::monoToMaybe);
  }

  Single<Integer> testSingleFlatMapLambda() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .flatMap(i -> Single.just(i * 2).as(RxJava2Adapter::singleToMono))
        .as(RxJava2Adapter::monoToSingle);
  }

  Single<Integer> testSingleMap() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .map(i -> i + 1)
        .as(RxJava2Adapter::monoToSingle);
  }

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
}
