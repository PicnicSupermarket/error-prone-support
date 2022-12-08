package tech.picnic.errorprone.migration.util;

import io.reactivex.flowables.GroupedFlowable;
import io.reactivex.functions.Action;
import io.reactivex.internal.fuseable.ConditionalSubscriber;
import io.reactivex.internal.fuseable.QueueSubscription;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Operators;

/**
 * This util helps bridge the gap between RxJava and Reactor. The methods are used to safely rewrite
 * RxJava code to Reactor.
 */
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
  @SuppressWarnings("IllegalCatch")
  public static <T> java.util.function.Consumer<T> toJdkConsumer(
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
   * Convert an {@link Action} to a {@link Runnable}.
   *
   * @param action the {@link Action} to convert.
   * @return a {@link Runnable}
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

  /**
   * Utility used to migrate from {@link GroupedFlowable} to {@link GroupedFlux}.
   *
   * @param source the {@link GroupedFlux} to convert
   * @param <K> XXX
   * @param <V> XXX
   * @return the GroupedFlowable
   */
  public static <K, V> GroupedFlowable<K, V> groupedFluxToGroupedFlowable(
      GroupedFlux<K, V> source) {
    return new GroupedFluxAsGroupedFlowable<>(source);
  }

  private static final class GroupedFluxAsGroupedFlowable<K, V> extends GroupedFlowable<K, V> {
    private final GroupedFlux<K, V> source;

    GroupedFluxAsGroupedFlowable(GroupedFlux<K, V> source) {
      super(source.key());

      this.source = source;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void subscribeActual(Subscriber<? super V> s) {
      if (s instanceof ConditionalSubscriber) {
        source.subscribe(
            new FluxAsFlowableConditionalSubscriber<>((ConditionalSubscriber<? super V>) s));
      } else {
        source.subscribe(new FluxAsFlowableSubscriber<>(s));
      }
    }

    private static final class FluxAsFlowableSubscriber<T>
        implements CoreSubscriber<T>, QueueSubscription<T> {
      private final Subscriber<? super T> actual;

      private Subscription subscription;
      private Fuseable.QueueSubscription<T> qs;

      FluxAsFlowableSubscriber(Subscriber<? super T> actual) {
        this.actual = actual;
      }

      @Override
      @SuppressWarnings("unchecked")
      // XXX: Add `@Nonnull` here?
      public void onSubscribe(Subscription s) {
        if (Operators.validate(this.subscription, s)) {
          this.subscription = s;
          if (s instanceof Fuseable.QueueSubscription) {
            this.qs = (Fuseable.QueueSubscription<T>) s;
          }

          actual.onSubscribe(this);
        }
      }

      @Override
      public void onNext(T t) {
        actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
        actual.onError(t);
      }

      @Override
      public void onComplete() {
        actual.onComplete();
      }

      @Override
      public void request(long n) {
        subscription.request(n);
      }

      @Override
      public void cancel() {
        subscription.cancel();
      }

      @Override
      public T poll() {
        return qs.poll();
      }

      @Override
      public boolean isEmpty() {
        return qs.isEmpty();
      }

      @Override
      public void clear() {
        qs.clear();
      }

      @Override
      public int requestFusion(int requestedMode) {
        if (qs != null) {
          return qs.requestFusion(requestedMode);
        }
        return Fuseable.NONE;
      }

      @Override
      public boolean offer(T value) {
        throw new UnsupportedOperationException("Should not be called");
      }

      @Override
      public boolean offer(T v1, T v2) {
        throw new UnsupportedOperationException("Should not be called");
      }
    }

    private static final class FluxAsFlowableConditionalSubscriber<T>
        implements Fuseable.ConditionalSubscriber<T>, QueueSubscription<T> {
      private final ConditionalSubscriber<? super T> actual;

      private Subscription subscription;
      private QueueSubscription<T> qs;

      FluxAsFlowableConditionalSubscriber(ConditionalSubscriber<? super T> actual) {
        this.actual = actual;
      }

      @Override
      @SuppressWarnings("unchecked")
      public void onSubscribe(Subscription s) {
        if (Operators.validate(this.subscription, s)) {
          this.subscription = s;
          if (s instanceof QueueSubscription) {
            this.qs = (QueueSubscription<T>) s;
          }

          actual.onSubscribe(this);
        }
      }

      @Override
      public void onNext(T t) {
        actual.onNext(t);
      }

      @Override
      public boolean tryOnNext(T t) {
        return actual.tryOnNext(t);
      }

      @Override
      public void onError(Throwable t) {
        actual.onError(t);
      }

      @Override
      public void onComplete() {
        actual.onComplete();
      }

      @Override
      public void request(long n) {
        subscription.request(n);
      }

      @Override
      public void cancel() {
        subscription.cancel();
      }

      @Override
      @SuppressWarnings("IllegalCatch")
      public T poll() {
        try {
          return qs.poll();
        } catch (Exception ex) {
          throw Exceptions.bubble(ex);
        }
      }

      @Override
      public boolean isEmpty() {
        return qs.isEmpty();
      }

      @Override
      public void clear() {
        qs.clear();
      }

      @Override
      public int requestFusion(int requestedMode) {
        if (qs != null) {
          return qs.requestFusion(requestedMode);
        }
        return NONE;
      }

      @Override
      public boolean offer(T v1) {
        throw new UnsupportedOperationException("Should not be called!");
      }

      @Override
      public boolean offer(T v1, T v2) {
        throw new UnsupportedOperationException("Should not be called!");
      }
    }
  }
}
