package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaToReactorTemplatesTest implements RefasterTemplateTestCase {

  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(RxJavaReactorMigrationUtil.class);
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

  // This one doesnt work
  Maybe<String> testRemoveRedundantCast() {
    return (Maybe<String>) Maybe.just("foo");
  }

  Mono<Integer> testMonoErrorCallableSupplierUtil() {
    return Mono.just(1)
        .switchIfEmpty(
            Mono.error(
                RxJavaReactorMigrationUtil.callableAsSupplier(() -> new IllegalStateException())));
  }

  Maybe<Integer> testRemoveUtilCallable() {
    return RxJava2Adapter.monoToMaybe(
        Mono.fromSupplier(
            RxJavaReactorMigrationUtil.callableAsSupplier(
                () -> {
                  String s = "foo";
                  return null;
                })));
  }

  Flowable<String> testUnnecessaryFunctionConversion() {
    Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .map(RxJavaReactorMigrationUtil.<Integer, String>toJdkFunction(String::valueOf))
        .as(RxJava2Adapter::fluxToFlowable);

    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .map(RxJavaReactorMigrationUtil.toJdkFunction(e -> String.valueOf(e)))
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Flowable<Integer> testUnnecessaryBiFunctionConversion() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.zip(
            Flowable.just(1),
            Flowable.just(2),
            RxJavaReactorMigrationUtil.toJdkBiFunction((i1, i2) -> i1 + i2)));
  }

  Single<Integer> testUnnecessaryConsumerConversion() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(System.out::println))
        .as(RxJava2Adapter::monoToSingle);
  }

  Maybe<Integer> testUnnecessaryPredicateConversion() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .filter(RxJavaReactorMigrationUtil.toJdkPredicate(i -> i > 2))
        .as(RxJava2Adapter::monoToMaybe);
  }

  Mono<Integer> testMonoFromNestedPublisher() {
    return Mono.from(RxJava2Adapter.fluxToFlowable(Flux.just(1)));
  }

  Mono<Integer> testMonoThen() {
    return Mono.just(1).then().then(Mono.just(2));
  }

  Flux<Integer> testMonoThenMany() {
    return Mono.just(1).then().thenMany(Flux.just(1));
  }

  Mono<Void> testFluxThen() {
    return Flux.just(1).ignoreElements().then();
  }

  Mono<List<Integer>> testMonoCollectToImmutableList() {
    return Flux.just(1).collectList();
  }

  Mono<Integer> testMonoDefaultIfEmpty() {
    return Mono.just(1).switchIfEmpty(Mono.just(2));
  }

  Flux<Integer> testFluxDefaultIfEmpty() {
    return Flux.just(1).switchIfEmpty(Flux.just(2));
  }

  Mono<Void> testMonoVoid() {
    return Mono.when(Flux.just(1)).then();
  }

  Flux<Object> testFlatMapFluxFromArray() {
    Flux<String[]> test = null;
    return test.flatMap(Flowable::fromArray);
  }

  Mono<ImmutableSet<Integer>> testFluxToImmutableSet() {
    return Flux.just(1).collect(toImmutableList()).map(ImmutableSet::copyOf);
  }

  Integer testMonoBlock() {
    return Mono.just(1).timeout(Duration.ofMillis(1)).block();
  }

  ImmutableList<Integer> testFluxCollectBlock() {
    return ImmutableList.copyOf(Flux.just(1).toIterable());
  }

  ImmutableSet<Flux<String>> testConcatMapIterable() {
    return ImmutableSet.of(
        Flux.just(ImmutableList.of("1")).flatMap(Flux::fromIterable),
        Flux.just(ImmutableList.of("2")).concatMap(Flux::fromIterable));
  }

  Mono<Map<Integer, Integer>> testCollectToImmutableMap() {
    return Flux.just(1).collectMap(i -> i);
  }
}
