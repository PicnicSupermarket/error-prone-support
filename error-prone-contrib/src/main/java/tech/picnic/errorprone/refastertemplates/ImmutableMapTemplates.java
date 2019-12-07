package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

  /**
   * Prefer {@link ImmutableMap#of(Object, Object)} over more contrived alternatives and
   * alternatives that don't communicate the immutability of the resulting map at the type level..
   */
  // XXX: One can define variants for more than one key-value pair, but at some point the builder
  // actually produces nicer code. So it's not clear we should add Refaster templates for those
  // variants.
  // XXX: Note that the `singletonMap` rewrite rule is incorrect for nullable elements.
  static final class PairToImmutableMap<K, V> {
    @BeforeTemplate
    Map<K, V> before(K key, V value) {
      return Refaster.anyOf(
          ImmutableMap.<K, V>builder().put(key, value).build(),
          Collections.singletonMap(key, value));
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
   * com.google.common.base.Function)} over creating and directly collecting a stream.
   */
  abstract static class TransformMapValuesToImmutableMap<K, V1, V2> {
    @Placeholder
    abstract V2 valueTransformation(@MayOptionallyUse V1 value);

    // XXX: Instead of `Map.Entry::getKey` we could also match `e -> e.getKey()`. But for some
    // reason Refaster doesn't handle that case. This doesn't matter if we roll out use of
    // `MethodReferenceUsageCheck`. Same observation applies to a lot of other Refaster checks.
    @BeforeTemplate
    ImmutableMap<K, V2> before(Map<K, V1> map) {
      return map.entrySet().stream()
          .collect(toImmutableMap(Map.Entry::getKey, e -> valueTransformation(e.getValue())));
    }

    @AfterTemplate
    ImmutableMap<K, V2> after(Map<K, V1> map) {
      return ImmutableMap.copyOf(Maps.transformValues(map, v -> valueTransformation(v)));
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

  // XXX: Add a template for this:
  // Maps.transformValues(streamOfEntries.collect(groupBy(fun)), ImmutableMap::copyOf)
  // ->
  // streamOfEntries.collect(groupBy(fun, toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)))
  //
  // map.entrySet().stream().filter(keyPred).forEach(mapBuilder::put)
  // ->
  // mapBuilder.putAll(Maps.filterKeys(map, pred))
  //
  // map.entrySet().stream().filter(entry ->
  // pred(entry.getKey())).collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue))
  // ->
  // ImmutableMap.copyOf(Maps.filterKeys(map, pred))
}
