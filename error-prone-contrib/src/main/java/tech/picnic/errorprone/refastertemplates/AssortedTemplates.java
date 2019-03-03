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
import java.util.Map;
import java.util.Objects;
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
  // XXX: There's also an Iterator variant. Worth the hassle?
  // XXX: `Refaster.anyOf(identity(), k -> k)` instead of `identity()` doesn't cause the second
  // variant to be supported. Why?
  // XXX: Also cover collection to multimaps.
  abstract static class IterableToMap<K, V> {
    @Placeholder
    abstract V valueFunction(@MayOptionallyUse K element);

    @BeforeTemplate
    ImmutableMap<K, V> before(Iterable<K> iterable) {
      return Streams.stream(iterable).collect(toImmutableMap(identity(), k -> valueFunction(k)));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(Collection<K> iterable) {
      return iterable.stream().collect(toImmutableMap(identity(), k -> valueFunction(k)));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(Collection<K> iterable) {
      return Maps.toMap(iterable, k -> valueFunction(k));
    }
  }

  /**
   * Prefer {@link Maps#uniqueIndex(Iterable, com.google.common.base.Function)} over the
   * stream-based alternative.
   */
  // XXX: There's also an Iterator variant. Worth the hassle?
  // XXX: `Refaster.anyOf(identity(), k -> k)` instead of `identity()` doesn't cause the second
  // variant to be supported. Why?
  // XXX: Also cover collection to multimaps.
  abstract static class IterableUniqueIndex<K, V> {
    @Placeholder
    abstract K keyFunction(@MayOptionallyUse V element);

    @BeforeTemplate
    ImmutableMap<K, V> before(Iterable<V> iterable) {
      return Streams.stream(iterable).collect(toImmutableMap(v -> keyFunction(v), identity()));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(Collection<V> iterable) {
      return iterable.stream().collect(toImmutableMap(v -> keyFunction(v), identity()));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(Collection<V> iterable) {
      return Maps.uniqueIndex(iterable, v -> keyFunction(v));
    }
  }
}
