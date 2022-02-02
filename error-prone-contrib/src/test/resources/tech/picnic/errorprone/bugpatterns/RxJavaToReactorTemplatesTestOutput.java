package tech.picnic.errorprone.bugpatterns;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
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

  // This one doesnt work
  Maybe<String> testRemoveRedundantCast() {
    return (Maybe<String>) Maybe.just("foo");
  }

  Mono<Integer> testMonoErrorCallableSupplierUtil() {
    return Mono.just(1).switchIfEmpty(Mono.error(() -> new IllegalStateException()));
  }

  Maybe<Integer> testRemoveUtilCallable() {
    return RxJava2Adapter.monoToMaybe(
        Mono.fromSupplier(
            () -> {
              String s = "foo";
              return null;
            }));
  }

  Flowable<String> testUnnecessaryFunctionConversion() {
    Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .map(String::valueOf)
        .as(RxJava2Adapter::fluxToFlowable);

    return Flowable.just(1)
        .as(RxJava2Adapter::flowableToFlux)
        .map(e -> String.valueOf(e))
        .as(RxJava2Adapter::fluxToFlowable);
  }

  Flowable<Integer> testUnnecessaryBiFunctionConversion() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.<Integer, Integer, Integer>zip(
            Flowable.just(1), Flowable.just(2), (i1, i2) -> i1 + i2));
  }

  Single<Integer> testUnnecessaryConsumerConversion() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .doOnSuccess(System.out::println)
        .as(RxJava2Adapter::monoToSingle);
  }

  Maybe<Integer> testUnnecessaryPredicateConversion() {
    return Single.just(1)
        .as(RxJava2Adapter::singleToMono)
        .filter(i -> i > 2)
        .as(RxJava2Adapter::monoToMaybe);
  }

  Mono<Integer> testMonoFromNestedPublisher() {
    return Mono.from(Flux.just(1));
  }

  Mono<Integer> testMonoThen() {
    return Mono.just(1).then(Mono.just(2));
  }

  Flux<Integer> testMonoThenMany() {
    return Mono.just(1).thenMany(Flux.just(1));
  }

  Mono<Void> testFluxThen() {
    return Flux.just(1).then();
  }

  Mono<List<Integer>> testMonoCollectToImmutableList() {
    return Flux.just(1).collect(toImmutableList());
  }

  Mono<Integer> testMonoDefaultIfEmpty() {
    return Mono.just(1).defaultIfEmpty(2);
  }

  Flux<Integer> testFluxDefaultIfEmpty() {
    return Flux.just(1).defaultIfEmpty(2);
  }

  Mono<Void> testMonoVoid() {
    return Mono.when(Flux.just(1));
  }

  Flux<Object> testFlatMapFluxFromArray() {
    Flux<String[]> test = null;
    return test.flatMap(Flux::fromArray);
  }

  Mono<ImmutableSet<Integer>> testFluxToImmutableSet() {
    return Flux.just(1).collect(toImmutableSet());
  }

  Integer testMonoBlock() {
    return Mono.just(1).block(Duration.ofMillis(1));
  }

  ImmutableList<Integer> testFluxCollectBlock() {
    return Flux.just(1).collect(toImmutableList()).block();
  }
}
