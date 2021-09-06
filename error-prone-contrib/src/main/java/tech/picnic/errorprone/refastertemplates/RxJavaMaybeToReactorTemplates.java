package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** The Refaster templates for the migration of the RxJava Maybe type to Reactor */
final class RxJavaMaybeToReactorTemplates {

  private RxJavaMaybeToReactorTemplates() {}

  static final class MaybeAmb<T> {
    @BeforeTemplate
    Maybe<T> before(Iterable<? extends Maybe<? extends T>> iterable) {
      return Maybe.amb(iterable);
    }

    @AfterTemplate
    Maybe<T> after(Iterable<? extends Maybe<? extends T>> iterable) {
      return RxJava2Adapter.monoToMaybe(
          Mono.firstWithSignal(
              Streams.stream(iterable)
                  .map(RxJava2Adapter::maybeToMono)
                  .collect(toImmutableList())));
    }
  }

  static final class MaybeAmbArray<T> {
    @BeforeTemplate
    Maybe<T> before(Maybe<? extends T>... sources) {
      return Maybe.ambArray(sources);
    }

    @AfterTemplate
    Maybe<T> after(Maybe<? extends T>... sources) {
      return RxJava2Adapter.monoToMaybe(
          Mono.firstWithSignal(
              Arrays.stream(sources).map(RxJava2Adapter::maybeToMono).collect(toImmutableList())));
    }
  }

  // XXX: public static Flowable concat(Iterable)
  // XXX: public static Flowable concat(MaybeSource,MaybeSource)
  // XXX: public static Flowable concat(MaybeSource,MaybeSource,MaybeSource)
  // XXX: public static Flowable concat(MaybeSource,MaybeSource,MaybeSource,MaybeSource)
  // XXX: public static Flowable concat(Publisher)
  // XXX: public static Flowable concat(Publisher,int)

  // XXX: The test is not triggering? What did I do wrong? Perhaps it should be a MaybeSource...
  static final class MaybeConcatArray<T> {
    @BeforeTemplate
    Flowable<T> before(Maybe<? extends T>... sources) {
      return Maybe.concatArray(sources);
    }

    @AfterTemplate
    Flowable<T> after(Maybe<? extends T>... sources) {
      return RxJava2Adapter.fluxToFlowable(
          Flux.concat(
              Arrays.stream(sources).map(RxJava2Adapter::maybeToMono).collect(toImmutableList())));
    }
  }

  // XXX: public static Flowable concatArrayDelayError(MaybeSource[])
  // XXX: public static Flowable concatArrayEager(MaybeSource[])
  // XXX: public static Flowable concatDelayError(Iterable)
  // XXX: public static Flowable concatDelayError(Publisher)
  // XXX: public static Flowable concatEager(Iterable)
  // XXX: public static Flowable concatEager(Publisher)
  // XXX: public static Maybe create(MaybeOnSubscribe)

  // XXX: Is this correct?
  /**
   * Check this one: private MonoVoid verifyTagExists_migrated(OptionalString tagId) { return
   * RxJava2Adapter.completableToMono( Maybe.defer(() -
   * tagId.map(Maybe::just).orElseGet(Maybe::empty)) - .flatMapSingleElement(this::getTagById) -
   * .ignoreElement()); + .flatMapSingleElement(this::getTagById).as(RxJava2Adapter::maybeToMono).
   */
  abstract static class MaybeDefer<T> {
    @Placeholder
    abstract Maybe<T> maybeProducer();

    @BeforeTemplate
    Mono<T> before() {
      return Maybe.defer(() -> maybeProducer()).as(RxJava2Adapter::maybeToMono);
    }

    @AfterTemplate
    Mono<T> after() {
      return Mono.defer(() -> maybeProducer().as(RxJava2Adapter::maybeToMono));
    }
  }

  // XXX: public static Maybe empty() --> this one.

  static final class MaybeEmpty<T> {
    @BeforeTemplate
    Maybe<T> before() {
      return Maybe.empty();
    }

    @AfterTemplate
    Maybe<T> after() {
      return RxJava2Adapter.monoToMaybe(Mono.empty());
    }
  }

  // XXX: public static Maybe error(Callable) --> Required
  // XXX: public static Maybe error(Throwable) --> Required
  // XXX: public static Maybe fromAction(Action)

  static final class MaybeFromCallable<T> {
    @BeforeTemplate
    Maybe<T> before(Callable<? extends T> callable) {
      return Maybe.fromCallable(callable);
    }

