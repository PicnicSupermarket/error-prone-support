package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.CanTransformToTargetType;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
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
          RxJava2Adapter.completableToMono(mono.as(RxJava2Adapter::monoToCompletable)),
          RxJava2Adapter.monoToCompletable(mono).as(RxJava2Adapter::completableToMono));
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono) {
      return mono;
    }
  }

  // XXX: Make test
  static final class MonoToFlowableToFlux<T> {
    @BeforeTemplate
    Flux<T> before(Mono<T> mono) {
      return mono.as(RxJava2Adapter::monoToFlowable).as(RxJava2Adapter::flowableToFlux);
    }

    @AfterTemplate
    Flux<T> after(Mono<T> mono) {
      return mono.flux();
    }
  }

  // XXX: Temporarily disabled @CanBeTransformedTo...
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

//  static final class MonoErrorCallableSupplierUtil<T> {
//    @BeforeTemplate
//    Mono<T> before(@CanTransformToTargetType Callable<? extends Throwable> callable) {
//      return Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(callable));
//    }
//
//    @AfterTemplate
//    Mono<T> after(Supplier<? extends Throwable> callable) {
//      return Mono.error(callable);
//    }
//  }
//
//  @SuppressWarnings({"NoFunctionalReturnType", "FunctionalInterfaceClash"})
//  static final class RemoveUtilCallable<T> {
//    @BeforeTemplate
//    Supplier<T> before(@CanTransformToTargetType Callable<T> callable) {
//      return RxJavaReactorMigrationUtil.callableAsSupplier(callable);
//    }
//
//    @AfterTemplate
//    Supplier<T> before(Supplier<T> callable) {
//      return callable;
//    }
//  }
//
//  // XXX: @NotMatches(IsMethodReferenceOrLambdaHasReturnStatement.class)  add this temporarily
//  //   and  remove at end of migration.
//  @SuppressWarnings("NoFunctionalReturnType")
//  static final class UnnecessaryFunctionConversion<I, O> {
//    @BeforeTemplate
//    java.util.function.Function<I, O> before(@CanTransformToTargetType Function<I, O> function) {
//      return Refaster.anyOf(
//          //          RxJavaReactorMigrationUtil.toJdkFunction((Function<I, O>) function), --> This
//          // one gets us in non-compilable state.
//          RxJavaReactorMigrationUtil.toJdkFunction(function));
//    }
//
//    // XXX: Redundant cast to cover the case in which `function` is a method reference on which
//    // `.apply` is invoked.
//    // XXX: This happens e.g. in lambda expressions, but we can't seem to match those with
//    //   Refaster preventing simplification. Investigate.
//    @AfterTemplate
//    java.util.function.Function<I, O> after(java.util.function.Function<I, O> function) {
//      return function;
//    }
//  }
//
//  @SuppressWarnings("NoFunctionalReturnType")
//  static final class UnnecessaryBiFunctionConversion<T, U, R> {
//    @BeforeTemplate
//    java.util.function.BiFunction<? super T, ? super U, ? extends R> before(
//        @CanTransformToTargetType BiFunction<? super T, ? super U, ? extends R> zipper) {
//      return RxJavaReactorMigrationUtil.toJdkBiFunction(zipper);
//    }
//
//    @AfterTemplate
//    java.util.function.BiFunction<? super T, ? super U, ? extends R> after(
//        java.util.function.BiFunction<? super T, ? super U, ? extends R> zipper) {
//      return zipper;
//    }
//  }
//
//  @SuppressWarnings("NoFunctionalReturnType")
//  static final class UnnecessaryConsumerConversion<T> {
//    @BeforeTemplate
//    java.util.function.Consumer<? extends T> before(
//        @CanTransformToTargetType Consumer<? extends T> consumer) {
//      return RxJavaReactorMigrationUtil.toJdkConsumer(consumer);
//    }
//
//    @AfterTemplate
//    java.util.function.Consumer<? extends T> after(
//        java.util.function.Consumer<? extends T> consumer) {
//      return consumer;
//    }
//  }
//
//  static final class UnnecessaryRunnableConversion {
//    @BeforeTemplate
//    Runnable before(@CanTransformToTargetType Action action) {
//      return RxJavaReactorMigrationUtil.toRunnable(action);
//    }
//
//    @AfterTemplate
//    Runnable after(Runnable action) {
//      return action;
//    }
//  }
//
//  @SuppressWarnings("NoFunctionalReturnType")
//  static final class UnnecessaryPredicateConversion<T> {
//    @BeforeTemplate
//    java.util.function.Predicate<? extends T> before(
//        @CanTransformToTargetType Predicate<? extends T> predicate) {
//      return RxJavaReactorMigrationUtil.toJdkPredicate(predicate);
//    }
//
//    @AfterTemplate
//    java.util.function.Predicate<? extends T> after(
//        java.util.function.Predicate<? extends T> predicate) {
//      return predicate;
//    }
//  }
//
//  static final class FlowableBiFunctionRemoveUtil<T, U, R> {
//    @BeforeTemplate
//    Flowable<R> before(
//        Publisher<? extends T> source1,
//        Publisher<? extends U> source2,
//        @CanTransformToTargetType BiFunction<? super T, ? super U, ? extends R> zipper) {
//      return RxJava2Adapter.fluxToFlowable(
//          Flux.<T, U, R>zip(source1, source2, RxJavaReactorMigrationUtil.toJdkBiFunction(zipper)));
//    }
//
//    @AfterTemplate
//    Flowable<R> after(
//        Publisher<? extends T> source1,
//        Publisher<? extends U> source2,
//        java.util.function.BiFunction<? super T, ? super U, ? extends R> zipper) {
//      return RxJava2Adapter.fluxToFlowable(Flux.<T, U, R>zip(source1, source2, zipper));
//    }
//  }

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

  static final class MonoThenThen<T> {
    @BeforeTemplate
    Mono<T> before(Mono<T> mono, Mono<T> other) {
      return mono.then().then(other);
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono, Mono<T> other) {
      return mono.then(other);
    }
  }
}
