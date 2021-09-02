package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;

/** The Refaster templates for the migration of the RxJava Flowable type to Reactor */
final class RxJavaFlowableToReactorTemplates {

  private RxJavaFlowableToReactorTemplates() {}

  // XXX: static Flowable amb(Iterable)

  // XXX: Write test
  static final class FlowableAmbArray<T> {
    @BeforeTemplate
    Flowable<T> before(Publisher<? extends T>... sources) {
      return Flowable.ambArray(sources);
    }

    @AfterTemplate
    Flowable<T> after(Publisher<? extends T>... sources) {
      return RxJava2Adapter.fluxToFlowable(Flux.firstWithSignal(sources));
    }
  }

  // XXX: static int bufferSize() --> How rewrite this one? Is it valid to do so?
  // Integer.MAX_VALUE...
  // XXX: static Flowable combineLatest(Function,Publisher[])
  // XXX: static Flowable combineLatest(Iterable,Function)
  // XXX: static Flowable combineLatest(Iterable,Function,int)
  // XXX: static Flowable combineLatest(Publisher[],Function)
  // XXX: static Flowable combineLatest(Publisher[],Function,int)

  static final class FlowableCombineLatest<T1, T2, R> {
    @BeforeTemplate
    Flowable<R> before(
        Publisher<? extends T1> p1,
        Publisher<? extends T2> p2,
        BiFunction<? super T1, ? super T2, ? extends R> combiner) {
      return Flowable.combineLatest(p1, p2, combiner);
    }

    @AfterTemplate
    Flowable<R> after(
        Publisher<? extends T1> p1,
        Publisher<? extends T2> p2,
        BiFunction<? super T1, ? super T2, ? extends R> combiner) {
      return RxJava2Adapter.fluxToFlowable(
          Flux.<T1, T2, R>combineLatest(
              p1,
              p2,
              RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkBiFunction(combiner)));
    }
  }

  // XXX: static Flowable combineLatest(Publisher,Publisher,Publisher,Function3)
  // XXX: static Flowable combineLatest(Publisher,Publisher,Publisher,Publisher,Function4)
  // XXX: static Flowable
  // combineLatest(Publisher,Publisher,Publisher,Publisher,Publisher,Function5)
  // XXX: static Flowable
  // combineLatest(Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Function6)
  // XXX: static Flowable
  // combineLatest(Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Function7)
  // XXX: static Flowable
  // combineLatest(Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Function8)
  // XXX: static Flowable
  // combineLatest(Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Function9)
  // XXX: static Flowable combineLatestDelayError(Function,int,Publisher[])
  // XXX: static Flowable combineLatestDelayError(Function,Publisher[])
  // XXX: static Flowable combineLatestDelayError(Iterable,Function)
  // XXX: static Flowable combineLatestDelayError(Iterable,Function,int)
  // XXX: static Flowable combineLatestDelayError(Publisher[],Function)
  // XXX: static Flowable combineLatestDelayError(Publisher[],Function,int)
  // XXX: static Flowable concat(Iterable)
  // XXX: static Flowable concat(Publisher)
  // XXX: static Flowable concat(Publisher,int)
  // XXX: static Flowable concat(Publisher,Publisher)
  // XXX: static Flowable concat(Publisher,Publisher,Publisher)
  // XXX: static Flowable concat(Publisher,Publisher,Publisher,Publisher)
  // XXX: static Flowable concatArray(Publisher[])
  // XXX: static Flowable concatArrayDelayError(Publisher[])
  // XXX: static Flowable concatArrayEager(int,int,Publisher[])
  // XXX: static Flowable concatArrayEager(Publisher[])
  // XXX: static Flowable concatArrayEagerDelayError(int,int,Publisher[])
  // XXX: static Flowable concatArrayEagerDelayError(Publisher[])
  // XXX: static Flowable concatDelayError(Iterable)
  // XXX: static Flowable concatDelayError(Publisher)
  // XXX: static Flowable concatDelayError(Publisher,int,boolean)
  // XXX: static Flowable concatEager(Iterable)
  // XXX: static Flowable concatEager(Iterable,int,int)
  // XXX: static Flowable concatEager(Publisher)
  // XXX: static Flowable concatEager(Publisher,int,int)
  // XXX: static Flowable create(FlowableOnSubscribe,BackpressureStrategy)

  static final class FlowableDefer<T> {
    @BeforeTemplate
    Flowable<T> before(Callable<? extends Publisher<? extends T>> supplier) {
      return Flowable.defer(supplier);
    }

    @AfterTemplate
    Flowable<T> after(Callable<? extends Publisher<T>> supplier) {
      return RxJava2Adapter.fluxToFlowable(
          Flux.defer(
              RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.callableAsSupplier(supplier)));
    }
  }

  static final class FlowableEmpty<T> {
    @BeforeTemplate
    Flowable<T> before() {
      return Flowable.empty();
    }

    @AfterTemplate
    Flowable<T> after() {
      return RxJava2Adapter.fluxToFlowable(Flux.empty());
    }
  }

  // XXX: Use `CanBeCoercedTo`.
  static final class FlowableErrorCallable<T> {
    @BeforeTemplate
    Flowable<T> before(Callable<? extends Throwable> throwable) {
      return Flowable.error(throwable);
    }

