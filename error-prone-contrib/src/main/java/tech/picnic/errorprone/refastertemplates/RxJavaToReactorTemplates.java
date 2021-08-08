package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.assertj.core.util.Streams;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Refaster templates which replace RxJava expressions with equivalent Reactor assertions. */
// XXX: Document the approach and any limitations; see the `TestNGToAssertJTemplates` documentation.
// XXX: Document that the templates use `Completable` rather than `CompletableSource`, etc.
// XXX: Have separate files for Completable, Maybe, Single, Flowable?
final class RxJavaToReactorTemplates {
  private RxJavaToReactorTemplates() {}

  // XXX: Handle array and varargs cases.
  // XXX: Simplify rule with `Completable.amb(Arrays.asList(sources))`?
  static final class CompletableAmbArray {
    @BeforeTemplate
    Completable before(Completable... sources) {
      return Completable.ambArray(sources);
    }

    @AfterTemplate
    Completable after(Completable... sources) {
      return Mono.firstWithSignal(
              Arrays.stream(sources)
                  .map(RxJava2Adapter::completableToMono)
                  .collect(toImmutableList()))
          .as(RxJava2Adapter::monoToCompletable);
    }
  }

  static final class CompletableAmb {
    @BeforeTemplate
    Completable before(Iterable<? extends Completable> sources) {
      return Completable.amb(sources);
    }

    @AfterTemplate
    Completable after(Iterable<? extends Completable> sources) {
      return Mono.firstWithSignal(
              Streams.stream(sources)
                  .map(RxJava2Adapter::completableToMono)
                  .collect(toImmutableList()))
          .as(RxJava2Adapter::monoToCompletable);
    }
  }

  static final class CompletableComplete {
    @BeforeTemplate
    Completable before() {
      return Completable.complete();
    }

    @AfterTemplate
    Completable after() {
      return Mono.empty().as(RxJava2Adapter::monoToCompletable);
    }
  }

  // XXX: Handle array and varargs cases.
  // XXX: Simplify rule with `Completable.amb(Arrays.asList(sources))`?
  static final class CompletableConcatArray {
    @BeforeTemplate
    Completable before(Completable... sources) {
      return Completable.concatArray(sources);
    }

    @AfterTemplate
    Completable after(Completable... sources) {
      return Flux.concat(
              Arrays.stream(sources)
                  .map(RxJava2Adapter::completableToMono)
                  .collect(toImmutableList()))
          .then()
          .as(RxJava2Adapter::monoToCompletable);
    }
  }

  // XXX: Simplify rule with `Completable.concat(Flux.fromIterable(sources))`?
  static final class CompletableConcatIterable {
    @BeforeTemplate
    Completable before(Iterable<? extends Completable> sources) {
      return Completable.concat(sources);
    }

    @AfterTemplate
    Completable after(Iterable<? extends Completable> sources) {
      return Flux.concat(Flux.fromIterable(sources).map(RxJava2Adapter::completableToMono))
          .then()
          .as(RxJava2Adapter::monoToCompletable);
    }
  }

  // XXX: Simplify rule with `Completable.concat(sources, 2)`?
  // XXX: Arguably we should, since the Reactor prefetch is `Queues.XS_BUFFER_SIZE`.
  static final class CompletableConcatPublisher {
    @BeforeTemplate
    Completable before(Publisher<? extends Completable> sources) {
      return Completable.concat(sources);
    }

    @AfterTemplate
    Completable after(Publisher<? extends Completable> sources) {
      return Flux.concat(Flux.from(sources).map(RxJava2Adapter::completableToMono))
          .then()
          .as(RxJava2Adapter::monoToCompletable);
    }
  }

  static final class CompletableConcatPublisherPrefetch {
    @BeforeTemplate
    Completable before(Publisher<? extends Completable> sources, int prefetch) {
      return Completable.concat(sources, prefetch);
    }

    @AfterTemplate
    Completable after(Publisher<? extends Completable> sources, int prefetch) {
      return Flux.concat(Flux.from(sources).map(RxJava2Adapter::completableToMono), prefetch)
          .then()
          .as(RxJava2Adapter::monoToCompletable);
    }
  }

  // XXX: Migrate `Completable#create(CompletableOnSubscribe)`

  // XXX: Migrate `Completable#unsafeCreate(CompletableSource)`