    @AfterTemplate
    Maybe<T> after(Callable<? extends T> callable) {
      return RxJava2Adapter.monoToMaybe(
          Mono.fromSupplier(
              RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.callableAsSupplier(callable)));
    }
  }

  // XXX: public static Maybe fromCompletable(CompletableSource)

  // XXX: Also handle `Future`s that don't extend `CompletableFuture`.
  static final class MaybeFromFuture<T> {
    @BeforeTemplate
    Maybe<T> before(CompletableFuture<? extends T> future) {
      return Maybe.fromFuture(future);
    }

    @AfterTemplate
    Maybe<T> after(CompletableFuture<? extends T> future) {
      return RxJava2Adapter.monoToMaybe(Mono.fromFuture(future));
    }
  }

  // XXX: public static Maybe fromFuture(Future,long,TimeUnit)
  // XXX: public static Maybe fromRunnable(Runnable)
  // XXX: public static Maybe fromSingle(SingleSource)
  // XXX: public static Maybe just(Object)
  // XXX: public static Flowable merge(Iterable)
  // XXX: public static Maybe merge(MaybeSource)
  // XXX: public static Flowable merge(MaybeSource,MaybeSource)
  // XXX: public static Flowable merge(MaybeSource,MaybeSource,MaybeSource)
  // XXX: public static Flowable merge(MaybeSource,MaybeSource,MaybeSource,MaybeSource)
  // XXX: public static Flowable merge(Publisher)
  // XXX: public static Flowable merge(Publisher,int)
  // XXX: public static Flowable mergeArray(MaybeSource[])
  // XXX: public static Flowable mergeArrayDelayError(MaybeSource[])
  // XXX: public static Flowable mergeDelayError(Iterable)
  // XXX: public static Flowable mergeDelayError(MaybeSource,MaybeSource)
  // XXX: public static Flowable mergeDelayError(MaybeSource,MaybeSource,MaybeSource)
  // XXX: public static Flowable mergeDelayError(MaybeSource,MaybeSource,MaybeSource,MaybeSource)
  // XXX: public static Flowable mergeDelayError(Publisher)
  // XXX: public static Flowable mergeDelayError(Publisher,int)
  // XXX: public static Maybe never()
  // XXX: public static Single sequenceEqual(MaybeSource,MaybeSource)
  // XXX: public static Single sequenceEqual(MaybeSource,MaybeSource,BiPredicate)
  // XXX: public static Maybe timer(long,TimeUnit)
  // XXX: public static Maybe timer(long,TimeUnit,Scheduler)
  // XXX: public static Maybe unsafeCreate(MaybeSource)
  // XXX: public static Maybe using(Callable,Function,Consumer)
  // XXX: public static Maybe using(Callable,Function,Consumer,boolean)

  static final class MaybeWrap<T> {
    @BeforeTemplate
    Maybe<T> before(Maybe<T> maybe) {
      return Maybe.wrap(maybe);
    }

    @AfterTemplate
    Maybe<T> after(Maybe<T> maybe) {
      return maybe;
    }
  }

  // XXX: public static Maybe zip(Iterable,Function)
  // XXX: public static Maybe zip(MaybeSource,MaybeSource,BiFunction)
  // XXX: public static Maybe zip(MaybeSource,MaybeSource,MaybeSource,Function3)
  // XXX: public static Maybe zip(MaybeSource,MaybeSource,MaybeSource,MaybeSource,Function4)
  // XXX: public static Maybe
  // zip(MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,Function5)
  // XXX: public static Maybe
  // zip(MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,Function6)
  // XXX: public static Maybe
  // zip(MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,Function7)
  // XXX: public static Maybe
  // zip(MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,Function8)
  // XXX: public static Maybe
  // zip(MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,MaybeSource,Function9)
  // XXX: public static Maybe zipArray(Function,MaybeSource[])

  static final class MaybeAmbWith<T> {
    @BeforeTemplate
    Maybe<T> before(Maybe<T> maybe, Maybe<? extends T> otherMaybe) {
      return maybe.ambWith(otherMaybe);
    }

    @AfterTemplate
    Maybe<T> after(Maybe<T> maybe, Maybe<? extends T> otherMaybe) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .or(otherMaybe.as(RxJava2Adapter::maybeToMono))
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

  // XXX: public final Object as(MaybeConverter)
  // XXX: public final Object blockingGet()
  // XXX: public final Object blockingGet(Object)
  // XXX: public final Maybe cache()

  static final class MaybeCast<T> {
    @BeforeTemplate
    Maybe<T> before(Maybe<T> maybe) {
      return maybe.cast(Refaster.<T>clazz());
    }