    @AfterTemplate
    Flowable<T> after(Supplier<? extends Throwable> throwable) {
      return RxJava2Adapter.fluxToFlowable(Flux.error(throwable));
    }
  }

  static final class FlowableErrorThrowable<T> {
    @BeforeTemplate
    Flowable<T> before(Throwable throwable) {
      return Flowable.error(throwable);
    }

    @AfterTemplate
    Flowable<T> after(Throwable throwable) {
      return RxJava2Adapter.fluxToFlowable(Flux.error(throwable));
    }
  }

  // XXX: Or should this be Object[] instead of T...?
  static final class FlowableFromArray<T> {
    @BeforeTemplate
    Flowable<T> before(T... items) {
      return Flowable.fromArray(items);
    }

    @AfterTemplate
    Flowable<T> after(T... items) {
      return RxJava2Adapter.fluxToFlowable(Flux.fromArray(items));
    }
  }
  // XXX: static Flowable fromCallable(Callable) --> Required
  // XXX: static Flowable fromFuture(Future)
  // XXX: static Flowable fromFuture(Future,long,TimeUnit)
  // XXX: static Flowable fromFuture(Future,long,TimeUnit,Scheduler)
  // XXX: static Flowable fromFuture(Future,Scheduler)

  static final class FlowableFromIterable<T> {
    @BeforeTemplate
    Flowable<T> before(Iterable<? extends T> iterable) {
      return Flowable.fromIterable(iterable);
    }

    @AfterTemplate
    Flowable<T> after(Iterable<? extends T> iterable) {
      return RxJava2Adapter.fluxToFlowable(Flux.fromIterable(iterable));
    }
  }

  static final class FlowableFromPublisher<T> {
    @BeforeTemplate
    Flowable<T> before(Publisher<? extends T> source) {
      return Flowable.fromPublisher(source);
    }

    @AfterTemplate
    Flowable<T> after(Publisher<? extends T> source) {
      return RxJava2Adapter.fluxToFlowable(Flux.from(source));
    }
  }

  // XXX: static Flowable generate(Callable,BiConsumer)
  // XXX: static Flowable generate(Callable,BiConsumer,Consumer)
  // XXX: static Flowable generate(Callable,BiFunction)
  // XXX: static Flowable generate(Callable,BiFunction,Consumer)
  // XXX: static Flowable generate(Consumer)
  // XXX: static Flowable interval(long,long,TimeUnit)
  // XXX: static Flowable interval(long,long,TimeUnit,Scheduler)
  // XXX: static Flowable interval(long,TimeUnit)
  // XXX: static Flowable interval(long,TimeUnit,Scheduler)
  // XXX: static Flowable intervalRange(long,long,long,long,TimeUnit)
  // XXX: static Flowable intervalRange(long,long,long,long,TimeUnit,Scheduler)

  static final class FlowableJust<T> {
    @BeforeTemplate
    Flowable<T> before(T t) {
      return Flowable.just(t);
    }

    @AfterTemplate
    Flowable<T> after(T t) {
      return RxJava2Adapter.fluxToFlowable(Flux.just(t));
    }
  }

  static final class FlowableJustTwo<T> {
    @BeforeTemplate
    Flowable<T> before(T t, @Repeated T arguments) {
      return Flowable.just(t, arguments);
    }

    @AfterTemplate
    Flowable<T> after(T t, @Repeated T arguments) {
      return RxJava2Adapter.fluxToFlowable(Flux.just(t, arguments));
    }
  }

  // XXX: static Flowable just(Object,Object,Object)
  // XXX: static Flowable just(Object,Object,Object,Object)
  // XXX: static Flowable just(Object,Object,Object,Object,Object)
  // XXX: static Flowable just(Object,Object,Object,Object,Object,Object)
  // XXX: static Flowable just(Object,Object,Object,Object,Object,Object,Object)
  // XXX: static Flowable just(Object,Object,Object,Object,Object,Object,Object,Object)
  // XXX: static Flowable
  // just(Object,Object,Object,Object,Object,Object,Object,Object,Object)
  // XXX: static Flowable
  // just(Object,Object,Object,Object,Object,Object,Object,Object,Object,Object)
  // XXX: static Flowable merge(Iterable)
  // XXX: static Flowable merge(Iterable,int)
  // XXX: static Flowable merge(Iterable,int,int)
  // XXX: static Flowable merge(Publisher)
  // XXX: static Flowable merge(Publisher,int)
  // XXX: static Flowable merge(Publisher,Publisher)
  // XXX: static Flowable merge(Publisher,Publisher,Publisher)
  // XXX: static Flowable merge(Publisher,Publisher,Publisher,Publisher)
  // XXX: static Flowable mergeArray(int,int,Publisher[])
  // XXX: static Flowable mergeArray(Publisher[])
  // XXX: static Flowable mergeArrayDelayError(int,int,Publisher[])
  // XXX: static Flowable mergeArrayDelayError(Publisher[])
  // XXX: static Flowable mergeDelayError(Iterable)
  // XXX: static Flowable mergeDelayError(Iterable,int)
  // XXX: static Flowable mergeDelayError(Iterable,int,int)
  // XXX: static Flowable mergeDelayError(Publisher)
  // XXX: static Flowable mergeDelayError(Publisher,int)
  // XXX: static Flowable mergeDelayError(Publisher,Publisher)
  // XXX: static Flowable mergeDelayError(Publisher,Publisher,Publisher)
  // XXX: static Flowable mergeDelayError(Publisher,Publisher,Publisher,Publisher)
  // XXX: static Flowable never()

