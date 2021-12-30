package tech.picnic.errorprone.refastertemplates;

import static com.google.auto.common.MoreStreams.toImmutableSet;
import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.CanTransformToTargetType;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/** Assorted Refaster templates for the migration of RxJava to Reactor. */
final class RxJavaToReactorTemplates {
  private RxJavaToReactorTemplates() {}

  @SuppressWarnings("NullableProblems")
  static final class FluxToFlowableToFlux<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, BackpressureStrategy strategy) {
      return Refaster.anyOf(
          RxJava2Adapter.fluxToFlowable(flux).as(RxJava2Adapter::flowableToFlux),
          RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(flux)),
          RxJava2Adapter.flowableToFlux(flux.as(RxJava2Adapter::fluxToFlowable)),
          RxJava2Adapter.observableToFlux(flux.as(RxJava2Adapter::fluxToObservable), strategy),
          flux.as(RxJava2Adapter::fluxToObservable)
              .toFlowable(strategy)
              .as(RxJava2Adapter::flowableToFlux),
          RxJava2Adapter.observableToFlux(RxJava2Adapter.fluxToObservable(flux), strategy),
          flux.as(RxJava2Adapter::fluxToFlowable).as(RxJava2Adapter::flowableToFlux));
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux) {
      return flux;
    }
  }

  @SuppressWarnings("NullableProblems")
  static final class MonoToFlowableToMono<T> {
    @BeforeTemplate
    Mono<Void> before(Mono<Void> mono) {
      return Refaster.anyOf(
          RxJava2Adapter.monoToCompletable(mono).as(RxJava2Adapter::completableToMono),
          mono.as(RxJava2Adapter::monoToCompletable).as(RxJava2Adapter::completableToMono),
          RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(mono)),
          RxJava2Adapter.completableToMono(mono.as(RxJava2Adapter::monoToCompletable)));
    }

    @BeforeTemplate
    Mono<T> before2(Mono<T> mono) {
      return Refaster.anyOf(
          RxJava2Adapter.monoToMaybe(mono).as(RxJava2Adapter::maybeToMono),
          RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(mono)),
          RxJava2Adapter.maybeToMono(mono.as(RxJava2Adapter::monoToMaybe)),
          mono.as(RxJava2Adapter::monoToMaybe).as(RxJava2Adapter::maybeToMono),
          RxJava2Adapter.monoToSingle(mono).as(RxJava2Adapter::singleToMono),
          RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(mono)),
          RxJava2Adapter.singleToMono(mono.as(RxJava2Adapter::monoToSingle)),
          mono.as(RxJava2Adapter::monoToSingle).as(RxJava2Adapter::singleToMono));
    }

    @BeforeTemplate
    Mono<Void> before3(Mono<T> mono) {
      return Refaster.anyOf(
          RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(mono)),
          RxJava2Adapter.completableToMono(mono.as(RxJava2Adapter::monoToCompletable)),
          RxJava2Adapter.monoToCompletable(mono).as(RxJava2Adapter::completableToMono));
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono) {
      return mono;
    }
  }

  // XXX: Add test cases
  static final class MonoToFlowableToFlux<T> {
    @BeforeTemplate
    @SuppressWarnings("NullableProblems")
    Flux<T> before(Mono<T> mono) {
      return mono.as(RxJava2Adapter::monoToFlowable).as(RxJava2Adapter::flowableToFlux);
    }

    @AfterTemplate
    Flux<T> after(Mono<T> mono) {
      return mono.flux();
    }
  }

  static final class MonoErrorCallableSupplierUtil<T> {
    @BeforeTemplate
    Mono<T> before(@CanTransformToTargetType Callable<? extends Throwable> callable) {
      return Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(callable));
    }

    @AfterTemplate
    Mono<T> after(Supplier<? extends Throwable> callable) {
      return Mono.error(callable);
    }
  }

  @SuppressWarnings({"NoFunctionalReturnType", "FunctionalInterfaceClash"})
  static final class RemoveUtilCallable<T> {
    @BeforeTemplate
    Supplier<T> before(
        //        @NotMatches(IsMethodReferenceOrLambdaHasReturnStatement.class)
        @CanTransformToTargetType Callable<T> callable) {
      return RxJavaReactorMigrationUtil.callableAsSupplier(callable);
    }

    @AfterTemplate
    Supplier<T> before(Supplier<T> callable) {
      return callable;
    }
  }

  @SuppressWarnings("NoFunctionalReturnType")
  static final class UnnecessaryFunctionConversion<I, O> {
    @BeforeTemplate
    java.util.function.Function<? extends I, ? extends O> before(
        //        @NotMatches(IsMethodReferenceOrLambdaHasReturnStatement.class)
        @CanTransformToTargetType Function<? extends I, ? extends O> function) {
      return RxJavaReactorMigrationUtil.toJdkFunction(function);
    }

    @AfterTemplate
    java.util.function.Function<? extends I, ? extends O> after(
        java.util.function.Function<? extends I, ? extends O> function) {
      return function;
    }
  }

  @SuppressWarnings("NoFunctionalReturnType")
  static final class UnnecessaryBiFunctionConversion<T, U, R> {
    @BeforeTemplate
    java.util.function.BiFunction<? super T, ? super U, ? extends R> before(
        @CanTransformToTargetType BiFunction<? super T, ? super U, ? extends R> zipper) {
      return RxJavaReactorMigrationUtil.toJdkBiFunction(zipper);
    }

    @AfterTemplate
    java.util.function.BiFunction<? super T, ? super U, ? extends R> after(
        java.util.function.BiFunction<? super T, ? super U, ? extends R> zipper) {
      return zipper;
    }
  }

  @SuppressWarnings("NoFunctionalReturnType")
  static final class UnnecessaryConsumerConversion<T> {
    @BeforeTemplate
    java.util.function.Consumer<? extends T> before(
        @CanTransformToTargetType Consumer<? extends T> consumer) {
      return RxJavaReactorMigrationUtil.toJdkConsumer(consumer);
    }

    @AfterTemplate
    java.util.function.Consumer<? extends T> after(
        java.util.function.Consumer<? extends T> consumer) {
      return consumer;
    }
  }

  static final class UnnecessaryRunnableConversion {
    @BeforeTemplate
    Runnable before(@CanTransformToTargetType Action action) {
      return RxJavaReactorMigrationUtil.toRunnable(action);
    }

    @AfterTemplate
    Runnable after(Runnable action) {
      return action;
    }
  }

  @SuppressWarnings("NoFunctionalReturnType")
  static final class UnnecessaryPredicateConversion<T> {
    @BeforeTemplate
    java.util.function.Predicate<? extends T> before(
        @CanTransformToTargetType Predicate<? extends T> predicate) {
      return RxJavaReactorMigrationUtil.toJdkPredicate(predicate);
    }

    @AfterTemplate
    java.util.function.Predicate<? extends T> after(
        java.util.function.Predicate<? extends T> predicate) {
      return predicate;
    }
  }

  static final class FlowableBiFunctionRemoveUtil<T, U, R> {
    @BeforeTemplate
    Flowable<R> before(
        Publisher<? extends T> source1,
        Publisher<? extends U> source2,
        @CanTransformToTargetType BiFunction<? super T, ? super U, ? extends R> zipper) {
      return RxJava2Adapter.fluxToFlowable(
          Flux.<T, U, R>zip(source1, source2, RxJavaReactorMigrationUtil.toJdkBiFunction(zipper)));
    }

    @AfterTemplate
    Flowable<R> after(
        Publisher<? extends T> source1,
        Publisher<? extends U> source2,
        java.util.function.BiFunction<? super T, ? super U, ? extends R> zipper) {
      return RxJava2Adapter.fluxToFlowable(Flux.<T, U, R>zip(source1, source2, zipper));
    }
  }

  ///////////////////////////////////////////
  //////////// ASSORTED TEMPLATES ///////////
  ///////////////////////////////////////////

  static final class MonoFromNestedPublisher<T> {
    @BeforeTemplate
    Mono<T> before(Flux<T> flux) {
      return Mono.from(RxJava2Adapter.fluxToFlowable(flux));
    }

    @AfterTemplate
    Mono<T> after(Flux<T> flux) {
      return Mono.from(flux);
    }
  }

  static final class FlowableToFluxWithFilter<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Predicate<T> predicate) {
      return RxJava2Adapter.flowableToFlux(
          flux.filter(RxJavaReactorMigrationUtil.toJdkPredicate(predicate))
              .as(RxJava2Adapter::fluxToFlowable));
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Predicate<T> predicate) {
      return flux.filter(RxJavaReactorMigrationUtil.toJdkPredicate(predicate));
    }
  }

  static final class ObservableToFlux<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, Predicate<T> predicate, BackpressureStrategy strategy) {
      return RxJava2Adapter.observableToFlux(
          RxJava2Adapter.fluxToObservable(flux).filter(predicate), strategy);
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, Predicate<T> predicate, BackpressureStrategy strategy) {
      return flux.filter(RxJavaReactorMigrationUtil.toJdkPredicate(predicate));
    }
  }

  static final class MonoFromToFlowableToFlux<T> {
    @BeforeTemplate
    Flux<T> before(Mono<T> mono) {
      return RxJava2Adapter.flowableToFlux(mono.as(RxJava2Adapter::monoToFlowable));
    }

    @AfterTemplate
    Flux<T> after(Mono<T> mono) {
      return mono.flux();
    }
  }

  /** Remove unnecessary {@code Mono#then} */
  static final class MonoThen<T, S> {
    @BeforeTemplate
    Mono<S> before(Mono<T> mono, Mono<S> other) {
      return mono.then().then(other);
    }

    @AfterTemplate
    Mono<S> after(Mono<T> mono, Mono<S> other) {
      return mono.then(other);
    }
  }

  /** Remove unnecessary {@code Flux#ignoreElements} */
  static final class FluxThen<T> {
    @BeforeTemplate
    Mono<Void> before(Flux<T> flux) {
      return flux.ignoreElements().then();
    }

    @AfterTemplate
    Mono<Void> after(Flux<T> flux) {
      return flux.then();
    }
  }

  static final class MonoCollectToImmutableList<T> {
    @BeforeTemplate
    Mono<List<T>> before(Flux<T> flux) {
      return flux.collectList();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    Mono<List<T>> after(Flux<T> flux) {
      return flux.collect(toImmutableList());
    }
  }

  static final class MonoDefaultIfEmpty<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, T item) {
      return mono.switchIfEmpty(Mono.just(item));
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, T item) {
      return mono.defaultIfEmpty(item);
    }
  }

  static final class FluxDefaultIfEmpty<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, T item) {
      return flux.switchIfEmpty(Flux.just(item));
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux, T item) {
      return flux.defaultIfEmpty(item);
    }
  }

  /** Remove unnecessary {@code Mono#then} */
  static final class MonoVoid {
    @BeforeTemplate
    Mono<Void> before(Mono<Void> mono) {
      return mono.then();
    }

    @AfterTemplate
    Mono<Void> after(Mono<Void> mono) {
      return mono;
    }
  }

  static final class FlatMapFluxFromArray<T> {
    @BeforeTemplate
    @SuppressWarnings("unchecked")
    Flux<Object> before(Flux<T[]> flux) {
      return flux.flatMap(Flowable::fromArray);
    }

    @UseImportPolicy(ImportPolicy.IMPORT_CLASS_DIRECTLY)
    @AfterTemplate
    Flux<Object> after(Flux<T[]> flux) {
      return flux.flatMap(Flux::fromArray);
    }
  }

  // XXX: Move this to correct class later on.
  static final class FluxToImmutableSet<T> {
    @BeforeTemplate
    Mono<ImmutableSet<T>> before(Flux<T> flux) {
      return flux.collect(toImmutableList()).map(ImmutableSet::copyOf);
    }

    @AfterTemplate
    Mono<ImmutableSet<T>> after(Flux<T> flux) {
      return flux.collect(toImmutableSet());
    }
  }

  //  /** Remove unnecessary {@code Flux#next}. This is not *strictly* behavior preserving. */
  //  static final class FluxSingle<T> {
  //    @BeforeTemplate
  //    Mono<T> before(Flux<T> flux) {
  //      return flux.next().single();
  //    }
  //
  //    @AfterTemplate
  //    Mono<T> after(Flux<T> flux) {
  //      return flux.single();
  //    }
  //  }

  // XXX: Find out how we can use this in the future.
  //  static final class RemoveRedundantCast<T> {
  //    @BeforeTemplate
  //    T before(T object) {
  //      return (T) object;
  //    }
  //
  //    @AfterTemplate
  //    T after(T object) {
  //      return object;
  //    }
  //  }
}
