package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link ImmutableSetMultimap}s. */
final class ImmutableSetMultimapTemplates {
  private ImmutableSetMultimapTemplates() {}

  /** Prefer {@link ImmutableSetMultimap#builder()} over the associated constructor. */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. Anything
  // we can do about that?
  static final class ImmutableSetMultimapBuilder<K, V> {
    @BeforeTemplate
    ImmutableSetMultimap.Builder<K, V> before() {
      return new ImmutableSetMultimap.Builder<>();
    }

    @AfterTemplate
    ImmutableSetMultimap.Builder<K, V> after() {
      return ImmutableSetMultimap.builder();
    }
  }

  /** Prefer {@link ImmutableSetMultimap#of()} over more contrived alternatives. */
  static final class EmptyImmutableSetMultimap<K, V> {
    @BeforeTemplate
    ImmutableSetMultimap<K, V> before() {
      return ImmutableSetMultimap.<K, V>builder().build();
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V> after() {
      return ImmutableSetMultimap.of();
    }
  }

  /** Prefer {@link ImmutableSetMultimap#of(Object, Object)} over more contrived alternatives. */
  // XXX: One can define variants for more than one key-value pair, but at some point the builder
  // actually produces nicer code. So it's not clear we should add Refaster templates for those
  // variants.
  static final class PairToImmutableSetMultimap<K, V> {
    @BeforeTemplate
    ImmutableSetMultimap<K, V> before(K key, V value) {
      return ImmutableSetMultimap.<K, V>builder().put(key, value).build();
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V> after(K key, V value) {
      return ImmutableSetMultimap.of(key, value);
    }
  }

  /** Prefer {@link ImmutableSetMultimap#of(Object, Object)} over more contrived alternatives. */
  static final class EntryToImmutableSetMultimap<K, V> {
    @BeforeTemplate
    ImmutableSetMultimap<K, V> before(Map.Entry<? extends K, ? extends V> entry) {
      return Refaster.anyOf(
          ImmutableSetMultimap.<K, V>builder().put(entry).build(),
          Stream.of(entry).collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V> after(Map.Entry<? extends K, ? extends V> entry) {
      return ImmutableSetMultimap.of(entry.getKey(), entry.getValue());
    }
  }

  /** Prefer {@link ImmutableSetMultimap#copyOf(Iterable)} over more contrived alternatives. */
  static final class IterableToImmutableSetMultimap<K, V> {
    @BeforeTemplate
    ImmutableSetMultimap<K, V> before(Multimap<? extends K, ? extends V> iterable) {
      return Refaster.anyOf(
          ImmutableSetMultimap.copyOf(iterable.entries()),
          ImmutableSetMultimap.<K, V>builder().putAll(iterable).build());
    }

    @BeforeTemplate
    ImmutableSetMultimap<K, V> before(
        Iterable<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return Refaster.anyOf(
          ImmutableSetMultimap.<K, V>builder().putAll(iterable).build(),
          Streams.stream(iterable)
              .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @BeforeTemplate
    ImmutableSetMultimap<K, V> before(
        Collection<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return iterable.stream()
          .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V> after(
        Iterable<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return ImmutableSetMultimap.copyOf(iterable);
    }
  }

  /** Don't unnecessarily copy an {@link ImmutableSetMultimap}. */
  static final class ImmutableSetMultimapCopyOfImmutableSetMultimap<K, V> {
    @BeforeTemplate
    ImmutableSetMultimap<K, V> before(ImmutableSetMultimap<K, V> multimap) {
      return ImmutableSetMultimap.copyOf(multimap);
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V> after(ImmutableSetMultimap<K, V> multimap) {
      return multimap;
    }
  }
}
