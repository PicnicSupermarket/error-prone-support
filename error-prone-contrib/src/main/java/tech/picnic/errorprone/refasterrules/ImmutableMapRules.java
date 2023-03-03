package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.function.Function.identity;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link ImmutableMap}s. */
@OnlineDocumentation
final class ImmutableMapRules {
  private ImmutableMapRules() {}

  /** Prefer {@link ImmutableMap#builder()} over the associated constructor. */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. See
  // https://github.com/google/error-prone/pull/2706.
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

  /**
   * Prefer {@link Maps#toMap(Iterable, com.google.common.base.Function)} over more contrived
   * alternatives.
   */
  // XXX: Drop the `Refaster.anyOf` if/when we decide to rewrite one to the other.
  static final class IterableToImmutableMap<K, V> {
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

    @BeforeTemplate
    ImmutableMap<K, V> before(
        Set<K> iterable, com.google.common.base.Function<? super K, V> valueFunction) {
      return ImmutableMap.copyOf(Maps.asMap(iterable, valueFunction));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(
        Iterable<K> iterable, com.google.common.base.Function<? super K, V> valueFunction) {
      return Maps.toMap(iterable, valueFunction);
    }
  }

  /** Prefer {@link ImmutableMap#copyOf(Iterable)} over more contrived alternatives. */
  static final class EntryIterableToImmutableMap<K, V> {
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

  /**
   * Don't map a a stream's elements to map entries, only to subsequently collect them into an
   * {@link ImmutableMap}. The collection can be performed directly.
   */
  abstract static class StreamOfMapEntriesToImmutableMap<E, K, V> {
    @Placeholder(allowsIdentity = true)
    abstract K keyFunction(@MayOptionallyUse E element);

    @Placeholder(allowsIdentity = true)
    abstract V valueFunction(@MayOptionallyUse E element);

    // XXX: We could add variants in which the entry is created some other way, but we have another
    // rule that covers canonicalization to `Map.entry`.
    @BeforeTemplate
    ImmutableMap<K, V> before(Stream<E> stream) {
      return stream
          .map(e -> Map.entry(keyFunction(e), valueFunction(e)))
          .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ImmutableMap<K, V> after(Stream<E> stream) {
      return stream.collect(toImmutableMap(e -> keyFunction(e), e -> valueFunction(e)));
    }
  }

  /**
   * Prefer {@link Maps#uniqueIndex(Iterable, com.google.common.base.Function)} over the
   * stream-based alternative.
   */
  // XXX: Drop the `Refaster.anyOf` if/when we decide to rewrite one to the other.
  static final class IndexIterableToImmutableMap<K, V> {
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
   * Prefer creating an immutable copy of the result of {@link Maps#transformValues(Map,
   * com.google.common.base.Function)} over more contrived alternatives.
   */
  abstract static class TransformMapValuesToImmutableMap<K, V1, V2> {
    @Placeholder(allowsIdentity = true)
    abstract V2 valueTransformation(@MayOptionallyUse V1 value);

    // XXX: Instead of `Map.Entry::getKey` we could also match `e -> e.getKey()`. But for some
    // reason Refaster doesn't handle that case. This doesn't matter if we roll out use of
    // `MethodReferenceUsage`. Same observation applies to a lot of other Refaster checks.
    @BeforeTemplate
    @SuppressWarnings("NullAway")
    ImmutableMap<K, V2> before(Map<K, V1> map) {
      return Refaster.anyOf(
          map.entrySet().stream()
              .collect(toImmutableMap(Map.Entry::getKey, e -> valueTransformation(e.getValue()))),
          Maps.toMap(map.keySet(), key -> valueTransformation(map.get(key))));
    }

    @AfterTemplate
    ImmutableMap<K, V2> after(Map<K, V1> map) {
      return ImmutableMap.copyOf(Maps.transformValues(map, v -> valueTransformation(v)));
    }
  }

  /**
   * Prefer {@link ImmutableMap#of()} over more contrived alternatives or alternatives that don't
   * communicate the immutability of the resulting map at the type level.
   */
  static final class ImmutableMapOf<K, V> {
    @BeforeTemplate
    Map<K, V> before() {
      return Refaster.anyOf(ImmutableMap.<K, V>builder().build(), emptyMap(), Map.of());
    }

    @AfterTemplate
    ImmutableMap<K, V> after() {
      return ImmutableMap.of();
    }
  }

