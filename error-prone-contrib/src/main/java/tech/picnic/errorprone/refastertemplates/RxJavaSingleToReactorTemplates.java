package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Callable;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/** The Refaster templates for the migration of the RxJava Single type to Reactor */
final class RxJavaSingleToReactorTemplates {

  private RxJavaSingleToReactorTemplates() {}

  // XXX: public static Single amb(Iterable)
  // XXX: public static Single ambArray(SingleSource[])
  // XXX: public static Flowable concat(Iterable)
  // XXX: public static Observable concat(ObservableSource)
  // XXX: public static Flowable concat(Publisher)
  // XXX: public static Flowable concat(Publisher,int)
  // XXX: public static Flowable concat(SingleSource,SingleSource)
  // XXX: public static Flowable concat(SingleSource,SingleSource,SingleSource)
  // XXX: public static Flowable concat(SingleSource,SingleSource,SingleSource,SingleSource)
  // XXX: public static Flowable concatArray(SingleSource[])
  // XXX: public static Flowable concatArrayEager(SingleSource[])
  // XXX: public static Flowable concatEager(Iterable)
  // XXX: public static Flowable concatEager(Publisher)
  // XXX: public static Single create(SingleOnSubscribe)

  abstract static class SingleDeferFirst<T> {
    @Placeholder
    abstract Single<? extends T> singleProducer();

    @BeforeTemplate
    Single<T> before() {
      return Single.defer(() -> singleProducer());
    }

    @AfterTemplate
    Single<? extends T> after() {
      return RxJava2Adapter.monoToSingle(
          Mono.defer(() -> singleProducer().as(RxJava2Adapter::singleToMono)));
    }
  }

  // XXX: public static Single equals(SingleSource,SingleSource)

  static final class SingleErrorCallable<T> {
    @BeforeTemplate
    Single<T> before(Callable<? extends Throwable> throwable) {
      return Single.error(throwable);
    }

