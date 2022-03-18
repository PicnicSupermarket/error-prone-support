package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Streams;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/** The Refaster templates for the migration of the RxJava {@link Observable} to Reactor. */
final class RxJavaObservableToReactorTemplates {
  private RxJavaObservableToReactorTemplates() {}

  static final class ObservableAmb<T> {
    @BeforeTemplate
    Observable<T> before(Iterable<? extends Observable<T>> sources) {
      return Observable.amb(sources);
    }

    @AfterTemplate
    Observable<T> after(Iterable<? extends Observable<T>> sources) {
      return RxJava2Adapter.fluxToObservable(
          Flux.<T>firstWithSignal(
              Streams.stream(sources)
                  .map(e -> e.toFlowable(BackpressureStrategy.BUFFER))
                  .map(RxJava2Adapter::flowableToFlux)
                  .collect(toImmutableList())));
    }
  }

  // XXX: public static Observable ambArray(ObservableSource[])
  // XXX: public static int bufferSize()
  // XXX: public static Observable combineLatest(Function,int,ObservableSource[])
  // XXX: public static Observable combineLatest(Iterable,Function)
  // XXX: public static Observable combineLatest(Iterable,Function,int)
  // XXX: public static Observable combineLatest(ObservableSource[],Function)
  // XXX: public static Observable combineLatest(ObservableSource[],Function,int)
  // XXX: public static Observable combineLatest(ObservableSource,ObservableSource,BiFunction)
  // XXX: public static Observable
  // combineLatest(ObservableSource,ObservableSource,ObservableSource,Function3)
  // XXX: public static Observable
  // combineLatest(ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function4)
  // XXX: public static Observable
  // combineLatest(ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function5)
  // XXX: public static Observable
  // combineLatest(ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function6)
  // XXX: public static Observable
  // combineLatest(ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function7)
  // XXX: public static Observable
  // combineLatest(ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function8)
  // XXX: public static Observable
  // combineLatest(ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function9)
  // XXX: public static Observable combineLatestDelayError(Function,int,ObservableSource[])
  // XXX: public static Observable combineLatestDelayError(Iterable,Function)
  // XXX: public static Observable combineLatestDelayError(Iterable,Function,int)
  // XXX: public static Observable combineLatestDelayError(ObservableSource[],Function)
  // XXX: public static Observable combineLatestDelayError(ObservableSource[],Function,int)
  // XXX: public static Observable concat(Iterable)
  // XXX: public static Observable concat(ObservableSource)
  // XXX: public static Observable concat(ObservableSource,int)
  // XXX: public static Observable concat(ObservableSource,ObservableSource)
  // XXX: public static Observable concat(ObservableSource,ObservableSource,ObservableSource)
  // XXX: public static Observable
  // concat(ObservableSource,ObservableSource,ObservableSource,ObservableSource)
  // XXX: public static Observable concatArray(ObservableSource[])
  // XXX: public static Observable concatArrayDelayError(ObservableSource[])
  // XXX: public static Observable concatArrayEager(int,int,ObservableSource[])
  // XXX: public static Observable concatArrayEager(ObservableSource[])
  // XXX: public static Observable concatArrayEagerDelayError(int,int,ObservableSource[])
  // XXX: public static Observable concatArrayEagerDelayError(ObservableSource[])
  // XXX: public static Observable concatDelayError(Iterable)
  // XXX: public static Observable concatDelayError(ObservableSource)
  // XXX: public static Observable concatDelayError(ObservableSource,int,boolean)
  // XXX: public static Observable concatEager(Iterable)
  // XXX: public static Observable concatEager(Iterable,int,int)
  // XXX: public static Observable concatEager(ObservableSource)
  // XXX: public static Observable concatEager(ObservableSource,int,int)
  // XXX: public static Observable create(ObservableOnSubscribe)
  // XXX: public static Observable defer(Callable)

  static final class ObservableEmpty<T> {
    @BeforeTemplate
    Observable<T> before() {
      return Observable.empty();
    }

    @AfterTemplate
    Observable<T> after() {
      return RxJava2Adapter.fluxToObservable(Flux.empty());
    }
  }

  // XXX: public static Observable error(Callable)
  // XXX: public static Observable error(Throwable)
  // XXX: public static Observable fromArray(Object[])

  static final class ObservableFromCallable<T> {
    @BeforeTemplate
    Observable<? extends T> before(Callable<? extends T> callable) {
      return Observable.fromCallable(callable);
    }

    @AfterTemplate
    Observable<? extends T> after(Callable<? extends T> callable) {
      return RxJava2Adapter.fluxToObservable(
          Mono.fromSupplier(RxJavaReactorMigrationUtil.callableAsSupplier(callable)).flux());
    }
  }

  // XXX: public static Observable fromFuture(Future)
  // XXX: public static Observable fromFuture(Future,long,TimeUnit)
  // XXX: public static Observable fromFuture(Future,long,TimeUnit,Scheduler)
  // XXX: public static Observable fromFuture(Future,Scheduler)
  // XXX: public static Observable fromIterable(Iterable)

  static final class ObservableFromPublisher<T> {
    @BeforeTemplate
    Observable<T> before(Publisher<? extends T> source) {
      return Observable.fromPublisher(source);
    }

    @AfterTemplate
    Observable<T> after(Publisher<? extends T> source) {
      return RxJava2Adapter.fluxToObservable(Flux.from(source));
    }
  }