  /**
   * Prefer {@link ImmutableMap#of(Object, Object)} over more contrived alternatives or alternatives
   * that don't communicate the immutability of the resulting map at the type level.
   */
  // XXX: Note that the replacement of `Collections#singletonMap` is incorrect for nullable
  // elements.
  static final class ImmutableMapOf1<K, V> {
    @BeforeTemplate
    Map<K, V> before(K k1, V v1) {
      return Refaster.anyOf(
          ImmutableMap.<K, V>builder().put(k1, v1).build(), singletonMap(k1, v1), Map.of(k1, v1));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1) {
      return ImmutableMap.of(k1, v1);
    }
  }

  /**
   * Prefer {@link ImmutableMap#of(Object, Object, Object, Object)} over alternatives that don't
   * communicate the immutability of the resulting map at the type level.
   */
  // XXX: Also rewrite the `ImmutableMap.builder()` variant?
  static final class ImmutableMapOf2<K, V> {
    @BeforeTemplate
    Map<K, V> before(K k1, V v1, K k2, V v2) {
      return Map.of(k1, v1, k2, v2);
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1, K k2, V v2) {
      return ImmutableMap.of(k1, v1, k2, v2);
    }
  }

  /**
   * Prefer {@link ImmutableMap#of(Object, Object, Object, Object, Object, Object)} over
   * alternatives that don't communicate the immutability of the resulting map at the type level.
   */
  // XXX: Also rewrite the `ImmutableMap.builder()` variant?
  static final class ImmutableMapOf3<K, V> {
    @BeforeTemplate
    Map<K, V> before(K k1, V v1, K k2, V v2, K k3, V v3) {
      return Map.of(k1, v1, k2, v2, k3, v3);
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1, K k2, V v2, K k3, V v3) {
      return ImmutableMap.of(k1, v1, k2, v2, k3, v3);
    }
  }

  /**
   * Prefer {@link ImmutableMap#of(Object, Object, Object, Object, Object, Object, Object, Object)}
   * over alternatives that don't communicate the immutability of the resulting map at the type
   * level.
   */
  // XXX: Also rewrite the `ImmutableMap.builder()` variant?
  static final class ImmutableMapOf4<K, V> {
    @BeforeTemplate
    Map<K, V> before(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      return Map.of(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      return ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4);
    }
  }

  /**
   * Prefer {@link ImmutableMap#of(Object, Object, Object, Object, Object, Object, Object, Object,
   * Object, Object)} over alternatives that don't communicate the immutability of the resulting map
   * at the type level.
   */
  // XXX: Also rewrite the `ImmutableMap.builder()` variant?
  static final class ImmutableMapOf5<K, V> {
    @BeforeTemplate
    Map<K, V> before(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      return Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      return ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }
  }

  /**
   * Prefer creation of an immutable submap using {@link Maps#filterKeys(Map, Predicate)} over more
   * contrived alternatives.
   */
  abstract static class ImmutableMapCopyOfMapsFilterKeys<K, V> {
    @Placeholder(allowsIdentity = true)
    abstract boolean keyFilter(@MayOptionallyUse K key);

    @BeforeTemplate
    ImmutableMap<K, V> before(ImmutableMap<K, V> map) {
      return map.entrySet().stream()
          .filter(e -> keyFilter(e.getKey()))
          .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(ImmutableMap<K, V> map) {
      return ImmutableMap.copyOf(Maps.filterKeys(map, k -> keyFilter(k)));
    }
  }

  /**
   * Prefer creation of an immutable submap using {@link Maps#filterValues(Map, Predicate)} over
   * more contrived alternatives.
   */
  abstract static class ImmutableMapCopyOfMapsFilterValues<K, V> {
    @Placeholder(allowsIdentity = true)
    abstract boolean valueFilter(@MayOptionallyUse V value);

    @BeforeTemplate
    ImmutableMap<K, V> before(ImmutableMap<K, V> map) {
      return map.entrySet().stream()
          .filter(e -> valueFilter(e.getValue()))
          .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(ImmutableMap<K, V> map) {
      return ImmutableMap.copyOf(Maps.filterValues(map, v -> valueFilter(v)));
    }
  }

  // XXX: Add a rule for this:
  // Maps.transformValues(streamOfEntries.collect(groupBy(fun)), ImmutableMap::copyOf)
  // ->
  // streamOfEntries.collect(groupBy(fun, toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)))
  //
  // map.entrySet().stream().filter(keyPred).forEach(mapBuilder::put)
  // ->
  // mapBuilder.putAll(Maps.filterKeys(map, pred))
}
