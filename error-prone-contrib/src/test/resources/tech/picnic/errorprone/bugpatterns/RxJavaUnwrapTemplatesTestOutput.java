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

final class RxJavaUnwrapTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        CompletableSource.class, Function.class, RxJavaReactorMigrationUtil.class, List.class);
  }

  Completable testFlowableFlatMapUnwrapLambda() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.flowableToFlux(Flowable.just(1))
            .flatMap(
                v ->
                    RxJava2Adapter.completableToMono(
                        Completable.wrap(RxJava2Adapter.monoToCompletable(Mono.empty()))))
            .then());
  }

  Completable testSingleRemoveLambdaWithCast() {
    return RxJava2Adapter.monoToCompletable(
        RxJava2Adapter.singleToMono(Single.just(1)).flatMap(v -> Mono.justOrEmpty(null)).then());
  }

  Mono<Void> testSingleRemoveLambdaWithCompletable() {
    return Flux.just(1, 2)
        .collectList()
        .flatMap(u -> RxJava2Adapter.completableToMono(Completable.complete()))
        .then();
  }

  // Many tests for the `RxJavaUnwrapTemplates` class are not written due to time constraints.
}
