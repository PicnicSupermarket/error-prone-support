package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
      return ImmutableSet.<T>builder();
    }
  }

  /** Prefer {@link ImmutableSet#of()} over more contrived alternatives. */
  static final class EmptyImmutableSet<T> {
    @BeforeTemplate
    ImmutableSet<T> before() {
      return Refaster.anyOf(
          ImmutableSet.<T>builder().build(), Stream.<T>empty().collect(toImmutableSet()));
    }

    @AfterTemplate
    ImmutableSet<T> after() {
      return ImmutableSet.of();
    }
  }

  /**
   * Prefer {@link ImmutableSet#of(Object)} over alternatives that don't communicate the
   * immutability of the resulting set at the type level.
   */
  // XXX: Note that this rewrite rule is incorrect for nullable elements.
  static final class SingletonImmutableSet<T> {
    @BeforeTemplate
    Set<T> before(T element) {
      return Collections.singleton(element);
    }

    @AfterTemplate
    ImmutableSet<T> after(T element) {
      return ImmutableSet.of(element);
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
          ImmutableSet.copyOf(stream.iterator()),
          stream.distinct().collect(toImmutableSet()),
          stream.collect(collectingAndThen(toList(), ImmutableSet::copyOf)),
          stream.collect(collectingAndThen(toSet(), ImmutableSet::copyOf)));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableSet<T> after(Stream<T> stream) {
      return stream.collect(toImmutableSet());
    }
  }

  /** Don't unnecessarily copy an {@link ImmutableSet}. */
  static final class ImmutableSetCopyOfImmutableSet<T> {
    @BeforeTemplate
    ImmutableSet<T> before(ImmutableSet<T> set) {
      return ImmutableSet.copyOf(set);
    }

    @AfterTemplate
    ImmutableSet<T> after(ImmutableSet<T> set) {
      return set;
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
}
