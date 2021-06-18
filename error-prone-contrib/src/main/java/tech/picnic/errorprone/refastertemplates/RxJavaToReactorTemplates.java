package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import java.util.Map;
import java.util.concurrent.Flow;

import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class RxJavaToReactorTemplates {
  private RxJavaToReactorTemplates() {}

  static final class RemoveRedundantCast<T> {
    @BeforeTemplate
    T before(T object) {
      return (T) object;
    }

    @AfterTemplate
    T after(T object) {
      return object;
    }
  }

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

  static final class FlowableConcatWithPublisher<T> {
    @BeforeTemplate
    Flowable<T> before(Flowable<T> flowable, Publisher<T> source) {
      return flowable.concatWith(source);
    }

    @AfterTemplate
    Flowable<T> after(Flowable<T> flowable, Publisher<T> source) {
      return flowable.as(RxJava2Adapter::flowableToFlux)
              .concatWith(source)
              .as(RxJava2Adapter::fluxToFlowable);
    }
  }

  // XXX: Flowable.concatWith. -> CompletableSource
  // XXX: Flowable.concatWith. -> SingleSource
  // XXX: Flowable.concatWith. -> MaybeSource
  
  // XXX: `function` type change; look into `Refaster.canBeCoercedTo(...)`.
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

  // XXX: `function` type change; look into `Refaster.canBeCoercedTo(...)`.
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

  // XXX: `function` type change; look into `Refaster.canBeCoercedTo(...)`.
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

  static final class FlowableSwitchIfEmptyPublisher<T> {
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

  // XXX: Change location and perhaps omit this one.
  abstract static class MaybeDeferToMono<T> {
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

  // XXX: Temporary solution, this could be fixed when we know whether the function throws an
  // Exception.
  public static class MyUtil {
    public static <I, O> java.util.function.Function<I, O> convert(
        Function<? super I, ? extends O> function) {
      return input -> {
        try {
          return function.apply(input);
        } catch (Exception e) {
          throw Exceptions.propagate(e);
        }
      };
    }
  }

  static final class MaybeFlatMapFunction<I, T extends I, O, M extends MaybeSource<? extends O>> {
    @BeforeTemplate
    Maybe<O> before(Maybe<T> maybe, Function<I, M> function) {
      return maybe.flatMap(function);
    }

    @AfterTemplate
    Maybe<O> after(Maybe<T> maybe, Function<I, M> function) {
      return maybe
          .as(RxJava2Adapter::maybeToMono)
          .flatMap(
              v ->
                  RxJava2Adapter.maybeToMono(
                      Maybe.wrap((Maybe<O>) MyUtil.convert(function).apply(v))))
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

  // "Coersion" (find better name):
  // instanceof (support this?)
  // two functional interfaces with:
  // B.return type extends A.return type
  // A.param 1 type extends B.param 1 type
  // ....
  // B throws a subset of the exceptions thrown by A

  //  @CheckParameterCoersion
  static final class UnnecessaryConversion<I, O> {
    @BeforeTemplate
    java.util.function.Function<I, O> before(Function<I, O> function) {
      return MyUtil.convert(function);
    }

    @AfterTemplate
    java.util.function.Function<I, O> after(java.util.function.Function<I, O> function) {
      return function;
    }
  }

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

  //    static final class MaybeFlatMapSingleElement<
  //        I, T extends I, O, P extends SingleSource<? extends O>> { // <S, T extends S, O> {
  //      @BeforeTemplate
  //      Maybe<O> before(Maybe<T> maybe, Function<I, P> function) {
  //        return maybe.flatMapSingleElement(function);
  //      }
  //
  //      @AfterTemplate
  //      Maybe<O> after(Maybe<T> maybe, java.util.function.Function<I, P> function) {
  //        return maybe
  //            .as(RxJava2Adapter::maybeToMono)
  //            .flatMap(function)
  //            .as(RxJava2Adapter::monoToMaybe);
  //      }
  //    }

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

  // XXX: `function` type change; look into `Refaster.canBeCoercedTo(...)`.
  static final class SingleFilter<S, T extends S> {
    @BeforeTemplate
    Maybe<T> before(Single<T> single, Predicate<S> predicate) {
      return single.filter(predicate);
    }

    @AfterTemplate
    Maybe<T> after(Single<T> single, java.util.function.Predicate<S> predicate) {
      return single
          .as(RxJava2Adapter::singleToMono)
          .filter(predicate)
          .as(RxJava2Adapter::monoToMaybe);
    }
  }

  // XXX: `function` type change; look into `Refaster.canBeCoercedTo(...)`.
  abstract static class SingleFlatMapLambda<S, T> {
    @Placeholder
    abstract Single<T> toSingleFunction(@MayOptionallyUse S element);

    @BeforeTemplate
    Single<T> before(Single<S> single) {
      return single.flatMap(v -> toSingleFunction(v));
    }

    @AfterTemplate
    Single<T> after(Single<S> single) {
      return single
          .as(RxJava2Adapter::singleToMono)
          .flatMap(v -> toSingleFunction(v).as(RxJava2Adapter::singleToMono))
          .as(RxJava2Adapter::monoToSingle);
    }
  }

  // XXX: `function` type change; look into `Refaster.canBeCoercedTo(...)`.
  static final class SingleMap<I, T extends I, O> {
    @BeforeTemplate
    Single<O> before(Single<T> single, Function<I, O> function) {
      return single.map(function);
    }

    @AfterTemplate
    Single<O> after(Single<T> single, java.util.function.Function<I, O> function) {
      return single.as(RxJava2Adapter::singleToMono).map(function).as(RxJava2Adapter::monoToSingle);
    }
  }

  static final class FluxToFlowableToFlux<T> {
    @BeforeTemplate
    Flux<T> before(Flux<T> flux, BackpressureStrategy strategy) {
      return Refaster.anyOf(
          flux.as(RxJava2Adapter::fluxToObservable)
              .toFlowable(strategy)
              .as(RxJava2Adapter::flowableToFlux),
          flux.as(RxJava2Adapter::fluxToFlowable).as(RxJava2Adapter::flowableToFlux));
    }

    @AfterTemplate
    Flux<T> after(Flux<T> flux) {
      return flux;
    }
  }

  // XXX: Stephan, what should we do with the naming here? Since it is not entirely correct now.
  static final class MonoToFlowableToMono<T> {
    @BeforeTemplate
    Mono<Void> before(Mono<Void> mono) {
      return mono.as(RxJava2Adapter::monoToCompletable).as(RxJava2Adapter::completableToMono);
    }

    @BeforeTemplate
    Mono<T> before2(Mono<T> mono) {
      return Refaster.anyOf(
          mono.as(RxJava2Adapter::monoToMaybe).as(RxJava2Adapter::maybeToMono),
          mono.as(RxJava2Adapter::monoToSingle).as(RxJava2Adapter::singleToMono));
    }

    @AfterTemplate
    Mono<T> after(Mono<T> mono) {
      return mono;
    }
  }

  //  @Matches(value = DoesNotThrowException.class, arguments = "java.lang.Exception")
  //  @interface DoesNotThrowCheckedException {}
  //
  //  abstract static class Test {
  //    @Placeholder
  //    @DoesNotThrowCheckedException
  //    abstract void operation();
  //
  //    @BeforeTemplate
  //    void before(ExecutorService es) {
  //      es.submit(
  //          () -> {
  //            operation();
  //            return null;
  //          });
  //    }
  //
  //    @AfterTemplate
  //    void after(ExecutorService es) {
  //      es.submit(() -> operation());
  //    }
  //  }
  //
  //  public class MethodThrowsExceptionTemplate<T> {
  //    @BeforeTemplate
  //    Mono<T> before(@DoesNotThrowCheckedException T obj) {
  //      return Mono.fromCallable(() -> obj);
  //    }
  //
  //    @AfterTemplate
  //    Mono<T> after(T obj) {
  //      return Mono.fromSupplier(() -> obj);
  //    }
  //  }
  //
  //  public class MethodThrowsExceptionTemplate<T> {
  //    @BeforeTemplate
  //    Mono<T> before(@Matches(DefersToExpressionWhichDoesNotThrow.class) Callable<T> callable) {
  //      return Mono.fromCallable(callable);
  //    }
  //
  //    @AfterTemplate
  //    Mono<T> after(Supplier<T> callable) {
  //      return Mono.fromSupplier(callable);
  //    }
  //  }
}