    @AfterTemplate
    Maybe<T> after(Maybe<T> maybe) {
      return maybe;
    }
  }

  // XXX: public final Maybe compose(MaybeTransformer)
  // XXX: public final Maybe concatMap(Function)
  // XXX: public final Flowable concatWith(MaybeSource)
  // XXX: public final Single contains(Object)
  // XXX: public final Single count()
  // XXX: public final Maybe defaultIfEmpty(Object)
  // XXX: public final Maybe delay(long,TimeUnit)
  // XXX: public final Maybe delay(long,TimeUnit,Scheduler)
  // XXX: public final Maybe delay(Publisher)
  // XXX: public final Maybe delaySubscription(long,TimeUnit)
  // XXX: public final Maybe delaySubscription(long,TimeUnit,Scheduler)
  // XXX: public final Maybe delaySubscription(Publisher)
  // XXX: public final Maybe doAfterSuccess(Consumer)
  // XXX: public final Maybe doAfterTerminate(Action)
  // XXX: public final Maybe doFinally(Action)
  // XXX: public final Maybe doOnComplete(Action)
  // XXX: public final Maybe doOnDispose(Action)
  // XXX: public final Maybe doOnError(Consumer)
  // XXX: public final Maybe doOnEvent(BiConsumer)
  // XXX: public final Maybe doOnSubscribe(Consumer)
  // XXX: public final Maybe doOnSuccess(Consumer) --> Required
  // XXX: public final Maybe doOnTerminate(Action)
  // XXX: public final Maybe filter(Predicate)