  static final class FlowableRange {
    @BeforeTemplate
    Flowable<Integer> before(Integer start, Integer count) {
      return Flowable.range(start, count);
    }

    @AfterTemplate
    Flowable<Integer> after(Integer start, Integer count) {
      return RxJava2Adapter.fluxToFlowable(Flux.range(start, count));
    }
  }

  // XXX: static Flowable rangeLong(long,long) --> Required

  // XXX: static Single sequenceEqual(Publisher,Publisher)
  // XXX: static Single sequenceEqual(Publisher,Publisher,BiPredicate)
  // XXX: static Single sequenceEqual(Publisher,Publisher,BiPredicate,int)
  // XXX: static Single sequenceEqual(Publisher,Publisher,int)
  // XXX: static Flowable switchOnNext(Publisher)
  // XXX: static Flowable switchOnNext(Publisher,int)
  // XXX: static Flowable switchOnNextDelayError(Publisher)
  // XXX: static Flowable switchOnNextDelayError(Publisher,int)
  // XXX: static Flowable timer(long,TimeUnit)
  // XXX: static Flowable timer(long,TimeUnit,Scheduler)
  // XXX: static Flowable unsafeCreate(Publisher)
  // XXX: static Flowable using(Callable,Function,Consumer)
  // XXX: static Flowable using(Callable,Function,Consumer,boolean)
  // XXX: static Flowable zip(Iterable,Function)
  // XXX: static Flowable zip(Publisher,Function)
  // XXX: static Flowable zip(Publisher,Publisher,BiFunction) --> Required
  // XXX: static Flowable zip(Publisher,Publisher,BiFunction,boolean)
  // XXX: static Flowable zip(Publisher,Publisher,BiFunction,boolean,int)
  // XXX: static Flowable zip(Publisher,Publisher,Publisher,Function3)
  // XXX: static Flowable zip(Publisher,Publisher,Publisher,Publisher,Function4)
  // XXX: static Flowable zip(Publisher,Publisher,Publisher,Publisher,Publisher,Function5)
  // XXX: static Flowable
  // zip(Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Function6)
  // XXX: static Flowable
  // zip(Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Function7)
  // XXX: static Flowable
  // zip(Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Function8)
  // XXX: static Flowable
  // zip(Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Publisher,Function9)
  // XXX: static Flowable zipArray(Function,boolean,int,Publisher[])
  // XXX: static Flowable zipIterable(Iterable,Function,boolean,int)

  static final class FlowableAll<T> {
    @BeforeTemplate
    Single<Boolean> before(Flowable<T> flowable, Predicate<? super T> predicate) {
      return flowable.all(predicate);
    }

    @AfterTemplate
    Single<Boolean> after(Flowable<T> flowable, Predicate<? super T> predicate) {
      return flowable
          .as(RxJava2Adapter::flowableToFlux)
          .all(RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkPredicate(predicate))
          .as(RxJava2Adapter::monoToSingle);
    }
  }
  // XXX: final Flowable ambWith(Publisher)
  // XXX: final Single any(Predicate)
  // XXX: final Object as(FlowableConverter)
  // XXX: final Object blockingFirst()
  // XXX: final Object blockingFirst(Object)/home/rick/repos/error-prone-support/error-prone-contrib/src/main/java/tech/picnic/errorprone/refastertemplates/RxJavaToReactorTemplates.java
  // XXX: final void blockingForEach(Consumer)
  // XXX: final Iterable blockingIterable()
  // XXX: final Iterable blockingIterable(int)
  // XXX: final Object blockingLast()
  // XXX: final Object blockingLast(Object)
  // XXX: final Iterable blockingLatest()
  // XXX: final Iterable blockingMostRecent(Object)
  // XXX: final Iterable blockingNext()
  // XXX: final Object blockingSingle()
  // XXX: final Object blockingSingle(Object)
  // XXX: final void blockingSubscribe()
  // XXX: final void blockingSubscribe(Consumer)
  // XXX: final void blockingSubscribe(Consumer,Consumer)
  // XXX: final void blockingSubscribe(Consumer,Consumer,Action)
  // XXX: final void blockingSubscribe(Consumer,Consumer,Action,int)
  // XXX: final void blockingSubscribe(Consumer,Consumer,int)
  // XXX: final void blockingSubscribe(Consumer,int)
  // XXX: final void blockingSubscribe(Subscriber)
  // XXX: final Flowable buffer(Callable)
  // XXX: final Flowable buffer(Callable,Callable)
  // XXX: final Flowable buffer(Flowable,Function)
  // XXX: final Flowable buffer(Flowable,Function,Callable)
  // XXX: final Flowable buffer(int)
  // XXX: final Flowable buffer(int,Callable)
  // XXX: final Flowable buffer(int,int)
  // XXX: final Flowable buffer(int,int,Callable)
  // XXX: final Flowable buffer(long,long,TimeUnit)
  // XXX: final Flowable buffer(long,long,TimeUnit,Scheduler)
  // XXX: final Flowable buffer(long,long,TimeUnit,Scheduler,Callable)
  // XXX: final Flowable buffer(long,TimeUnit)
  // XXX: final Flowable buffer(long,TimeUnit,int)
  // XXX: final Flowable buffer(long,TimeUnit,Scheduler)
  // XXX: final Flowable buffer(long,TimeUnit,Scheduler,int)
  // XXX: final Flowable buffer(long,TimeUnit,Scheduler,int,Callable,boolean)
  // XXX: final Flowable buffer(Publisher)
  // XXX: final Flowable buffer(Publisher,Callable)
  // XXX: final Flowable buffer(Publisher,int)
  // XXX: final Flowable cache()
  // XXX: final Flowable cacheWithInitialCapacity(int)
  // XXX: final Flowable cast(Class)
  // XXX: final Single collect(Callable,BiConsumer)
  // XXX: final Single collectInto(Object,BiConsumer)
  // XXX: final Flowable compose(FlowableTransformer)
  // XXX: final Flowable concatMap(Function)
  // XXX: final Flowable concatMap(Function,int)
  // XXX: final Completable concatMapCompletable(Function) --> Do this one

