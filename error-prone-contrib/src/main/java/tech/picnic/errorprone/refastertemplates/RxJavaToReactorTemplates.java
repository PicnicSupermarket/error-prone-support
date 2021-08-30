package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import io.reactivex.BackpressureStrategy;
import io.reactivex.functions.Function;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Assorted Refaster templates for the migration of RxJava to Reactor */
public final class RxJavaToReactorTemplates {
  private RxJavaToReactorTemplates() {}

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

  // XXX: What should we do with the naming here? Since it is not entirely correct now.
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

  // XXX: Use `CanBeCoercedTo`
  @SuppressWarnings("NoFunctionalReturnType")
  static final class UnnecessaryConversion<I, O> {
    @BeforeTemplate
    java.util.function.Function<I, O> before(Function<I, O> function) {
      return RxJava2ReactorMigrationUtil.toJdkFunction(function);
    }

    @AfterTemplate
    java.util.function.Function<I, O> after(java.util.function.Function<I, O> function) {
      return function;
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

  //  /**
  //   * XXX: Temporary solution, this could be fixed when we know whether the function throws an
  //   * Exception.
  //   */
  //  public static final class MyUtil {
  //
  //    private MyUtil() {}
  //
  //    /** Temporary construct to convert functions that do not throw an exception */
  //    @SuppressWarnings({"IllegalCatch", "NoFunctionalReturnType"})
  //    public static <I, O> java.util.function.Function<I, O> convert(
  //        Function<? super I, ? extends O> function) {
  //      return input -> {
  //        try {
  //          return function.apply(input);
  //        } catch (Exception e) {
  //          throw Exceptions.propagate(e);
  //        }
  //      };
  //    }
  //  }

  // XXX: Move to a separate Maven module.
  /** Util for the conversion of types */
  public static final class RxJava2ReactorMigrationUtil {
    private RxJava2ReactorMigrationUtil() {}

    /**
     * Convert {@code Callable<T>} to T
     *
     * @param callable XXX
     * @param <T> XXX
     * @return XXX
     */
    // XXX: Rename.
    // XXX: Introduce Refaster rules to drop this wrapper when possible.
    @SuppressWarnings("IllegalCatch")
    public static <T> T getUnchecked(Callable<T> callable) {
      try {
        return callable.call();
      } catch (Exception e) {
        throw new IllegalArgumentException("Callable threw checked exception", e);
      }
    }

    /**
     * Convert {@link io.reactivex.functions.Function} to {@link java.util.function.Function}
     *
     * @param function XXX
     * @param <T> XXX
     * @param <R> XXX
     * @return XXX
     */
    // XXX: Rename.
    // XXX: Introduce Refaster rules to drop this wrapper when possible.
    @SuppressWarnings("IllegalCatch")
    public static <T, R> java.util.function.Function<T, R> toJdkFunction(
        io.reactivex.functions.Function<T, R> function) {
      return (t) -> {
        try {
          return function.apply(t);
        } catch (Exception e) {
          throw new IllegalArgumentException("BiFunction threw checked exception", e);
        }
      };
    }

    /**
     * Convert {@link io.reactivex.functions.BiFunction} to {@link java.util.function.BiFunction}
     *
     * @param biFunction XXX
     * @param <T> XXX
     * @param <U> XXX
     * @param <R> XXX
     * @return XXX
     */
    @SuppressWarnings("IllegalCatch")
    public static <T, U, R>
        java.util.function.BiFunction<? super T, ? super U, ? extends R> toJdkBiFunction(
            io.reactivex.functions.BiFunction<? super T, ? super U, ? extends R> biFunction) {
      return (t, u) -> {
        try {
          return biFunction.apply(t, u);
        } catch (Exception e) {
          throw new IllegalArgumentException("BiFunction threw checked exception", e);
        }
      };
    }

    /**
     * Convert {@link io.reactivex.functions.BiFunction} to {@link java.util.function.BiFunction}
     *
     * @param callable XXX
     * @param <T> XXX
     * @return XXX
     */
    @SuppressWarnings("IllegalCatch")
    public static <T> Supplier<T> callableAsSupplier(Callable<T> callable) {
      return () -> {
        try {
          return callable.call();
        } catch (Exception e) {
          throw new IllegalArgumentException("Callable threw checked exception", e);
        }
      };
    }

    // "Coersion" (find better name):
    // instanceof (support this?)
    // two functional interfaces with:
    // B.return type extends A.return type
    // A.param 1 type extends B.param 1 type
    // ....
    // B throws a subset of the exceptions thrown by A
  }
}
