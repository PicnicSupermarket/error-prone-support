package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link ImmutableSortedMap}s. */
@OnlineDocumentation
final class ImmutableSortedMapRules {
  private ImmutableSortedMapRules() {}

  /** Prefer {@link ImmutableSortedMap#orderedBy(Comparator)} over the associated constructor. */
  static final class ImmutableSortedMapOrderedBy<K, V> {
    @BeforeTemplate
    ImmutableSortedMap.Builder<K, V> before(Comparator<K> comparator) {
      return new ImmutableSortedMap.Builder<>(comparator);
    }

    @AfterTemplate
    ImmutableSortedMap.Builder<K, V> after(Comparator<K> comparator) {
      return ImmutableSortedMap.orderedBy(comparator);
    }
  }

  /** Prefer {@link ImmutableSortedMap#naturalOrder()} over more verbose alternatives. */
  // XXX: This rule may drop generic type information, leading to non-compilable code.
  static final class ImmutableSortedMapNaturalOrder<K extends Comparable<? super K>, V> {
    @BeforeTemplate
    ImmutableSortedMap.Builder<K, V> before() {
      return ImmutableSortedMap.orderedBy(Comparator.<K>naturalOrder());
    }

    @AfterTemplate
    ImmutableSortedMap.Builder<K, V> after() {
      return ImmutableSortedMap.naturalOrder();
    }
  }

  /** Prefer {@link ImmutableSortedMap#reverseOrder()} over more verbose alternatives. */
  // XXX: This rule may drop generic type information, leading to non-compilable code.
  static final class ImmutableSortedMapReverseOrder<K extends Comparable<? super K>, V> {
    @BeforeTemplate
    ImmutableSortedMap.Builder<K, V> before() {
      return ImmutableSortedMap.orderedBy(Comparator.<K>reverseOrder());
    }

    @AfterTemplate
    ImmutableSortedMap.Builder<K, V> after() {
      return ImmutableSortedMap.reverseOrder();
    }
  }

  /** Prefer {@link ImmutableSortedMap#of()} over less efficient alternatives. */
  static final class ImmutableSortedMapOf<K extends Comparable<? super K>, V> {
    @BeforeTemplate
    ImmutableSortedMap<K, V> before() {
      return ImmutableSortedMap.<K, V>naturalOrder().buildOrThrow();
    }

    @AfterTemplate
    ImmutableSortedMap<K, V> after() {
      return ImmutableSortedMap.of();
    }
  }

  /** Prefer {@link ImmutableSortedMap#of(Object, Object)} over less efficient alternatives. */
  // XXX: One can define variants for more than one key-value pair, but at some point the builder
  // actually produces nicer code. So it's not clear we should add Refaster rules for those
  // variants.
  // XXX: We could also rewrite builders with non-natural orders, but that would affect
  // `ImmutableSortedMap#comparator()`.
  static final class ImmutableSortedMapOfWithComparableAndObject<
      K extends Comparable<? super K>, V> {
    @BeforeTemplate
    ImmutableSortedMap<K, V> before(K k1, V v1) {
      return ImmutableSortedMap.<K, V>naturalOrder().put(k1, v1).buildOrThrow();
    }

    @AfterTemplate
    ImmutableSortedMap<K, V> after(K k1, V v1) {
      return ImmutableSortedMap.of(k1, v1);
    }
  }

  /**
   * Prefer {@link ImmutableSortedMap#of(Object, Object)} over less efficient or more contrived
   * alternatives.
   */
  // XXX: We could also rewrite builders with non-natural orders, but that would affect
  // `ImmutableSortedMap#comparator()`.
  static final class ImmutableSortedMapOfEntryGetKeyEntryGetValue<
      K extends Comparable<? super K>, V, K2 extends K, V2 extends V> {
    @BeforeTemplate
    ImmutableSortedMap<K, V> before(Map.Entry<K2, V2> entry) {
      return Refaster.anyOf(
          ImmutableSortedMap.<K, V>naturalOrder().put(entry).buildOrThrow(),
          Stream.of(entry).collect(toImmutableSortedMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @AfterTemplate
    ImmutableSortedMap<K, V> after(Map.Entry<K2, V2> entry) {
      return ImmutableSortedMap.of(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Prefer {@link ImmutableSortedMap#copyOf(Iterable)} over more verbose, less efficient, or more
   * contrived alternatives.
   */
  // XXX: There's also a variant with a custom Comparator. (And some special cases with
  // `reverseOrder`.) Worth the hassle?
  static final class ImmutableSortedMapCopyOf<
      K extends Comparable<? super K>, V, K2 extends K, V2 extends V, E extends Map.Entry<K2, V2>> {
    @BeforeTemplate
    ImmutableSortedMap<K, V> before(Map<K2, V2> entries) {
      return Refaster.anyOf(
          ImmutableSortedMap.copyOf(entries, naturalOrder()),
          ImmutableSortedMap.copyOf(entries.entrySet()),
          ImmutableSortedMap.<K, V>naturalOrder().putAll(entries).buildOrThrow());
    }

    @BeforeTemplate
    ImmutableSortedMap<K, V> before(Iterable<E> entries) {
      return Refaster.anyOf(
          ImmutableSortedMap.copyOf(entries, naturalOrder()),
          ImmutableSortedMap.<K, V>naturalOrder().putAll(entries).buildOrThrow(),
          Streams.stream(entries)
              .collect(toImmutableSortedMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @BeforeTemplate
    ImmutableSortedMap<K, V> before(Collection<E> entries) {
      return entries.stream().collect(toImmutableSortedMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    ImmutableSortedMap<K, V> after(Iterable<E> entries) {
      return ImmutableSortedMap.copyOf(entries);
    }
  }

  /**
   * Prefer {@link ImmutableSortedMap#toImmutableSortedMap(Function, Function)} over more verbose
   * alternatives.
   */
  static final class ToImmutableSortedMap<
      K1 extends Comparable<? super K1>, K2 extends K1, V1, V2 extends V1, T1, T2 extends T1> {
    @BeforeTemplate
    Collector<T2, ?, ImmutableSortedMap<K1, V1>> before(
        Function<T1, K2> keyFunction, Function<T1, V2> valueFunction) {
      return toImmutableSortedMap(naturalOrder(), keyFunction, valueFunction);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Collector<T2, ?, ImmutableSortedMap<K1, V1>> after(
        Function<T1, K2> keyFunction, Function<T1, V2> valueFunction) {
      return toImmutableSortedMap(keyFunction, valueFunction);
    }
  }

  /**
   * Prefer {@link ImmutableSortedMap#toImmutableSortedMap(Function, Function, BinaryOperator)} over
   * more verbose alternatives.
   */
  static final class ToImmutableSortedMapWithBinaryOperator<
      K1 extends Comparable<? super K1>, K2 extends K1, V1, V2 extends V1, T1, T2 extends T1> {
    @BeforeTemplate
    Collector<T2, ?, ImmutableSortedMap<K1, V1>> before(
        Function<T1, K2> keyFunction,
        Function<T1, V2> valueFunction,
        BinaryOperator<V1> mergeFunction) {
      return toImmutableSortedMap(naturalOrder(), keyFunction, valueFunction, mergeFunction);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    Collector<T2, ?, ImmutableSortedMap<K1, V1>> after(
        Function<T1, K2> keyFunction,
        Function<T1, V2> valueFunction,
        BinaryOperator<V1> mergeFunction) {
      return toImmutableSortedMap(keyFunction, valueFunction, mergeFunction);
    }
  }
}
