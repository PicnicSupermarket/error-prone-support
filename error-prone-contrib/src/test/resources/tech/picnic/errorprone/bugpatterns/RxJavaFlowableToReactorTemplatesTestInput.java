package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaFlowableToReactorTemplatesTest implements RefasterTemplateTestCase {
  Completable testRandomness() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .flatMap(
                e ->
                    RxJava2Adapter.completableToMono(
                        Completable.wrap(
                            RxJavaReactorMigrationUtil.<Integer, Completable>toJdkFunction(
                                    v -> RxJava2Adapter.monoToCompletable(Mono.empty()))
                                .apply(e))))
            .then());
  }

  Flowable<Integer> testFlowableAmbArray() {
    return Flowable.ambArray(Flowable.just(1), Flowable.just(2));
  }

  Flowable<Integer> testFlowableCombineLatest() {
    return Flowable.combineLatest(Flowable.just(1), Flowable.just(2), Integer::sum);
  }

  Flowable<Integer> testFlowableConcatWithPublisher() {
    return Flowable.just(1).concatWith(Flowable.just(2));
  }

  Flowable<Integer> testFlowableDefer() {
    return Flowable.defer(() -> Flowable.just(1));
  }

  Flowable<Object> testFlowableEmpty() {
    return Flowable.empty();
  }

  Flowable<Object> testFlowableErrorThrowable() {
    return Flowable.error(new IllegalStateException());
  }

  Flowable<Object> testFlowableErrorCallable() {
    return Flowable.error(
        () -> {
          throw new IllegalStateException();
        });
  }

  Flowable<Integer> testFlowableFromArray() {
    return Flowable.fromArray(1, 2, 3);
  }

  Flowable<Integer> testFlowableFromCallable() {
    return Flowable.fromCallable(() -> 1);
  }

  Flowable<Integer> testFlowableFromIterable() {
    return Flowable.fromIterable(ImmutableList.of(1, 2, 3));
  }

  Flowable<Integer> testFlowableFromPublisher() {
    return Flowable.fromPublisher(Flowable.just(1));
  }

  Flowable<Integer> testFlowableFilter() {
    return Flowable.just(1).filter(i -> i > 2);
  }

  Flowable<Integer> testFlowableDistinct() {
    return Flowable.just(1).distinct();
  }

  Maybe<Integer> testFlowableFirstElement() {
    return Flowable.just(1).firstElement();
  }

  Single<Integer> testFlowableFirstOrError() {
    return Flowable.just(1).firstOrError();
  }

  Completable testFlowableFlatMapCompletable() {
    return Flowable.just(1).flatMapCompletable(integer -> Completable.complete());
  }

  Flowable<Object> testFlowableFlatMap() {
    Flowable.just(1).flatMap(this::exampleMethod2);
    return Flowable.just(1).flatMap(i -> ImmutableSet::of);
  }

  private Maybe<Integer> exampleMethod(Integer x) {
    return null;
  }

  private Flowable<Integer> exampleMethod2(Integer x) {
    return null;
  }

  ImmutableList<Flowable<Integer>> testFlowableJust() {
    return ImmutableList.of(Flowable.just(1), Flowable.just(1, 2));
  }

  Flowable<Integer> testFlowableRange() {
    return Flowable.range(1, 10);
  }

  Flowable<?> testFlowableRangeLong() {
    return Flowable.rangeLong(1, 10);
  }

  Flowable<Integer> testFlowableZip() {
    return Flowable.zip(Flowable.just(1), Flowable.just(2), (i1, i2) -> i1 + i2);
  }

  Flowable<Integer> testFlowableBiFunctionRemoveUtil() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.zip(
            Flowable.just(1),
            Flowable.just(2),
            RxJavaReactorMigrationUtil.toJdkBiFunction((i1, i2) -> i1 + i2)));
  }

  Single<Boolean> testFlowableAll() {
    return Flowable.just(true, true).all(Boolean::booleanValue);
  }

  Single<Boolean> testFlowableAny() {
    return Flowable.just(true, true).any(Boolean::booleanValue);
  }

  Object testFlowableBlockingFirst() {
    return Flowable.just(1).blockingFirst();
  }

  Flowable<Integer> testFlowableMap() {
    return Flowable.just(1).map(i -> i + 1);
  }

  Flowable<Integer> testFlowableMergeWith() {
    return Flowable.just(1).mergeWith(Single.just(1));
  }

  Single<Integer> testFlowableSingleDefault() {
    return Flowable.just(1).single(2);
  }

  Maybe<Integer> testFlowableSingleElement() {
    return Flowable.just(1).singleElement();
  }

  Single<Integer> testFlowableSingleOrError() {
    return Flowable.just(1).singleOrError();
  }

  Flowable<Integer> testFlowableSorted() {
    return Flowable.just(1).sorted();
  }

  Flowable<Integer> testFlowableSortedComparator() {
    return Flowable.just(1).sorted((i1, i2) -> 0);
  }

  Flowable<Integer> testFlowableSwitchIfEmptyPublisher() {
    return Flowable.just(1)
        .switchIfEmpty(
            Flowable.error(
                () -> {
                  throw new IllegalStateException();
                }));
  }

  Single<List<Integer>> testFlowableToList() {
    return Flowable.just(1, 2).toList();
  }

  Single<Map<Boolean, Integer>> testFlowableToMap() {
    return Flowable.just(1).toMap(i -> i > 1);
  }

  void testFlowableTestAssertResultItem() throws InterruptedException {
    Flowable.just(1).test().await().assertResult(1);
    Flowable.just(2).test().await().assertValue(2);
  }

  void testFlowableTestAssertResult() throws InterruptedException {
    Flowable.just(1).test().await().assertResult();
  }

  void testFlowableTestAssertValue() throws InterruptedException {
    Flowable.just(1).test().await().assertValue(i -> i > 2);
    Flowable.just(3).test().await().assertValue(i -> i > 4).assertComplete();
  }

  void testFlowableTestAssertResultValues() throws InterruptedException {
    Flowable.just(1, 2, 3).test().await().assertResult(1, 2, 3);
    Flowable.just(4, 5, 6).test().await().assertValues(4, 5, 6);
  }

  void testFlowableTestAssertComplete() throws InterruptedException {
    Flowable.just(1).test().await().assertComplete();
  }

  void testFlowableTestAssertErrorClass() throws InterruptedException {
    Flowable.just(1).test().await().assertError(InterruptedException.class);
  }

  void testFlowableTestAssertNoErrors() throws InterruptedException {
    Flowable.just(1).test().await().assertNoErrors();
  }

  void testFlowableTestAssertValueCount() throws InterruptedException {
    Flowable.just(1).test().await().assertValueCount(1);
  }
}