  // XXX: public static Observable generate(Callable,BiConsumer)
  // XXX: public static Observable generate(Callable,BiConsumer,Consumer)
  // XXX: public static Observable generate(Callable,BiFunction)
  // XXX: public static Observable generate(Callable,BiFunction,Consumer)
  // XXX: public static Observable generate(Consumer)
  // XXX: public static Observable interval(long,long,TimeUnit)
  // XXX: public static Observable interval(long,long,TimeUnit,Scheduler)
  // XXX: public static Observable interval(long,TimeUnit)
  // XXX: public static Observable interval(long,TimeUnit,Scheduler)
  // XXX: public static Observable intervalRange(long,long,long,long,TimeUnit)
  // XXX: public static Observable intervalRange(long,long,long,long,TimeUnit,Scheduler)

  static final class ObservableJust<T> {
    @BeforeTemplate
    Observable<T> before(T t) {
      return Observable.just(t);
    }

    @AfterTemplate
    Observable<T> after(T t) {
      return RxJava2Adapter.fluxToObservable(Flux.just(t));
    }
  }

  static final class ObservableJustTwo<T> {
    @BeforeTemplate
    Observable<T> before(T t, T t2) {
      return Observable.just(t, t2);
    }

    @AfterTemplate
    Observable<T> after(T t, T t2) {
      return RxJava2Adapter.fluxToObservable(Flux.just(t, t2));
    }
  }

  static final class ObservableJustThree<T> {
    @BeforeTemplate
    Observable<T> before(T t, T t2, T t3) {
      return Observable.just(t, t2, t3);
    }

    @AfterTemplate
    Observable<T> after(T t, T t2, T t3) {
      return RxJava2Adapter.fluxToObservable(Flux.just(t, t2, t3));
    }
  }

