package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.CanTransformToTargetType;
import io.reactivex.BackpressureStrategy;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/** Assorted Refaster templates for the migration of RxJava to Reactor */
public final class RxJavaToReactorTemplates {
  private RxJavaToReactorTemplates() {}

  // XXX: This one is for a specific case in the FcService... Can we solve this in a different way?
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

  // XXX: This one is for a specific case in the  getActiveDcs_migrated()... Can
  // we solve this in a different way?
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

  // XXX: Add test cases
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

  // XXX: What should we do with the naming here? Since it is not entirely correct now.
  // (ObsoleteConversions?)
  // XXX: Add test cases for the RxJava2adapter.XXX way. And the combination.
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
          RxJava2Adapter.completableToMono(mono.as(RxJava2Adapter::monoToCompletable)));
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono) {
      return mono;
    }
  }

  // XXX: Find solution to this?
  //  static final class MaybeConversions<T> {
  //    @BeforeTemplate
  //    Maybe<T> before(Single<T> single) {
  //      return RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(single));
  //    }
  //  }

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

  static final class MonoErrorCallableSupplierUtil<T> {
    @BeforeTemplate
    Mono<T> before(@CanTransformToTargetType Callable<Throwable> callable) {
      return Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(callable));
    }

    @AfterTemplate
    Mono<T> after(Supplier<Throwable> callable) {
      return Mono.error(callable);
    }
  }

  @SuppressWarnings({"NoFunctionalReturnType", "FunctionalInterfaceClash"})
  static final class RemoveUtilCallable<T> {
    @BeforeTemplate
    Supplier<T> before(@CanTransformToTargetType Callable<T> callable) {
      return RxJavaReactorMigrationUtil.callableAsSupplier(callable);
    }

    @AfterTemplate
    Supplier<T> before(Supplier<T> callable) {
      return callable;
    }
  }

  // XXX: Temporarily disabled @CanBeTransformedTo...
  @SuppressWarnings("NoFunctionalReturnType")
  static final class UnnecessaryFunctionConversion<I, O> {
    @BeforeTemplate
    java.util.function.Function<I, O> before(Function<I, O> function) {
      return Refaster.anyOf(
          RxJavaReactorMigrationUtil.toJdkFunction((Function<I, O>) function),
          RxJavaReactorMigrationUtil.toJdkFunction(function));
    }

    @AfterTemplate
    java.util.function.Function<I, O> after(java.util.function.Function<I, O> function) {
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
    java.util.function.Consumer<T> before(@CanTransformToTargetType Consumer<T> consumer) {
      return RxJavaReactorMigrationUtil.toJdkConsumer(consumer);
    }

    @AfterTemplate
    java.util.function.Consumer<T> after(java.util.function.Consumer<T> consumer) {
      return consumer;
    }
  }

  // XXX: This one is without CanBeTransformedTo...
  @SuppressWarnings("NoFunctionalReturnType")
  static final class UnnecessaryPredicateConversion<T> {
    @BeforeTemplate
    java.util.function.Predicate<T> before(Predicate<T> predicate) {
      return RxJavaReactorMigrationUtil.toJdkPredicate(predicate);
    }

    @AfterTemplate
    java.util.function.Predicate<T> after(java.util.function.Predicate<T> predicate) {
      return predicate;
    }
  }

  ///////////////////////////////////
  //////////// ASSORTED TEMPLATES
  ///////////////////////////////////
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

  // XXX: This conversion is in the testAddAndFindBadWord of the bad-word-service.
  // It is caused by the Flowable.then.then(Mono.from()).
  // This is officially not correct, but it is in the test, so we can allow it?
  static final class MonoToFlowableToFlux<T> {
    @BeforeTemplate
    Flux<T> before(Mono<T> mono) {
      return RxJava2Adapter.flowableToFlux(mono.as(RxJava2Adapter::monoToFlowable));
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono) {
      return mono;
    }
  }
}
