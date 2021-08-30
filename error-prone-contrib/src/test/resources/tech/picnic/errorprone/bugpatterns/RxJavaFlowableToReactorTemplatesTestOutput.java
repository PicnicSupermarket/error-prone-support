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

  Flowable<Integer> testFlowableMap() {
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .map(i -> i + 1)
        .as(RxJava2Adapter::fluxToFlowable);
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