  //  static final class FlowableConcatMapCompletable<T> {
  //    @BeforeTemplate
  //    Completable before(Flowable<T> flowable, Function<? super T, ? extends CompletableSource>
  // function) {
  //      return flowable.concatMapCompletable(function);
  //    }
  //
  //    @AfterTemplate
  //    Completable after(Flowable<T> flowable,  Function<? super T, ? extends CompletableSource>
  // function) {
  //      return flowable.
  //    }
  //  }

  // XXX: final Completable concatMapCompletable(Function,int)
  // XXX: final Completable concatMapCompletableDelayError(Function)
  // XXX: final Completable concatMapCompletableDelayError(Function,boolean)
  // XXX: final Completable concatMapCompletableDelayError(Function,boolean,int)
  // XXX: final Flowable concatMapDelayError(Function)
  // XXX: final Flowable concatMapDelayError(Function,int,boolean)
  // XXX: final Flowable concatMapEager(Function)
  // XXX: final Flowable concatMapEager(Function,int,int)
  // XXX: final Flowable concatMapEagerDelayError(Function,boolean)
  // XXX: final Flowable concatMapEagerDelayError(Function,int,int,boolean)
  // XXX: final Flowable concatMapIterable(Function)
  // XXX: final Flowable concatMapIterable(Function,int)
  // XXX: final Flowable concatMapMaybe(Function) --> Required.

  //  static final class FlowableConcatMaybe<T, R> {
  //    @BeforeTemplate
  //    Flowable<R> before(
  //        Flowable<T> flowable, Function<? super T, ? extends MaybeSource<? extends R>> mapper) {
  //      return flowable.concatMapMaybe(mapper);
  //    }
  //
  //    Flowable<R> after(
  //        Flowable<T> flowable, Function<? super T, ? extends MaybeSource<? extends R>> mapper) {
  //      return flowable.as(RxJava2Adapter::flowableToFlux).concat
  //    }
  //  }

  // XXX: final Flowable concatMapMaybe(Function,int)
  // XXX: final Flowable concatMapMaybeDelayError(Function) --> This one
  // XXX: final Flowable concatMapMaybeDelayError(Function,boolean)
  // XXX: final Flowable concatMapMaybeDelayError(Function,boolean,int)
  // XXX: final Flowable concatMapSingle(Function)
  // XXX: final Flowable concatMapSingle(Function,int)
  // XXX: final Flowable concatMapSingleDelayError(Function)
  // XXX: final Flowable concatMapSingleDelayError(Function,boolean)
  // XXX: final Flowable concatMapSingleDelayError(Function,boolean,int)
  // XXX: final Flowable concatWith(CompletableSource)
  // XXX: final Flowable concatWith(MaybeSource)

  static final class FlowableConcatWithPublisher<T> {
    @BeforeTemplate
    Flowable<T> before(Flowable<T> flowable, Publisher<T> source) {
      return flowable.concatWith(source);
    }

    @AfterTemplate
    Flowable<T> after(Flowable<T> flowable, Publisher<T> source) {
      return flowable
          .as(RxJava2Adapter::flowableToFlux)
          .concatWith(source)
          .as(RxJava2Adapter::fluxToFlowable);
    }
  }