    @UseImportPolicy(ImportPolicy.IMPORT_CLASS_DIRECTLY)
    @AfterTemplate
    Single<T> after(Callable<? extends Throwable> throwable) {
      return RxJava2Adapter.monoToSingle(
          Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(throwable)));
    }
  }

  static final class SingleErrorThrowable<T> {
    @BeforeTemplate
    Single<T> before(Throwable throwable) {
      return Single.error(throwable);
    }

    @AfterTemplate
    Single<T> after(Throwable throwable) {
      return RxJava2Adapter.monoToSingle(Mono.error(throwable));
    }
  }

  static final class SingleFromCallable<T> {
    @BeforeTemplate
    Single<T> before(Callable<? extends T> callable) {
      return Single.fromCallable(callable);
    }

    @UseImportPolicy(ImportPolicy.IMPORT_CLASS_DIRECTLY)
    @AfterTemplate
    Single<T> after(Callable<? extends T> callable) {
      return RxJava2Adapter.monoToSingle(
          Mono.fromSupplier(RxJavaReactorMigrationUtil.callableAsSupplier(callable)));
    }
  }

  // XXX: public static Single fromFuture(Future)
  // XXX: public static Single fromFuture(Future,long,TimeUnit)
  // XXX: public static Single fromFuture(Future,long,TimeUnit,Scheduler)
  // XXX: public static Single fromFuture(Future,Scheduler)
  // XXX: public static Single fromObservable(ObservableSource)
  // XXX: public static Single fromPublisher(Publisher)

  static final class SingleJust<T> {
    @BeforeTemplate
    Single<T> before(T item) {
      return Single.just(item);
    }

    @AfterTemplate
    Single<T> after(T item) {
      return RxJava2Adapter.monoToSingle(Mono.just(item));
    }
  }

  // XXX: public static Flowable merge(Iterable)
  // XXX: public static Flowable merge(Publisher)
  // XXX: public static Single merge(SingleSource)
  // XXX: public static Flowable merge(SingleSource,SingleSource)
  // XXX: public static Flowable merge(SingleSource,SingleSource,SingleSource)
  // XXX: public static Flowable merge(SingleSource,SingleSource,SingleSource,SingleSource)
  // XXX: public static Flowable mergeDelayError(Iterable)
  // XXX: public static Flowable mergeDelayError(Publisher)
  // XXX: public static Flowable mergeDelayError(SingleSource,SingleSource)
  // XXX: public static Flowable mergeDelayError(SingleSource,SingleSource,SingleSource)
  // XXX: public static Flowable
  // mergeDelayError(SingleSource,SingleSource,SingleSource,SingleSource)
  // XXX: public static Single never()
  // XXX: public static Single timer(long,TimeUnit)
  // XXX: public static Single timer(long,TimeUnit,Scheduler)
  // XXX: public static Single unsafeCreate(SingleSource)
  // XXX: public static Single using(Callable,Function,Consumer)
  // XXX: public static Single using(Callable,Function,Consumer,boolean)

  static final class SingleWrap<T> {
    @BeforeTemplate
    Single<T> before(Single<T> single) {
      return Single.wrap(single);
    }

    @AfterTemplate
    Single<T> after(Single<T> single) {
      return single;
    }
  }

  // XXX: public static Single zip(Iterable,Function)
  // XXX: public static Single zip(SingleSource,SingleSource,BiFunction)
  // XXX: public static Single zip(SingleSource,SingleSource,SingleSource,Function3)
  // XXX: public static Single zip(SingleSource,SingleSource,SingleSource,SingleSource,Function4)
  // XXX: public static Single
  // zip(SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,Function5)
  // XXX: public static Single
  // zip(SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,Function6)
  // XXX: public static Single
  // zip(SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,Function7)
  // XXX: public static Single
  // zip(SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,Function8)
  // XXX: public static Single
  // zip(SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,SingleSource,Function9)
  // XXX: public static Single zipArray(Function,SingleSource[])
  // XXX: public final Single ambWith(SingleSource)
  // XXX: public final Object as(SingleConverter)

  static final class SingleBlockingGet<T> {
    @BeforeTemplate
    Object before(Single<T> single) {
      return single.blockingGet();
    }

    @AfterTemplate
    Object after(Single<T> single) {
      return RxJava2Adapter.singleToMono(single).block();
    }
  }

  // XXX: public final Single cache()
  // XXX: public final Single cast(Class)
  // XXX: public final Single compose(SingleTransformer)

  static final class SingleConcatWith<T> {
    @BeforeTemplate
    Flowable<T> before(Single<T> single, SingleSource<? extends T> source) {
      return single.concatWith(source);
    }

    @AfterTemplate
    Flowable<T> after(Single<T> single, SingleSource<? extends T> source) {
      return RxJava2Adapter.fluxToFlowable(
          RxJava2Adapter.singleToMono(single)
              .concatWith(RxJava2Adapter.singleToMono(Single.wrap(source))));
    }
  }

  // XXX: public final Single contains(Object)
  // XXX: public final Single contains(Object,BiPredicate)
  // XXX: public final Single delay(long,TimeUnit)
  // XXX: public final Single delay(long,TimeUnit,boolean)
  // XXX: public final Single delay(long,TimeUnit,Scheduler)
  // XXX: public final Single delay(long,TimeUnit,Scheduler,boolean)
  // XXX: public final Single delaySubscription(CompletableSource)
  // XXX: public final Single delaySubscription(long,TimeUnit)
  // XXX: public final Single delaySubscription(long,TimeUnit,Scheduler)
  // XXX: public final Single delaySubscription(ObservableSource)
  // XXX: public final Single delaySubscription(Publisher)
  // XXX: public final Single delaySubscription(SingleSource)
  // XXX: public final Maybe dematerialize(Function)
  // XXX: public final Single doAfterSuccess(Consumer)
  // XXX: public final Single doAfterTerminate(Action)
  // XXX: public final Single doFinally(Action)
  // XXX: public final Single doOnDispose(Action)

  static final class SingleDoOnError<T> {
    @BeforeTemplate
    Single<T> before(Single<T> single, Consumer<? super Throwable> consumer) {
      return single.doOnError(consumer);
    }

    @AfterTemplate
    Single<T> after(Single<T> single, Consumer<? super Throwable> consumer) {
      return RxJava2Adapter.monoToSingle(
          RxJava2Adapter.singleToMono(single)
              .doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(consumer)));
    }
  }

  // XXX: public final Single doOnEvent(BiConsumer)
  // XXX: public final Single doOnSubscribe(Consumer)

  static final class SingleDoOnSuccess<T> {
    @BeforeTemplate
    Single<T> before(Single<T> single, Consumer<T> consumer) {
      return single.doOnSuccess(consumer);
    }

    @AfterTemplate
    Single<T> after(Single<T> single, Consumer<T> consumer) {
      return RxJava2Adapter.monoToSingle(
          RxJava2Adapter.singleToMono(single)
              .doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(consumer)));
    }
  }

  // XXX: public final Single doOnTerminate(Action)

  static final class SingleFilter<S, T extends S> {
    @BeforeTemplate
    Maybe<T> before(Single<T> single, Predicate<S> predicate) {
      return single.filter(predicate);
    }

    @AfterTemplate
    Maybe<T> after(Single<T> single, Predicate<S> predicate) {
      return RxJava2Adapter.monoToMaybe(
          RxJava2Adapter.singleToMono(single)
              .filter(RxJavaReactorMigrationUtil.toJdkPredicate(predicate)));
    }
  }

  // XXX: Test this one
  static final class SingleFlatMapFunction<I, T extends I, O, M extends SingleSource<? extends O>> {
    @BeforeTemplate
    Single<O> before(Single<T> single, Function<I, M> function) {
      return single.flatMap(function);
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    @UseImportPolicy(ImportPolicy.IMPORT_CLASS_DIRECTLY)
    Single<O> after(Single<T> single, Function<I, M> function) {
      return RxJava2Adapter.monoToSingle(
          RxJava2Adapter.singleToMono(single)
              .flatMap(
                  v ->
                      RxJava2Adapter.singleToMono(
                          Single.wrap(
                              (Single<O>)
                                  RxJavaReactorMigrationUtil.toJdkFunction(
                                          (Function<I, Single<O>>) function)
                                      .apply(v)))));
    }
  }

  // XXX: remove this one?
  // This one doesnt work yet...
  abstract static class SingleFlatMapUnwrapLambda222<T, R extends T> {
    @Placeholder
    abstract Mono<? extends R> placeholder(@MayOptionallyUse T input);

    @BeforeTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends R>> before() {
      return v ->
          RxJava2Adapter.singleToMono(
              (Single<? extends R>)
                  RxJavaReactorMigrationUtil.toJdkFunction(
                          (T ident) -> RxJava2Adapter.monoToSingle(placeholder(ident)))
                      .apply(v));
    }

    @AfterTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends R>> after() {
      return v -> placeholder(v);
    }
  }

  // XXX: Write a test.
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

  // XXX: Does this one work? See addressCompletionServiceClient.java validateAndComplete with the
  // this::handle.
  //  I dont think that it picks up on method references....
  abstract static class SingleFlatMapLambda<S, T> {
    @Placeholder
    abstract Single<T> toSingleFunction(@MayOptionallyUse S element);

    @BeforeTemplate
    Single<T> before(Single<S> single) {
      return Refaster.anyOf(
          single.flatMap(v -> toSingleFunction(v)), single.flatMap((S v) -> toSingleFunction(v)));
    }

    @AfterTemplate
    Single<T> after(Single<S> single) {
      return RxJava2Adapter.monoToSingle(
          RxJava2Adapter.singleToMono(single)
              .flatMap(v -> toSingleFunction(v).as(RxJava2Adapter::singleToMono)));
    }
  }

  static final class SingleFlatMapCompletable<T, R extends CompletableSource> {
    @BeforeTemplate
    Completable before(Single<T> single, Function<? super T, R> function) {
      return single.flatMapCompletable(function);
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    Completable after(Single<T> single, Function<? super T, R> function) {
      return RxJava2Adapter.monoToCompletable(
          RxJava2Adapter.singleToMono(single)
              .flatMap(
                  y ->
                      RxJava2Adapter.completableToMono(
                          Completable.wrap(
                              RxJavaReactorMigrationUtil.toJdkFunction((Function<T, R>) function)
                                  .apply(y))))
              .then());
    }
  }

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
                                  v -> (placeholder(v).as(RxJava2Adapter::monoToCompletable)))
                          .apply(e))),
          e ->
              RxJava2Adapter.completableToMono(
                  Completable.wrap(
                      RxJavaReactorMigrationUtil.<T, Completable>toJdkFunction(
                              (Function<T, Completable>)
                                  v -> RxJava2Adapter.monoToCompletable(placeholder(v)))
                          .apply(e))));
    }

    @AfterTemplate
    java.util.function.Function<? super T, ? extends Mono<?>> after() {
      return v -> placeholder(v);
    }
  }

  abstract static class SingleRemoveLambdaWithCompletable<T> {
    @BeforeTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends Void>> before(
        Completable completable) {
      return e ->
          RxJava2Adapter.completableToMono(
              Completable.wrap(
                  RxJavaReactorMigrationUtil.<T, CompletableSource>toJdkFunction(
                          (Function<T, CompletableSource>) v -> completable)
                      .apply(e)));
    }

    @AfterTemplate
    java.util.function.Function<? super T, ? extends Mono<? extends Void>> after(
        Completable completable) {
      return v -> RxJava2Adapter.completableToMono(completable);
    }
  }

  // XXX: Verify if this template still flags other cases.
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

  // XXX: Test this one
  static final class SingleFlatMapMaybe<T, R> {
    @BeforeTemplate
    Maybe<R> before(
        Single<T> single, Function<? super T, ? extends MaybeSource<? extends R>> mapper) {
      return single.flatMapMaybe(mapper);
    }

    @AfterTemplate
    Maybe<R> after(
        Single<T> single, Function<? super T, ? extends MaybeSource<? extends R>> mapper) {
      return RxJava2Adapter.monoToMaybe(
          RxJava2Adapter.singleToMono(single)
              .flatMap(
                  e ->
                      RxJava2Adapter.maybeToMono(
                          Maybe.wrap(
                              RxJavaReactorMigrationUtil.toJdkFunction(
                                      (Function<T, MaybeSource<R>>) mapper)
                                  .apply(e)))));
    }
  }

  // XXX: Test this one. (Disabled, created a better one (?) in RxJavaUnwrapTemplates.
  //  abstract static class SingleFlatMapMaybeUnwrapLambda<T, R> {
  //    @Placeholder
  //    abstract Mono<? extends R> placeholder(@MayOptionallyUse T input);
  //
  //    @BeforeTemplate
  //    java.util.function.Function<T, ? extends Mono<? extends R>> before() {
  //      return e ->
  //          RxJava2Adapter.maybeToMono(
  //              RxJavaReactorMigrationUtil.toJdkFunction(
  //                      ident -> RxJava2Adapter.monoToMaybe(placeholder(e)))
  //                  .apply(e));
  //    }
  //
  //    @AfterTemplate
  //    java.util.function.Function<T, ? extends Mono<? extends R>> after() {
  //      return v -> placeholder(v);
  //    }
  //  }

  // XXX: public final Observable flatMapObservable(Function)

  static final class SingleFlatMapPublisher<T, R> {
    @BeforeTemplate
    Flowable<R> before(
        Single<T> single, Function<? super T, ? extends Publisher<? extends R>> mapper) {
      return single.flatMapPublisher(mapper);
    }

    @AfterTemplate
    Flowable<R> after(
        Single<T> single, Function<? super T, ? extends Publisher<? extends R>> mapper) {
      return RxJava2Adapter.fluxToFlowable(
          RxJava2Adapter.singleToMono(single)
              .flatMapMany(RxJavaReactorMigrationUtil.toJdkFunction(mapper)));
    }
  }

  // XXX: public final Flowable flattenAsFlowable(Function)
  // XXX: public final Observable flattenAsObservable(Function)
  // XXX: public final Single hide()

  static final class CompletableIgnoreElement<T> {
    @BeforeTemplate
    Completable before(Single<T> single) {
      return single.ignoreElement();
    }

    @AfterTemplate
    Completable after(Single<T> single) {
      return RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(single).then());
    }
  }

  // XXX: public final Single lift(SingleOperator)

  static final class SingleMap<I, T extends I, O> {
    @BeforeTemplate
    Single<O> before(Single<T> single, Function<I, O> function) {
      return single.map(function);
    }

    @AfterTemplate
    Single<O> after(Single<T> single, Function<I, O> function) {
      return RxJava2Adapter.monoToSingle(
          RxJava2Adapter.singleToMono(single)
              .map(RxJavaReactorMigrationUtil.toJdkFunction(function)));
    }
  }

  // XXX: public final Single materialize()
  // XXX: public final Flowable mergeWith(SingleSource)
  // XXX: public final Single observeOn(Scheduler)

  // XXX Keep the original for safety, first verify that others work!
  //  static final class SingleOnErrorResumeNext<T> {
  //    @BeforeTemplate
  //    Single<T> before(
  //            Single<T> single,
  //            Function<? super Throwable, ? extends SingleSource<? extends T>> function) {
  //      return single.onErrorResumeNext(function);
  //    }
  //
  //    @AfterTemplate
  //    Single<T> after(
  //            Single<T> single,
  //            Function<? super Throwable, ? extends SingleSource<? extends T>> function) {
  //      return RxJava2Adapter.monoToSingle(
  //              single
  //                      .as(RxJava2Adapter::singleToMono)
  //                      .onErrorResume(
  //                              err ->
  //                                      RxJava2Adapter.singleToMono(
  //                                              Single.wrap(
  //                                                      RxJavaReactorMigrationUtil.toJdkFunction(
  //                                                                      (Function<Throwable,
  // Single<T>>) function)
  //                                                              .apply(err)))));
  //    }
  //  }
  static final class SingleOnErrorResumeNext<
      S, T extends S, R, P extends Throwable, Q extends Single<T>> {
    @BeforeTemplate
    Single<T> before(Single<T> single, Function<? super Throwable, Q> function) {
      return single.onErrorResumeNext(function);
    }

    @AfterTemplate
    Single<T> after(Single<T> single, Function<Throwable, Single<T>> function) {
      return RxJava2Adapter.monoToSingle(
          single
              .as(RxJava2Adapter::singleToMono)
              .onErrorResume(
                  err ->
                      RxJava2Adapter.singleToMono(
                          (Q)
                              RxJavaReactorMigrationUtil.<Throwable, Single<T>>toJdkFunction(
                                      function)
                                  .apply(err))));
    }
  } // Why doesn't it add the <Throwable, Single<T>>???????
  //  static final class FlowableFlatMapMaybeSecond<
  //      S, T extends S, R, P extends R, Q extends Maybe<P>> {
  //    @BeforeTemplate
  //    Flowable<R> before(Flowable<T> flowable, Function<S, Q> function) {
  //      return flowable.flatMapMaybe(function);
  //    }
  //
  //    @AfterTemplate
  //    Flowable<R> after(Flowable<T> flowable, Function<S, Q> function) {
  //      return RxJava2Adapter.fluxToFlowable(
  //          RxJava2Adapter.flowableToFlux(flowable)
  //              .flatMap(
  //                  e ->
  //                      RxJava2Adapter.maybeToMono(
  //                          (Q) RxJavaReactorMigrationUtil.<S,
  // Q>toJdkFunction(function).apply(e))));
  //    }
  //  }

  // XXX: public final Single onErrorResumeNext(Single)
  // XXX: public final Single onErrorReturn(Function)
  // XXX: public final Single onErrorReturnItem(Object)
  // XXX: public final Single onTerminateDetach()
  // XXX: public final Flowable repeat()
  // XXX: public final Flowable repeat(long)
  // XXX: public final Flowable repeatUntil(BooleanSupplier)
  // XXX: public final Flowable repeatWhen(Function)
  // XXX: public final Single retry()
  // XXX: public final Single retry(BiPredicate)
  // XXX: public final Single retry(long)
  // XXX: public final Single retry(long,Predicate)
  // XXX: public final Single retry(Predicate)

  // Can be skipped because of:
  //  https://picnic.atlassian.net/browse/PRP-12237
  //   XXX: public final Single retryWhen(Function)

  // XXX: public final Disposable subscribe()
  // XXX: public final Disposable subscribe(BiConsumer)
  // XXX: public final Disposable subscribe(Consumer)
  // XXX: public final Disposable subscribe(Consumer,Consumer)
  // XXX: public final void subscribe(SingleObserver)

  // XXX: Not accounting for the Schedulers.computation()
  static final class SingleSubscribeOn<T> {
    @BeforeTemplate
    Single<T> before(Single<T> single) {
      return single.subscribeOn(Schedulers.io());
    }

    @AfterTemplate
    Single<T> after(Single<T> single) {
      return RxJava2Adapter.monoToSingle(
          RxJava2Adapter.singleToMono(single)
              .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()));
    }
  }

  // XXX: public final SingleObserver subscribeWith(SingleObserver)
  // XXX: public final Single takeUntil(CompletableSource)
  // XXX: public final Single takeUntil(Publisher)
  // XXX: public final Single takeUntil(SingleSource)
  // XXX: public final Single timeout(long,TimeUnit)
  // XXX: public final Single timeout(long,TimeUnit,Scheduler)
  // XXX: public final Single timeout(long,TimeUnit,Scheduler,SingleSource)
  // XXX: public final Single timeout(long,TimeUnit,SingleSource)
  // XXX: public final Object to(Function)
  // XXX: public final Completable toCompletable()

  static final class SingleToFlowable<T> {
    @BeforeTemplate
    Flowable<T> before(Single<T> single) {
      return single.toFlowable();
    }

    @AfterTemplate
    Flowable<T> after(Single<T> single) {
      return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(single).flux());
    }
  }

  // XXX: public final Future toFuture()

  static final class SingleToMaybe<T> {
    @BeforeTemplate
    Maybe<T> before(Single<T> single) {
      return single.toMaybe();
    }

    @AfterTemplate
    Maybe<T> after(Single<T> single) {
      return RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(single));
    }
  }

  // XXX: public final Observable toObservable()
  // XXX: public final Single unsubscribeOn(Scheduler)

  static final class SingleZipWith<T, R, U> {
    @BeforeTemplate
    Single<R> before(
        Single<T> single,
        SingleSource<U> source,
        BiFunction<? super T, ? super U, ? extends R> biFunction) {
      return single.zipWith(source, biFunction);
    }

    @AfterTemplate
    Single<R> after(
        Single<T> single,
        SingleSource<U> source,
        BiFunction<? super T, ? super U, ? extends R> biFunction) {
      return RxJava2Adapter.monoToSingle(
          RxJava2Adapter.singleToMono(single)
              .zipWith(
                  RxJava2Adapter.singleToMono(Single.wrap(source)),
                  RxJavaReactorMigrationUtil.toJdkBiFunction(biFunction)));
    }
  }

  // XXX: public final TestObserver test()

  static final class SingleTestAssertResultItem<T> {
    @BeforeTemplate
    void before(Single<T> single, T item) throws InterruptedException {
      Refaster.anyOf(
          single.test().assertResult(item),
          single.test().await().assertResult(item),
          single.test().await().assertComplete().assertResult(item),
          single.test().await().assertResult(item).assertComplete(),
          single.test().await().assertValue(item),
          single.test().await().assertComplete().assertValue(item),
          single.test().assertValue(item),
          single.test().await().assertValue(item).assertComplete());
    }

    @AfterTemplate
    void after(Single<T> single, T item) {
      RxJava2Adapter.singleToMono(single)
          .as(StepVerifier::create)
          .expectNext(item)
          .verifyComplete();
    }
  }

  static final class SingleAssertValueSet<T> {
    @BeforeTemplate
    void before(Single<T> single, ImmutableSet<? extends T> set) throws InterruptedException {
      single.test().await().assertNoErrors().assertValueSet(set).assertComplete();
    }

    @AfterTemplate
    void after(Single<T> single, ImmutableSet<? extends T> set) throws InterruptedException {
      RxJava2Adapter.singleToMono(single)
          .map(ImmutableSet::of)
          .as(StepVerifier::create)
          .expectNext(ImmutableSet.copyOf(set))
          .verifyComplete();
    }
  }

  static final class SingleTestAssertResult<T> {
    @BeforeTemplate
    void before(Single<T> single) throws InterruptedException {
      single.test().await().assertResult();
    }

    @AfterTemplate
    void after(Single<T> single) {
      RxJava2Adapter.singleToMono(single).as(StepVerifier::create).verifyComplete();
    }
  }

  static final class SingleTestAssertValue<T> {
    @BeforeTemplate
    void before(Single<T> single, Predicate<T> predicate) throws InterruptedException {
      Refaster.anyOf(
          single.test().await().assertValue(predicate),
          single.test().await().assertValue(predicate).assertComplete(),
          single.test().await().assertValue(predicate).assertNoErrors().assertComplete(),
          single.test().await().assertComplete().assertValue(predicate));
    }

    @AfterTemplate
    void after(Single<T> single, Predicate<T> predicate) {
      RxJava2Adapter.singleToMono(single)
          .as(StepVerifier::create)
          .expectNextMatches(RxJavaReactorMigrationUtil.toJdkPredicate(predicate))
          .verifyComplete();
    }
  }

  static final class SingleTestAssertComplete<T> {
    @BeforeTemplate
    void before(Single<T> single) throws InterruptedException {
      single.test().await().assertComplete();
    }

    @AfterTemplate
    void after(Single<T> single) {
      RxJava2Adapter.singleToMono(single).as(StepVerifier::create).verifyComplete();
    }
  }

  static final class SingleTestAssertErrorClass<T> {
    @BeforeTemplate
    void before(Single<T> single, Class<? extends Throwable> errorClass)
        throws InterruptedException {
      single.test().await().assertError(errorClass);
    }

    @AfterTemplate
    void after(Single<T> single, Class<? extends Throwable> errorClass) {
      RxJava2Adapter.singleToMono(single).as(StepVerifier::create).verifyError(errorClass);
    }
  }

  // XXX: .assertError(Throwable) -> (not used in PRP).

  static final class SingleTestAssertNoErrors<T> {
    @BeforeTemplate
    void before(Single<T> single) throws InterruptedException {
      single.test().await().assertNoErrors();
    }

    @AfterTemplate
    void after(Single<T> single) {
      RxJava2Adapter.singleToMono(single).as(StepVerifier::create).verifyComplete();
    }
  }

  static final class SingleTestAssertValueCount<T> {
    @BeforeTemplate
    void before(Single<T> single, int count) throws InterruptedException {
      single.test().await().assertValueCount(count);
    }

    @AfterTemplate
    void after(Single<T> single, int count) {
      RxJava2Adapter.singleToMono(single)
          .as(StepVerifier::create)
          .expectNextCount(count)
          .verifyComplete();
    }
  }

  // XXX: Add test
  static final class SingleTestAssertFailure<T> {
    @BeforeTemplate
    void before(Single<T> single, Class<? extends Throwable> error) throws InterruptedException {
      single.test().await().assertFailure(error);
    }

    @AfterTemplate
    void after(Single<T> single, Class<? extends Throwable> error) {
      RxJava2Adapter.singleToMono(single).as(StepVerifier::create).verifyError(error);
    }
  }

  // XXX: Add test
  static final class SingleTestAssertNoValues<T> {
    @BeforeTemplate
    void before(Single<T> single) throws InterruptedException {
      Refaster.anyOf(
          single.test().await().assertNoValues(),
          single.test().await().assertNoValues().assertComplete());
    }

    @AfterTemplate
    void after(Single<T> single) {
      RxJava2Adapter.singleToMono(single).as(StepVerifier::create).verifyComplete();
    }
  }

  // XXX: Add test
  // XXX: This introduces AssertJ dependency
  static final class SingleTestAssertFailureAndMessage<T> {
    @BeforeTemplate
    void before(Single<T> single, Class<? extends Throwable> error, String message)
        throws InterruptedException {
      single.test().await().assertFailureAndMessage(error, message);
    }

    @AfterTemplate
    void after(Single<T> single, Class<? extends Throwable> error, String message) {
      RxJava2Adapter.singleToMono(single)
          .as(StepVerifier::create)
          .expectErrorSatisfies(
              t -> assertThat(t).isInstanceOf(error).hasMessageContaining(message))
          .verify();
    }
  }

  // XXX: public final TestObserver test(boolean)
}
