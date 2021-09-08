package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.Streams;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.functions.Action;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** The Refaster templates for the migration of the RxJava Completable type to Reactor */
final class RxJavaCompletableToReactorTemplates {

  private RxJavaCompletableToReactorTemplates() {}

  // XXX: Copied over from Stephan's test, to make the test run.
  static final class CompletableAmb {
    @BeforeTemplate
    Completable before(Iterable<? extends Completable> sources) {
      return Completable.amb(sources);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.IMPORT_CLASS_DIRECTLY)
    Completable after(Iterable<? extends Completable> sources) {
      return Mono.firstWithSignal(
              Streams.stream(sources)
                  .map(RxJava2Adapter::completableToMono)
                  .collect(toImmutableList()))
          .as(RxJava2Adapter::monoToCompletable);
    }
  }

  // XXX: public static Completable ambArray(CompletableSource[])
  // XXX: public static Completable complete()
  // XXX: public static Completable concat(Iterable)
  // XXX: public static Completable concat(Publisher)
  // XXX: public static Completable concat(Publisher,int)
  // XXX: public static Completable concatArray(CompletableSource[])
  // XXX: public static Completable create(CompletableOnSubscribe)
  // XXX: public static Completable defer(Callable) --> Required.
  // XXX: public static Completable error(Callable) --> Required.
  // XXX: public static Completable error(Throwable) --> Required.

  // XXX: Make the test.
  static final class CompletableFromAction<T> {
    @BeforeTemplate
    Completable before(Action action) {
      return Completable.fromAction(action);
    }

    @AfterTemplate
    Completable after(Action action) {
      return RxJava2Adapter.monoToCompletable(
          Mono.fromRunnable(
              RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toRunnable(action)));
    }
  }



  // XXX: public static Completable fromCallable(Callable)
  // XXX: public static Completable fromFuture(Future)
  // XXX: public static Completable fromMaybe(MaybeSource)
  // XXX: public static Completable fromObservable(ObservableSource)
  // XXX: public static Completable fromPublisher(Publisher) --> Required
  // XXX: public static Completable fromRunnable(Runnable)
  // XXX: public static Completable fromSingle(SingleSource)
  // XXX: public static Completable merge(Iterable)
  // XXX: public static Completable merge(Publisher)
  // XXX: public static Completable merge(Publisher,int)
  // XXX: public static Completable mergeArray(CompletableSource[])
  // XXX: public static Completable mergeArrayDelayError(CompletableSource[])
  // XXX: public static Completable mergeDelayError(Iterable)
  // XXX: public static Completable mergeDelayError(Publisher)
  // XXX: public static Completable mergeDelayError(Publisher,int)
  // XXX: public static Completable never()
  // XXX: public static Completable timer(long,TimeUnit)
  // XXX: public static Completable timer(long,TimeUnit,Scheduler)
  // XXX: public static Completable unsafeCreate(CompletableSource)
  // XXX: public static Completable using(Callable,Function,Consumer)
  // XXX: public static Completable using(Callable,Function,Consumer,boolean)
  // XXX: public static Completable wrap(CompletableSource)
  // XXX: public final Completable ambWith(CompletableSource)
  // XXX: public final Completable andThen(CompletableSource)
  // XXX: public final Maybe andThen(MaybeSource)
  // XXX: public final Observable andThen(ObservableSource)
  // XXX: public final Flowable andThen(Publisher)
  // XXX: public final Single andThen(SingleSource)
  // XXX: public final Object as(CompletableConverter)
  // XXX: public final void blockingAwait()
  // XXX: public final boolean blockingAwait(long,TimeUnit)
  // XXX: public final Throwable blockingGet()
  // XXX: public final Throwable blockingGet(long,TimeUnit)
  // XXX: public final Completable cache()
  // XXX: public final Completable compose(CompletableTransformer)
  // XXX: public final Completable concatWith(CompletableSource)
  // XXX: public final Completable delay(long,TimeUnit)
  // XXX: public final Completable delay(long,TimeUnit,Scheduler)
  // XXX: public final Completable delay(long,TimeUnit,Scheduler,boolean)
  // XXX: public final Completable delaySubscription(long,TimeUnit)
  // XXX: public final Completable delaySubscription(long,TimeUnit,Scheduler)
  // XXX: public final Completable doAfterTerminate(Action)
  // XXX: public final Completable doFinally(Action)
  // XXX: public final Completable doOnComplete(Action) --> Required
  // XXX: public final Completable doOnDispose(Action)
  // XXX: public final Completable doOnError(Consumer)
  // XXX: public final Completable doOnEvent(Consumer)
  // XXX: public final Completable doOnSubscribe(Consumer) --> Required
  // XXX: public final Completable doOnTerminate(Action)
  // XXX: public final Completable hide()
  // XXX: public final Completable lift(CompletableOperator)
  // XXX: public final Single materialize()
  // XXX: public final Completable mergeWith(CompletableSource)
  // XXX: public final Completable observeOn(Scheduler)
  // XXX: public final Completable onErrorComplete()
  // XXX: public final Completable onErrorComplete(Predicate)
  // XXX: public final Completable onErrorResumeNext(Function)
  // XXX: public final Completable onTerminateDetach()
  // XXX: public final Completable repeat()
  // XXX: public final Completable repeat(long)
  // XXX: public final Completable repeatUntil(BooleanSupplier)
  // XXX: public final Completable repeatWhen(Function)
  // XXX: public final Completable retry()
  // XXX: public final Completable retry(BiPredicate)
  // XXX: public final Completable retry(long)
  // XXX: public final Completable retry(long,Predicate)
  // XXX: public final Completable retry(Predicate)
  // XXX: public final Completable retryWhen(Function)
  // XXX: public final Completable startWith(CompletableSource)
  // XXX: public final Observable startWith(Observable)
  // XXX: public final Flowable startWith(Publisher)
  // XXX: public final Disposable subscribe()
  // XXX: public final Disposable subscribe(Action)
  // XXX: public final Disposable subscribe(Action,Consumer)
  // XXX: public final void subscribe(CompletableObserver)
  // XXX: public final Completable subscribeOn(Scheduler)
  // XXX: public final CompletableObserver subscribeWith(CompletableObserver)
  // XXX: public final Completable takeUntil(CompletableSource)
  // XXX: public final observers.TestObserver test()
  // XXX: public final observers.TestObserver test(boolean)
  // XXX: public final Completable timeout(long,TimeUnit)
  // XXX: public final Completable timeout(long,TimeUnit,CompletableSource)
  // XXX: public final Completable timeout(long,TimeUnit,Scheduler)
  // XXX: public final Completable timeout(long,TimeUnit,Scheduler,CompletableSource)
  // XXX: public final Object to(Function)
  // XXX: public final Flowable toFlowable()
  // XXX: public final Maybe toMaybe()
  // XXX: public final Observable toObservable()
  // XXX: public final Single toSingle(Callable)
  // XXX: public final Single toSingleDefault(Object)
  // XXX: public final Completable unsubscribeOn(Scheduler)
}