  // XXX: final Flowable concatWith(SingleSource)
  // XXX: final Single contains(Object)
  // XXX: final Single count()
  // XXX: final Flowable debounce(Function)
  // XXX: final Flowable debounce(long,TimeUnit)
  // XXX: final Flowable debounce(long,TimeUnit,Scheduler)
  // XXX: final Flowable defaultIfEmpty(Object)
  // XXX: final Flowable delay(Function)
  // XXX: final Flowable delay(long,TimeUnit)
  // XXX: final Flowable delay(long,TimeUnit,boolean)
  // XXX: final Flowable delay(long,TimeUnit,Scheduler)
  // XXX: final Flowable delay(long,TimeUnit,Scheduler,boolean)
  // XXX: final Flowable delay(Publisher,Function)
  // XXX: final Flowable delaySubscription(long,TimeUnit)
  // XXX: final Flowable delaySubscription(long,TimeUnit,Scheduler)
  // XXX: final Flowable delaySubscription(Publisher)
  // XXX: final Flowable dematerialize()
  // XXX: final Flowable dematerialize(Function)
  // XXX: final Flowable distinct()
  // XXX: final Flowable distinct(Function)
  // XXX: final Flowable distinct(Function,Callable)
  // XXX: final Flowable distinctUntilChanged()
  // XXX: final Flowable distinctUntilChanged(BiPredicate)
  // XXX: final Flowable distinctUntilChanged(Function)
  // XXX: final Flowable doAfterNext(Consumer)
  // XXX: final Flowable doAfterTerminate(Action)
  // XXX: final Flowable doFinally(Action)
  // XXX: final Flowable doOnCancel(Action)
  // XXX: final Flowable doOnComplete(Action)
  // XXX: final Flowable doOnEach(Consumer)
  // XXX: final Flowable doOnEach(Subscriber)
  // XXX: final Flowable doOnError(Consumer)
  // XXX: final Flowable doOnLifecycle(Consumer,LongConsumer,Action)
  // XXX: final Flowable doOnNext(Consumer)
  // XXX: final Flowable doOnRequest(LongConsumer)
  // XXX: final Flowable doOnSubscribe(Consumer)
  // XXX: final Flowable doOnTerminate(Action)
  // XXX: final Maybe elementAt(long)
  // XXX: final Single elementAt(long,Object)
  // XXX: final Single elementAtOrError(long)

  // XXX: `Refaster.canBeCoercedTo(...)`.
  static final class FlowableFilter<S, T extends S> {
    @BeforeTemplate
    Flowable<T> before(Flowable<T> flowable, Predicate<S> predicate) {
      return flowable.filter(predicate);
    }

    @AfterTemplate
    Flowable<T> after(Flowable<T> flowable, java.util.function.Predicate<S> predicate) {
      return flowable
          .as(RxJava2Adapter::flowableToFlux)
          .filter(predicate)
          .as(RxJava2Adapter::fluxToFlowable);
    }
  }
  // XXX: final Single first(Object)

  static final class FlowableFirstElement<T> {
    @BeforeTemplate
    Maybe<T> before(Flowable<T> flowable) {
      return flowable.firstElement();
    }

    @AfterTemplate
    Maybe<T> after(Flowable<T> flowable) {
      return flowable.as(RxJava2Adapter::flowableToFlux).next().as(RxJava2Adapter::monoToMaybe);
    }
  }

  // XXX: final Single firstOrError()

  // XXX: `Refaster.canBeCoercedTo(...)`.
  static final class FlowableFlatMap<I, T extends I, O, P extends Publisher<? extends O>> {
    @BeforeTemplate
    Flowable<O> before(Flowable<T> flowable, Function<I, P> function) {
      return flowable.flatMap(function);
    }

    @AfterTemplate
    Flowable<O> after(Flowable<I> flowable, java.util.function.Function<I, P> function) {
      return flowable
          .as(RxJava2Adapter::flowableToFlux)
          .flatMap(function)
          .as(RxJava2Adapter::fluxToFlowable);
    }
  }

  // XXX: final Flowable flatMap(Function,BiFunction)
  // XXX: final Flowable flatMap(Function,BiFunction,boolean)
  // XXX: final Flowable flatMap(Function,BiFunction,boolean,int)
  // XXX: final Flowable flatMap(Function,BiFunction,boolean,int,int)
  // XXX: final Flowable flatMap(Function,BiFunction,int)
  // XXX: final Flowable flatMap(Function,boolean)
  // XXX: final Flowable flatMap(Function,boolean,int)
  // XXX: final Flowable flatMap(Function,boolean,int,int)
  // XXX: final Flowable flatMap(Function,Function,Callable)
  // XXX: final Flowable flatMap(Function,Function,Callable,int)
  // XXX: final Flowable flatMap(Function,int)
  // XXX: final Completable flatMapCompletable(Function)
  // XXX: final Completable flatMapCompletable(Function,boolean,int)
  // XXX: final Flowable flatMapIterable(Function)
  // XXX: final Flowable flatMapIterable(Function,BiFunction)
  // XXX: final Flowable flatMapIterable(Function,BiFunction,int)
  // XXX: final Flowable flatMapIterable(Function,int)
  // XXX: final Flowable flatMapMaybe(Function)
  // XXX: final Flowable flatMapMaybe(Function,boolean,int)
  // XXX: final Flowable flatMapSingle(Function)
  // XXX: final Flowable flatMapSingle(Function,boolean,int)
  // XXX: final Disposable forEach(Consumer)
  // XXX: final Disposable forEachWhile(Predicate)
  // XXX: final Disposable forEachWhile(Predicate,Consumer)
  // XXX: final Disposable forEachWhile(Predicate,Consumer,Action)
  // XXX: final Flowable groupBy(Function)
  // XXX: final Flowable groupBy(Function,boolean)
  // XXX: final Flowable groupBy(Function,Function)
  // XXX: final Flowable groupBy(Function,Function,boolean)
  // XXX: final Flowable groupBy(Function,Function,boolean,int)
  // XXX: final Flowable groupBy(Function,Function,boolean,int,Function)
  // XXX: final Flowable groupJoin(Publisher,Function,Function,BiFunction)
  // XXX: final Flowable hide()
  // XXX: final Completable ignoreElements()
  // XXX: final Single isEmpty()
  // XXX: final Flowable join(Publisher,Function,Function,BiFunction)
  // XXX: final Single last(Object)
  // XXX: final Maybe lastElement()
  // XXX: final Single lastOrError()
  // XXX: final Flowable lift(FlowableOperator)
  // XXX: final Flowable limit(long)

