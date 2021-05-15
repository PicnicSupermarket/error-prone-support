package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;

final class RxJavaToReactorTemplates {
  private RxJavaToReactorTemplates() {}

  static final class FlowableFlatMapInReactor<I, O> {
    @BeforeTemplate
    Flowable<O> before(
        Flowable<I> flowable, Function<? super I, ? extends Publisher<? extends O>> function) {
      return flowable.flatMap(function);
    }

    @AfterTemplate
    Flowable<O> after(
        Flowable<I> flowable,
        java.util.function.Function<? super I, ? extends Publisher<? extends O>> function) {
      return flowable
          .as(RxJava2Adapter::flowableToFlux)
          .flatMap(function)
          .as(RxJava2Adapter::fluxToFlowable);
    }
  }

  static final class FlowableFilterInReactor<T> {
    @BeforeTemplate
    Flowable<T> before(Flowable<T> flowable, Predicate<? super T> predicate) {
      return flowable.filter(predicate);
    }

    @AfterTemplate
    Flowable<T> after(Flowable<T> flowable, java.util.function.Predicate<? super T> predicate) {
      return flowable
          .as(RxJava2Adapter::flowableToFlux)
          .filter(predicate)
          .as(RxJava2Adapter::fluxToFlowable);
    }
  }

  // XXX: I don't think calling `next()` here is the right way...
  static final class FlowableFirstElementInReactor<T> {
    @BeforeTemplate
    Maybe<T> before(Flowable<T> flowable) {
      return flowable.firstElement();
    }

    @AfterTemplate
    Maybe<T> after(Flowable<T> flowable) {
      return flowable
              .as(RxJava2Adapter::flowableToFlux)
              .next()
              .as(RxJava2Adapter::monoToMaybe);
    }
  }


  //  default Single<Warehouse> getWarehouse(WarehouseId warehouseId) {
  //    return getAllWarehouses()
  //            .filter(warehouse -> warehouse.getId().equals(warehouseId))
  //            .firstElement()
  //            .switchIfEmpty(
  //                    Single.error(
  //                            itemNotFound(Warehouse.class.getName(), warehouseId.toString())
  //                                    ::get));
  //  }

  static final class RemoveUnnecessaryConversion<I> {
    @BeforeTemplate
    Flux<I> before(Flux<I> flux) {
      return flux.as(RxJava2Adapter::fluxToFlowable).as(RxJava2Adapter::flowableToFlux);
    }

    @AfterTemplate
    Flux<I> after(Flux<I> flux) {
      return flux;
    }
  }
}
  //  static final class FlowableToMapInReactor<I, O> {
  //    @BeforeTemplate
  //    Single<Map<O, I>> before(Flowable<I> flowable, Function<? super I, ? extends O> function) {
  //      return flowable.toMap(function);
  //    }
  //
  //    @AfterTemplate
  //    Single<Map<O, I>> after(Flowable<I> flowable, java.util.function.Function<? super I, ?
  // extends O> function) {
  //      return flowable.as(RxJava2Adapter::flowableToFlux)
  //              .collectMap(function)
  //              .as(RxJava2Adapter::monoToSingle);
  //    }
  //  }

  // Check this with Stephan.
  //  static final class FlowableMapToFluxMapToFlowable<T, R> {
  //    @BeforeTemplate
  //    Flowable<R> before(Flowable<T> flowable, Function<? super T, ? extends R> function) {
  //      return flowable.map(function);
  //    }
  //
  //    @AfterTemplate
  //    Flowable<R> after(
  //        Flowable<T> flowable, java.util.function.Function<? super T, ? extends R> function) {
  //      return flowable
  //          .as(RxJava2Adapter::flowableToFlux)
  //          .map(function)
  //          .as(RxJava2Adapter::fluxToFlowable);
  //      // Moeten we hier ook iets doen met Refaster.canBeCoercedTo()
  //      // omdat we moeten weten dat het geen Flux<Object> maar Flux<T> is...
  //    }
  //  }

  // Stephan: Bad return type in method reference: cannot convert io.reactivex.Flowable<T> to
  // io.reactivex.Flowable<java.lang.Object}
