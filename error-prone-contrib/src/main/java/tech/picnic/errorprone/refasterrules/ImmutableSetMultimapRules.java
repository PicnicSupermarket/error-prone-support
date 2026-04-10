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
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link ImmutableSetMultimap}s. */
@OnlineDocumentation
final class ImmutableSetMultimapRules {
  private ImmutableSetMultimapRules() {}

  /** Prefer {@link ImmutableSetMultimap#builder()} over the associated constructor. */
  // XXX: This rule may drop generic type information, leading to non-compilable code.
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

  /** Prefer {@link ImmutableSetMultimap#of()} over less efficient alternatives. */
  static final class ImmutableSetMultimapOf0<K, V> {
    @BeforeTemplate
    ImmutableSetMultimap<K, V> before() {
      return ImmutableSetMultimap.<K, V>builder().build();
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V> after() {
      return ImmutableSetMultimap.of();
    }
  }

  /** Prefer {@link ImmutableSetMultimap#of(Object, Object)} over less efficient alternatives. */
  // XXX: One can define variants for more than one key-value pair, but at some point the builder
  // actually produces nicer code. So it's not clear we should add Refaster rules for those
  // variants.
  static final class ImmutableSetMultimapOf2<K, V> {
    @BeforeTemplate
    ImmutableSetMultimap<K, V> before(K k1, V v1) {
      return ImmutableSetMultimap.<K, V>builder().put(k1, v1).build();
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V> after(K k1, V v1) {
      return ImmutableSetMultimap.of(k1, v1);
    }
  }

  /**
   * Prefer {@link ImmutableSetMultimap#of(Object, Object)} over less efficient or more contrived
   * alternatives.
   */
  static final class ImmutableSetMultimapOfEntryGetKeyEntryGetValue<
      K, V, K2 extends K, V2 extends V> {
    @BeforeTemplate
    ImmutableSetMultimap<K, V> before(Map.Entry<K2, V2> entry) {
      return Refaster.anyOf(
          ImmutableSetMultimap.<K, V>builder().put(entry).build(),
          Stream.of(entry).collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V> after(Map.Entry<K2, V2> entry) {
      return ImmutableSetMultimap.of(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Prefer {@link ImmutableSetMultimap#copyOf(Iterable)} over less efficient or more contrived
   * alternatives.
   */
  static final class ImmutableSetMultimapCopyOf<
      K, V, K2 extends K, V2 extends V, E extends Map.Entry<K2, V2>> {
    @BeforeTemplate
    ImmutableSetMultimap<K, V> before(Multimap<K2, V2> entries) {
      return Refaster.anyOf(
          ImmutableSetMultimap.copyOf(entries.entries()),
          ImmutableSetMultimap.<K, V>builder().putAll(entries).build());
    }

    @BeforeTemplate
    ImmutableSetMultimap<K, V> before(Iterable<E> entries) {
      return Refaster.anyOf(
          ImmutableSetMultimap.<K, V>builder().putAll(entries).build(),
          Streams.stream(entries)
              .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @BeforeTemplate
    ImmutableSetMultimap<K, V> before(Collection<E> entries) {
      return entries.stream()
          .collect(toImmutableSetMultimap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V> after(Iterable<E> entries) {
      return ImmutableSetMultimap.copyOf(entries);
    }
  }

  /**
   * Prefer {@code stream.collect(toImmutableSetMultimap(...))} over more contrived alternatives.
   */
  abstract static class StreamCollectToImmutableSetMultimap<E, K, V> {
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
   * Prefer an immutable copy of {@link Multimaps#transformValues(Multimap,
   * com.google.common.base.Function)} over more contrived alternatives.
   */
  abstract static class ImmutableSetMultimapCopyOfMultimapsTransformValues<K, V1, V2> {
    @Placeholder(allowsIdentity = true)
    abstract V2 valueTransformation(@MayOptionallyUse V1 value);

    @BeforeTemplate
    ImmutableSetMultimap<K, V2> before(Multimap<K, V1> fromMultimap) {
      return fromMultimap.entries().stream()
          .collect(
              toImmutableSetMultimap(Map.Entry::getKey, e -> valueTransformation(e.getValue())));
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V2> after(Multimap<K, V1> fromMultimap) {
      return ImmutableSetMultimap.copyOf(
          Multimaps.transformValues(fromMultimap, e -> valueTransformation(e)));
    }
  }

  /**
   * Prefer an immutable copy of {@link Multimaps#transformValues(Multimap,
   * com.google.common.base.Function)} over more contrived alternatives.
   */
  static final class ImmutableSetMultimapCopyOfMultimapsTransformValuesWithFunction<
      K, S, V1 extends S, T extends V2, V2> {
    // XXX: Drop the `Refaster.anyOf` if we decide to rewrite one to the other.
    @BeforeTemplate
    ImmutableSetMultimap<K, V2> before(Multimap<K, V1> fromMultimap, Function<S, T> function) {
      return Refaster.anyOf(fromMultimap.asMap(), Multimaps.asMap(fromMultimap)).entrySet().stream()
          .collect(
              flatteningToImmutableSetMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(function)));
    }

    @BeforeTemplate
    ImmutableSetMultimap<K, V2> before(ListMultimap<K, V1> fromMultimap, Function<S, T> function) {
      return Multimaps.asMap(fromMultimap).entrySet().stream()
          .collect(
              flatteningToImmutableSetMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(function)));
    }

    @BeforeTemplate
    ImmutableSetMultimap<K, V2> before(SetMultimap<K, V1> fromMultimap, Function<S, T> function) {
      return Multimaps.asMap(fromMultimap).entrySet().stream()
          .collect(
              flatteningToImmutableSetMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(function)));
    }

    @BeforeTemplate
    ImmutableSetMultimap<K, V2> before(
        SortedSetMultimap<K, V1> fromMultimap, Function<S, T> function) {
      return Multimaps.asMap(fromMultimap).entrySet().stream()
          .collect(
              flatteningToImmutableSetMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(function)));
    }

    @AfterTemplate
    ImmutableSetMultimap<K, V2> after(
        Multimap<K, V1> fromMultimap, com.google.common.base.Function<S, T> function) {
      return ImmutableSetMultimap.copyOf(Multimaps.transformValues(fromMultimap, function));
    }
  }

  /**
   * Prefer {@link ImmutableSetMultimap.Builder#put(Object, Object)} over more contrived
   * alternatives.
   */
  static final class BuilderPut<K, V> {
    @BeforeTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    ImmutableSetMultimap.Builder<K, V> before(
        ImmutableSetMultimap.Builder<K, V> builder, K key, V value) {
      return Refaster.anyOf(builder.put(Map.entry(key, value)), builder.putAll(key, value));
    }

    @AfterTemplate
    ImmutableSetMultimap.Builder<K, V> after(
        ImmutableSetMultimap.Builder<K, V> builder, K key, V value) {
      return builder.put(key, value);
    }
  }
}
