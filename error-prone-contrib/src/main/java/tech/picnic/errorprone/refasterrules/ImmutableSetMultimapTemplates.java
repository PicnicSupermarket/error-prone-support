package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;

import com.google.common.collect.ImmutableSetMultimap;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/** Refaster rules related to expressions dealing with {@link ImmutableSetMultimap}s. */
final class ImmutableSetMultimapTemplates {
  private ImmutableSetMultimapTemplates() {}

  /** Prefer {@link ImmutableSetMultimap#builder()} over the associated constructor. */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. See
  // https://github.com/google/error-prone/pull/2706.
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
  // actually produces nicer code. So it's not clear we should add Refaster rules for those
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

  /**
   * Don't map a a stream's elements to map entries, only to subsequently collect them into an
   * {@link ImmutableSetMultimap}. The collection can be performed directly.
   */
  abstract static class StreamOfMapEntriesToImmutableSetMultimap<E, K, V> {
    @Placeholder(allowsIdentity = true)
    abstract K keyFunction(@MayOptionallyUse E element);

    @Placeholder(allowsIdentity = true)
    abstract V valueFunction(@MayOptionallyUse E element);

    // XXX: We could add variants in which the entry is created some other way, but we have another
    // rule which covers canonicalization to `Map.entry`.
    @BeforeTemplate
    ImmutableSetMultimap<K, V> before(Stream<E> stream) {
      return stream
          .map(e -> Map.entry(keyFunction(e), valueFunction(e)))
          .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ImmutableSetMultimap<K, V> after(Stream<E> stream) {
      return stream.collect(toImmutableSetMultimap(e -> keyFunction(e), e -> valueFunction(e)));
    }
  }

  /**
   * Prefer creating an immutable copy of the result of {@link Multimaps#transformValues(Multimap,
   * com.google.common.base.Function)} over creating and directly collecting a stream.
   */
  abstract static class TransformMultimapValuesToImmutableSetMultimap<K, V1, V2> {
    @Placeholder(allowsIdentity = true)
    abstract V2 valueTransformation(@MayOptionallyUse V1 value);

    @BeforeTemplate
    ImmutableSetMultimap<K, V2> before(Multimap<K, V1> multimap) {
      return multimap.entries().stream()
          .collect(
              toImmutableSetMultimap(Map.Entry::getKey, e -> valueTransformation(e.getValue())));
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V2> after(Multimap<K, V1> multimap) {
      return ImmutableSetMultimap.copyOf(
          Multimaps.transformValues(multimap, e -> valueTransformation(e)));
    }
  }

  /**
   * Prefer creating an immutable copy of the result of {@link Multimaps#transformValues(Multimap,
   * com.google.common.base.Function)} over creating and directly collecting a stream.
   */
  static final class TransformMultimapValuesToImmutableSetMultimap2<K, V1, V2> {
    // XXX: Drop the `Refaster.anyOf` if we decide to rewrite one to the other.
    @BeforeTemplate
    ImmutableSetMultimap<K, V2> before(
        Multimap<K, V1> multimap, Function<? super V1, ? extends V2> transformation) {
      return Refaster.anyOf(multimap.asMap(), Multimaps.asMap(multimap)).entrySet().stream()
          .collect(
              flatteningToImmutableSetMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(transformation)));
    }

    @BeforeTemplate
    ImmutableSetMultimap<K, V2> before(
        ListMultimap<K, V1> multimap, Function<? super V1, ? extends V2> transformation) {
      return Multimaps.asMap(multimap).entrySet().stream()
          .collect(
              flatteningToImmutableSetMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(transformation)));
    }

    @BeforeTemplate
    ImmutableSetMultimap<K, V2> before(
        SetMultimap<K, V1> multimap, Function<? super V1, ? extends V2> transformation) {
      return Multimaps.asMap(multimap).entrySet().stream()
          .collect(
              flatteningToImmutableSetMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(transformation)));
    }

    @BeforeTemplate
    ImmutableSetMultimap<K, V2> before(
        SortedSetMultimap<K, V1> multimap, Function<? super V1, ? extends V2> transformation) {
      return Multimaps.asMap(multimap).entrySet().stream()
          .collect(
              flatteningToImmutableSetMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(transformation)));
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V2> after(
        Multimap<K, V1> multimap,
        com.google.common.base.Function<? super V1, ? extends V2> transformation) {
      return ImmutableSetMultimap.copyOf(Multimaps.transformValues(multimap, transformation));
    }
  }
}
