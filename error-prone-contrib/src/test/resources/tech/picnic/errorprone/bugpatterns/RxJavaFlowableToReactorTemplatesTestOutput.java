package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
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
import tech.picnic.errorprone.refastertemplates.RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil;

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
        Flux.defer(
            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.callableAsSupplier(
                () -> Flowable.just(1))));
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
            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.callableAsSupplier(
                () -> {
                  throw new IllegalStateException();
                })));
  }

  Flowable<Integer> testFlowableFromArray() {
    return Flowable.fromArray(1, 2, 3);
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

  Completable testFlowableFlatMapCompletable() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .flatMap(
                e ->
                    RxJava2Adapter.completableToMono(
                        Completable.wrap(
                            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkFunction(
                                    integer -> Completable.complete())
                                .apply(e))))
            .then());
  }

  Flowable<Object> testFlowableFlatMap() {
    Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .flatMap(RxJava2ReactorMigrationUtil.toJdkFunction(this::exampleMethod2))
        .as(RxJava2Adapter::fluxToFlowable);
    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .flatMap(RxJava2ReactorMigrationUtil.toJdkFunction(i -> ImmutableSet::of))
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

  Flowable<?> testFlowableRangeLong() {
    return RxJava2Adapter.fluxToFlowable(Flux.range(1, 10));
  }

  Flowable<Integer> testFlowableZip() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.zip(
            Flowable.just(1),
            Flowable.just(2),
            RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkBiFunction(
                (i1, i2) -> i1 + i2)));
  }

  Flowable<Integer> testFlowableBiFunctionRemoveUtil() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.zip(Flowable.just(1), Flowable.just(2), (i1, i2) -> i1 + i2));
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
    return RxJava2Adapter.fluxToFlowable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .mergeWith(RxJava2Adapter.singleToMono(Single.wrap(Single.just(1)))));
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