  // XXX: `Refaster.canBeCoercedTo(...)`.
  static final class FlowableMap<I, T extends I, O> {
    @BeforeTemplate
    Flowable<O> before(Flowable<T> flowable, Function<I, O> function) {
      return flowable.map(function);
    }

    @AfterTemplate
    Flowable<O> after(Flowable<T> flowable, java.util.function.Function<I, O> function) {
      return flowable
          .as(RxJava2Adapter::flowableToFlux)
          .map(function)
          .as(RxJava2Adapter::fluxToFlowable);
    }
  }

  // XXX: final Flowable materialize()
  // XXX: final Flowable mergeWith(CompletableSource)
  // XXX: final Flowable mergeWith(MaybeSource)
  // XXX: final Flowable mergeWith(Publisher)
  // XXX: final Flowable mergeWith(SingleSource) --> required.
  // XXX: final Flowable observeOn(Scheduler)
  // XXX: final Flowable observeOn(Scheduler,boolean)
  // XXX: final Flowable observeOn(Scheduler,boolean,int)
  // XXX: final Flowable ofType(Class)
  // XXX: final Flowable onBackpressureBuffer()
  // XXX: final Flowable onBackpressureBuffer(boolean)
  // XXX: final Flowable onBackpressureBuffer(int)
  // XXX: final Flowable onBackpressureBuffer(int,Action)
  // XXX: final Flowable onBackpressureBuffer(int,boolean)
  // XXX: final Flowable onBackpressureBuffer(int,boolean,boolean)
  // XXX: final Flowable onBackpressureBuffer(int,boolean,boolean,Action)
  // XXX: final Flowable onBackpressureBuffer(long,Action,BackpressureOverflowStrategy)
  // XXX: final Flowable onBackpressureDrop()
  // XXX: final Flowable onBackpressureDrop(Consumer)
  // XXX: final Flowable onBackpressureLatest()
  // XXX: final Flowable onErrorResumeNext(Function)
  // XXX: final Flowable onErrorResumeNext(Publisher)
  // XXX: final Flowable onErrorReturn(Function)
  // XXX: final Flowable onErrorReturnItem(Object)
  // XXX: final Flowable onExceptionResumeNext(Publisher)
  // XXX: final Flowable onTerminateDetach()
  // XXX: final ParallelFlowable parallel()
  // XXX: final ParallelFlowable parallel(int)
  // XXX: final ParallelFlowable parallel(int,int)
  // XXX: final ConnectableFlowable publish()
  // XXX: final Flowable publish(Function)
  // XXX: final Flowable publish(Function,int)
  // XXX: final ConnectableFlowable publish(int)
  // XXX: final Flowable rebatchRequests(int)
  // XXX: final Maybe reduce(BiFunction)
  // XXX: final Single reduce(Object,BiFunction)
  // XXX: final Single reduceWith(Callable,BiFunction)
  // XXX: final Flowable repeat()
  // XXX: final Flowable repeat(long)
  // XXX: final Flowable repeatUntil(BooleanSupplier)
  // XXX: final Flowable repeatWhen(Function)
  // XXX: final ConnectableFlowable replay()
  // XXX: final Flowable replay(Function)
  // XXX: final Flowable replay(Function,int)
  // XXX: final Flowable replay(Function,int,long,TimeUnit)
  // XXX: final Flowable replay(Function,int,long,TimeUnit,Scheduler)
  // XXX: final Flowable replay(Function,int,Scheduler)
  // XXX: final Flowable replay(Function,long,TimeUnit)
  // XXX: final Flowable replay(Function,long,TimeUnit,Scheduler)
  // XXX: final Flowable replay(Function,Scheduler)
  // XXX: final ConnectableFlowable replay(int)
  // XXX: final ConnectableFlowable replay(int,long,TimeUnit)
  // XXX: final ConnectableFlowable replay(int,long,TimeUnit,Scheduler)
  // XXX: final ConnectableFlowable replay(int,Scheduler)
  // XXX: final ConnectableFlowable replay(long,TimeUnit)
  // XXX: final ConnectableFlowable replay(long,TimeUnit,Scheduler)
  // XXX: final ConnectableFlowable replay(Scheduler)
  // XXX: final Flowable retry()
  // XXX: final Flowable retry(BiPredicate)
  // XXX: final Flowable retry(long)
  // XXX: final Flowable retry(long,Predicate)
  // XXX: final Flowable retry(Predicate)
  // XXX: final Flowable retryUntil(BooleanSupplier)
  // XXX: final Flowable retryWhen(Function)
  // XXX: final void safeSubscribe(Subscriber)
  // XXX: final Flowable sample(long,TimeUnit)
  // XXX: final Flowable sample(long,TimeUnit,boolean)
  // XXX: final Flowable sample(long,TimeUnit,Scheduler)
  // XXX: final Flowable sample(long,TimeUnit,Scheduler,boolean)
  // XXX: final Flowable sample(Publisher)
  // XXX: final Flowable sample(Publisher,boolean)
  // XXX: final Flowable scan(BiFunction)
  // XXX: final Flowable scan(Object,BiFunction)
  // XXX: final Flowable scanWith(Callable,BiFunction)
  // XXX: final Flowable serialize()
  // XXX: final Flowable share()
  // XXX: final Single single(Object)
  // XXX: final Maybe singleElement()
  // XXX: final Single singleOrError()
  // XXX: final Flowable skip(long)
  // XXX: final Flowable skip(long,TimeUnit)
  // XXX: final Flowable skip(long,TimeUnit,Scheduler)
  // XXX: final Flowable skipLast(int)
  // XXX: final Flowable skipLast(long,TimeUnit)
  // XXX: final Flowable skipLast(long,TimeUnit,boolean)
  // XXX: final Flowable skipLast(long,TimeUnit,Scheduler)
  // XXX: final Flowable skipLast(long,TimeUnit,Scheduler,boolean)
  // XXX: final Flowable skipLast(long,TimeUnit,Scheduler,boolean,int)
  // XXX: final Flowable skipUntil(Publisher)
  // XXX: final Flowable skipWhile(Predicate)
  // XXX: final Flowable sorted()
  // XXX: final Flowable sorted(java.util.Comparator)
  // XXX: final Flowable startWith(Iterable)
  // XXX: final Flowable startWith(Object)
  // XXX: final Flowable startWith(Publisher)
  // XXX: final Flowable startWithArray(Object[])
  // XXX: final Disposable subscribe()
  // XXX: final Disposable subscribe(Consumer)
  // XXX: final Disposable subscribe(Consumer,Consumer)
  // XXX: final Disposable subscribe(Consumer,Consumer,Action)
  // XXX: final Disposable subscribe(Consumer,Consumer,Action,Consumer)
  // XXX: final void subscribe(FlowableSubscriber)
  // XXX: final void subscribe(Subscriber)
  // XXX: final Flowable subscribeOn(Scheduler)
  // XXX: final Flowable subscribeOn(Scheduler,boolean)
  // XXX: final Subscriber subscribeWith(Subscriber)

