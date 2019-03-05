package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
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

  /** Prefer {@link ImmutableSet#copyOf(Iterable)} and variants over more contrived alternatives. */
  static final class IterableToImmutableSet<T> {
    // XXX: Drop the inner `Refaster.anyOf` if/when we introduce a rule to choose between one and
    // the other.
    @BeforeTemplate
    ImmutableSet<T> before(T[] iterable) {
      return Refaster.anyOf(
          ImmutableSet.<T>builder().add(iterable).build(),
          Refaster.anyOf(Stream.of(iterable), Arrays.stream(iterable)).collect(toImmutableSet()));
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
