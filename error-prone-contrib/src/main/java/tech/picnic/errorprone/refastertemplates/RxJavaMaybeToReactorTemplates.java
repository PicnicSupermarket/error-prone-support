package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Streams;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

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

  @SuppressWarnings("unchecked")
  // XXX: Refaster rule is almost good, but is not in PRP, so skipping for now.
  //  static final class MaybeAmbArray<T> {
  //    @BeforeTemplate
  //    Maybe<T> before(@Repeated Maybe<T> sources) {
  //      return Maybe.ambArray(sources);
  //    }
  //
  //    @AfterTemplate
  //    Maybe<T> after(@Repeated Maybe<T> sources) {
  //      return RxJava2Adapter.monoToMaybe(
  //          Mono.firstWithSignal(
  //              Arrays.stream(Refaster.asVarargs(sources))
  //                  .map(RxJava2Adapter::maybeToMono)
  //                  .collect(toImmutableList())));
  //    }
  //  }

  // XXX: public static Flowable concat(Iterable)
  // XXX: public static Flowable concat(MaybeSource,MaybeSource)
  // XXX: public static Flowable concat(MaybeSource,MaybeSource,MaybeSource)
  // XXX: public static Flowable concat(MaybeSource,MaybeSource,MaybeSource,MaybeSource)
  // XXX: public static Flowable concat(Publisher)
  // XXX: public static Flowable concat(Publisher,int)

  // XXX: How to make this conversion correct??? Turned off test for now.
  static final class MaybeConcatArray<T> {
    @BeforeTemplate
    Flowable<T> before(@Repeated Maybe<T> sources) {
      return Maybe.concatArray(Refaster.asVarargs(sources));
    }

    @AfterTemplate
    Flowable<T> after(@Repeated Maybe<T> sources) {
      return RxJava2Adapter.fluxToFlowable(
          Flux.concat(
              Arrays.stream(Refaster.asVarargs(sources))
                  .map(RxJava2Adapter::maybeToMono)
                  .collect(toImmutableList())));
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
  /// XXX: Check this one is required for case above.
  abstract static class MaybeDeferFirst<T> {
    @Placeholder
    abstract Maybe<T> maybeProducer();

    @BeforeTemplate
    Maybe<T> before() {
      return Maybe.defer(() -> maybeProducer());
    }

    @AfterTemplate
    Maybe<T> after() {
      return Mono.defer(() -> maybeProducer().as(RxJava2Adapter::maybeToMono))
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

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

  static final class MaybeErrorCallable<T> {
    @BeforeTemplate
    Maybe<T> before(Callable<? extends Throwable> throwable) {
      return Maybe.error(throwable);
    }

    @AfterTemplate
    Maybe<T> after(Callable<? extends Throwable> throwable) {
      return RxJava2Adapter.monoToMaybe(
          Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(throwable)));
    }
  }

  static final class MaybeErrorThrowable<T> {
    @BeforeTemplate
    Maybe<T> before(Throwable throwable) {
      return Maybe.error(throwable);
    }

    @AfterTemplate
    Maybe<T> after(Throwable throwable) {
      return RxJava2Adapter.monoToMaybe(Mono.error(throwable));
    }
  }

  // XXX: Use `CanBeCoercedTo`.
  static final class MaybeFromAction<T> {
    @BeforeTemplate
    Maybe<T> before(Action action) {
      return Maybe.fromAction(action);
    }

    @AfterTemplate
    Maybe<T> after(Action action) {
      return RxJava2Adapter.monoToMaybe(
          Mono.fromRunnable(RxJavaReactorMigrationUtil.toRunnable(action)));
    }
  }

  static final class MaybeFromCallable<T> {
    @BeforeTemplate
    Maybe<T> before(Callable<? extends T> callable) {
      return Maybe.fromCallable(callable);
    }

    @AfterTemplate
    Maybe<T> after(Callable<? extends T> callable) {
      return RxJava2Adapter.monoToMaybe(
          Mono.fromSupplier(RxJavaReactorMigrationUtil.callableAsSupplier(callable)));
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

  static final class MaybeFromRunnable<T> {
    @BeforeTemplate
    Maybe<T> before(Runnable runnable) {
      return Maybe.fromRunnable(runnable);
    }

    @AfterTemplate
    Maybe<T> after(Runnable runnable) {
      return RxJava2Adapter.monoToMaybe(Mono.fromRunnable(runnable));
    }
  }

  static final class MaybeFromSingle<T> {
    @BeforeTemplate
    Maybe<T> before(SingleSource<T> source) {
      return Maybe.fromSingle(source);
    }

    @AfterTemplate
    Maybe<T> after(SingleSource<T> source) {
      return RxJava2Adapter.monoToMaybe(
          Mono.from(RxJava2Adapter.singleToMono(Single.wrap(source))));
    }
  }

  static final class MaybeJust<T> {
    @BeforeTemplate
    Maybe<T> before(T item) {
      return Maybe.just(item);
    }

    @AfterTemplate
    Maybe<T> after(T item) {
      return RxJava2Adapter.monoToMaybe(Mono.just(item));
    }
  }

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
  //   Maybe.just("test").as(RxJava2Adapter::maybeToMono)?
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

  static final class MaybeDoOnError<T> {
    @BeforeTemplate
    Maybe<T> before(Maybe<T> maybe, Consumer<? super Throwable> consumer) {
      return maybe.doOnError(consumer);
    }

    @AfterTemplate
    Maybe<T> after(Maybe<T> maybe, Consumer<? super Throwable> consumer) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(consumer))
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

  // XXX: public final Maybe doOnEvent(BiConsumer)
  // XXX: public final Maybe doOnSubscribe(Consumer)

  static final class MaybeDoOnSuccess<T> {
    @BeforeTemplate
    Maybe<T> before(Maybe<T> maybe, Consumer<T> consumer) {
      return maybe.doOnSuccess(consumer);
    }

    @AfterTemplate
    Maybe<T> after(Maybe<T> maybe, Consumer<T> consumer) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(consumer))
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

  // XXX: public final Maybe doOnTerminate(Action)

  static final class MaybeFilter<T> {
    @BeforeTemplate
    Maybe<T> before(Maybe<T> maybe, Predicate<T> predicate) {
      return maybe.filter(predicate);
    }

    @AfterTemplate
    Maybe<T> after(Maybe<T> maybe, Predicate<T> predicate) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .filter(RxJavaReactorMigrationUtil.toJdkPredicate(predicate))
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

  static final class MaybeFlatMapFunction<I, T extends I, O, M extends MaybeSource<? extends O>> {
    @BeforeTemplate
    Maybe<O> before(Maybe<T> maybe, Function<I, M> function) {
      return maybe.flatMap(function);
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    @UseImportPolicy(ImportPolicy.IMPORT_CLASS_DIRECTLY)
    Maybe<O> after(Maybe<T> maybe, Function<I, M> function) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .flatMap(
              v ->
                  RxJava2Adapter.maybeToMono(
                      Maybe.wrap(
                          (Maybe<O>) RxJavaReactorMigrationUtil.toJdkFunction(function).apply(v))))
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

  // XXX: Test this one
  static final class MaybeFlatMapCompletable<T, R extends CompletableSource> {
    @BeforeTemplate
    Completable before(Maybe<T> maybe, Function<T, R> function) {
      return maybe.flatMapCompletable(function);
    }

    @AfterTemplate
    Completable after(Maybe<T> maybe, Function<T, R> function) {
      return RxJava2Adapter.monoToCompletable(
          RxJava2Adapter.maybeToMono(maybe)
              .flatMap(
                  e ->
                      RxJava2Adapter.completableToMono(
                          Completable.wrap(
                              RxJavaReactorMigrationUtil.toJdkFunction((Function<T, R>) function)
                                  .apply(e))))
              .then());
    }
  }

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

  // XXX: This one is still not correct, see example above. Perhaps try this? <S, T extends S, O> {
  static final class MaybeFlatMapSingleElement<T, O> {
    @BeforeTemplate
    Maybe<O> before(
        Maybe<T> maybe, Function<? super T, ? extends SingleSource<? extends O>> function) {
      return maybe.flatMapSingleElement(function);
    }

    @AfterTemplate
    Maybe<O> after(
        Maybe<T> maybe, Function<? super T, ? extends SingleSource<? extends O>> function) {
      return RxJava2Adapter.monoToMaybe(
          RxJava2Adapter.maybeToMono(maybe)
              .flatMap(
                  e ->
                      RxJava2Adapter.singleToMono(
                          Single.wrap(
                              RxJavaReactorMigrationUtil.toJdkFunction(
                                      (Function<T, SingleSource<O>>) function)
                                  .apply(e)))));
    }
  }

  // XXX: Discuss with Stephan. Still not useful for methodreferences.
  //  abstract class MaybeUnwrapSingleElement<T, O> {
  //    @BeforeTemplate
  //    Maybe<O> after(
  //        Maybe<T> maybe, Function<? super T, ? extends SingleSource<? extends O>> function) {
  //      return RxJava2Adapter.monoToMaybe(
  //          RxJava2Adapter.maybeToMono(maybe)
  //              .flatMap(
  //                  e ->
  //                      RxJava2Adapter.singleToMono(
  //                          Single.wrap(
  //                              RxJavaReactorMigrationUtil.toJdkFunction(
  //                                      (Function<T, SingleSource<O>>) function)
  //                                  .apply(e)))));
  //    }
  //  }

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
      return maybe.as(RxJava2Adapter::maybeToMono).then().as(RxJava2Adapter::monoToCompletable);
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
          .map(RxJavaReactorMigrationUtil.toJdkFunction(mapper))
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

  // XXX: Remove the toJdkFunction with `CanBeCoercedTo`.

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

  // XXX: Make test
  static final class MaybeSourceSwitchIfEmpty<S, T extends S> {
    @BeforeTemplate
    Maybe<S> before(Maybe<S> maybe, MaybeSource<T> maybeSource) {
      return maybe.switchIfEmpty(maybeSource);
    }

    @AfterTemplate
    Maybe<S> after(Maybe<S> maybe, MaybeSource<T> maybeSource) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .switchIfEmpty(RxJava2Adapter.maybeToMono(Maybe.wrap(maybeSource)))
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

  static final class MaybeSwitchIfEmpty<S, T extends S> {
    @BeforeTemplate
    Single<S> before(Maybe<S> maybe, SingleSource<T> single) {
      return maybe.switchIfEmpty(single);
    }

    @AfterTemplate
    Single<S> after(Maybe<S> maybe, SingleSource<T> single) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .switchIfEmpty(RxJava2Adapter.singleToMono(Single.wrap(single)))
          .as(RxJava2Adapter::monoToSingle);
    }
  }

  // XXX: public final Maybe takeUntil(MaybeSource)
  // XXX: public final Maybe takeUntil(Publisher)
  // XXX: public final Maybe timeout(long,TimeUnit)
  // XXX: public final Maybe timeout(long,TimeUnit,MaybeSource)
  // XXX: public final Maybe timeout(long,TimeUnit,Scheduler)
  // XXX: public final Maybe timeout(long,TimeUnit,Scheduler,MaybeSource)
  // XXX: public final Maybe timeout(MaybeSource)
  // XXX: public final Maybe timeout(MaybeSource,MaybeSource)
  // XXX: public final Maybe timeout(Publisher)
  // XXX: public final Maybe timeout(Publisher,MaybeSource)
  // XXX: public final Object to(Function)

  static final class MaybeToFlowable<T> {
    @BeforeTemplate
    Flowable<T> before(Maybe<T> maybe) {
      return maybe.toFlowable();
    }

    @AfterTemplate
    Flowable<T> after(Maybe<T> maybe) {
      return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(maybe).flux());
    }
  }

  // XXX: public final Observable toObservable()

  // XXX: This one should be improved, it is not correct yet.
  static final class MaybeToSingle<T> {
    @BeforeTemplate
    Single<T> before(Maybe<T> maybe) {
      return maybe.toSingle();
    }

    @AfterTemplate
    Single<T> after(Maybe<T> maybe) {
      return RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(maybe).single());
    }
  }

  // XXX: public final Single toSingle(Object)
  // XXX: public final Maybe unsubscribeOn(Scheduler)
  // XXX: public final Maybe zipWith(MaybeSource,BiFunction) --> Required.

  static final class MaybeTestAssertResultItem<T> {
    @BeforeTemplate
    void before(Maybe<T> maybe, T item) throws InterruptedException {
      Refaster.anyOf(
          maybe.test().await().assertResult(item), maybe.test().await().assertValue(item));
    }

    @AfterTemplate
    void after(Maybe<T> maybe, T item) {
      RxJava2Adapter.maybeToMono(maybe).as(StepVerifier::create).expectNext(item).verifyComplete();
    }
  }

  static final class MaybeTestAssertResult<T> {
    @BeforeTemplate
    void before(Maybe<T> maybe) throws InterruptedException {
      maybe.test().await().assertResult();
    }

    @AfterTemplate
    void after(Maybe<T> maybe) {
      RxJava2Adapter.maybeToMono(maybe).as(StepVerifier::create).verifyComplete();
    }
  }

  static final class MaybeTestAssertValue<T> {
    @BeforeTemplate
    void before(Maybe<T> maybe, Predicate<T> predicate) throws InterruptedException {
      Refaster.anyOf(
          maybe.test().await().assertValue(predicate),
          maybe.test().await().assertValue(predicate).assertComplete(),
          maybe.test().await().assertValue(predicate).assertNoErrors().assertComplete(),
          maybe.test().await().assertComplete().assertValue(predicate));
    }

    @AfterTemplate
    void after(Maybe<T> maybe, Predicate<T> predicate) {
      RxJava2Adapter.maybeToMono(maybe)
          .as(StepVerifier::create)
          .expectNextMatches(RxJavaReactorMigrationUtil.toJdkPredicate(predicate))
          .verifyComplete();
    }
  }

  static final class MaybeTestAssertComplete<T> {
    @BeforeTemplate
    void before(Maybe<T> maybe) throws InterruptedException {
      maybe.test().await().assertComplete();
      // XXX: Add this one here? maybe.test().await().assertEmpty();
    }

    @AfterTemplate
    void after(Maybe<T> maybe) {
      RxJava2Adapter.maybeToMono(maybe).as(StepVerifier::create).verifyComplete();
    }
  }

  static final class MaybeTestAssertErrorClass<T> {
    @BeforeTemplate
    void before(Maybe<T> maybe, Class<? extends Throwable> errorClass) throws InterruptedException {
      maybe.test().await().assertError(errorClass);
    }

    @AfterTemplate
    void after(Maybe<T> maybe, Class<? extends Throwable> errorClass) {
      RxJava2Adapter.maybeToMono(maybe).as(StepVerifier::create).expectError(errorClass).verify();
    }
  }

  // XXX: .assertError(Throwable) -> (not used in PRP).

  static final class MaybeTestAssertNoErrors<T> {
    @BeforeTemplate
    void before(Maybe<T> maybe) throws InterruptedException {
      maybe.test().await().assertNoErrors();
    }

    @AfterTemplate
    void after(Maybe<T> maybe) {
      RxJava2Adapter.maybeToMono(maybe).as(StepVerifier::create).verifyComplete();
    }
  }

  // XXX: The following two are combined often.
  //  static final class MaybeTestAssertValueSet<T> {
  //    @BeforeTemplate
  //    void before(Maybe<T> maybe, Collection<? extends T> expected) throws InterruptedException {
  //      maybe.test().await().assertValueSet(expected);
  //      expected.it
  //    }
  //
  //    @AfterTemplate
  //    void after(Maybe<T> maybe, Collection<? extends T> expected) {
  //      RxJava2Adapter.maybeToMono(maybe).as(StepVerifier::create).expectNextMatches(t ->
  // ).verifyComplete();
  //    }
  //  }

  static final class MaybeTestAssertValueCount<T> {
    @BeforeTemplate
    void before(Maybe<T> maybe, int count) throws InterruptedException {
      maybe.test().await().assertValueCount(count);
    }

    @AfterTemplate
    void after(Maybe<T> maybe, int count) {
      RxJava2Adapter.maybeToMono(maybe)
          .as(StepVerifier::create)
          .expectNextCount(count)
          .verifyComplete();
    }
  }

  // XXX: Add test
  static final class MaybeTestAssertFailure<T> {
    @BeforeTemplate
    void before(Maybe<T> maybe, Class<? extends Throwable> error) throws InterruptedException {
      maybe.test().await().assertFailure(error);
    }

    @AfterTemplate
    void after(Maybe<T> maybe, Class<? extends Throwable> error) {
      RxJava2Adapter.maybeToMono(maybe).as(StepVerifier::create).verifyError(error);
    }
  }

  // XXX: Add test
  // XXX: This introduces AssertJ dependency
  static final class MaybeTestAssertFailureAndMessage<T> {
    @BeforeTemplate
    void before(Maybe<T> maybe, Class<? extends Throwable> error, String message)
        throws InterruptedException {
      maybe.test().await().assertFailureAndMessage(error, message);
    }

    @AfterTemplate
    void after(Maybe<T> maybe, Class<? extends Throwable> error, String message) {
      RxJava2Adapter.maybeToMono(maybe)
          .as(StepVerifier::create)
          .expectErrorSatisfies(
              t -> assertThat(t).isInstanceOf(error).hasMessageContaining(message))
          .verify();
    }
  }

  // XXX: public final TestObserver test(boolean)
}
