package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.function.Predicate.not;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link ImmutableSet}s. */
@OnlineDocumentation
final class ImmutableSetRules {
  private ImmutableSetRules() {}

  /** Prefer {@link ImmutableSet#builder()} over the associated constructor. */
  // XXX: This rule may drop generic type information, leading to non-compilable code.
  static final class ImmutableSetBuilder<T> {
    @BeforeTemplate
    ImmutableSet.Builder<T> before() {
      return new ImmutableSet.Builder<>();
    }

    @AfterTemplate
    ImmutableSet.Builder<T> after() {
      return ImmutableSet.builder();
    }
  }

  /** Prefer {@link ImmutableSet#copyOf(Iterable)} and variants over more contrived alternatives. */
  static final class IterableToImmutableSet<T> {
    @BeforeTemplate
    ImmutableSet<T> before(T[] iterable) {
      return Refaster.anyOf(
          ImmutableSet.<T>builder().add(iterable).build(),
          Arrays.stream(iterable).collect(toImmutableSet()));
    }

    @BeforeTemplate
    ImmutableSet<T> before(Iterator<T> iterable) {
      return Refaster.anyOf(
          ImmutableSet.<T>builder().addAll(iterable).build(),
          Streams.stream(iterable).collect(toImmutableSet()));
    }

    @BeforeTemplate
    ImmutableSet<T> before(Iterable<T> iterable) {
      return Refaster.anyOf(
          ImmutableSet.<T>builder().addAll(iterable).build(),
          Streams.stream(iterable).collect(toImmutableSet()));
    }

    @BeforeTemplate
    ImmutableSet<T> before(Collection<T> iterable) {
      return iterable.stream().collect(toImmutableSet());
    }

    @AfterTemplate
    ImmutableSet<T> after(Iterable<T> iterable) {
      return ImmutableSet.copyOf(iterable);
    }
  }

  /** Prefer {@link ImmutableSet#toImmutableSet()} over less idiomatic alternatives. */
  static final class StreamToImmutableSet<T> {
    @BeforeTemplate
    ImmutableSet<T> before(Stream<T> stream) {
      return Refaster.anyOf(
          ImmutableSet.copyOf(stream.iterator()), stream.distinct().collect(toImmutableSet()));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ImmutableSet<T> after(Stream<T> stream) {
      return stream.collect(toImmutableSet());
    }
  }

  /** Prefer {@link SetView#immutableCopy()} over the more verbose alternative. */
  static final class ImmutableSetCopyOfSetView<T> {
    @BeforeTemplate
    ImmutableSet<T> before(SetView<T> set) {
      return ImmutableSet.copyOf(set);
    }

    @AfterTemplate
    ImmutableSet<T> after(SetView<T> set) {
      return set.immutableCopy();
    }
  }

  /**
   * Prefer {@link ImmutableSet#of()} over more contrived alternatives or alternatives that don't
   * communicate the immutability of the resulting set at the type level.
   */
  // XXX: The `Stream` variant may be too contrived to warrant inclusion. Review its usage if/when
  // this and similar Refaster rules are replaced with an Error Prone check.
  static final class ImmutableSetOf<T> {
    @BeforeTemplate
    Set<T> before() {
      return Refaster.anyOf(
          ImmutableSet.<T>builder().build(),
          Stream.<T>empty().collect(toImmutableSet()),
          emptySet(),
          Set.of());
    }

    @AfterTemplate
    ImmutableSet<T> after() {
      return ImmutableSet.of();
    }
  }

  /**
   * Prefer {@link ImmutableSet#of(Object)} over more contrived alternatives or alternatives that
   * don't communicate the immutability of the resulting set at the type level.
   */
  // XXX: Note that the replacement of `Collections#singleton` is incorrect for nullable elements.
  static final class ImmutableSetOf1<T> {
    @BeforeTemplate
    Set<T> before(T e1) {
      return Refaster.anyOf(ImmutableSet.<T>builder().add(e1).build(), singleton(e1), Set.of(e1));
    }

    @AfterTemplate
    ImmutableSet<T> after(T e1) {
      return ImmutableSet.of(e1);
    }
  }

  /**
   * Prefer {@link ImmutableSet#of(Object, Object)} over alternatives that don't communicate the
   * immutability of the resulting set at the type level.
   */
  // XXX: Consider writing an Error Prone check that also flags straightforward
  // `ImmutableSet.builder()` usages.
  static final class ImmutableSetOf2<T> {
    @BeforeTemplate
    Set<T> before(T e1, T e2) {
      return Set.of(e1, e2);
    }

    @AfterTemplate
    ImmutableSet<T> after(T e1, T e2) {
      return ImmutableSet.of(e1, e2);
    }
  }

  /**
   * Prefer {@link ImmutableSet#of(Object, Object, Object)} over alternatives that don't communicate
   * the immutability of the resulting set at the type level.
   */
  // XXX: Consider writing an Error Prone check that also flags straightforward
  // `ImmutableSet.builder()` usages.
  static final class ImmutableSetOf3<T> {
    @BeforeTemplate
    Set<T> before(T e1, T e2, T e3) {
      return Set.of(e1, e2, e3);
    }

    @AfterTemplate
    ImmutableSet<T> after(T e1, T e2, T e3) {
      return ImmutableSet.of(e1, e2, e3);
    }
  }

