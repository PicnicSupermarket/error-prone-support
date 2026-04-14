package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableListMultimap.flatteningToImmutableListMultimap;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;

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
import com.google.errorprone.refaster.annotation.Matches;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.matchers.IsIdentityOperation;

/** Refaster rules related to expressions dealing with {@link ImmutableListMultimap}s. */
@OnlineDocumentation
final class ImmutableListMultimapRules {
  private ImmutableListMultimapRules() {}

  /**
   * Prefer {@link ImmutableListMultimap#builder()} over the associated constructor or imprecisely
   * typed alternatives.
   */
  // XXX: This rule may drop generic type information, leading to non-compilable code.
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
   * Prefer {@link ImmutableListMultimap#of()} over imprecisely typed or less efficient
   * alternatives.
   */
  static final class ImmutableListMultimapOf0<K, V> {
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
   * Prefer {@link ImmutableListMultimap#of(Object, Object)} over imprecisely typed or less
   * efficient alternatives.
   */
  // XXX: One can define variants for more than one key-value pair, but at some point the builder
  // actually produces nicer code. So it's not clear we should add Refaster rules for those
  // variants.
  static final class ImmutableListMultimapOf2<K, V> {
    @BeforeTemplate
    ImmutableMultimap<K, V> before(K k1, V v1) {
      return Refaster.anyOf(
          ImmutableListMultimap.<K, V>builder().put(k1, v1).build(), ImmutableMultimap.of(k1, v1));
    }

    @AfterTemplate
    ImmutableListMultimap<K, V> after(K k1, V v1) {
      return ImmutableListMultimap.of(k1, v1);
    }
  }

  /**
   * Prefer {@link ImmutableListMultimap#of(Object, Object)} over less efficient or more contrived
   * alternatives.
   */
  static final class ImmutableListMultimapOfEntryGetKeyEntryGetValue<
      K, V, K2 extends K, V2 extends V> {
    @BeforeTemplate
    ImmutableListMultimap<K, V> before(Map.Entry<K2, V2> entry) {
      return Refaster.anyOf(
          ImmutableListMultimap.<K, V>builder().put(entry).build(),
          Stream.of(entry)
              .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @AfterTemplate
    ImmutableListMultimap<K, V> after(Map.Entry<K2, V2> entry) {
      return ImmutableListMultimap.of(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Prefer {@link ImmutableListMultimap#copyOf(Iterable)} over less efficient or more contrived
   * alternatives.
   */
  static final class ImmutableListMultimapCopyOf<
      K, V, K2 extends K, V2 extends V, E extends Map.Entry<K2, V2>> {
    @BeforeTemplate
    ImmutableMultimap<K, V> before(Multimap<K2, V2> entries) {
      return Refaster.anyOf(
          ImmutableListMultimap.copyOf(entries.entries()),
          ImmutableListMultimap.<K, V>builder().putAll(entries).build(),
          ImmutableMultimap.copyOf(entries),
          ImmutableMultimap.copyOf(entries.entries()));
    }

    @BeforeTemplate
    ImmutableMultimap<K, V> before(Iterable<E> entries) {
      return Refaster.anyOf(
          ImmutableListMultimap.<K, V>builder().putAll(entries).build(),
          Streams.stream(entries)
              .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue)),
          ImmutableMultimap.copyOf(entries));
    }

    @BeforeTemplate
    ImmutableListMultimap<K, V> before(Collection<E> entries) {
      return entries.stream()
          .collect(toImmutableListMultimap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    ImmutableListMultimap<K, V> after(Iterable<E> entries) {
      return ImmutableListMultimap.copyOf(entries);
    }
  }

  /**
   * Prefer {@code stream.collect(toImmutableListMultimap(...))} over more contrived alternatives.
   */
  abstract static class StreamCollectToImmutableListMultimap<E, K, V> {
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
   * Prefer {@link Multimaps#index(Iterable, com.google.common.base.Function)} over more contrived
   * alternatives.
   */
  static final class MultimapsIndex<S, K, V extends S, K2 extends K, V2 extends V> {
    @BeforeTemplate
    ImmutableListMultimap<K, V> before(
        Iterable<V> values,
        Function<S, K2> keyFunction,
        @Matches(IsIdentityOperation.class) Function<S, V2> identityValueFunction) {
      return Streams.stream(values)
          .collect(toImmutableListMultimap(keyFunction, identityValueFunction));
    }

    @BeforeTemplate
    ImmutableListMultimap<K, V> before(
        Collection<V> values,
        Function<S, K2> keyFunction,
        @Matches(IsIdentityOperation.class) Function<S, V2> identityValueFunction) {
      return values.stream().collect(toImmutableListMultimap(keyFunction, identityValueFunction));
    }

    @BeforeTemplate
    ImmutableListMultimap<K, V> before(
        Iterator<V> values,
        Function<S, K2> keyFunction,
        @Matches(IsIdentityOperation.class) Function<S, V2> identityValueFunction) {
      return Streams.stream(values)
          .collect(toImmutableListMultimap(keyFunction, identityValueFunction));
    }

    @AfterTemplate
    ImmutableListMultimap<K, V> after(
        Iterable<V> values, com.google.common.base.Function<S, K> keyFunction) {
      return Multimaps.index(values, keyFunction);
    }
  }

  /**
   * Prefer an immutable copy of {@link Multimaps#transformValues(Multimap,
   * com.google.common.base.Function)} over more contrived alternatives.
   */
  abstract static class ImmutableListMultimapCopyOfMultimapsTransformValues<K, V1, V2> {
    @Placeholder(allowsIdentity = true)
    abstract V2 valueTransformation(@MayOptionallyUse V1 value);

    @BeforeTemplate
    ImmutableListMultimap<K, V2> before(Multimap<K, V1> fromMultimap) {
      return fromMultimap.entries().stream()
          .collect(
              toImmutableListMultimap(Map.Entry::getKey, e -> valueTransformation(e.getValue())));
    }

    @AfterTemplate
    ImmutableListMultimap<K, V2> after(Multimap<K, V1> fromMultimap) {
      return ImmutableListMultimap.copyOf(
          Multimaps.transformValues(fromMultimap, v -> valueTransformation(v)));
    }
  }

  /**
   * Prefer an immutable copy of {@link Multimaps#transformValues(Multimap,
   * com.google.common.base.Function)} over more contrived alternatives.
   */
  static final class ImmutableListMultimapCopyOfMultimapsTransformValuesWithFunction<
      S, K, V1 extends S, V2, T extends V2> {
    // XXX: Drop the `Refaster.anyOf` if we decide to rewrite one to the other.
    @BeforeTemplate
    ImmutableListMultimap<K, V2> before(Multimap<K, V1> fromMultimap, Function<S, T> function) {
      return Refaster.anyOf(fromMultimap.asMap(), Multimaps.asMap(fromMultimap)).entrySet().stream()
          .collect(
              flatteningToImmutableListMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(function)));
    }

    @BeforeTemplate
    ImmutableListMultimap<K, V2> before(ListMultimap<K, V1> fromMultimap, Function<S, T> function) {
      return Multimaps.asMap(fromMultimap).entrySet().stream()
          .collect(
              flatteningToImmutableListMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(function)));
    }

    @BeforeTemplate
    ImmutableListMultimap<K, V2> before(SetMultimap<K, V1> fromMultimap, Function<S, T> function) {
      return Multimaps.asMap(fromMultimap).entrySet().stream()
          .collect(
              flatteningToImmutableListMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(function)));
    }

    @BeforeTemplate
    ImmutableListMultimap<K, V2> before(
        SortedSetMultimap<K, V1> fromMultimap, Function<S, T> function) {
      return Multimaps.asMap(fromMultimap).entrySet().stream()
          .collect(
              flatteningToImmutableListMultimap(
                  Map.Entry::getKey, e -> e.getValue().stream().map(function)));
    }

    @AfterTemplate
    ImmutableListMultimap<K, V2> after(
        Multimap<K, V1> fromMultimap, com.google.common.base.Function<S, V2> function) {
      return ImmutableListMultimap.copyOf(Multimaps.transformValues(fromMultimap, function));
    }
  }

  /**
   * Prefer {@link ImmutableListMultimap.Builder#put(Object, Object)} over more contrived
   * alternatives.
   */
  static final class BuilderPut<K, V> {
    @BeforeTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    ImmutableListMultimap.Builder<K, V> before(
        ImmutableListMultimap.Builder<K, V> builder, K key, V value) {
      return Refaster.anyOf(builder.put(Map.entry(key, value)), builder.putAll(key, value));
    }

    @AfterTemplate
    ImmutableListMultimap.Builder<K, V> after(
        ImmutableListMultimap.Builder<K, V> builder, K key, V value) {
      return builder.put(key, value);
    }
  }
}