  // XXX: public static Observable just(Object,Object,Object,Object)
  // XXX: public static Observable just(Object,Object,Object,Object,Object)
  // XXX: public static Observable just(Object,Object,Object,Object,Object,Object)
  // XXX: public static Observable just(Object,Object,Object,Object,Object,Object,Object)
  // XXX: public static Observable just(Object,Object,Object,Object,Object,Object,Object,Object)
  // XXX: public static Observable
  // just(Object,Object,Object,Object,Object,Object,Object,Object,Object)
  // XXX: public static Observable
  // just(Object,Object,Object,Object,Object,Object,Object,Object,Object,Object)
  // XXX: public static Observable merge(Iterable)
  // XXX: public static Observable merge(Iterable,int)
  // XXX: public static Observable merge(Iterable,int,int)
  // XXX: public static Observable merge(ObservableSource)
  // XXX: public static Observable merge(ObservableSource,int)
  // XXX: public static Observable merge(ObservableSource,ObservableSource)
  // XXX: public static Observable merge(ObservableSource,ObservableSource,ObservableSource)
  // XXX: public static Observable
  // merge(ObservableSource,ObservableSource,ObservableSource,ObservableSource)
  // XXX: public static Observable mergeArray(int,int,ObservableSource[])
  // XXX: public static Observable mergeArray(ObservableSource[])
  // XXX: public static Observable mergeArrayDelayError(int,int,ObservableSource[])
  // XXX: public static Observable mergeArrayDelayError(ObservableSource[])
  // XXX: public static Observable mergeDelayError(Iterable)
  // XXX: public static Observable mergeDelayError(Iterable,int)
  // XXX: public static Observable mergeDelayError(Iterable,int,int)
  // XXX: public static Observable mergeDelayError(ObservableSource)
  // XXX: public static Observable mergeDelayError(ObservableSource,int)
  // XXX: public static Observable mergeDelayError(ObservableSource,ObservableSource)
  // XXX: public static Observable
  // mergeDelayError(ObservableSource,ObservableSource,ObservableSource)
  // XXX: public static Observable
  // mergeDelayError(ObservableSource,ObservableSource,ObservableSource,ObservableSource)
  // XXX: public static Observable never()
  // XXX: public static Observable range(int,int)
  // XXX: public static Observable rangeLong(long,long)
  // XXX: public static Single sequenceEqual(ObservableSource,ObservableSource)
  // XXX: public static Single sequenceEqual(ObservableSource,ObservableSource,BiPredicate)
  // XXX: public static Single sequenceEqual(ObservableSource,ObservableSource,BiPredicate,int)
  // XXX: public static Single sequenceEqual(ObservableSource,ObservableSource,int)
  // XXX: public static Observable switchOnNext(ObservableSource)
  // XXX: public static Observable switchOnNext(ObservableSource,int)
  // XXX: public static Observable switchOnNextDelayError(ObservableSource)
  // XXX: public static Observable switchOnNextDelayError(ObservableSource,int)
  // XXX: public static Observable timer(long,TimeUnit)
  // XXX: public static Observable timer(long,TimeUnit,Scheduler)
  // XXX: public static Observable unsafeCreate(ObservableSource)
  // XXX: public static Observable using(Callable,Function,Consumer)
  // XXX: public static Observable using(Callable,Function,Consumer,boolean)
  // XXX: public static Observable wrap(ObservableSource)
  // XXX: public static Observable zip(Iterable,Function)
  // XXX: public static Observable zip(ObservableSource,Function)
  // XXX: public static Observable zip(ObservableSource,ObservableSource,BiFunction)
  // XXX: public static Observable zip(ObservableSource,ObservableSource,BiFunction,boolean)
  // XXX: public static Observable zip(ObservableSource,ObservableSource,BiFunction,boolean,int)
  // XXX: public static Observable zip(ObservableSource,ObservableSource,ObservableSource,Function3)
  // XXX: public static Observable
  // zip(ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function4)
  // XXX: public static Observable
  // zip(ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function5)
  // XXX: public static Observable
  // zip(ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function6)
  // XXX: public static Observable
  // zip(ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function7)
  // XXX: public static Observable
  // zip(ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function8)
  // XXX: public static Observable
  // zip(ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function9)
  // XXX: public static Observable zipArray(Function,boolean,int,ObservableSource[])
  // XXX: public static Observable zipIterable(Iterable,Function,boolean,int)
  // XXX: public final Single all(Predicate)
  // XXX: public final Observable ambWith(ObservableSource)
  // XXX: public final Single any(Predicate)
  // XXX: public final Object as(ObservableConverter)
  // XXX: public final Object blockingFirst()
  // XXX: public final Object blockingFirst(Object)
  // XXX: public final void blockingForEach(Consumer)
  // XXX: public final Iterable blockingIterable()
  // XXX: public final Iterable blockingIterable(int)
  // XXX: public final Object blockingLast()
  // XXX: public final Object blockingLast(Object)
  // XXX: public final Iterable blockingLatest()
  // XXX: public final Iterable blockingMostRecent(Object)
  // XXX: public final Iterable blockingNext()
  // XXX: public final Object blockingSingle()
  // XXX: public final Object blockingSingle(Object)
  // XXX: public final void blockingSubscribe()
  // XXX: public final void blockingSubscribe(Consumer)
  // XXX: public final void blockingSubscribe(Consumer,Consumer)
  // XXX: public final void blockingSubscribe(Consumer,Consumer,Action)
  // XXX: public final void blockingSubscribe(Observer)
  // XXX: public final Observable buffer(Callable)
  // XXX: public final Observable buffer(Callable,Callable)
  // XXX: public final Observable buffer(int)
  // XXX: public final Observable buffer(int,Callable)
  // XXX: public final Observable buffer(int,int)
  // XXX: public final Observable buffer(int,int,Callable)
  // XXX: public final Observable buffer(long,long,TimeUnit)
  // XXX: public final Observable buffer(long,long,TimeUnit,Scheduler)
  // XXX: public final Observable buffer(long,long,TimeUnit,Scheduler,Callable)
  // XXX: public final Observable buffer(long,TimeUnit)
  // XXX: public final Observable buffer(long,TimeUnit,int)
  // XXX: public final Observable buffer(long,TimeUnit,Scheduler)
  // XXX: public final Observable buffer(long,TimeUnit,Scheduler,int)
  // XXX: public final Observable buffer(long,TimeUnit,Scheduler,int,Callable,boolean)
  // XXX: public final Observable buffer(ObservableSource)
  // XXX: public final Observable buffer(ObservableSource,Callable)
  // XXX: public final Observable buffer(ObservableSource,Function)
  // XXX: public final Observable buffer(ObservableSource,Function,Callable)
  // XXX: public final Observable buffer(ObservableSource,int)
  // XXX: public final Observable cache()
  // XXX: public final Observable cacheWithInitialCapacity(int)
  // XXX: public final Observable cast(Class)
  // XXX: public final Single collect(Callable,BiConsumer)
  // XXX: public final Single collectInto(Object,BiConsumer)
  // XXX: public final Observable compose(ObservableTransformer)
  // XXX: public final Observable concatMap(Function)
  // XXX: public final Observable concatMap(Function,int)
  // XXX: public final Completable concatMapCompletable(Function)
  // XXX: public final Completable concatMapCompletable(Function,int)
  // XXX: public final Completable concatMapCompletableDelayError(Function)
  // XXX: public final Completable concatMapCompletableDelayError(Function,boolean)
  // XXX: public final Completable concatMapCompletableDelayError(Function,boolean,int)
  // XXX: public final Observable concatMapDelayError(Function)
  // XXX: public final Observable concatMapDelayError(Function,int,boolean)
  // XXX: public final Observable concatMapEager(Function)
  // XXX: public final Observable concatMapEager(Function,int,int)
  // XXX: public final Observable concatMapEagerDelayError(Function,boolean)
  // XXX: public final Observable concatMapEagerDelayError(Function,int,int,boolean)
  // XXX: public final Observable concatMapIterable(Function)
  // XXX: public final Observable concatMapIterable(Function,int)
  // XXX: public final Observable concatMapMaybe(Function)
  // XXX: public final Observable concatMapMaybe(Function,int)
  // XXX: public final Observable concatMapMaybeDelayError(Function)
  // XXX: public final Observable concatMapMaybeDelayError(Function,boolean)
  // XXX: public final Observable concatMapMaybeDelayError(Function,boolean,int)
  // XXX: public final Observable concatMapSingle(Function)
  // XXX: public final Observable concatMapSingle(Function,int)
  // XXX: public final Observable concatMapSingleDelayError(Function)
  // XXX: public final Observable concatMapSingleDelayError(Function,boolean)
  // XXX: public final Observable concatMapSingleDelayError(Function,boolean,int)
  // XXX: public final Observable concatWith(CompletableSource)
  // XXX: public final Observable concatWith(MaybeSource)
  // XXX: public final Observable concatWith(ObservableSource)
  // XXX: public final Observable concatWith(SingleSource)
  // XXX: public final Single contains(Object)
  // XXX: public final Single count()
  // XXX: public final Observable debounce(Function)
  // XXX: public final Observable debounce(long,TimeUnit)
  // XXX: public final Observable debounce(long,TimeUnit,Scheduler)
  // XXX: public final Observable defaultIfEmpty(Object)
  // XXX: public final Observable delay(Function)
  // XXX: public final Observable delay(long,TimeUnit)
  // XXX: public final Observable delay(long,TimeUnit,boolean)
  // XXX: public final Observable delay(long,TimeUnit,Scheduler)
  // XXX: public final Observable delay(long,TimeUnit,Scheduler,boolean)
  // XXX: public final Observable delay(ObservableSource,Function)
  // XXX: public final Observable delaySubscription(long,TimeUnit)
  // XXX: public final Observable delaySubscription(long,TimeUnit,Scheduler)
  // XXX: public final Observable delaySubscription(ObservableSource)
  // XXX: public final Observable dematerialize()
  // XXX: public final Observable dematerialize(Function)
  // XXX: public final Observable distinct()
  // XXX: public final Observable distinct(Function)
  // XXX: public final Observable distinct(Function,Callable)
  // XXX: public final Observable distinctUntilChanged()
  // XXX: public final Observable distinctUntilChanged(BiPredicate)
  // XXX: public final Observable distinctUntilChanged(Function)
  // XXX: public final Observable doAfterNext(Consumer)
  // XXX: public final Observable doAfterTerminate(Action)
  // XXX: public final Observable doFinally(Action)
  // XXX: public final Observable doOnComplete(Action)
  // XXX: public final Observable doOnDispose(Action)
  // XXX: public final Observable doOnEach(Consumer)
  // XXX: public final Observable doOnEach(Observer)
  // XXX: public final Observable doOnError(Consumer)
  // XXX: public final Observable doOnLifecycle(Consumer,Action)
  // XXX: public final Observable doOnNext(Consumer)
  // XXX: public final Observable doOnSubscribe(Consumer)
  // XXX: public final Observable doOnTerminate(Action)
  // XXX: public final Maybe elementAt(long)
  // XXX: public final Single elementAt(long,Object)
  // XXX: public final Single elementAtOrError(long)

