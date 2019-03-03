package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Assorted Refaster templates that do not (yet) belong in one of the other classes with more
 * topical Refaster templates.
 */
final class AssortedTemplates {
  private AssortedTemplates() {}

  /**
   * Prefer {@link Map#entry(Object, Object)} over alternative ways to create an immutable map
   * entry.
   *
   * <p><strong>Warning:</strong> while both {@link Maps#immutableEntry(Object, Object)} and {@link
   * AbstractMap.SimpleImmutableEntry} allow {@code null} keys and values, the preferred @link
   * Map#entry(Object, Object)} variant does not. Moreover, the {@link Map.Entry} instances produced
   * by the former approaches is {@link java.io.Serializable}, while this does not hold for the
   * object returned by the preferred approach.
   */
  static final class MapEntry<K, V> {
    @BeforeTemplate
    Map.Entry<K, V> before(K key, V value) {
      return Refaster.anyOf(
          Maps.immutableEntry(key, value), new AbstractMap.SimpleImmutableEntry<>(key, value));
    }

    @AfterTemplate
    Map.Entry<K, V> after(K key, V value) {
      return Map.entry(key, value);
    }
  }

  /** Prefer {@link Objects#checkIndex(int, int)} over the Guava alternative. */
  static final class CheckIndex {
    @BeforeTemplate
    int before(int index, int size) {
      return Preconditions.checkElementIndex(index, size);
    }

    @AfterTemplate
    int after(int index, int size) {
      return Objects.checkIndex(index, size);
    }
  }

  /**
   * Don't map a a stream's elements to map entries, only to subsequently collect them into a map.
   * The collection can be performed directly.
   */
  // XXX: Also cover collection to multimaps.
  abstract static class StreamOfMapEntriesImmutableMap<E, K, V> {
    @Placeholder
    abstract K keyFunction(@MayOptionallyUse E element);

    @Placeholder
    abstract V valueFunction(@MayOptionallyUse E element);

    // XXX: We could add variants in which the entry is created some other way, but we have another
    // rule which covers canonicalization to `Map.entry`.
    @BeforeTemplate
    ImmutableMap<K, V> before(Stream<E> stream) {
      return stream
          .map(e -> Map.entry(keyFunction(e), valueFunction(e)))
          .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableMap<K, V> after(Stream<E> stream) {
      return stream.collect(toImmutableMap(e -> keyFunction(e), e -> valueFunction(e)));
    }
  }

  /**
   * Prefer {@link Maps#toMap(Iterable, com.google.common.base.Function)} over the stream-based
   * alternative.
   */
  // XXX: Drop the `Refaster.anyOf` if/when we decide to rewrite one to the other.
  // XXX: Also cover collection to multimaps.
  static final class IterableToMap<K, V> {
    @BeforeTemplate
    ImmutableMap<K, V> before(
        Iterator<K> iterable, Function<? super K, ? extends V> valueFunction) {
      return Streams.stream(iterable)
          .collect(toImmutableMap(Refaster.anyOf(identity(), k -> k), valueFunction));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(
        Iterable<K> iterable, Function<? super K, ? extends V> valueFunction) {
      return Streams.stream(iterable)
          .collect(toImmutableMap(Refaster.anyOf(identity(), k -> k), valueFunction));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(
        Collection<K> iterable, Function<? super K, ? extends V> valueFunction) {
      return iterable.stream()
          .collect(toImmutableMap(Refaster.anyOf(identity(), k -> k), valueFunction));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(
        Iterable<K> iterable, com.google.common.base.Function<? super K, V> valueFunction) {
      return Maps.toMap(iterable, valueFunction);
    }
  }

  /**
   * Prefer {@link Maps#uniqueIndex(Iterable, com.google.common.base.Function)} over the
   * stream-based alternative.
   */
  // XXX: Drop the `Refaster.anyOf` if/when we decide to rewrite one to the other.
  // XXX: Also cover collection to multimaps.
  static final class IterableUniqueIndex<K, V> {
    @BeforeTemplate
    ImmutableMap<K, V> before(Iterator<V> iterable, Function<? super V, ? extends K> keyFunction) {
      return Streams.stream(iterable)
          .collect(toImmutableMap(keyFunction, Refaster.anyOf(identity(), v -> v)));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(Iterable<V> iterable, Function<? super V, ? extends K> keyFunction) {
      return Streams.stream(iterable)
          .collect(toImmutableMap(keyFunction, Refaster.anyOf(identity(), v -> v)));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(
        Collection<V> iterable, Function<? super V, ? extends K> keyFunction) {
      return iterable.stream()
          .collect(toImmutableMap(keyFunction, Refaster.anyOf(identity(), v -> v)));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(
        Iterable<V> iterable, com.google.common.base.Function<? super V, K> keyFunction) {
      return Maps.uniqueIndex(iterable, keyFunction);
    }
  }

  /**
   * Prefer {@link Maps#toMap(Iterable, com.google.common.base.Function)} over the alternative if
   * the resultant map should be immutable anyway.
   */
  static final class SetToImmutableMap<K, V> {
    @BeforeTemplate
    ImmutableMap<K, V> before(Set<K> set, com.google.common.base.Function<? super K, V> fun) {
      return ImmutableMap.copyOf(Maps.asMap(set, fun));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(Set<K> set, com.google.common.base.Function<? super K, V> fun) {
      return Maps.toMap(set, fun);
    }
  }
}
