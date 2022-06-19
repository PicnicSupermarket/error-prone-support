package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link ImmutableSet}s. */
final class ImmutableSetTemplates {
  private ImmutableSetTemplates() {}

  /** Prefer {@link ImmutableSet#builder()} over the associated constructor. */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. Anything
  // we can do about that?
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
  // XXX: Once the code base has been sufficiently cleaned up, we might want to also rewrite
  // `Collectors.toSet(`), with the caveat that it allows mutation (though this cannot be relied
  // upon) as well as nulls. Another option is to explicitly rewrite those variants to
  // `Collectors.toSet(HashSet::new)`.
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
  // this and similar Refaster templates are replaced with an Error Prone check.
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
  // XXX: Consider writing an Error Prone check which also flags straightforward
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
  // XXX: Consider writing an Error Prone check which also flags straightforward
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
  // XXX: Consider writing an Error Prone check which also flags straightforward
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
  // XXX: Consider writing an Error Prone check which also flags straightforward
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
}
