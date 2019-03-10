package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.function.Function.identity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * Assorted Refaster templates that do not (yet) belong in one of the other classes with more
 * topical Refaster templates.
 */
final class AssortedTemplates {
  private AssortedTemplates() {}

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

  static final class MapGetOrNull<K, V, L> {
    @Nullable
    @BeforeTemplate
    V before(Map<K, V> map, L key) {
      return map.getOrDefault(key, null);
    }

    @Nullable
    @AfterTemplate
    V after(Map<K, V> map, L key) {
      return map.get(key);
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

  /**
   * Use {@link Sets#toImmutableEnumSet()} when possible, as it is more efficient than {@link
   * ImmutableSet#toImmutableSet()} and produces a more compact object.
   *
   * <p><strong>Warning:</strong> this rewrite rule is not completely behavior preserving: while the
   * original code produces a set which iterates over the elements in encounter order, the
   * replacement code iterates over the elements in enum definition order.
   */
  static final class StreamToImmutableEnumSet<T extends Enum<T>> {
    @BeforeTemplate
    ImmutableSet<T> before(Stream<T> stream) {
      return stream.collect(toImmutableSet());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableSet<T> after(Stream<T> stream) {
      return stream.collect(Sets.toImmutableEnumSet());
    }
  }

  /**
   * Prefer creating an immutable copy of the result of {@link Maps#transformValues(Map,
   * com.google.common.base.Function)} over creating and directly collecting a stream.
   */
  abstract static class TransformMapValueToImmutableMap<K, V1, V2> {
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

  /** Prefer {@link Iterators#getNext(Iterator, Object)} over more contrived alternatives. */
  static final class IteratorGetNextOrDefault<T> {
    @BeforeTemplate
    T before(Iterator<T> iterator, T defaultValue) {
      return Refaster.anyOf(
          iterator.hasNext() ? iterator.next() : defaultValue,
          Streams.stream(iterator).findFirst().orElse(defaultValue),
          Streams.stream(iterator).findAny().orElse(defaultValue));
    }

    @Nullable
    @AfterTemplate
    T after(Iterator<T> iterator, T defaultValue) {
      return Iterators.getNext(iterator, defaultValue);
    }
  }

  /** Don't unnecessarily repeat boolean expressions. */
  // XXX: This template only captures only the simplest case. `@AlsoNegation` doesn't help. Consider
  // contributing a Refaster patch which handles the negation in the `@BeforeTemplate` more
  // intelligently.
  static final class LogicalImplication {
    @BeforeTemplate
    boolean before(boolean firstTest, boolean secondTest) {
      return firstTest || (!firstTest && secondTest);
    }

    @AfterTemplate
    boolean after(boolean firstTest, boolean secondTest) {
      return firstTest || secondTest;
    }
  }

  /**
   * Prefer {@link Stream#generate(java.util.function.Supplier)} over more contrived alternatives.
   */
  static final class UnboundedSingleElementStream<T> {
    @BeforeTemplate
    Stream<T> before(T object) {
      return Streams.stream(Iterables.cycle(object));
    }

    @AfterTemplate
    Stream<T> after(T object) {
      return Stream.generate(() -> object);
    }
  }
}