  static final class FlowableSwitchIfEmpty<T> {
    @BeforeTemplate
    Flowable<T> before(Flowable<T> flowable, Publisher<T> publisher) {
      return flowable.switchIfEmpty(publisher);
    }

    @AfterTemplate
    Flowable<T> after(Flowable<T> flowable, Publisher<T> publisher) {
      return flowable
          .as(RxJava2Adapter::flowableToFlux)
          .switchIfEmpty(publisher)
          .as(RxJava2Adapter::fluxToFlowable);
    }
  }

  // XXX: final Flowable switchMap(Function)
  // XXX: final Flowable switchMap(Function,int)
  // XXX: final Completable switchMapCompletable(Function)
  // XXX: final Completable switchMapCompletableDelayError(Function)
  // XXX: final Flowable switchMapDelayError(Function)
  // XXX: final Flowable switchMapDelayError(Function,int)
  // XXX: final Flowable switchMapMaybe(Function)
  // XXX: final Flowable switchMapMaybeDelayError(Function)
  // XXX: final Flowable switchMapSingle(Function)
  // XXX: final Flowable switchMapSingleDelayError(Function)
  // XXX: final Flowable take(long)
  // XXX: final Flowable take(long,TimeUnit)
  // XXX: final Flowable take(long,TimeUnit,Scheduler)
  // XXX: final Flowable takeLast(int)
  // XXX: final Flowable takeLast(long,long,TimeUnit)
  // XXX: final Flowable takeLast(long,long,TimeUnit,Scheduler)
  // XXX: final Flowable takeLast(long,long,TimeUnit,Scheduler,boolean,int)
  // XXX: final Flowable takeLast(long,TimeUnit)
  // XXX: final Flowable takeLast(long,TimeUnit,boolean)
  // XXX: final Flowable takeLast(long,TimeUnit,Scheduler)
  // XXX: final Flowable takeLast(long,TimeUnit,Scheduler,boolean)
  // XXX: final Flowable takeLast(long,TimeUnit,Scheduler,boolean,int)
  // XXX: final Flowable takeUntil(Predicate)
  // XXX: final Flowable takeUntil(Publisher)
  // XXX: final Flowable takeWhile(Predicate)
  // XXX: final Flowable throttleFirst(long,TimeUnit)
  // XXX: final Flowable throttleFirst(long,TimeUnit,Scheduler)
  // XXX: final Flowable throttleLast(long,TimeUnit)
  // XXX: final Flowable throttleLast(long,TimeUnit,Scheduler)
  // XXX: final Flowable throttleLatest(long,TimeUnit)
  // XXX: final Flowable throttleLatest(long,TimeUnit,boolean)
  // XXX: final Flowable throttleLatest(long,TimeUnit,Scheduler)
  // XXX: final Flowable throttleLatest(long,TimeUnit,Scheduler,boolean)
  // XXX: final Flowable throttleWithTimeout(long,TimeUnit)
  // XXX: final Flowable throttleWithTimeout(long,TimeUnit,Scheduler)
  // XXX: final Flowable timeInterval()
  // XXX: final Flowable timeInterval(Scheduler)
  // XXX: final Flowable timeInterval(TimeUnit)
  // XXX: final Flowable timeInterval(TimeUnit,Scheduler)
  // XXX: final Flowable timeout(Function)
  // XXX: final Flowable timeout(Function,Flowable)
  // XXX: final Flowable timeout(long,TimeUnit)
  // XXX: final Flowable timeout(long,TimeUnit,Publisher)
  // XXX: final Flowable timeout(long,TimeUnit,Scheduler)
  // XXX: final Flowable timeout(long,TimeUnit,Scheduler,Publisher)
  // XXX: final Flowable timeout(Publisher,Function)
  // XXX: final Flowable timeout(Publisher,Function,Publisher)
  // XXX: final Flowable timestamp()
  // XXX: final Flowable timestamp(Scheduler)
  // XXX: final Flowable timestamp(TimeUnit)
  // XXX: final Flowable timestamp(TimeUnit,Scheduler)
  // XXX: final Object to(Function)
  // XXX: final Future toFuture()
  // XXX: final Single toList()
  // XXX: final Single toList(Callable)
  // XXX: final Single toList(int)

