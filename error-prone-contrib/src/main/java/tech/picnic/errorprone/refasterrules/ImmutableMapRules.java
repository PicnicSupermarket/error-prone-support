package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Matches;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.matchers.IsIdentityOperation;

/** Refaster rules related to expressions dealing with {@link ImmutableMap}s. */
@OnlineDocumentation
final class ImmutableMapRules {
  private ImmutableMapRules() {}

  /** Prefer {@link ImmutableMap#builder()} over the associated constructor. */
  // XXX: This rule may drop generic type information, leading to non-compilable code.
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

  /** Prefer {@link ImmutableMap.Builder#buildOrThrow()} over less explicit alternatives. */
  static final class BuilderBuildOrThrow<K, V> {
    @BeforeTemplate
    ImmutableMap<K, V> before(ImmutableMap.Builder<K, V> builder) {
      return builder.build();
    }

    @AfterTemplate
    ImmutableMap<K, V> after(ImmutableMap.Builder<K, V> builder) {
      return builder.buildOrThrow();
    }
  }

  /** Prefer {@link ImmutableMap#of(Object, Object)} over more contrived alternatives. */
  static final class ImmutableMapOfEntryGetKeyEntryGetValue<K, V, K2 extends K, V2 extends V> {
    @BeforeTemplate
    ImmutableMap<K, V> before(Map.Entry<K2, V2> entry) {
      return Refaster.anyOf(
          ImmutableMap.<K, V>builder().put(entry).buildOrThrow(),
          Stream.of(entry).collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(Map.Entry<K2, V2> entry) {
      return ImmutableMap.of(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Prefer {@link Maps#toMap(Iterable, com.google.common.base.Function)} over more contrived
   * alternatives.
   */
  static final class MapsToMap<S, K extends S, V, V2 extends V, K2 extends K> {
    @BeforeTemplate
    ImmutableMap<K, V> before(
        Iterator<K> keys,
        Function<S, V2> valueFunction,
        @Matches(IsIdentityOperation.class) Function<S, K2> identityKeyFunction) {
      return Streams.stream(keys).collect(toImmutableMap(identityKeyFunction, valueFunction));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(
        Iterable<K> keys,
        Function<S, V2> valueFunction,
        @Matches(IsIdentityOperation.class) Function<S, K2> identityKeyFunction) {
      return Streams.stream(keys).collect(toImmutableMap(identityKeyFunction, valueFunction));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(
        Collection<K> keys,
        Function<S, V2> valueFunction,
        @Matches(IsIdentityOperation.class) Function<S, K2> identityKeyFunction) {
      return keys.stream().collect(toImmutableMap(identityKeyFunction, valueFunction));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(Set<K> keys, com.google.common.base.Function<S, V> valueFunction) {
      return ImmutableMap.copyOf(Maps.asMap(keys, valueFunction));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(
        Iterable<K> keys, com.google.common.base.Function<S, V> valueFunction) {
      return Maps.toMap(keys, valueFunction);
    }
  }

  /**
   * Prefer {@link ImmutableMap#copyOf(Iterable)} over imprecisely typed or more contrived
   * alternatives.
   */
  static final class ImmutableMapCopyOf<
      K, V, K2 extends K, V2 extends V, E extends Map.Entry<K2, V2>> {
    @BeforeTemplate
    Map<K, V> before(Map<K2, V2> entries) {
      return Refaster.anyOf(
          ImmutableMap.copyOf(entries.entrySet()),
          ImmutableMap.<K, V>builder().putAll(entries).buildOrThrow(),
          Map.copyOf(entries));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(Iterable<E> entries) {
      return Refaster.anyOf(
          ImmutableMap.<K, V>builder().putAll(entries).buildOrThrow(),
          Streams.stream(entries).collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(Collection<E> entries) {
      return entries.stream().collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(Iterable<E> entries) {
      return ImmutableMap.copyOf(entries);
    }
  }

  /** Prefer {@code stream.collect(toImmutableMap(...))} over more contrived alternatives. */
  abstract static class StreamCollectToImmutableMap<E, K, V> {
    @Placeholder(allowsIdentity = true)
    abstract K keyFunction(@MayOptionallyUse E element);

    @Placeholder(allowsIdentity = true)
    abstract V valueFunction(@MayOptionallyUse E element);

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
   * Prefer {@link Maps#uniqueIndex(Iterable, com.google.common.base.Function)} over more contrived
   * alternatives.
   */
  static final class MapsUniqueIndex<S, K, V extends S, K2 extends K, V2 extends V> {
    @BeforeTemplate
    ImmutableMap<K, V> before(
        Iterator<V> values,
        Function<S, K2> keyFunction,
        @Matches(IsIdentityOperation.class) Function<S, V2> identityValueFunction) {
      return Streams.stream(values).collect(toImmutableMap(keyFunction, identityValueFunction));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(
        Iterable<V> values,
        Function<S, K2> keyFunction,
        @Matches(IsIdentityOperation.class) Function<S, V2> identityValueFunction) {
      return Streams.stream(values).collect(toImmutableMap(keyFunction, identityValueFunction));
    }

    @BeforeTemplate
    ImmutableMap<K, V> before(
        Collection<V> values,
        Function<S, K2> keyFunction,
        @Matches(IsIdentityOperation.class) Function<S, V2> identityValueFunction) {
      return values.stream().collect(toImmutableMap(keyFunction, identityValueFunction));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(
        Iterable<V> values, com.google.common.base.Function<S, K> keyFunction) {
      return Maps.uniqueIndex(values, keyFunction);
    }
  }

  /**
   * Prefer an immutable copy of {@link Maps#transformValues(Map, com.google.common.base.Function)}
   * over more contrived alternatives.
   */
  abstract static class ImmutableMapCopyOfMapsTransformValues<K, V1, V2> {
    @Placeholder(allowsIdentity = true)
    abstract V2 valueTransformation(@MayOptionallyUse @Nullable V1 value);

    // XXX: Instead of `Map.Entry::getKey` we could also match `e -> e.getKey()`. But for some
    // reason Refaster doesn't handle that case. This doesn't matter if we roll out use of
    // `MethodReferenceUsage`. Same observation applies to a lot of other Refaster checks.
    @BeforeTemplate
    ImmutableMap<K, V2> before(Map<K, V1> fromMap) {
      return Refaster.anyOf(
          fromMap.entrySet().stream()
              .collect(toImmutableMap(Map.Entry::getKey, e -> valueTransformation(e.getValue()))),
          Maps.toMap(fromMap.keySet(), key -> valueTransformation(fromMap.get(key))));
    }

    @AfterTemplate
    ImmutableMap<K, V2> after(Map<K, V1> fromMap) {
      return ImmutableMap.copyOf(Maps.transformValues(fromMap, v -> valueTransformation(v)));
    }
  }

  /** Prefer {@link ImmutableMap#of()} over more verbose or imprecisely typed alternatives. */
  static final class ImmutableMapOf0<K, V> {
    @BeforeTemplate
    Map<K, V> before() {
      return Refaster.anyOf(
          ImmutableMap.<K, V>builder().buildOrThrow(),
          ImmutableMap.ofEntries(),
          emptyMap(),
          Map.of());
    }

    @AfterTemplate
    ImmutableMap<K, V> after() {
      return ImmutableMap.of();
    }
  }

  /**
   * Prefer {@link ImmutableMap#of(Object, Object)} over more verbose or imprecisely typed
   * alternatives.
   */
  // XXX: Note that the replacement of `Collections#singletonMap` is incorrect for nullable
  // elements.
  static final class ImmutableMapOf2<K, V> {
    @BeforeTemplate
    Map<K, V> before(K k1, V v1) {
      return Refaster.anyOf(
          ImmutableMap.<K, V>builder().put(k1, v1).buildOrThrow(),
          ImmutableMap.ofEntries(Map.entry(k1, v1)),
          singletonMap(k1, v1),
          Map.of(k1, v1));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1) {
      return ImmutableMap.of(k1, v1);
    }
  }

  /**
   * Prefer {@link ImmutableMap#of(Object, Object, Object, Object)} over more verbose or imprecisely
   * typed alternatives.
   */
  // XXX: Consider introducing a `BugChecker` to replace these `ImmutableMapOfX` rules. That will
  // also make it easier to rewrite various `ImmutableMap.builder()` variants.
  static final class ImmutableMapOf4<K, V> {
    @BeforeTemplate
    Map<K, V> before(K k1, V v1, K k2, V v2) {
      return Refaster.anyOf(
          ImmutableMap.ofEntries(Map.entry(k1, v1), Map.entry(k2, v2)), Map.of(k1, v1, k2, v2));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1, K k2, V v2) {
      return ImmutableMap.of(k1, v1, k2, v2);
    }
  }

  /**
   * Prefer {@link ImmutableMap#of(Object, Object, Object, Object, Object, Object)} over more
   * verbose or imprecisely typed alternatives.
   */
  // XXX: Consider introducing a `BugChecker` to replace these `ImmutableMapOfX` rules. That will
  // also make it easier to rewrite various `ImmutableMap.builder()` variants.
  static final class ImmutableMapOf6<K, V> {
    @BeforeTemplate
    Map<K, V> before(K k1, V v1, K k2, V v2, K k3, V v3) {
      return Refaster.anyOf(
          ImmutableMap.ofEntries(Map.entry(k1, v1), Map.entry(k2, v2), Map.entry(k3, v3)),
          Map.of(k1, v1, k2, v2, k3, v3));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1, K k2, V v2, K k3, V v3) {
      return ImmutableMap.of(k1, v1, k2, v2, k3, v3);
    }
  }

  /**
   * Prefer {@link ImmutableMap#of(Object, Object, Object, Object, Object, Object, Object, Object)}
   * over more verbose or imprecisely typed alternatives.
   */
  // XXX: Consider introducing a `BugChecker` to replace these `ImmutableMapOfX` rules. That will
  // also make it easier to rewrite various `ImmutableMap.builder()` variants.
  @SuppressWarnings("java:S107" /* Can't avoid many method parameters here. */)
  static final class ImmutableMapOf8<K, V> {
    @BeforeTemplate
    Map<K, V> before(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      return Refaster.anyOf(
          ImmutableMap.ofEntries(
              Map.entry(k1, v1), Map.entry(k2, v2), Map.entry(k3, v3), Map.entry(k4, v4)),
          Map.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      return ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4);
    }
  }

  /**
   * Prefer {@link ImmutableMap#of(Object, Object, Object, Object, Object, Object, Object, Object,
   * Object, Object)} over more verbose or imprecisely typed alternatives.
   */
  // XXX: Consider introducing a `BugChecker` to replace these `ImmutableMapOfX` rules. That will
  // also make it easier to rewrite various `ImmutableMap.builder()` variants.
  @SuppressWarnings("java:S107" /* Can't avoid many method parameters here. */)
  static final class ImmutableMapOf10<K, V> {
    @BeforeTemplate
    Map<K, V> before(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      return Refaster.anyOf(
          ImmutableMap.ofEntries(
              Map.entry(k1, v1),
              Map.entry(k2, v2),
              Map.entry(k3, v3),
              Map.entry(k4, v4),
              Map.entry(k5, v5)),
          Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      return ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }
  }

  /**
   * Prefer an immutable copy of {@link Maps#filterKeys(Map, Predicate)} over more contrived
   * alternatives.
   */
  abstract static class ImmutableMapCopyOfMapsFilterKeys<K, V> {
    @Placeholder(allowsIdentity = true)
    abstract boolean keyFilter(@MayOptionallyUse K key);

    @BeforeTemplate
    ImmutableMap<K, V> before(Map<K, V> unfiltered) {
      return unfiltered.entrySet().stream()
          .filter(e -> keyFilter(e.getKey()))
          .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(Map<K, V> unfiltered) {
      return ImmutableMap.copyOf(Maps.filterKeys(unfiltered, k -> keyFilter(k)));
    }
  }

  /**
   * Prefer an immutable copy of {@link Maps#filterValues(Map, Predicate)} over more contrived
   * alternatives.
   */
  abstract static class ImmutableMapCopyOfMapsFilterValues<K, V> {
    @Placeholder(allowsIdentity = true)
    abstract boolean valueFilter(@MayOptionallyUse V value);

    @BeforeTemplate
    ImmutableMap<K, V> before(Map<K, V> unfiltered) {
      return unfiltered.entrySet().stream()
          .filter(e -> valueFilter(e.getValue()))
          .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(Map<K, V> unfiltered) {
      return ImmutableMap.copyOf(Maps.filterValues(unfiltered, v -> valueFilter(v)));
    }
  }

  /** Prefer {@link ImmutableMap#ofEntries(Map.Entry[])} over imprecisely typed alternatives. */
  static final class ImmutableMapOfEntries<K, V, K2 extends K, V2 extends V> {
    @BeforeTemplate
    Map<K, V> before(@Repeated Map.Entry<K2, V2> entries) {
      return Map.ofEntries(Refaster.asVarargs(entries));
    }

    @AfterTemplate
    ImmutableMap<K, V> after(@Repeated Map.Entry<K2, V2> entries) {
      return ImmutableMap.ofEntries(Refaster.asVarargs(entries));
    }
  }

  /** Prefer {@link ImmutableMap.Builder#put(Object, Object)} over more contrived alternatives. */
  static final class BuilderPut<K, V> {
    @BeforeTemplate
    ImmutableMap.Builder<K, V> before(ImmutableMap.Builder<K, V> builder, K key, V value) {
      return Refaster.anyOf(
          builder.put(Map.entry(key, value)), builder.putAll(ImmutableMap.of(key, value)));
    }

    @AfterTemplate
    ImmutableMap.Builder<K, V> after(ImmutableMap.Builder<K, V> builder, K key, V value) {
      return builder.put(key, value);
    }
  }
}
