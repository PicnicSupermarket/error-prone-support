package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

public final class RxJavaUnwrapTemplates {
  private RxJavaUnwrapTemplates() {}

  abstract static class FlowableConcatMapCompletableUnwrapLambda<T> {
    @Placeholder
    abstract Mono<?> placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<? super T, ? extends Publisher<?>> before() {
      return e ->
          RxJava2Adapter.completableToMono(
              RxJavaReactorMigrationUtil.toJdkFunction(
                      (T ident) -> RxJava2Adapter.monoToCompletable(placeholder(ident)))
                  .apply(e));
    }

    @AfterTemplate
    java.util.function.Function<T, ? extends Publisher<?>> after() {
      return v -> placeholder(v);
    }
  }

  abstract static class MaybeFlatMapUnwrapLambda<I, T extends I, O> {
    @Placeholder
    abstract Mono<? extends O> placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    @SuppressWarnings("unchecked")
    java.util.function.Function<? super T, ? extends Mono<? extends O>> before() {
      return v ->
          RxJava2Adapter.maybeToMono(
              (Maybe<O>)
                  RxJavaReactorMigrationUtil.toJdkFunction(
                          (T ident) -> RxJava2Adapter.monoToMaybe(placeholder(ident)))
                      .apply(v));
    }

    @AfterTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends O>> after() {
      return v -> placeholder(v);
    }
  }

  abstract static class MaybeFlatMapSingleElementUnwrapLambda<T, R> {
    @Placeholder
    abstract Mono<R> placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<T, ? extends Mono<? extends R>> before() {
      return e ->
          RxJava2Adapter.singleToMono(
              Single.wrap(
                  RxJavaReactorMigrationUtil.toJdkFunction(
                          (Function<T, SingleSource<R>>)
                              (T ident) -> RxJava2Adapter.monoToSingle(placeholder(ident)))
                      .apply(e)));
    }

    @AfterTemplate
    java.util.function.Function<T, ? extends Mono<? extends R>> after() {
      return e -> placeholder(e);
    }
  }

  abstract static class SingleFlatMapMaybeUnwrapLambda<T, R> {
    @Placeholder
    abstract Mono<R> placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends R>> before() {
      return Refaster.anyOf(
          e ->
              RxJava2Adapter.maybeToMono(
                  Maybe.wrap(
                      RxJavaReactorMigrationUtil.toJdkFunction(
                              (Function<T, MaybeSource<R>>)
                                  (T ident) -> RxJava2Adapter.monoToMaybe(placeholder(ident)))
                          .apply(e))),
          e ->
              RxJava2Adapter.maybeToMono(
                  Maybe.wrap(
                      RxJavaReactorMigrationUtil.toJdkFunction(
                              (Function<T, MaybeSource<R>>)
                                  ident -> RxJava2Adapter.monoToMaybe(placeholder(ident)))
                          .apply(e))));
    }

    @AfterTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends R>> after() {
      return e -> placeholder(e);
    }
  }
}
