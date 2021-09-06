package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import tech.picnic.errorprone.refastertemplates.RxJavaToReactorTemplates;

final class RxJavaFlowableToReactorTemplatesTest implements RefasterTemplateTestCase {

  Flowable<Integer> testFlowableCombineLatest() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.combineLatest(
            Flowable.just(1),
            Flowable.just(2),
            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkBiFunction(Integer::sum)));
  }

  Flowable<Integer> testFlowableConcatWithPublisher() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .concatWith(Flowable.just(2))
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Flowable<Integer> testFlowableDefer() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.defer(() -> Flowable.just(1).as(RxJava2Adapter::flowableToFlux)));
  }

  Flowable<Object> testFlowableEmpty() {
    return RxJava2Adapter.fluxToFlowable(Flux.empty());
  }

  Flowable<Object> testFlowableErrorThrowable() {
    return RxJava2Adapter.fluxToFlowable(Flux.error(new IllegalStateException()));
  }

  Flowable<Object> testFlowableErrorCallable() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.error(
            () -> {
              throw new IllegalStateException();
            }));
  }

  Flowable<Integer> testFlowableFromArray() {
    return RxJava2Adapter.fluxToFlowable(Flux.fromArray(1, 2, 3));
  }

  Flowable<Integer> testFlowableFromCallable() {
    return RxJava2Adapter.monoToFlowable(Mono.fromCallable(() -> 1));
  }

  Flowable<Integer> testFlowableFromIterable() {
    return RxJava2Adapter.fluxToFlowable(Flux.fromIterable(ImmutableList.of(1, 2, 3)));
  }

  Flowable<Integer> testFlowableFromPublisher() {
    return RxJava2Adapter.fluxToFlowable(Flux.from(Flowable.just(1)));
  }

  Flowable<Integer> testFlowableFilter() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .filter(RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkPredicate(i -> i > 2))
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Maybe<Integer> testFlowableFirstElement() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .next()
        .as(RxJava2Adapter::monoToMaybe);
  }

  Single<Integer> testFlowableFirstOrError() {
    return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(Flowable.just(1)).next());
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

  private Maybe<Integer> exampleMethod(Integer x) {
    return null;
  }

  private Flowable<Integer> exampleMethod2(Integer x) {
    return null;
  }

  ImmutableList<Flowable<Integer>> testFlowableJust() {
    return ImmutableList.of(
        RxJava2Adapter.fluxToFlowable(Flux.just(1)),
        RxJava2Adapter.fluxToFlowable(Flux.just(1, 2)));
  }

  Flowable<Integer> testFlowableRange() {
    return RxJava2Adapter.fluxToFlowable(Flux.range(1, 10));
  }

  Flowable<Integer> testFlowableZip() {
    return RxJava2Adapter.flowableToFlux(
        Flux.zip(
            Flowable.just(1),
            Flowable.just(2),
            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkBiFunction(
                (i1, i2) -> i1 + i2)));
  }

  Single<Boolean> testFlowableAll() {
    return Flowable.just(true, true)
        .as(RxJava2Adapter::flowableToFlux)
        .all(
            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkPredicate(
                Boolean::booleanValue))
        .as(RxJava2Adapter::monoToSingle);
  }

  Single<Boolean> testFlowableAny() {
    return Flowable.just(true, true)
        .as(RxJava2Adapter::flowableToFlux)
        .any(
            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkPredicate(
                Boolean::booleanValue))
        .as(RxJava2Adapter::monoToSingle);
  }

  Object testFlowableBlockingFirst() {
    return RxJava2Adapter.flowableToFlux(Flowable.just(1)).blockFirst();
  }

  Flowable<Integer> testFlowableMap() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .map(i -> i + 1)
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Flowable<Integer> testFlowableMergeWith() {
    return RxJava2Adapter.flowableToFlux(Flowable.just(1)).mergeWith(Single.just(1).toFlowable());
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

  Single<Map<Boolean, Integer>> testFlowableToMap() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .collectMap(i -> i > 1)
        .as(RxJava2Adapter::monoToSingle);
  }
}
