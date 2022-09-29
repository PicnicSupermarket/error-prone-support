package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableListMultimap.flatteningToImmutableListMultimap;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.function.Function.identity;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.SortedSetMultimap;
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
import java.util.function.Function;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link ImmutableListMultimap}s. */
final class ImmutableListMultimapTemplates {
  private ImmutableListMultimapTemplates() {}

  /**
   * Prefer {@link ImmutableListMultimap#builder()} over the associated constructor on constructions
   * that produce a less-specific type.
   */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. See
  // https://github.com/google/error-prone/pull/2706.
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
      return Refaster.anyOf(ImmutableListMultimap.<K, V>builder().build(), ImmutableMultimap.of());
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
    ImmutableListMultimap<K, V> before(Map.Entry<? extends K, ? extends V> entry) {
      return Refaster.anyOf(
          ImmutableListMultimap.<K, V>builder().put(entry).build(),
          Stream.of(entry)
              .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)));
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
          ImmutableMultimap.copyOf(iterable.entries()));
    }

    @BeforeTemplate
    ImmutableMultimap<K, V> before(
        Iterable<? extends Map.Entry<? extends K, ? extends V>> iterable) {
      return Refaster.anyOf(
          ImmutableListMultimap.<K, V>builder().putAll(iterable).build(),
          Streams.stream(iterable)
              .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)),
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

  /**
   * Don't map stream's elements to map entries, only to subsequently collect them into an {@link
   * ImmutableListMultimap}. The collection can be performed directly.
   */
  abstract static class StreamOfMapEntriesToImmutableListMultimap<E, K, V> {
    @Placeholder(allowsIdentity = true)
    abstract K keyFunction(@MayOptionallyUse E element);

    @Placeholder(allowsIdentity = true)
    abstract V valueFunction(@MayOptionallyUse E element);

    // XXX: We could add variants in which the entry is created some other way, but we have another
    // rule which covers canonicalization to `Map.entry`.
    @BeforeTemplate
    ImmutableListMultimap<K, V> before(Stream<E> stream) {
      return stream
          .map(e -> Map.entry(keyFunction(e), valueFunction(e)))
          .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ImmutableListMultimap<K, V> after(Stream<E> stream) {
      return stream.collect(toImmutableListMultimap(e -> keyFunction(e), e -> valueFunction(e)));
    }
  }

  /**
   * Prefer {@link Multimaps#index(Iterable, com.google.common.base.Function)} over the stream-based
   * alternative.
   */
  // XXX: Drop the `Refaster.anyOf` if/when we decide to rewrite one to the other.
  static final class IndexIterableToImmutableListMultimap<K, V> {
    @BeforeTemplate
    ImmutableListMultimap<K, V> before(
        Iterator<V> iterable, Function<? super V, ? extends K> keyFunction) {
      return Streams.stream(iterable)
          .collect(toImmutableListMultimap(keyFunction, Refaster.anyOf(identity(), v -> v)));
    }

    @BeforeTemplate
    ImmutableListMultimap<K, V> before(
        Iterable<V> iterable, Function<? super V, ? extends K> keyFunction) {
      return Streams.stream(iterable)
          .collect(toImmutableListMultimap(keyFunction, Refaster.anyOf(identity(), v -> v)));
    }

    @BeforeTemplate
    ImmutableListMultimap<K, V> before(
        Collection<V> iterable, Function<? super V, ? extends K> keyFunction) {
      return iterable.stream()
          .collect(toImmutableListMultimap(keyFunction, Refaster.anyOf(identity(), v -> v)));
    }

    @AfterTemplate
    ImmutableListMultimap<K, V> after(
        Iterable<V> iterable, com.google.common.base.Function<? super V, K> keyFunction) {
      return Multimaps.index(iterable, keyFunction);
    }
  }

  /**
   * Prefer creating an immutable copy of the result of {@link Multimaps#transformValues(Multimap,
   * com.google.common.base.Function)} over creating and directly collecting a stream.
   */
  abstract static class TransformMultimapValuesToImmutableListMultimap<K, V1, V2> {
    @Placeholder(allowsIdentity = true)
    abstract V2 valueTransformation(@MayOptionallyUse V1 value);

    @BeforeTemplate
    ImmutableListMultimap<K, V2> before(Multimap<K, V1> multimap) {
      return multimap.entries().stream()
          .collect(
              toImmutableListMultimap(Map.Entry::getKey, e -> valueTransformation(e.getValue())));
    }

    @AfterTemplate
    ImmutableListMultimap<K, V2> after(Multimap<K, V1> multimap) {
      return ImmutableListMultimap.copyOf(
          Multimaps.transformValues(multimap, v -> valueTransformation(v)));
    }
  }

  /**
   * Prefer creating an immutable copy of the result of {@link Multimaps#transformValues(Multimap,
   * com.google.common.base.Function)} over creating and directly collecting a stream.
   */
  static final class TransformMultimapValuesToImmutableListMultimap2<K, V1, V2> {
    // XXX: Drop the `Refaster.anyOf` if we decide to rewrite one to the other.
    @BeforeTemplate
    ImmutableListMultimap<K, V2> before(
        Multimap<K, V1> multimap, Function<? super V1, ? extends V2> transformation) {
      return Refaster.anyOf(multimap.asMap(), Multimaps.asMap(multimap)).entrySet().stream()
          .collect(
              flatteningToImmutableListMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(transformation)));
    }

    @BeforeTemplate
    ImmutableListMultimap<K, V2> before(
        ListMultimap<K, V1> multimap, Function<? super V1, ? extends V2> transformation) {
      return Multimaps.asMap(multimap).entrySet().stream()
          .collect(
              flatteningToImmutableListMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(transformation)));
    }

    @BeforeTemplate
    ImmutableListMultimap<K, V2> before(
        SetMultimap<K, V1> multimap, Function<? super V1, ? extends V2> transformation) {
      return Multimaps.asMap(multimap).entrySet().stream()
          .collect(
              flatteningToImmutableListMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(transformation)));
    }

    @BeforeTemplate
    ImmutableListMultimap<K, V2> before(
        SortedSetMultimap<K, V1> multimap, Function<? super V1, ? extends V2> transformation) {
      return Multimaps.asMap(multimap).entrySet().stream()
          .collect(
              flatteningToImmutableListMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(transformation)));
    }

    @AfterTemplate
    ImmutableListMultimap<K, V2> after(
        Multimap<K, V1> multimap,
        com.google.common.base.Function<? super V1, ? extends V2> transformation) {
      return ImmutableListMultimap.copyOf(Multimaps.transformValues(multimap, transformation));
    }
  }
}
