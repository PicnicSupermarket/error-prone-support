package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

/** Refaster rules related to expressions dealing with {@link ImmutableSortedMap}s. */
final class ImmutableSortedMapTemplates {
  private ImmutableSortedMapTemplates() {}

  /** Prefer {@link ImmutableSortedMap#orderedBy(Comparator)} over the associated constructor. */
  static final class ImmutableSortedMapBuilder<K, V> {
    @BeforeTemplate
    ImmutableSortedMap.Builder<K, V> before(Comparator<K> cmp) {
      return new ImmutableSortedMap.Builder<>(cmp);
    }

    @AfterTemplate
    ImmutableSortedMap.Builder<K, V> after(Comparator<K> cmp) {
      return ImmutableSortedMap.orderedBy(cmp);
    }
  }

  /**
   * Prefer {@link ImmutableSortedMap#naturalOrder()} over the alternative that requires explicitly
   * providing the {@link Comparator}.
   */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. See
  // https://github.com/google/error-prone/pull/2706.
  static final class ImmutableSortedMapNaturalOrderBuilder<K extends Comparable<? super K>, V> {
    @BeforeTemplate
    ImmutableSortedMap.Builder<K, V> before() {
      return ImmutableSortedMap.orderedBy(Comparator.<K>naturalOrder());
    }

    @AfterTemplate
    ImmutableSortedMap.Builder<K, V> after() {
      return ImmutableSortedMap.naturalOrder();
    }
  }

  /**
   * Prefer {@link ImmutableSortedMap#reverseOrder()} over the alternative that requires explicitly
   * providing the {@link Comparator}.
   */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. See
  // https://github.com/google/error-prone/pull/2706.
  static final class ImmutableSortedMapReverseOrderBuilder<K extends Comparable<? super K>, V> {
    @BeforeTemplate
    ImmutableSortedMap.Builder<K, V> before() {
      return ImmutableSortedMap.orderedBy(Comparator.<K>reverseOrder());
    }

    @AfterTemplate
    ImmutableSortedMap.Builder<K, V> after() {
      return ImmutableSortedMap.reverseOrder();
    }
  }

  /** Prefer {@link ImmutableSortedMap#of()} over more contrived alternatives. */
  static final class EmptyImmutableSortedMap<K extends Comparable<? super K>, V> {
    @BeforeTemplate
    ImmutableSortedMap<K, V> before() {
      return ImmutableSortedMap.<K, V>naturalOrder().build();
    }

    @AfterTemplate
    ImmutableSortedMap<K, V> after() {
      return ImmutableSortedMap.of();
    }
  }

  /** Prefer {@link ImmutableSortedMap#of(Object, Object)} over more contrived alternatives. */
  // XXX: One can define variants for more than one key-value pair, but at some point the builder
  // actually produces nicer code. So it's not clear we should add Refaster rules for those
  // variants.
  // XXX: We could also rewrite builders with non-natural orders, but that would affect
  // `ImmutableSortedMap#comparator()`.
  static final class PairToImmutableSortedMap<K extends Comparable<? super K>, V> {
    @BeforeTemplate
    ImmutableSortedMap<K, V> before(K key, V value) {
      return ImmutableSortedMap.<K, V>naturalOrder().put(key, value).build();
    }

    @AfterTemplate
    ImmutableSortedMap<K, V> after(K key, V value) {
      return ImmutableSortedMap.of(key, value);
    }
  }

  /** Prefer {@link ImmutableSortedMap#of(Object, Object)} over more contrived alternatives. */
  // XXX: We could also rewrite builders with non-natural orders, but that would affect
  // `ImmutableSortedMap#comparator()`.
  static final class EntryToImmutableSortedMap<K extends Comparable<? super K>, V> {
    @BeforeTemplate
    ImmutableSortedMap<K, V> before(Map.Entry<? extends K, ? extends V> entry) {
      return Refaster.anyOf(
          ImmutableSortedMap.<K, V>naturalOrder().put(entry).build(),
          Stream.of(entry)
              .collect(
                  toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)));
    }

    @AfterTemplate
    ImmutableSortedMap<K, V> after(Map.Entry<? extends K, ? extends V> entry) {
      return ImmutableSortedMap.of(entry.getKey(), entry.getValue());
    }
  }

  /** Prefer {@link ImmutableSortedMap#copyOf(Iterable)} over more contrived alternatives. */
  // XXX: There's also a variant with a custom Comparator. (And some special cases with
  // `reverseOrder`.) Worth the hassle?
  static final class IterableToImmutableSortedMap<K extends Comparable<? super K>, V> {
    @BeforeTemplate
    ImmutableMap<K, V> before(Map<? extends K, ? extends V> iterable) {
      return Refaster.anyOf(
          ImmutableSortedMap.copyOf(iterable, naturalOrder()),
          ImmutableSortedMap.copyOf(iterable.entrySet()),
          ImmutableSortedMap.<K, V>naturalOrder().putAll(iterable).build());
    }

    @BeforeTemplate
    ImmutableSortedMap<K, V> before(
        Iterable<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return Refaster.anyOf(
          ImmutableSortedMap.copyOf(iterable, naturalOrder()),
          ImmutableSortedMap.<K, V>naturalOrder().putAll(iterable).build(),
          Streams.stream(iterable)
              .collect(
                  toImmutableSortedMap(
                      Comparator.<K>naturalOrder(), Map.Entry::getKey, Map.Entry::getValue)));
    }

    @BeforeTemplate
    ImmutableSortedMap<K, V> before(
        Collection<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return iterable.stream()
          .collect(toImmutableSortedMap(naturalOrder(), Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    ImmutableSortedMap<K, V> after(
        Iterable<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return ImmutableSortedMap.copyOf(iterable);
    }
  }
}