  static final class FlowableToMap<I, T extends I, O> {
    @BeforeTemplate
    Single<Map<O, T>> before(Flowable<T> flowable, Function<I, O> function) {
      return flowable.toMap(function);
    }

    @AfterTemplate
    Single<Map<O, T>> after(Flowable<T> flowable, java.util.function.Function<I, O> function) {
      return flowable
          .as(RxJava2Adapter::flowableToFlux)
          .collectMap(function)
          .as(RxJava2Adapter::monoToSingle);
    }
  }

  // XXX: final Single toMap(Function,Function)
  // XXX: final Single toMap(Function,Function,Callable)
  // XXX: final Single toMultimap(Function)
  // XXX: final Single toMultimap(Function,Function)
  // XXX: final Single toMultimap(Function,Function,Callable)
  // XXX: final Single toMultimap(Function,Function,Callable,Function)
  // XXX: final Observable toObservable()
  // XXX: final Single toSortedList()
  // XXX: final Single toSortedList(int)
  // XXX: final Single toSortedList(java.util.Comparator)
  // XXX: final Single toSortedList(java.util.Comparator,int)
  // XXX: final Flowable unsubscribeOn(Scheduler)
  // XXX: final Flowable window(Callable)
  // XXX: final Flowable window(Callable,int)
  // XXX: final Flowable window(long)
  // XXX: final Flowable window(long,long)
  // XXX: final Flowable window(long,long,int)
  // XXX: final Flowable window(long,long,TimeUnit)
  // XXX: final Flowable window(long,long,TimeUnit,Scheduler)
  // XXX: final Flowable window(long,long,TimeUnit,Scheduler,int)
  // XXX: final Flowable window(long,TimeUnit)
  // XXX: final Flowable window(long,TimeUnit,long)
  // XXX: final Flowable window(long,TimeUnit,long,boolean)
  // XXX: final Flowable window(long,TimeUnit,Scheduler)
  // XXX: final Flowable window(long,TimeUnit,Scheduler,long)
  // XXX: final Flowable window(long,TimeUnit,Scheduler,long,boolean)
  // XXX: final Flowable window(long,TimeUnit,Scheduler,long,boolean,int)
  // XXX: final Flowable window(Publisher)
  // XXX: final Flowable window(Publisher,Function)
  // XXX: final Flowable window(Publisher,Function,int)
  // XXX: final Flowable window(Publisher,int)
  // XXX: final Flowable withLatestFrom(Iterable,Function)
  // XXX: final Flowable withLatestFrom(Publisher,BiFunction)
  // XXX: final Flowable withLatestFrom(Publisher[],Function)
  // XXX: final Flowable withLatestFrom(Publisher,Publisher,Function3)
  // XXX: final Flowable withLatestFrom(Publisher,Publisher,Publisher,Function4)
  // XXX: final Flowable withLatestFrom(Publisher,Publisher,Publisher,Publisher,Function5)
  // XXX: final Flowable zipWith(Iterable,BiFunction)
  // XXX: final Flowable zipWith(Publisher,BiFunction)
  // XXX: final Flowable zipWith(Publisher,BiFunction,boolean)
  // XXX: final Flowable zipWith(Publisher,BiFunction,boolean,int)
  // XXX: final TestSubscriber test()
  // XXX: final TestSubscriber test(long)
  // XXX: final TestSubscriber test(long,boolean)

}