  static final class CompletableDefer {
    @BeforeTemplate
    Completable before(Callable<? extends Completable> completableSupplier) {
      return Completable.defer(completableSupplier);
    }

    @AfterTemplate
    Completable after(Callable<? extends Completable> completableSupplier) {
      return Mono.defer(
              () ->
                  RxJava2ReactorMigrationUtil.getUnchecked(completableSupplier)
                      .as(RxJava2Adapter::completableToMono))
          .as(RxJava2Adapter::monoToCompletable);
    }
  }

  static final class CompletableErrorDeferred {
    @BeforeTemplate
    Completable before(Callable<? extends Throwable> errorSupplier) {
      return Completable.error(errorSupplier);
    }

    @AfterTemplate
    Completable after(Callable<? extends Throwable> errorSupplier) {
      return Mono.error(RxJava2ReactorMigrationUtil.callableAsSupplier(errorSupplier))
          .as(RxJava2Adapter::monoToCompletable);
    }
  }

  static final class CompletableError {
    @BeforeTemplate
    Completable before(Throwable error) {
      return Completable.error(error);
    }

    @AfterTemplate
    Completable after(Throwable error) {
      return Mono.error(error).as(RxJava2Adapter::monoToCompletable);
    }
  }

  // XXX: Migrate `Completable#fromAction(Action)`

  static final class CompletableFromCallable<T> {
    @BeforeTemplate
    Completable before(Callable<T> callable) {
      return Completable.fromCallable(callable);
    }

    @AfterTemplate
    Completable after(Callable<T> callable) {
      return Mono.fromSupplier(RxJava2ReactorMigrationUtil.callableAsSupplier(callable))
          .as(RxJava2Adapter::monoToCompletable);
    }
  }

  // XXX: Also handle `Future`s that don't extend `CompletableFuture`.
  static final class CompletableFromFuture<T> {
    @BeforeTemplate
    Completable before(CompletableFuture<T> future) {
      return Completable.fromFuture(future);
    }

    @AfterTemplate
    Completable after(CompletableFuture<T> future) {
      return Mono.fromFuture(future).as(RxJava2Adapter::monoToCompletable);
    }
  }

  // XXX: Next up: migrate `Completable#fromMaybe(Maybe)`

  // XXX: Move to a separate Maven module.
  static final class RxJava2ReactorMigrationUtil {
    private RxJava2ReactorMigrationUtil() {}

    // XXX: Rename.
    // XXX: Introduce Refaster rules to drop this wrapper when possible.
    static <T> T getUnchecked(Callable<T> callable) {
      try {
        return callable.call();
      } catch (Exception e) {
        throw new RuntimeException("Callable threw checked exception", e);
      }
    }

    // XXX: Rename.
    // XXX: Introduce Refaster rules to drop this wrapper when possible.
    static <T> Supplier<T> callableAsSupplier(Callable<T> callable) {
      return () -> {
        try {
          return callable.call();
        } catch (Exception e) {
          throw new RuntimeException("Callable threw checked exception", e);
        }
      };
    }

    static <T, U, R> BiFunction<? super T, ? super U, ? extends R> toJdkBiFunction(
        io.reactivex.functions.BiFunction<? super T, ? super U, ? extends R> biFunction) {
      return (t, u) -> {
        try {
          return biFunction.apply(t, u);
        } catch (Exception e) {
          throw new RuntimeException("BiFunction threw checked exception", e);
        }
      };
    }
  }

  ///////////////////////////////// FLOWABLE - MOVE!

  static final class FlowableCombineLatest<T1, T2, R> {
    @BeforeTemplate
    Flowable<R> before(
        Publisher<? extends T1> publisher1,
        Publisher<? extends T2> publisher2,
        io.reactivex.functions.BiFunction<? super T1, ? super T2, ? extends R> combiner) {
      return Flowable.combineLatest(publisher1, publisher2, combiner);
    }

    @AfterTemplate
    Flowable<R> after(
        Publisher<? extends T1> publisher1,
        Publisher<? extends T2> publisher2,
        io.reactivex.functions.BiFunction<? super T1, ? super T2, ? extends R> combiner) {
      // XXX: Generic type parameters are specified to appease IDEA; review.
      return RxJava2Adapter.fluxToFlowable(
          Flux.<T1, T2, R>combineLatest(
              publisher1, publisher2, RxJava2ReactorMigrationUtil.toJdkBiFunction(combiner)));
    }
  }
}
