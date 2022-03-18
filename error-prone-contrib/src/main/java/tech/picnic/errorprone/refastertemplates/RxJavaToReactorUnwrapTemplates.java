package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/** Templates to clean up nested lambdas. */
@SuppressWarnings({"Convert2MethodRef", "NoFunctionalReturnType"})
final class RxJavaToReactorUnwrapTemplates {
  private RxJavaToReactorUnwrapTemplates() {}

  // XXX: Add test
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

  // XXX: Add test
  abstract static class MaybeFlatMapUnwrapLambda<I, T extends I, O> {
    @Placeholder
    abstract Mono<? extends O> placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    @SuppressWarnings("unchecked")
    java.util.function.Function<? super T, ? extends Mono<? extends O>> before() {
      return Refaster.anyOf(
          v ->
              RxJava2Adapter.maybeToMono(
                  (Maybe<O>)
                      RxJavaReactorMigrationUtil.toJdkFunction(
                              (T ident) -> RxJava2Adapter.monoToMaybe(placeholder(ident)))
                          .apply(v)),
          v ->
              RxJava2Adapter.maybeToMono(
                  RxJavaReactorMigrationUtil.toJdkFunction(
                          (T ident) -> RxJava2Adapter.monoToMaybe(placeholder(ident)))
                      .apply(v)));
    }

    @AfterTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends O>> after() {
      return v -> placeholder(v);
    }
  }

  // XXX: Add test
  abstract static class MaybeFlatMapSingleElementUnwrapLambda<T, R> {
    @Placeholder
    abstract Mono<R> placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<T, ? extends Mono<? extends R>> before() {
      return Refaster.anyOf(
          e ->
              RxJava2Adapter.singleToMono(
                  Single.wrap(
                      RxJavaReactorMigrationUtil.toJdkFunction(
                              (Function<T, SingleSource<R>>)
                                  (T ident) -> RxJava2Adapter.monoToSingle(placeholder(ident)))
                          .apply(e))),
          e ->
              RxJava2Adapter.singleToMono(
                  Single.wrap(
                      RxJavaReactorMigrationUtil.<T, SingleSource<R>>toJdkFunction(
                              (T ident) -> RxJava2Adapter.monoToSingle(placeholder(ident)))
                          .apply(e))));
    }

    @AfterTemplate
    java.util.function.Function<T, ? extends Mono<? extends R>> after() {
      return e -> placeholder(e);
    }
  }

