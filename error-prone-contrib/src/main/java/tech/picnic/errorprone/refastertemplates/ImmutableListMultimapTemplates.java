package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link ImmutableListMultimap}s. */
final class ImmutableListMultimapTemplates {
  private ImmutableListMultimapTemplates() {}

  /**
   * Prefer {@link ImmutableListMultimap#builder()} over the associated constructor on constructions
   * that produce a less-specific type.
   */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. Anything
  // we can do about that?
  static final class ImmutableListMultimapBuilder<K, V> {
    @BeforeTemplate
    ImmutableMultimap.Builder<K, V> before() {
      return Refaster.anyOf(
          new ImmutableListMultimap.Builder<>(),
          new ImmutableMultimap.Builder<>(),
          ImmutableMultimap.builder());
    }

    @AfterTemplate
    ImmutableListMultimap.Builder<K, V> after() {
      return ImmutableListMultimap.builder();
    }
  }

  /**
   * Prefer {@link ImmutableListMultimap#of()} over more contrived or less-specific alternatives.
   */
  static final class EmptyImmutableListMultimap<K, V> {
    @BeforeTemplate
    ImmutableMultimap<K, V> before() {
      return Refaster.anyOf(
          ImmutableListMultimap.<K, V>builder().build(),
          ImmutableMultimap.<K, V>builder().build(),
          ImmutableMultimap.of());
    }

    @AfterTemplate
    ImmutableListMultimap<K, V> after() {
      return ImmutableListMultimap.of();
    }
  }

  /**
   * Prefer {@link ImmutableListMultimap#of(Object, Object)} over more contrived or less-specific
   * alternatives.
   */
  // XXX: One can define variants for more than one key-value pair, but at some point the builder
  // actually produces nicer code. So it's not clear we should add Refaster templates for those
  // variants.
  static final class PairToImmutableListMultimap<K, V> {
    @BeforeTemplate
    ImmutableMultimap<K, V> before(K key, V value) {
      return Refaster.anyOf(
          ImmutableListMultimap.<K, V>builder().put(key, value).build(),
          ImmutableMultimap.<K, V>builder().put(key, value).build(),
          ImmutableMultimap.of(key, value));
    }

    @AfterTemplate
    ImmutableListMultimap<K, V> after(K key, V value) {
      return ImmutableListMultimap.of(key, value);
    }
  }

  /**
   * Prefer {@link ImmutableListMultimap#of(Object, Object)} over more contrived or less-specific
   * alternatives.
   */
  static final class EntryToImmutableListMultimap<K, V> {
    @BeforeTemplate
    ImmutableMultimap<K, V> before(Map.Entry<? extends K, ? extends V> entry) {
      return Refaster.anyOf(
          ImmutableListMultimap.<K, V>builder().put(entry).build(),
          Stream.of(entry).collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)),
          ImmutableMultimap.<K, V>builder().put(entry).build(),
          ImmutableMultimap.of(entry.getKey(), entry.getValue()));
    }

    @AfterTemplate
    ImmutableListMultimap<K, V> after(Map.Entry<? extends K, ? extends V> entry) {
      return ImmutableListMultimap.of(entry.getKey(), entry.getValue());
    }
  }

  /** Prefer {@link ImmutableListMultimap#copyOf(Iterable)} over more contrived alternatives. */
  static final class IterableToImmutableListMultimap<K, V> {
    @BeforeTemplate
    ImmutableMultimap<K, V> before(Multimap<? extends K, ? extends V> iterable) {
      return Refaster.anyOf(
          ImmutableListMultimap.copyOf(iterable.entries()),
          ImmutableListMultimap.<K, V>builder().putAll(iterable).build(),
          ImmutableMultimap.copyOf(iterable),
          ImmutableMultimap.copyOf(iterable.entries()),
          ImmutableMultimap.<K, V>builder().putAll(iterable).build());
    }

    @BeforeTemplate
    ImmutableMultimap<K, V> before(
        Iterable<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return Refaster.anyOf(
          ImmutableListMultimap.<K, V>builder().putAll(iterable).build(),
          Streams.stream(iterable)
              .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)),
          ImmutableMultimap.<K, V>builder().putAll(iterable).build(),
          ImmutableMultimap.copyOf(iterable));
    }

    @BeforeTemplate
    ImmutableListMultimap<K, V> before(
        Collection<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return iterable.stream()
          .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    ImmutableListMultimap<K, V> after(
        Iterable<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return ImmutableListMultimap.copyOf(iterable);
    }
  }

  /** Don't unnecessarily copy an {@link ImmutableListMultimap}. */
  static final class ImmutableListMultimapCopyOfImmutableListMultimap<K, V> {
    @BeforeTemplate
    ImmutableListMultimap<K, V> before(ImmutableListMultimap<K, V> multimap) {
      return ImmutableListMultimap.copyOf(multimap);
    }

    @AfterTemplate
    ImmutableListMultimap<K, V> after(ImmutableListMultimap<K, V> multimap) {
      return multimap;
    }
  }
}
