package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;

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
}