  // XXX: Default BackPressureStrategy.BUFFER is set.
  static final class ObservableFilter<T> {
    @BeforeTemplate
    Observable<T> before(Observable<T> observable, Predicate<T> predicate) {
      return observable.filter(predicate);
    }

    @AfterTemplate
    Observable<T> after(Observable<T> observable, Predicate<T> predicate) {
      return RxJava2Adapter.fluxToObservable(
          RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
              .filter(RxJavaReactorMigrationUtil.toJdkPredicate(predicate)));
    }
  }

  // XXX: public final Single first(Object)

  // XXX: Default BUFFER is chosen here.
  static final class MaybeFirstElement<T> {
    @BeforeTemplate
    Maybe<T> before(Observable<T> observable) {
      return observable.firstElement();
    }

    @AfterTemplate
    Maybe<T> after(Observable<T> observable) {
      return RxJava2Adapter.monoToMaybe(
          RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER).next());
    }
  }

  // XXX: public final Single firstOrError()
  // XXX: public final Observable flatMap(Function)

  // XXX: Add test
  // XXX: Default BUFFER is set here.
  static final class ObservableFlatMap<I, T extends I, O, P extends ObservableSource<O>> {
    @BeforeTemplate
    Observable<O> before(
        Observable<T> observable,
        Function<? super T, ? extends ObservableSource<? extends O>> function) {
      return observable.flatMap(function);
    }

    @UseImportPolicy(ImportPolicy.IMPORT_CLASS_DIRECTLY)
    @AfterTemplate
    Observable<O> after(Observable<T> observable, Function<I, P> function) {
      return RxJava2Adapter.fluxToObservable(
          RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
              .flatMap(
                  z ->
                      RxJava2Adapter.observableToFlux(
                          Observable.wrap(
                              RxJavaReactorMigrationUtil.<I, P>toJdkFunction(function).apply(z)),
                          BackpressureStrategy.BUFFER)));
    }
  }

  // XXX: public final Observable flatMap(Function,BiFunction)
  // XXX: public final Observable flatMap(Function,BiFunction,boolean)
  // XXX: public final Observable flatMap(Function,BiFunction,boolean,int)
  // XXX: public final Observable flatMap(Function,BiFunction,boolean,int,int)
  // XXX: public final Observable flatMap(Function,BiFunction,int)
  // XXX: public final Observable flatMap(Function,boolean)
  // XXX: public final Observable flatMap(Function,boolean,int)
  // XXX: public final Observable flatMap(Function,boolean,int,int)
  // XXX: public final Observable flatMap(Function,Function,Callable)
  // XXX: public final Observable flatMap(Function,Function,Callable,int)
  // XXX: public final Observable flatMap(Function,int)
  // XXX: public final Completable flatMapCompletable(Function)
  // XXX: public final Completable flatMapCompletable(Function,boolean)

  static final class ObservableFromIterable<T> {
    @BeforeTemplate
    Observable<T> before(Iterable<? extends T> iterable) {
      return Observable.fromIterable(iterable);
    }

    @AfterTemplate
    Observable<T> after(Iterable<? extends T> iterable) {
      return RxJava2Adapter.fluxToObservable(Flux.fromIterable(iterable));
    }
  }

  // XXX: public final Observable flatMapIterable(Function,BiFunction)

  static final class ObservableFlatMapMaybe<T, R, O extends R, M extends MaybeSource<O>> {
    Observable<O> before(
        Observable<T> observable, Function<? super T, ? extends MaybeSource<? extends O>> mapper) {
      return observable.flatMapMaybe(mapper);
    }

    Observable<O> after(Observable<T> observable, Function<T, M> mapper) {
      return RxJava2Adapter.fluxToObservable(
          RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
              .flatMap(
                  t ->
                      RxJava2Adapter.maybeToMono(
                          Maybe.wrap(
                              RxJavaReactorMigrationUtil.<T, M>toJdkFunction(mapper).apply(t)))));
    }
  } // XXX: public final Observable flatMapMaybe(Function,boolean)
  // XXX: public final Observable flatMapSingle(Function)
  // XXX: public final Observable flatMapSingle(Function,boolean)
  // XXX: public final Disposable forEach(Consumer)
  // XXX: public final Disposable forEachWhile(Predicate)
  // XXX: public final Disposable forEachWhile(Predicate,Consumer)
  // XXX: public final Disposable forEachWhile(Predicate,Consumer,Action)
  // XXX: public final Observable groupBy(Function)
  // XXX: public final Observable groupBy(Function,boolean)
  // XXX: public final Observable groupBy(Function,Function)
  // XXX: public final Observable groupBy(Function,Function,boolean)
  // XXX: public final Observable groupBy(Function,Function,boolean,int)
  // XXX: public final Observable groupJoin(ObservableSource,Function,Function,BiFunction)
  // XXX: public final Observable hide()

  static final class ObservableIgnoreElements<T> {
    @BeforeTemplate
    Completable before(Observable<T> observable) {
      return observable.ignoreElements();
    }

    @AfterTemplate
    Completable after(Observable<T> observable) {
      return RxJava2Adapter.monoToCompletable(
          RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
              .ignoreElements()
              .then());
    }
  }

  // XXX: public final Single isEmpty()
  // XXX: public final Observable join(ObservableSource,Function,Function,BiFunction)
  // XXX: public final Single last(Object)
  // XXX: public final Maybe lastElement()
  // XXX: public final Single lastOrError()
  // XXX: public final Observable lift(ObservableOperator)
  // XXX: public final Observable map(Function)
  // XXX: public final Observable materialize()
  // XXX: public final Observable mergeWith(CompletableSource)
  // XXX: public final Observable mergeWith(MaybeSource)
  // XXX: public final Observable mergeWith(ObservableSource)
  // XXX: public final Observable mergeWith(SingleSource)
  // XXX: public final Observable observeOn(Scheduler)
  // XXX: public final Observable observeOn(Scheduler,boolean)
  // XXX: public final Observable observeOn(Scheduler,boolean,int)
  // XXX: public final Observable ofType(Class)
  // XXX: public final Observable onErrorResumeNext(Function)
  // XXX: public final Observable onErrorResumeNext(ObservableSource)
  // XXX: public final Observable onErrorReturn(Function)
  // XXX: public final Observable onErrorReturnItem(Object)
  // XXX: public final Observable onExceptionResumeNext(ObservableSource)
  // XXX: public final Observable onTerminateDetach()
  // XXX: public final ConnectableObservable publish()
  // XXX: public final Observable publish(Function)
  // XXX: public final Maybe reduce(BiFunction)
  // XXX: public final Single reduce(Object,BiFunction)
  // XXX: public final Single reduceWith(Callable,BiFunction)
  // XXX: public final Observable repeat()
  // XXX: public final Observable repeat(long)
  // XXX: public final Observable repeatUntil(BooleanSupplier)
  // XXX: public final Observable repeatWhen(Function)
  // XXX: public final ConnectableObservable replay()
  // XXX: public final Observable replay(Function)
  // XXX: public final Observable replay(Function,int)
  // XXX: public final Observable replay(Function,int,long,TimeUnit)
  // XXX: public final Observable replay(Function,int,long,TimeUnit,Scheduler)
  // XXX: public final Observable replay(Function,int,Scheduler)
  // XXX: public final Observable replay(Function,long,TimeUnit)
  // XXX: public final Observable replay(Function,long,TimeUnit,Scheduler)
  // XXX: public final Observable replay(Function,Scheduler)
  // XXX: public final ConnectableObservable replay(int)
  // XXX: public final ConnectableObservable replay(int,long,TimeUnit)
  // XXX: public final ConnectableObservable replay(int,long,TimeUnit,Scheduler)
  // XXX: public final ConnectableObservable replay(int,Scheduler)
  // XXX: public final ConnectableObservable replay(long,TimeUnit)
  // XXX: public final ConnectableObservable replay(long,TimeUnit,Scheduler)
  // XXX: public final ConnectableObservable replay(Scheduler)
  // XXX: public final Observable retry()
  // XXX: public final Observable retry(BiPredicate)
  // XXX: public final Observable retry(long)
  // XXX: public final Observable retry(long,Predicate)
  // XXX: public final Observable retry(Predicate)
  // XXX: public final Observable retryUntil(BooleanSupplier)
  // XXX: public final Observable retryWhen(Function)
  // XXX: public final void safeSubscribe(Observer)
  // XXX: public final Observable sample(long,TimeUnit)
  // XXX: public final Observable sample(long,TimeUnit,boolean)
  // XXX: public final Observable sample(long,TimeUnit,Scheduler)
  // XXX: public final Observable sample(long,TimeUnit,Scheduler,boolean)
  // XXX: public final Observable sample(ObservableSource)
  // XXX: public final Observable sample(ObservableSource,boolean)
  // XXX: public final Observable scan(BiFunction)
  // XXX: public final Observable scan(Object,BiFunction)
  // XXX: public final Observable scanWith(Callable,BiFunction)
  // XXX: public final Observable serialize()
  // XXX: public final Observable share()
  // XXX: public final Single single(Object)
  // XXX: public final Maybe singleElement()
  // XXX: public final Single singleOrError()
  // XXX: public final Observable skip(long)
  // XXX: public final Observable skip(long,TimeUnit)
  // XXX: public final Observable skip(long,TimeUnit,Scheduler)
  // XXX: public final Observable skipLast(int)
  // XXX: public final Observable skipLast(long,TimeUnit)
  // XXX: public final Observable skipLast(long,TimeUnit,boolean)
  // XXX: public final Observable skipLast(long,TimeUnit,Scheduler)
  // XXX: public final Observable skipLast(long,TimeUnit,Scheduler,boolean)
  // XXX: public final Observable skipLast(long,TimeUnit,Scheduler,boolean,int)
  // XXX: public final Observable skipUntil(ObservableSource)
  // XXX: public final Observable skipWhile(Predicate)
  // XXX: public final Observable sorted()
  // XXX: public final Observable sorted(Comparator)
  // XXX: public final Observable startWith(Iterable)
  // XXX: public final Observable startWith(Object)
  // XXX: public final Observable startWith(ObservableSource)
  // XXX: public final Observable startWithArray(Object[])
  // XXX: public final Disposable subscribe()
  // XXX: public final Disposable subscribe(Consumer)
  // XXX: public final Disposable subscribe(Consumer,Consumer)
  // XXX: public final Disposable subscribe(Consumer,Consumer,Action)
  // XXX: public final Disposable subscribe(Consumer,Consumer,Action,Consumer)
  // XXX: public final void subscribe(Observer)
  // XXX: public final Observable subscribeOn(Scheduler)
  // XXX: public final Observer subscribeWith(Observer)
  // XXX: public final Observable switchIfEmpty(ObservableSource)
  // XXX: public final Observable switchMap(Function)
  // XXX: public final Observable switchMap(Function,int)
  // XXX: public final Completable switchMapCompletable(Function)
  // XXX: public final Completable switchMapCompletableDelayError(Function)
  // XXX: public final Observable switchMapDelayError(Function)
  // XXX: public final Observable switchMapDelayError(Function,int)
  // XXX: public final Observable switchMapMaybe(Function)
  // XXX: public final Observable switchMapMaybeDelayError(Function)
  // XXX: public final Observable switchMapSingle(Function)
  // XXX: public final Observable switchMapSingleDelayError(Function)
  // XXX: public final Observable take(long)
  // XXX: public final Observable take(long,TimeUnit)
  // XXX: public final Observable take(long,TimeUnit,Scheduler)
  // XXX: public final Observable takeLast(int)
  // XXX: public final Observable takeLast(long,long,TimeUnit)
  // XXX: public final Observable takeLast(long,long,TimeUnit,Scheduler)
  // XXX: public final Observable takeLast(long,long,TimeUnit,Scheduler,boolean,int)
  // XXX: public final Observable takeLast(long,TimeUnit)
  // XXX: public final Observable takeLast(long,TimeUnit,boolean)
  // XXX: public final Observable takeLast(long,TimeUnit,Scheduler)
  // XXX: public final Observable takeLast(long,TimeUnit,Scheduler,boolean)
  // XXX: public final Observable takeLast(long,TimeUnit,Scheduler,boolean,int)
  // XXX: public final Observable takeUntil(ObservableSource)
  // XXX: public final Observable takeUntil(Predicate)
  // XXX: public final Observable takeWhile(Predicate)
  // XXX: public final Observable throttleFirst(long,TimeUnit)
  // XXX: public final Observable throttleFirst(long,TimeUnit,Scheduler)
  // XXX: public final Observable throttleLast(long,TimeUnit)
  // XXX: public final Observable throttleLast(long,TimeUnit,Scheduler)
  // XXX: public final Observable throttleLatest(long,TimeUnit)
  // XXX: public final Observable throttleLatest(long,TimeUnit,boolean)
  // XXX: public final Observable throttleLatest(long,TimeUnit,Scheduler)
  // XXX: public final Observable throttleLatest(long,TimeUnit,Scheduler,boolean)
  // XXX: public final Observable throttleWithTimeout(long,TimeUnit)
  // XXX: public final Observable throttleWithTimeout(long,TimeUnit,Scheduler)
  // XXX: public final Observable timeInterval()
  // XXX: public final Observable timeInterval(Scheduler)
  // XXX: public final Observable timeInterval(TimeUnit)
  // XXX: public final Observable timeInterval(TimeUnit,Scheduler)
  // XXX: public final Observable timeout(Function)
  // XXX: public final Observable timeout(Function,ObservableSource)

  // Default BackpressureStrategy.BUFFER is set
  static final class ObservableTimeoutLongTimeUnit<T> {
    @BeforeTemplate
    Observable<T> before(Observable<T> observable, long timeout, TimeUnit unit) {
      return observable.timeout(timeout, unit);
    }

    @AfterTemplate
    Observable<T> after(Observable<T> observable, long timeout, TimeUnit unit) {
      return RxJava2Adapter.fluxToObservable(
          RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
              .timeout(Duration.of(timeout, unit.toChronoUnit())));
    }
  }

  // XXX: public final Observable timeout(long,TimeUnit,ObservableSource)
  // XXX: public final Observable timeout(long,TimeUnit,Scheduler)
  // XXX: public final Observable timeout(long,TimeUnit,Scheduler,ObservableSource)
  // XXX: public final Observable timeout(ObservableSource,Function)
  // XXX: public final Observable timeout(ObservableSource,Function,ObservableSource)
  // XXX: public final Observable timestamp()
  // XXX: public final Observable timestamp(Scheduler)
  // XXX: public final Observable timestamp(TimeUnit)
  // XXX: public final Observable timestamp(TimeUnit,Scheduler)
  // XXX: public final Object to(Function)

  static final class ObservableToFlowable<T> {
    @BeforeTemplate
    Flowable<T> before(Observable<T> observable, BackpressureStrategy strategy) {
      return observable.toFlowable(strategy);
    }

    @AfterTemplate
    Flowable<T> after(Observable<T> observable, BackpressureStrategy strategy) {
      return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.observableToFlux(observable, strategy));
    }
  }

  // XXX: public final Future toFuture()
  // XXX: public final Single toList()
  // XXX: public final Single toList(Callable)
  // XXX: public final Single toList(int)
  // XXX: public final Single toMap(Function)
  // XXX: public final Single toMap(Function,Function)
  // XXX: public final Single toMap(Function,Function,Callable)
  // XXX: public final Single toMultimap(Function)
  // XXX: public final Single toMultimap(Function,Function)
  // XXX: public final Single toMultimap(Function,Function,Callable)
  // XXX: public final Single toMultimap(Function,Function,Callable,Function)
  // XXX: public final Single toSortedList()
  // XXX: public final Single toSortedList(Comparator)
  // XXX: public final Single toSortedList(Comparator,int)
  // XXX: public final Single toSortedList(int)
  // XXX: public final Observable unsubscribeOn(Scheduler)
  // XXX: public final Observable window(Callable)
  // XXX: public final Observable window(Callable,int)
  // XXX: public final Observable window(long)
  // XXX: public final Observable window(long,long)
  // XXX: public final Observable window(long,long,int)
  // XXX: public final Observable window(long,long,TimeUnit)
  // XXX: public final Observable window(long,long,TimeUnit,Scheduler)
  // XXX: public final Observable window(long,long,TimeUnit,Scheduler,int)
  // XXX: public final Observable window(long,TimeUnit)
  // XXX: public final Observable window(long,TimeUnit,long)
  // XXX: public final Observable window(long,TimeUnit,long,boolean)
  // XXX: public final Observable window(long,TimeUnit,Scheduler)
  // XXX: public final Observable window(long,TimeUnit,Scheduler,long)
  // XXX: public final Observable window(long,TimeUnit,Scheduler,long,boolean)
  // XXX: public final Observable window(long,TimeUnit,Scheduler,long,boolean,int)
  // XXX: public final Observable window(ObservableSource)
  // XXX: public final Observable window(ObservableSource,Function)
  // XXX: public final Observable window(ObservableSource,Function,int)
  // XXX: public final Observable window(ObservableSource,int)
  // XXX: public final Observable withLatestFrom(Iterable,Function)
  // XXX: public final Observable withLatestFrom(ObservableSource,BiFunction)
  // XXX: public final Observable withLatestFrom(ObservableSource[],Function)
  // XXX: public final Observable withLatestFrom(ObservableSource,ObservableSource,Function3)
  // XXX: public final Observable
  // withLatestFrom(ObservableSource,ObservableSource,ObservableSource,Function4)
  // XXX: public final Observable
  // withLatestFrom(ObservableSource,ObservableSource,ObservableSource,ObservableSource,Function5)
  // XXX: public final Observable zipWith(Iterable,BiFunction)
  // XXX: public final Observable zipWith(ObservableSource,BiFunction)
  // XXX: public final Observable zipWith(ObservableSource,BiFunction,boolean)
  // XXX: public final Observable zipWith(ObservableSource,BiFunction,boolean,int)

  // XXX: Default BackpressureStrategy.BUFFER is set
  @SuppressWarnings("unchecked")
  static final class ObservableTestAssertResultItem<T> {
    @BeforeTemplate
    void before(Observable<T> observable, T item) throws InterruptedException {
      Refaster.anyOf(
          observable.test().await().assertResult(item),
          observable.test().await().assertValue(item));
    }

    @AfterTemplate
    void after(Observable<T> observable, T item) {
      RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
          .as(StepVerifier::create)
          .expectNext(item)
          .verifyComplete();
    }
  }

  // XXX: Default BackpressureStrategy.BUFFER is set
  @SuppressWarnings("unchecked")
  static final class ObservableTestAssertResult<T> {
    @BeforeTemplate
    void before(Observable<T> observable) throws InterruptedException {
      Refaster.anyOf(observable.test().await().assertResult(), observable.test().await());
    }

    @AfterTemplate
    void after(Observable<T> observable) {
      RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
          .as(StepVerifier::create)
          .verifyComplete();
    }
  }

  @SuppressWarnings("unchecked")
  static final class ObservableTestAssertResultTwoItems<T> {
    @BeforeTemplate
    void before(Observable<T> observable, T t1, T t2) throws InterruptedException {
      observable.test().await().assertResult(t1, t2);
    }

    @AfterTemplate
    void after(Observable<T> observable, T t1, T t2) {
      RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
          .as(StepVerifier::create)
          .expectNext(t1, t2)
          .verifyComplete();
    }
  }

  // XXX: Default BackpressureStrategy.BUFFER is set
  static final class ObservableTestAssertValue<T> {
    @BeforeTemplate
    void before(Observable<T> observable, Predicate<T> predicate) throws InterruptedException {
      Refaster.anyOf(
          observable.test().await().assertValue(predicate),
          observable.test().await().assertValue(predicate).assertNoErrors().assertComplete(),
          observable.test().await().assertComplete().assertValue(predicate),
          observable.test().await().assertValue(predicate).assertComplete());
    }

    @AfterTemplate
    void after(Observable<T> observable, Predicate<T> predicate) {
      RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
          .as(StepVerifier::create)
          .expectNextMatches(RxJavaReactorMigrationUtil.toJdkPredicate(predicate))
          .verifyComplete();
    }
  }

  // XXX: Default BackpressureStrategy.BUFFER is set
  static final class ObservableTestAssertResultValues<T> {
    @BeforeTemplate
    void before(Observable<T> observable, @Repeated T item) throws InterruptedException {
      Refaster.anyOf(
          observable.test().await().assertResult(Refaster.asVarargs(item)),
          observable.test().await().assertValues(Refaster.asVarargs(item)));
    }

    @AfterTemplate
    void after(Observable<T> observable, @Repeated T item) {
      RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
          .as(StepVerifier::create)
          .expectNext(item)
          .verifyComplete();
    }
  }

  // XXX: Default BackpressureStrategy.BUFFER is set
  static final class ObservableTestAssertComplete<T> {
    @BeforeTemplate
    void before(Observable<T> observable) throws InterruptedException {
      observable.test().await().assertComplete();
    }

    @AfterTemplate
    void after(Observable<T> observable) {
      RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
          .as(StepVerifier::create)
          .verifyComplete();
    }
  }

  // XXX: Default BackpressureStrategy.BUFFER is set
  static final class ObservableTestAssertErrorClass<T> {
    @BeforeTemplate
    void before(Observable<T> observable, Class<? extends Throwable> errorClass)
        throws InterruptedException {
      observable.test().await().assertError(errorClass);
    }

    @AfterTemplate
    void after(Observable<T> observable, Class<? extends Throwable> errorClass) {
      RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
          .as(StepVerifier::create)
          .verifyError(errorClass);
    }
  }

  // XXX: Default BackpressureStrategy.BUFFER is set
  static final class ObservableTestAssertNoErrors<T> {
    @BeforeTemplate
    void before(Observable<T> observable) throws InterruptedException {
      observable.test().await().assertNoErrors();
    }

    @AfterTemplate
    void after(Observable<T> observable) {
      RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
          .as(StepVerifier::create)
          .verifyComplete();
    }
  }

  // XXX: Default BackpressureStrategy.BUFFER is set
  static final class ObservableTestAssertValueCount<T> {
    @BeforeTemplate
    void before(Observable<T> observable, int count) throws InterruptedException {
      observable.test().await().assertValueCount(count);
    }

    @AfterTemplate
    void after(Observable<T> observable, int count) {
      RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
          .as(StepVerifier::create)
          .expectNextCount(count)
          .verifyComplete();
    }
  }

  // XXX: Add test
  // XXX: Default BackpressureStrategy.BUFFER is set
  @SuppressWarnings("unchecked")
  static final class ObservableTestAssertFailure<T> {
    @BeforeTemplate
    void before(Observable<T> observable, Class<? extends Throwable> error)
        throws InterruptedException {
      observable.test().await().assertFailure(error);
    }

    @AfterTemplate
    void after(Observable<T> observable, Class<? extends Throwable> error) {
      RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
          .as(StepVerifier::create)
          .verifyError(error);
    }
  }

  // XXX: Add test
  // XXX: Default BackpressureStrategy.BUFFER is set
  static final class ObservableTestAssertNoValues<T> {
    @BeforeTemplate
    void before(Observable<T> observable) throws InterruptedException {
      Refaster.anyOf(
          observable.test().await().assertNoValues(),
          observable.test().await().assertNoValues().assertComplete());
    }

    @AfterTemplate
    void after(Observable<T> observable) {
      RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
          .as(StepVerifier::create)
          .verifyComplete();
    }
  }

  // XXX: Add test
  // XXX: This introduces AssertJ dependency
  @SuppressWarnings("unchecked")
  static final class ObservableTestAssertFailureAndMessage<T> {
    @BeforeTemplate
    void before(Observable<T> observable, Class<? extends Throwable> error, String message)
        throws InterruptedException {
      observable.test().await().assertFailureAndMessage(error, message);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.IMPORT_CLASS_DIRECTLY)
    void after(Observable<T> observable, Class<? extends Throwable> error, String message) {
      RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER)
          .as(StepVerifier::create)
          .expectErrorSatisfies(
              t -> assertThat(t).isInstanceOf(error).hasMessageContaining(message))
          .verify();
    }
  }

  // XXX: public final TestObserver test(boolean)
}
