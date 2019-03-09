package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link ImmutableMap}s. */
final class ImmutableMapTemplates {
  private ImmutableMapTemplates() {}

  /** Prefer {@link ImmutableMap#builder()} over the associated constructor. */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. Anything
  // we can do about that?
  static final class ImmutableMapBuilder<K, V> {
    @BeforeTemplate
    ImmutableMap.Builder<K, V> before() {
      return new ImmutableMap.Builder<>();
    }

    @AfterTemplate
    ImmutableMap.Builder<K, V> after() {
      return ImmutableMap.builder();
    }
  }

  /** Prefer {@link ImmutableMap#of()} over more contrived alternatives. */
  static final class EmptyImmutableMap<K, V> {
    @BeforeTemplate
    ImmutableMap<K, V> before() {
      return ImmutableMap.<K, V>builder().build();
    }

    @AfterTemplate
    ImmutableMap<K, V> after() {
      return ImmutableMap.of();
    }
  }

  /** Prefer {@link ImmutableMap#of(Object, Object)} over more contrived alternatives. */
  // XXX: One can define variants for more than one key-value pair, but at some point the builder
  // actually produces nicer code. So it's not clear we should add Refaster templates for those
  // variants.
  static final class PairToImmutableMap<K, V> {
    @BeforeTemplate
    ImmutableMap<K, V> before(K key, V value) {
      return ImmutableMap.<K, V>builder().put(key, value).build();
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K key, V value) {
      return ImmutableMap.of(key, value);
    }
  }

  /** Prefer {@link ImmutableMap#of(Object, Object)} over more contrived alternatives. */
  static final class EntryToImmutableMap<K, V> {
    @BeforeTemplate
    ImmutableMap<K, V> before(Map.Entry<? extends K, ? extends V> entry) {
      return Refaster.anyOf(
          ImmutableMap.<K, V>builder().put(entry).build(),
          Stream.of(entry).collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(Map.Entry<? extends K, ? extends V> entry) {
      return ImmutableMap.of(entry.getKey(), entry.getValue());
    }
  }

  /** Prefer {@link ImmutableMap#copyOf(Iterable)} over more contrived alternatives. */
  static final class IterableToImmutableMap<K, V> {
    @BeforeTemplate
    ImmutableMap<K, V> before(Map<? extends K, ? extends V> iterable) {
      return Refaster.anyOf(
          ImmutableMap.copyOf(iterable.entrySet()),
          ImmutableMap.<K, V>builder().putAll(iterable).build());
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(Iterable<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return Refaster.anyOf(
          ImmutableMap.<K, V>builder().putAll(iterable).build(),
          Streams.stream(iterable).collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(Collection<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return iterable.stream().collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(Iterable<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return ImmutableMap.copyOf(iterable);
    }
  }

  /** Don't unnecessarily copy an {@link ImmutableMap}. */
  static final class ImmutableMapCopyOfImmutableMap<K, V> {
    @BeforeTemplate
    ImmutableMap<K, V> before(ImmutableMap<K, V> map) {
      return ImmutableMap.copyOf(map);
    }

    @AfterTemplate
    ImmutableMap<K, V> after(ImmutableMap<K, V> map) {
      return map;
    }
  }
}