  // XXX: Add test
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
                          .apply(e))),
          e ->
              RxJava2Adapter.maybeToMono(
                  Maybe.wrap(
                      RxJavaReactorMigrationUtil.<T, MaybeSource<R>>toJdkFunction(
                              ident -> RxJava2Adapter.monoToMaybe(placeholder(ident)))
                          .apply(e))));
    }

    @AfterTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends R>> after() {
      return e -> placeholder(e);
    }
  }

  // XXX: Add test
  @SuppressWarnings("unchecked")
  abstract static class SingleOnResumeUnwrapLambda<T, R> {
    @Placeholder
    abstract Mono<? extends R> placeholder(@MayOptionallyUse Throwable input);

    @BeforeTemplate
    java.util.function.Function<? extends Throwable, ? extends Mono<? extends R>> before() {
      return Refaster.anyOf(
          e ->
              RxJava2Adapter.singleToMono(
                  RxJavaReactorMigrationUtil.toJdkFunction(
                          ident -> RxJava2Adapter.monoToSingle(placeholder(e)))
                      .apply(e)),
          e ->
              RxJava2Adapter.singleToMono(
                  Single.wrap(
                      RxJavaReactorMigrationUtil.toJdkFunction(
                              (Function<Throwable, ? extends SingleSource<? extends R>>)
                                  placeholder(e))
                          .apply(e))),
          e ->
              RxJava2Adapter.singleToMono(
                  Single.wrap(
                      RxJavaReactorMigrationUtil.<Throwable, SingleSource<R>>toJdkFunction(
                              (Function<Throwable, SingleSource<R>>) placeholder(e))
                          .apply(e))));
    }

    @AfterTemplate
    java.util.function.Function<Throwable, ? extends Mono<? extends R>> after() {
      return v -> placeholder(v);
    }
  }

  // XXX: Add test
  @SuppressWarnings("unchecked")
  abstract static class SingleOnResumeUnwrapLambdaSpecialCase<T, R> {
    @Placeholder
    abstract Mono<R> placeholder(@MayOptionallyUse Throwable input);

    @BeforeTemplate
    java.util.function.Function<? extends Throwable, ? extends Mono<? extends R>> before() {
      return e ->
          RxJava2Adapter.singleToMono(
              RxJavaReactorMigrationUtil.<Throwable, Single<R>>toJdkFunction(
                      t -> RxJava2Adapter.monoToSingle(placeholder(t)))
                  .apply(e));
    }

    @AfterTemplate
    java.util.function.Function<Throwable, ? extends Mono<? extends R>> after() {
      return v -> placeholder(v);
    }
  }

  // XXX: Add test
  abstract static class FlowableConcatMapMaybeDelayErrorUnwrapLambda<T, R> {
    @Placeholder
    abstract Mono<R> placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<? super T, ? extends Publisher<? extends R>> after() {
      return Refaster.anyOf(
          e ->
              Maybe.wrap(
                      RxJavaReactorMigrationUtil.toJdkFunction(
                              (Function<T, MaybeSource<R>>)
                                  ident -> RxJava2Adapter.monoToMaybe(placeholder(ident)))
                          .apply(e))
                  .toFlowable(),
          e ->
              Maybe.wrap(
                      RxJavaReactorMigrationUtil.<T, MaybeSource<R>>toJdkFunction(
                              ident -> RxJava2Adapter.monoToMaybe(placeholder(ident)))
                          .apply(e))
                  .toFlowable());
    }

    @AfterTemplate
    java.util.function.Function<? super T, ? extends Publisher<? extends R>> before() {
      return e -> placeholder(e);
    }
  }

  // XXX: Add test
  abstract static class FlowableFlatMapCompletableUnwrapLambda<T> {
    @Placeholder
    abstract Mono<?> placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<? super T, ? extends Publisher<? extends Void>> before() {
      return Refaster.anyOf(
          e ->
              RxJava2Adapter.completableToMono(
                  Completable.wrap(
                      RxJavaReactorMigrationUtil.<T, Completable>toJdkFunction(
                              (Function<T, Completable>)
                                  (T ident) -> RxJava2Adapter.monoToCompletable(placeholder(ident)))
                          .apply(e))),
          e ->
              RxJava2Adapter.completableToMono(
                  RxJavaReactorMigrationUtil.<T, Completable>toJdkFunction(
                          (Function<T, Completable>)
                              (T ident) -> RxJava2Adapter.monoToCompletable(placeholder(ident)))
                      .apply(e)),
          e ->
              RxJava2Adapter.completableToMono(
                  Completable.wrap(
                      RxJavaReactorMigrationUtil.<T, Completable>toJdkFunction(
                              (T ident) -> RxJava2Adapter.monoToCompletable(placeholder(ident)))
                          .apply(e))),
          e ->
              RxJava2Adapter.completableToMono(
                  Completable.wrap(
                      RxJavaReactorMigrationUtil.<T, CompletableSource>toJdkFunction(
                              (T ident) -> RxJava2Adapter.monoToCompletable(placeholder(ident)))
                          .apply(e))),
          e ->
              RxJava2Adapter.completableToMono(
                  RxJavaReactorMigrationUtil.<T, Completable>toJdkFunction(
                          (T ident) -> RxJava2Adapter.monoToCompletable(placeholder(ident)))
                      .apply(e)));
    }

    @AfterTemplate
    java.util.function.Function<T, Mono<?>> after() {
      return v -> placeholder(v);
    }
  }

  // XXX: Improve naming and add test case
  abstract static class FlowableUnwrapLambda<T> {
    @Placeholder
    abstract Completable placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<T, Publisher<? extends Void>> before() {
      return Refaster.anyOf(
          e ->
              RxJava2Adapter.completableToMono(
                  Completable.wrap(
                      RxJavaReactorMigrationUtil.<T, CompletableSource>toJdkFunction(
                              (Function<T, CompletableSource>) v -> placeholder(v))
                          .apply(e))),
          e ->
              RxJava2Adapter.completableToMono(
                  Completable.wrap(
                      RxJavaReactorMigrationUtil.<T, CompletableSource>toJdkFunction(
                              v -> placeholder(v))
                          .apply(e))));
    }

    @AfterTemplate
    java.util.function.Function<T, Mono<? extends Void>> after() {
      return v -> RxJava2Adapter.completableToMono(Completable.wrap(placeholder(v)));
    }
  }

  abstract static class FlowableFlatMapUnwrapLambda<T> {
    @Placeholder
    abstract CompletableSource placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<T, ? extends Publisher<? extends Void>> before() {
      return e ->
          RxJava2Adapter.completableToMono(
              Completable.wrap(
                  RxJavaReactorMigrationUtil.<T, CompletableSource>toJdkFunction(
                          (Function<T, CompletableSource>) v -> placeholder(v))
                      .apply(e)));
    }

    @AfterTemplate
    java.util.function.Function<T, Mono<? extends Void>> after() {
      return v -> RxJava2Adapter.completableToMono(Completable.wrap(placeholder(v)));
    }
  }

  abstract static class UnwrapCompletableExtendsMono<T, R> {
    @Placeholder
    abstract Mono<? extends R> placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends Void>> before() {
      return e ->
          RxJava2Adapter.completableToMono(
              Completable.wrap(
                  RxJavaReactorMigrationUtil.<T, CompletableSource>toJdkFunction(
                          (T ident) -> RxJava2Adapter.monoToCompletable(placeholder(ident)))
                      .apply(e)));
    }

    @AfterTemplate
    java.util.function.Function<T, Mono<? extends R>> after() {
      return v -> placeholder(v);
    }
  }

  // XXX: Add test
  abstract static class SingleFlatMapUnwrapLambda<T, R> {
    @Placeholder
    abstract Mono<? extends R> placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<T, ? extends Mono<? extends R>> before() {
      return v ->
          RxJava2Adapter.singleToMono(
              (Single<? extends R>)
                  RxJavaReactorMigrationUtil.toJdkFunction(
                          (T ident) -> RxJava2Adapter.monoToSingle(placeholder(ident)))
                      .apply(v));
    }

    @AfterTemplate
    java.util.function.Function<T, ? extends Mono<? extends R>> after() {
      return v -> placeholder(v);
    }
  }

  // XXX: Add test
  abstract static class SingleRemoveLambdaWithCast<T> {
    @Placeholder
    abstract Mono<?> placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<? super T, ? extends Publisher<? extends Void>> before() {
      return Refaster.anyOf(
          e ->
              RxJava2Adapter.completableToMono(
                  Completable.wrap(
                      RxJavaReactorMigrationUtil.<T, Completable>toJdkFunction(
                              (Function<T, Completable>)
                                  v -> placeholder(v).as(RxJava2Adapter::monoToCompletable))
                          .apply(e))),
          e ->
              RxJava2Adapter.completableToMono(
                  Completable.wrap(
                      RxJavaReactorMigrationUtil.<T, Completable>toJdkFunction(
                              (Function<T, Completable>)
                                  v -> RxJava2Adapter.monoToCompletable(placeholder(v)))
                          .apply(e))),
          e ->
              RxJava2Adapter.completableToMono(
                  Completable.wrap(
                      RxJavaReactorMigrationUtil.<T, Completable>toJdkFunction(
                              v -> RxJava2Adapter.monoToCompletable(placeholder(v)))
                          .apply(e))));
    }

    @AfterTemplate
    java.util.function.Function<? super T, ? extends Mono<?>> after() {
      return v -> placeholder(v);
    }
  }

  // XXX: Add test
  abstract static class SingleRemoveLambdaWithCompletable<T> {
    @BeforeTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends Void>> before(
        Completable completable) {
      return Refaster.anyOf(
          e ->
              RxJava2Adapter.completableToMono(
                  Completable.wrap(
                      RxJavaReactorMigrationUtil.<T, CompletableSource>toJdkFunction(
                              (Function<T, CompletableSource>) v -> completable)
                          .apply(e))),
          e ->
              RxJava2Adapter.completableToMono(
                  Completable.wrap(
                      RxJavaReactorMigrationUtil.<T, CompletableSource>toJdkFunction(
                              v -> completable)
                          .apply(e))));
    }

    @AfterTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends Void>> after(
        Completable completable) {
      return v -> RxJava2Adapter.completableToMono(completable);
    }
  }

  // XXX: Verify if this template still flags other cases than the one above.
  abstract static class SingleRemoveLambdaWithCompletableExtra<T> {
    @Placeholder
    abstract Completable placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends Void>> before() {
      return e ->
          RxJava2Adapter.completableToMono(
              Completable.wrap(
                  RxJavaReactorMigrationUtil.<T, CompletableSource>toJdkFunction(
                          (Function<T, CompletableSource>) v -> placeholder(v))
                      .apply(e)));
    }

    @AfterTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends Void>> after() {
      return v -> RxJava2Adapter.completableToMono(placeholder(v));
    }
  }
}
