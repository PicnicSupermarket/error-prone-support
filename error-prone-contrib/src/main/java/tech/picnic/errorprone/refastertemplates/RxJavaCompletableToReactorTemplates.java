package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Streams;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

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

  static final class CompletableComplete {
    @BeforeTemplate
    Completable before() {
      return Completable.complete();
    }

    @AfterTemplate
    Completable after() {
      return RxJava2Adapter.monoToCompletable(Mono.empty());
    }
  }

  // XXX: public static Completable concat(Iterable)
  // XXX: public static Completable concat(Publisher)
  // XXX: public static Completable concat(Publisher,int)
  // XXX: public static Completable concatArray(CompletableSource[])
  // XXX: public static Completable create(CompletableOnSubscribe)

  // XXX: The types of the @Before and @After are not matching, is this a problem?
  static final class CompletableDefer {
    @BeforeTemplate
    Completable before(Callable<? extends CompletableSource> supplier) {
      return Completable.defer(supplier);
    }

    @AfterTemplate
    Completable after(Callable<? extends Completable> supplier) {
      return RxJava2Adapter.monoToCompletable(
          Mono.defer(
              () ->
                  RxJavaReactorMigrationUtil.callableAsSupplier(supplier)
                      .get()
                      .as(RxJava2Adapter::completableToMono)));
    }
  }

  static final class CompletableErrorCallable {
    @BeforeTemplate
    Completable before(Callable<? extends Throwable> throwable) {
      return Completable.error(throwable);
    }

    @AfterTemplate
    Completable after(Supplier<? extends Throwable> throwable) {
      return RxJava2Adapter.monoToCompletable(Mono.error(throwable));
    }
  }

  static final class CompletableErrorThrowable {
    @BeforeTemplate
    Completable before(Throwable throwable) {
      return Completable.error(throwable);
    }

    @AfterTemplate
    Completable after(Throwable throwable) {
      return RxJava2Adapter.monoToCompletable(Mono.error(throwable));
    }
  }

  static final class CompletableFromAction {
    @BeforeTemplate
    Completable before(Action action) {
      return Completable.fromAction(action);
    }

    @AfterTemplate
    Completable after(Action action) {
      return RxJava2Adapter.monoToCompletable(
          Mono.fromRunnable(RxJavaReactorMigrationUtil.toRunnable(action)));
    }
  }

  static final class CompletableFromCallable {
    @BeforeTemplate
    Completable before(Callable<?> supplier) {
      return Completable.fromCallable(supplier);
    }

    @AfterTemplate
    Completable after(Callable<?> supplier) {
      return RxJava2Adapter.monoToCompletable(Mono.fromCallable(supplier));
    }
  }

  // XXX: public static Completable fromFuture(Future)
  // XXX: public static Completable fromMaybe(MaybeSource)
  // XXX: public static Completable fromObservable(ObservableSource)

  static final class CompletableFromPublisher<T> {
    @BeforeTemplate
    Completable before(Publisher<T> source) {
      return Completable.fromPublisher(source);
    }

    @AfterTemplate
    Completable after(Publisher<T> source) {
      return RxJava2Adapter.monoToCompletable(Mono.from(source));
    }
  }

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

  static final class CompletableWrap {
    @BeforeTemplate
    Completable before(Completable source) {
      return Completable.wrap(source);
    }

    @AfterTemplate
    Completable after(Completable source) {
      return source;
    }
  }

  // XXX: public final Completable ambWith(CompletableSource)

  static final class CompletableAndThenCompletable {
    @BeforeTemplate
    Completable before(Completable completable, CompletableSource source) {
      return completable.andThen(source);
    }

    @AfterTemplate
    Completable after(Completable completable, CompletableSource source) {
      return RxJava2Adapter.monoToCompletable(
          RxJava2Adapter.completableToMono(completable)
              .then(RxJava2Adapter.completableToMono(Completable.wrap(source))));
    }
  }

  // XXX: Add the test
  static final class CompletableAndThenMaybe<T> {
    @BeforeTemplate
    Maybe<T> before(Completable completable, MaybeSource<T> source) {
      return completable.andThen(source);
    }

    @AfterTemplate
    Maybe<T> after(Completable completable, MaybeSource<T> source) {
      return completable
          .as(RxJava2Adapter::completableToMono)
          .then(RxJava2Adapter.maybeToMono(Maybe.wrap(source)))
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

  // XXX: public final Observable andThen(ObservableSource)

  // XXX: Add test
  static final class CompletableAndThenPublisher<T> {
    @BeforeTemplate
    Flowable<T> before(Completable completable, Publisher<T> source) {
      return completable.andThen(source);
    }

    @AfterTemplate
    Flowable<T> after(Completable completable, Publisher<T> source) {
      return completable
          .as(RxJava2Adapter::completableToMono)
          .thenMany(source)
          .as(RxJava2Adapter::fluxToFlowable);
    }
  }

  // XXX: Verify this case
  static final class CompletableAndThenSingle<T> {
    @BeforeTemplate
    Single<T> before(Completable completable, SingleSource<T> source) {
      return completable.andThen(source);
    }

    @AfterTemplate
    Single<T> after(Completable completable, SingleSource<T> source) {
      return RxJava2Adapter.monoToSingle(
          RxJava2Adapter.completableToMono(completable)
              .then(RxJava2Adapter.singleToMono(Single.wrap(source))));
    }
  }

  // XXX: public final Object as(CompletableConverter)

  // XXX: Test this one.
  static final class CompletableBlockingAwait {
    @BeforeTemplate
    void before(Completable completable) {
      completable.blockingAwait();
    }

    @AfterTemplate
    void after(Completable completable) {
      completable.as(RxJava2Adapter::completableToMono).block();
    }
  }

  // XXX: public final boolean blockingAwait(long,TimeUnit)

  // XXX: Strictly speaking not correct, should return `Throwable`.
  static final class CompletableBlockingGet {
    @BeforeTemplate
    Object before(Completable completable) {
      return completable.blockingGet();
    }

    @AfterTemplate
    Object after(Completable completable) {
      return RxJava2Adapter.completableToMono(completable).block();
    }
  }

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
  // XXX: public final Completable doOnComplete(Action)
  // XXX: public final Completable doOnDispose(Action)
  // XXX: public final Completable doOnError(Consumer)
  // XXX: public final Completable doOnEvent(Consumer)
  // XXX: public final Completable doOnSubscribe(Consumer) --> Required

  // XXX Doesnt work yet....
  abstract static class CompletableDoOnSubscribe<T> {
    @Placeholder
    abstract T placeholder();

    @BeforeTemplate
    Completable before(Completable completable) {
      return completable.doOnSubscribe((Disposable d) -> placeholder());
    }

    @AfterTemplate
    Completable after(Completable completable) {
      return RxJava2Adapter.monoToCompletable(
          RxJava2Adapter.completableToMono(completable)
              .doOnSubscribe((Subscription s) -> placeholder()));
    }
  }

  // XXX: Add test
  static final class CompletableDoOnError {
    @BeforeTemplate
    Completable before(Completable completable, Consumer<? super Throwable> consumer) {
      return completable.doOnError(consumer);
    }

    @AfterTemplate
    Completable after(Completable completable, Consumer<? super Throwable> consumer) {
      return completable
          .as(RxJava2Adapter::completableToMono)
          .doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(consumer))
          .as(RxJava2Adapter::monoToCompletable);
    }
  }

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
  // XXX: public final Completable timeout(long,TimeUnit)
  // XXX: public final Completable timeout(long,TimeUnit,CompletableSource)
  // XXX: public final Completable timeout(long,TimeUnit,Scheduler)
  // XXX: public final Completable timeout(long,TimeUnit,Scheduler,CompletableSource)
  // XXX: public final Object to(Function)

  static final class CompletableToFlowable<T> {
    @BeforeTemplate
    Flowable<Void> before(Completable completable) {
      return completable.toFlowable();
    }

    @AfterTemplate
    Flowable<Void> after(Completable completable) {
      return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.completableToMono(completable).flux());
    }
  }

  static final class CompletableToMaybe<T> {
    @BeforeTemplate
    Maybe<Void> before(Completable completable) {
      return completable.toMaybe();
    }

    @AfterTemplate
    Maybe<Void> after(Completable completable) {
      return RxJava2Adapter.monoToMaybe(RxJava2Adapter.completableToMono(completable));
    }
  }

  // XXX: public final Observable toObservable()
  // XXX: public final Single toSingle(Callable)
  // XXX: public final Single toSingleDefault(Object)
  // XXX: public final Completable unsubscribeOn(Scheduler)

  static final class CompletableTestAssertResult {
    @BeforeTemplate
    void before(Completable completable) throws InterruptedException {
      completable.test().await().assertResult();
    }

    @AfterTemplate
    void after(Completable completable) {
      RxJava2Adapter.completableToMono(completable).as(StepVerifier::create).verifyComplete();
    }
  }

  static final class CompletableTestAssertComplete {
    @BeforeTemplate
    void before(Completable completable) throws InterruptedException {
      completable.test().await().assertComplete();
      // XXX: Add this one here? completable.test().await().assertEmpty();
    }

    @AfterTemplate
    void after(Completable completable) {
      RxJava2Adapter.completableToMono(completable).as(StepVerifier::create).verifyComplete();
    }
  }

  static final class CompletableTestAssertErrorClass {
    @BeforeTemplate
    void before(Completable completable, Class<? extends Throwable> errorClass)
        throws InterruptedException {
      Refaster.anyOf(
          completable.test().await().assertError(errorClass),
          completable.test().assertError(errorClass));
    }

    @AfterTemplate
    void after(Completable completable, Class<? extends Throwable> errorClass) {
      RxJava2Adapter.completableToMono(completable)
          .as(StepVerifier::create)
          .verifyError(errorClass);
    }
  }

  // XXX: .assertError(Throwable) -> (not used in PRP).

  static final class CompletableTestAssertNoErrors {
    @BeforeTemplate
    void before(Completable completable) throws InterruptedException {
      completable.test().await().assertNoErrors();
    }

    @AfterTemplate
    void after(Completable completable) {
      RxJava2Adapter.completableToMono(completable).as(StepVerifier::create).verifyComplete();
    }
  }

  static final class CompletableTestAssertValueCount {
    @BeforeTemplate
    void before(Completable completable, int count) throws InterruptedException {
      completable.test().await().assertValueCount(count);
    }

    @AfterTemplate
    void after(Completable completable, int count) {
      RxJava2Adapter.completableToMono(completable)
          .as(StepVerifier::create)
          .expectNextCount(count)
          .verifyComplete();
    }
  }

  // XXX: Add test
  static final class CompletableTestAssertFailure {
    @BeforeTemplate
    void before(Completable completable, Class<? extends Throwable> error)
        throws InterruptedException {
      completable.test().await().assertFailure(error);
    }

    @AfterTemplate
    void after(Completable completable, Class<? extends Throwable> error) {
      RxJava2Adapter.completableToMono(completable).as(StepVerifier::create).verifyError(error);
    }
  }

  // XXX: Add test
  static final class CompletableTestAssertNoValues {
    @BeforeTemplate
    void before(Completable completable) throws InterruptedException {
      completable.test().await().assertNoValues();
    }

    @AfterTemplate
    void after(Completable completable) {
      RxJava2Adapter.completableToMono(completable).as(StepVerifier::create).verifyComplete();
    }
  }

  // XXX: Add test
  static final class CompletableTestAssertFailureAndMessage {
    @BeforeTemplate
    void before(Completable completable, Class<? extends Throwable> error, String message)
        throws InterruptedException {
      completable.test().await().assertFailureAndMessage(error, message);
    }

    @AfterTemplate
    void after(Completable completable, Class<? extends Throwable> error, String message) {
      RxJava2Adapter.completableToMono(completable)
          .as(StepVerifier::create)
          .expectErrorSatisfies(
              t -> assertThat(t).isInstanceOf(error).hasMessageContaining(message))
          .verify();
    }
  }

  // XXX: public final TestObserver test(boolean)
}
