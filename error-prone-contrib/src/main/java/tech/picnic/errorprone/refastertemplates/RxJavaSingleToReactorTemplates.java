package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.concurrent.Callable;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
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
  // XXX: public static Single defer(Callable) --> Required
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
  // XXX: public final Object blockingGet()
  // XXX: public final Single cache()
  // XXX: public final Single cast(Class)
  // XXX: public final Single compose(SingleTransformer)
  // XXX: public final Flowable concatWith(SingleSource)
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
      return single
          .as(RxJava2Adapter::singleToMono)
          .doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(consumer))
          .as(RxJava2Adapter::monoToSingle);
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
      return single
          .as(RxJava2Adapter::singleToMono)
          .doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(consumer))
          .as(RxJava2Adapter::monoToSingle);
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
      return single
          .as(RxJava2Adapter::singleToMono)
          .filter(RxJavaReactorMigrationUtil.toJdkPredicate(predicate))
          .as(RxJava2Adapter::monoToMaybe);
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
      return single
          .as(RxJava2Adapter::singleToMono)
          .flatMap(
              v ->
                  RxJava2Adapter.singleToMono(
                      Single.wrap(
                          (Single<O>) RxJavaReactorMigrationUtil.toJdkFunction(function).apply(v))))
          .as(RxJava2Adapter::monoToSingle);
    }
  }

  // XXX: Does this one work? See addressCompletionServiceClient.java validateAndComplete with the
  // this::handle.
  //  I dont think that it picks methodreferences up....
  abstract static class SingleFlatMapLambda<S, T> {
    @Placeholder
    abstract Single<T> toSingleFunction(@MayOptionallyUse S element);

    @BeforeTemplate
    Single<T> before(Single<S> single) {
      return single.flatMap(v -> toSingleFunction(v));
    }

    @AfterTemplate
    Single<T> after(Single<S> single) {
      return single
          .as(RxJava2Adapter::singleToMono)
          .flatMap(v -> toSingleFunction(v).as(RxJava2Adapter::singleToMono))
          .as(RxJava2Adapter::monoToSingle);
    }
  }

  // XXX: public final Completable flatMapCompletable(Function)

  // XXX: Test this one.
  // In this case it doesnt work: flatMap(e ->
  // RxJava2Adapter.maybeToMono(geocodingService::complete.apply(e))). (see before apply is an
  // error)
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
                          Maybe.wrap(RxJavaReactorMigrationUtil.toJdkFunction(mapper).apply(e)))));
    }
  }

  // XXX: public final Observable flatMapObservable(Function)
  // XXX: public final Flowable flatMapPublisher(Function)
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
      return single
          .as(RxJava2Adapter::singleToMono)
          .ignoreElement()
          .as(RxJava2Adapter::monoToCompletable);
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
      return single
          .as(RxJava2Adapter::singleToMono)
          .map(RxJavaReactorMigrationUtil.toJdkFunction(function))
          .as(RxJava2Adapter::monoToSingle);
    }
  }

  // XXX: public final Single materialize()
  // XXX: public final Flowable mergeWith(SingleSource)
  // XXX: public final Single observeOn(Scheduler)

  // XXX: Add test. This doesn't work for method references, which is a problem.
  static final class SingleOnErrorResumeNext<T> {
    @BeforeTemplate
    Single<T> before(
        Single<T> single,
        Function<? super Throwable, ? extends SingleSource<? extends T>> function) {
      return single.onErrorResumeNext(function);
    }

    @AfterTemplate
    Single<T> after(
        Single<T> single,
        Function<? super Throwable, ? extends SingleSource<? extends T>> function) {
      return RxJava2Adapter.monoToSingle(
          single
              .as(RxJava2Adapter::singleToMono)
              .onErrorResume(
                  e ->
                      RxJava2Adapter.singleToMono(
                          Single.wrap(
                              RxJavaReactorMigrationUtil.toJdkFunction(function).apply(e)))));
    }
  }

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
  // XXX: public final Single retryWhen(Function)
  // XXX: public final Disposable subscribe()
  // XXX: public final Disposable subscribe(BiConsumer)
  // XXX: public final Disposable subscribe(Consumer)
  // XXX: public final Disposable subscribe(Consumer,Consumer)
  // XXX: public final void subscribe(SingleObserver)
  // XXX: public final Single subscribeOn(Scheduler) --> Required.
  // XXX: public final SingleObserver subscribeWith(SingleObserver)
  // XXX: public final Single takeUntil(CompletableSource)
  // XXX: public final Single takeUntil(Publisher)
  // XXX: public final Single takeUntil(SingleSource)
  // XXX: public final Single timeout(long,TimeUnit)
  // XXX: public final Single timeout(long,TimeUnit,Scheduler)
  // XXX: public final Single timeout(long,TimeUnit,Scheduler,SingleSource)
  // XXX: public final Single timeout(long,TimeUnit,SingleSource)
  // XXX: public final Object to(Function)
  // XXX: public final Completable toCompletable() <-- this one is @Deprecated

  // XXX: Validate this one and test it.
  static final class FlowableToFlowable<T> {
    @BeforeTemplate
    Flowable<T> before(Single<T> single) {
      return single.toFlowable();
    }

    @AfterTemplate
    Flowable<T> after(Single<T> single) {
      return single.as(RxJava2Adapter::singleToMono).flux().as(RxJava2Adapter::fluxToFlowable);
    }
  }

  // XXX: public final Future toFuture()
  // XXX: public final Maybe toMaybe() --> 1 usage
  // XXX: public final Observable toObservable()
  // XXX: public final Single unsubscribeOn(Scheduler)
  // XXX: public final Single zipWith(SingleSource,BiFunction) --> One usage.
  // XXX: public final TestObserver test()
  // XXX: public final TestObserver test(boolean)
}
