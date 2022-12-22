package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaToReactorUnwrapTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        CompletableSource.class, Function.class, RxJavaReactorMigrationUtil.class, List.class);
  }

  Completable testFlowableFlatMapUnwrapLambda() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .flatMap(
                e ->
                    RxJava2Adapter.completableToMono(
                        Completable.wrap(
                            RxJavaReactorMigrationUtil.<Integer, CompletableSource>toJdkFunction(
                                    (Function<Integer, CompletableSource>)
                                        v -> RxJava2Adapter.monoToCompletable(Mono.empty()))
                                .apply(e))))
            .then());
  }

  Completable testSingleRemoveLambdaWithCast() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.singleToMono(Single.just(1))
            .flatMap(
                e ->
                    RxJava2Adapter.completableToMono(
                        Completable.wrap(
                            RxJavaReactorMigrationUtil.<Integer, Completable>toJdkFunction(
                                    (Function<Integer, Completable>)
                                        v ->
                                            RxJava2Adapter.monoToCompletable(
                                                Mono.justOrEmpty(null)))
                                .apply(e))))
            .then());
  }

  Mono<Void> testSingleRemoveLambdaWithCompletable() {
    return Flux.just(1, 2)
        .collectList()
        .flatMap(
            e ->
                RxJava2Adapter.completableToMono(
                    Completable.wrap(
                        RxJavaReactorMigrationUtil.toJdkFunction(
                                (Function<List<Integer>, CompletableSource>)
                                    u -> Completable.complete())
                            .apply(e))))
        .then();
  }

  Mono<Void> testUnwrapCompletableExtendsMono() {
    return Mono.just(1)
        .flatMap(
            e ->
                RxJava2Adapter.completableToMono(
                    Completable.wrap(
                        RxJavaReactorMigrationUtil.<Integer, CompletableSource>toJdkFunction(
                                (Integer ident) ->
                                    RxJava2Adapter.monoToCompletable(
                                        produceMessage_migrated(ident)))
                            .apply(e))))
        .then();
  }

  private Mono<Void> produceMessage_migrated(Integer integer) {
    return Mono.empty();
  }

  // Many tests for the `RxJavaUnwrapTemplates` class are not written due to time constraints.
}