  // See the MyUtil for additional explanation.
  static final class MaybeFlatMapFunction<I, T extends I, O, M extends MaybeSource<? extends O>> {
    @BeforeTemplate
    Maybe<O> before(Maybe<T> maybe, Function<I, M> function) {
      return maybe.flatMap(function);
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    Maybe<O> after(Maybe<T> maybe, Function<I, M> function) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .flatMap(
              v ->
                  RxJava2Adapter.maybeToMono(
                      Maybe.wrap(
                          (Maybe<O>)
                              RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkFunction(
                                      function)
                                  .apply(v))))
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

  // XXX: There is no link to an original public method for this, but it is important.
  abstract static class MaybeFlatMapLambda<S, T> {
    @Placeholder
    abstract Maybe<T> toMaybeFunction(@MayOptionallyUse S element);

    @BeforeTemplate
    Maybe<T> before(Maybe<S> maybe) {
      return maybe.flatMap(v -> toMaybeFunction(v));
    }

    @AfterTemplate
    Maybe<T> after(Maybe<S> maybe) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .flatMap(v -> toMaybeFunction(v).as(RxJava2Adapter::maybeToMono))
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

  // XXX: public final Maybe flatMap(Function,BiFunction)
  // XXX: public final Maybe flatMap(Function,Function,Callable)
  // XXX: public final Completable flatMapCompletable(Function)
  // XXX: public final Observable flatMapObservable(Function)
  // XXX: public final Flowable flatMapPublisher(Function)
  // XXX: public final Single flatMapSingle(Function)

  // XXX: public final Maybe flatMapSingleElement(Function)
  //  The following template is required to rewrite this code from platform:
  //    private Completable verifyTagExists(Optional<String> tagId) {
  //    return Maybe.defer(() -> tagId.map(Maybe::just).orElseGet(Maybe::empty))
  //        .flatMapSingleElement(this::getTagById)
  //        .ignoreElement();
  //    }
  //      static final class MaybeFlatMapSingleElement<
  //          I, T extends I, O, S extends Single<? extends O>> { // <S, T extends S, O> {
  //        @BeforeTemplate
  //        Maybe<O> before(Maybe<T> maybe, Function<I, S> function) {
  //          return maybe.flatMapSingleElement(function);
  //        }
  //
  //        @AfterTemplate
  //        Maybe<O> after(Maybe<T> maybe, Function<? extends I, S> function) {
  //          return maybe
  //              .as(RxJava2Adapter::maybeToMono)
  //              .flatMap(RxJava2ReactorMigrationUtil.toJdkFunction(function))
  //              .as(RxJava2Adapter::monoToMaybe);
  //        }
  //      }

  // XXX: public final Flowable flattenAsFlowable(Function)
  // XXX: public final Observable flattenAsObservable(Function)
  // XXX: public final Maybe hide()

  static final class MaybeIgnoreElement<T> {
    @BeforeTemplate
    Completable before(Maybe<T> maybe) {
      return maybe.ignoreElement();
    }

    @AfterTemplate
    Completable after(Maybe<T> maybe) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .ignoreElement()
          .as(RxJava2Adapter::monoToCompletable);
    }
  }

  // XXX: public final Single isEmpty()
  // XXX: public final Maybe lift(MaybeOperator)
  // XXX: public final Maybe map(Function) --> required.

  static final class MaybeMap<T, R> {
    @BeforeTemplate
    Maybe<R> before(Maybe<T> maybe, Function<T, R> mapper) {
      return maybe.map(mapper);
    }

    @AfterTemplate
    Maybe<R> after(Maybe<T> maybe, Function<T, R> mapper) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .map(RxJavaToReactorTemplates.RxJava2ReactorMigrationUtil.toJdkFunction(mapper))
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

  //XXX: Remove the toJdkFunction with `CanBeCoercedTo`.

  // XXX: public final Single materialize()
  // XXX: public final Flowable mergeWith(MaybeSource)
  // XXX: public final Maybe observeOn(Scheduler)
  // XXX: public final Maybe ofType(Class)
  // XXX: public final Maybe onErrorComplete()
  // XXX: public final Maybe onErrorComplete(Predicate)
  // XXX: public final Maybe onErrorResumeNext(Function)
  // XXX: public final Maybe onErrorResumeNext(MaybeSource)
  // XXX: public final Maybe onErrorReturn(Function) --> This one, ArticleIssueServiceImpl 484,
  // double check please.
  // XXX: public final Maybe onErrorReturnItem(Object)
  // XXX: public final Maybe onExceptionResumeNext(MaybeSource)
  // XXX: public final Maybe onTerminateDetach()
  // XXX: public final Flowable repeat()
  // XXX: public final Flowable repeat(long)
  // XXX: public final Flowable repeatUntil(BooleanSupplier)
  // XXX: public final Flowable repeatWhen(Function)
  // XXX: public final Maybe retry()
  // XXX: public final Maybe retry(BiPredicate)
  // XXX: public final Maybe retry(long)
  // XXX: public final Maybe retry(long,Predicate)
  // XXX: public final Maybe retry(Predicate)
  // XXX: public final Maybe retryUntil(BooleanSupplier)
  // XXX: public final Maybe retryWhen(Function)
  // XXX: public final Disposable subscribe()
  // XXX: public final Disposable subscribe(Consumer)
  // XXX: public final Disposable subscribe(Consumer,Consumer)
  // XXX: public final Disposable subscribe(Consumer,Consumer,Action)
  // XXX: public final void subscribe(MaybeObserver)
  // XXX: public final Maybe subscribeOn(Scheduler)
  // XXX: public final MaybeObserver subscribeWith(MaybeObserver)
  // XXX: public final Maybe switchIfEmpty(MaybeSource)

  static final class MaybeSwitchIfEmpty<S, T extends S> {
    @BeforeTemplate
    Single<S> before(Maybe<S> maybe, Single<T> single) {
      return maybe.switchIfEmpty(single);
    }

    @AfterTemplate
    Single<S> after(Maybe<S> maybe, Single<T> single) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .switchIfEmpty(single.as(RxJava2Adapter::singleToMono))
          .as(RxJava2Adapter::monoToSingle);
    }
  }

  // XXX: public final Maybe takeUntil(MaybeSource)
  // XXX: public final Maybe takeUntil(Publisher)
  // XXX: public final TestObserver test()
  // XXX: public final TestObserver test(boolean)
  // XXX: public final Maybe timeout(long,TimeUnit)
  // XXX: public final Maybe timeout(long,TimeUnit,MaybeSource)
  // XXX: public final Maybe timeout(long,TimeUnit,Scheduler)
  // XXX: public final Maybe timeout(long,TimeUnit,Scheduler,MaybeSource)
  // XXX: public final Maybe timeout(MaybeSource)
  // XXX: public final Maybe timeout(MaybeSource,MaybeSource)
  // XXX: public final Maybe timeout(Publisher)
  // XXX: public final Maybe timeout(Publisher,MaybeSource)
  // XXX: public final Object to(Function)
  // XXX: public final Flowable toFlowable() --> Required I guess
  // XXX: public final Observable toObservable()
  // XXX: public final Single toSingle() --> Required
  // XXX: public final Single toSingle(Object)
  // XXX: public final Maybe unsubscribeOn(Scheduler)
  // XXX: public final Maybe zipWith(MaybeSource,BiFunction) --> Required.
}
