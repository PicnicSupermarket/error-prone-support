package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Map;

final class RxJavaFlowableToReactorTemplatesTest implements RefasterTemplateTestCase {

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
  
  Flowable<Integer> testFlowableFromIterable() {
    return Flowable.fromIterable(ImmutableList.of(1, 2, 3));
  }
  
  Flowable<Integer> testFlowableFromPublisher() { 
    return Flowable.fromPublisher(Flowable.just(1));
  }

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

  private Maybe<Integer> exampleMethod(Integer x) {
    return null;
  }

  private Flowable<Integer> exampleMethod2(Integer x) {
    return null;
  }

  ImmutableList<Flowable<Integer>> testFlowableJust() {
    return ImmutableList.of(Flowable.just(1), Flowable.just(1, 2));
  }

  Flowable<Integer> testFlowableMap() {
    return Flowable.just(1).map(i -> i + 1);
  }

  Flowable<Integer> testFlowableSwitchIfEmptyPublisher() {
    return Flowable.just(1)
        .switchIfEmpty(
            Flowable.error(
                () -> {
                  throw new IllegalStateException();
                }));
  }

  Single<Map<Boolean, Integer>> testFlowableToMap() {
    return Flowable.just(1).toMap(i -> i > 1);
  }
}
