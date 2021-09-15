package tech.picnic.errorprone.migration.util;

import io.reactivex.functions.Action;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public final class RxJavaReactorMigrationUtil {
  private RxJavaReactorMigrationUtil() {}

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
  public static <T, U, R> java.util.function.BiFunction<T, U, R> toJdkBiFunction(
      io.reactivex.functions.BiFunction<T, U, R> biFunction) {
    return (t, u) -> {
      try {
        return biFunction.apply(t, u);
      } catch (Exception e) {
        throw new IllegalArgumentException("BiFunction threw checked exception", e);
      }
    };
  }

  /**
   * Convert {@link java.util.concurrent.Callable} to {@link java.util.function.Supplier}
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

  /**
   * Convert {@link io.reactivex.functions.Predicate} to {@link java.util.function.Predicate}
   *
   * @param predicate XXX
   * @param <T> XXX
   * @return XXX
   */
  // XXX: Rename.
  // XXX: Introduce Refaster rules to drop this wrapper when possible.
  @SuppressWarnings("IllegalCatch")
  public static <T> java.util.function.Predicate<T> toJdkPredicate(
      io.reactivex.functions.Predicate<T> predicate) {
    return (t) -> {
      try {
        return predicate.test(t);
      } catch (Exception e) {
        throw new IllegalArgumentException("Predicate threw checked exception", e);
      }
    };
  }

  /**
   * Convert {@link io.reactivex.functions.Consumer} to {@link java.util.function.Consumer}
   *
   * @param consumer XXX
   * @param <T> XXX
   * @return XXX
   */
  // XXX: Rename.
  // XXX: Introduce Refaster rules to drop this wrapper when possible.
  @SuppressWarnings("IllegalCatch")
  public static <T> java.util.function.Consumer<? super T> toJdkConsumer(
      io.reactivex.functions.Consumer<T> consumer) {
    return (t) -> {
      try {
        consumer.accept(t);
      } catch (Exception e) {
        throw new IllegalArgumentException("Consumer threw checked exception", e);
      }
    };
  }

  /**
   * XXX
   *
   * @param action XXX
   * @return XXX
   */
  @SuppressWarnings("IllegalCatch")
  public static Runnable toRunnable(Action action) {
    return () -> {
      try {
        action.run();
      } catch (Exception e) {
        throw new IllegalArgumentException("Action threw checked exception", e);
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