  /**
   * Prefer {@link ImmutableSet#of(Object, Object, Object, Object)} over alternatives that don't
   * communicate the immutability of the resulting set at the type level.
   */
  // XXX: Consider writing an Error Prone check that also flags straightforward
  // `ImmutableSet.builder()` usages.
  static final class ImmutableSetOf4<T> {
    @BeforeTemplate
    Set<T> before(T e1, T e2, T e3, T e4) {
      return Set.of(e1, e2, e3, e4);
    }

    @AfterTemplate
    ImmutableSet<T> after(T e1, T e2, T e3, T e4) {
      return ImmutableSet.of(e1, e2, e3, e4);
    }
  }

  /**
   * Prefer {@link ImmutableSet#of(Object, Object, Object, Object, Object)} over alternatives that
   * don't communicate the immutability of the resulting set at the type level.
   */
  // XXX: Consider writing an Error Prone check that also flags straightforward
  // `ImmutableSet.builder()` usages.
  static final class ImmutableSetOf5<T> {
    @BeforeTemplate
    Set<T> before(T e1, T e2, T e3, T e4, T e5) {
      return Set.of(e1, e2, e3, e4, e5);
    }

    @AfterTemplate
    ImmutableSet<T> after(T e1, T e2, T e3, T e4, T e5) {
      return ImmutableSet.of(e1, e2, e3, e4, e5);
    }
  }

  /**
   * Prefer an immutable copy of {@link Sets#difference(Set, Set)} over more contrived alternatives.
   */
  static final class SetsDifference<S, T> {
    @BeforeTemplate
    ImmutableSet<S> before(Set<S> set1, Set<T> set2) {
      return set1.stream()
          .filter(Refaster.anyOf(not(set2::contains), e -> !set2.contains(e)))
          .collect(toImmutableSet());
    }

    @AfterTemplate
    ImmutableSet<S> after(Set<S> set1, Set<T> set2) {
      return Sets.difference(set1, set2).immutableCopy();
    }
  }

  /**
   * Prefer an immutable copy of {@link Sets#difference(Set, Set)} over more contrived alternatives.
   */
  static final class SetsDifferenceMap<T, K, V> {
    @BeforeTemplate
    ImmutableSet<T> before(Set<T> set, Map<K, V> map) {
      return set.stream()
          .filter(Refaster.anyOf(not(map::containsKey), e -> !map.containsKey(e)))
          .collect(toImmutableSet());
    }

    @AfterTemplate
    ImmutableSet<K> after(Set<K> set, Map<K, V> map) {
      return Sets.difference(set, map.keySet()).immutableCopy();
    }
  }

  /**
   * Prefer an immutable copy of {@link Sets#difference(Set, Set)} over more contrived alternatives.
   */
  static final class SetsDifferenceMultimap<T, K, V> {
    @BeforeTemplate
    ImmutableSet<T> before(Set<T> set, Multimap<K, V> multimap) {
      return set.stream()
          .filter(Refaster.anyOf(not(multimap::containsKey), e -> !multimap.containsKey(e)))
          .collect(toImmutableSet());
    }

    @AfterTemplate
    ImmutableSet<T> after(Set<T> set, Multimap<K, V> multimap) {
      return Sets.difference(set, multimap.keySet()).immutableCopy();
    }
  }

  /**
   * Prefer an immutable copy of {@link Sets#intersection(Set, Set)} over more contrived
   * alternatives.
   */
  static final class SetsIntersection<S, T> {
    @BeforeTemplate
    ImmutableSet<S> before(Set<S> set1, Set<T> set2) {
      return set1.stream().filter(set2::contains).collect(toImmutableSet());
    }

    @AfterTemplate
    ImmutableSet<S> after(Set<S> set1, Set<T> set2) {
      return Sets.intersection(set1, set2).immutableCopy();
    }
  }

  /**
   * Prefer an immutable copy of {@link Sets#intersection(Set, Set)} over more contrived
   * alternatives.
   */
  static final class SetsIntersectionMap<T, K, V> {
    @BeforeTemplate
    ImmutableSet<T> before(Set<T> set, Map<K, V> map) {
      return set.stream().filter(map::containsKey).collect(toImmutableSet());
    }

    @AfterTemplate
    ImmutableSet<T> after(Set<T> set, Map<K, V> map) {
      return Sets.intersection(set, map.keySet()).immutableCopy();
    }
  }

  /**
   * Prefer an immutable copy of {@link Sets#intersection(Set, Set)} over more contrived
   * alternatives.
   */
  static final class SetsIntersectionMultimap<T, K, V> {
    @BeforeTemplate
    ImmutableSet<T> before(Set<T> set, Multimap<K, V> multimap) {
      return set.stream().filter(multimap::containsKey).collect(toImmutableSet());
    }

    @AfterTemplate
    ImmutableSet<T> after(Set<T> set, Multimap<K, V> multimap) {
      return Sets.intersection(set, multimap.keySet()).immutableCopy();
    }
  }

  /** Prefer an immutable copy of {@link Sets#union(Set, Set)} over more contrived alternatives. */
  static final class SetsUnion<S, T extends S, U extends S> {
    @BeforeTemplate
    ImmutableSet<S> before(Set<T> set1, Set<U> set2) {
      return Stream.concat(set1.stream(), set2.stream()).collect(toImmutableSet());
    }

    @AfterTemplate
    ImmutableSet<S> after(Set<T> set1, Set<U> set2) {
      return Sets.union(set1, set2).immutableCopy();
    }
  }
}
