package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link ImmutableMultiset}s. */
final class ImmutableMultisetTemplates {
  private ImmutableMultisetTemplates() {}

  /** Prefer {@link ImmutableMultiset#builder()} over the associated constructor. */
  // XXX: This drops generic type information, sometimes leading to non-compilable code. Anything
  // we can do about that?
  static final class ImmutableMultisetBuilder<T> {
    @BeforeTemplate
    ImmutableMultiset.Builder<T> before() {
      return new ImmutableMultiset.Builder<>();
    }

    @AfterTemplate
    ImmutableMultiset.Builder<T> after() {
      return ImmutableMultiset.builder();
    }
  }

  /** Prefer {@link ImmutableMultiset#of()} over more contrived alternatives. */
  static final class EmptyImmutableMultiset<T> {
    @BeforeTemplate
    ImmutableMultiset<T> before() {
      return Refaster.anyOf(
          ImmutableMultiset.<T>builder().build(), Stream.<T>empty().collect(toImmutableMultiset()));
    }

    @AfterTemplate
    ImmutableMultiset<T> after() {
      return ImmutableMultiset.of();
    }
  }

  /**
   * Prefer {@link ImmutableMultiset#copyOf(Iterable)} and variants over more contrived
   * alternatives.
   */
  static final class IterableToImmutableMultiset<T> {
    // XXX: Drop the inner `Refaster.anyOf` if/when we introduce a rule to choose between one and
    // the other.
    @BeforeTemplate
    ImmutableMultiset<T> before(T[] iterable) {
      return Refaster.anyOf(
          ImmutableMultiset.<T>builder().add(iterable).build(),
          Refaster.anyOf(Stream.of(iterable), Arrays.stream(iterable))
              .collect(toImmutableMultiset()));
    }

    @BeforeTemplate
    ImmutableMultiset<T> before(Iterator<T> iterable) {
      return Refaster.anyOf(
          ImmutableMultiset.<T>builder().addAll(iterable).build(),
          Streams.stream(iterable).collect(toImmutableMultiset()));
    }

    @BeforeTemplate
    ImmutableMultiset<T> before(Iterable<T> iterable) {
      return Refaster.anyOf(
          ImmutableMultiset.<T>builder().addAll(iterable).build(),
          Streams.stream(iterable).collect(toImmutableMultiset()));
    }

    @BeforeTemplate
    ImmutableMultiset<T> before(Collection<T> iterable) {
      return iterable.stream().collect(toImmutableMultiset());
    }

    @AfterTemplate
    ImmutableMultiset<T> after(Iterable<T> iterable) {
      return ImmutableMultiset.copyOf(iterable);
    }
  }

  /** Don't unnecessarily copy an {@link ImmutableMultiset}. */
  static final class ImmutableMultisetCopyOfImmutableMultiset<T> {
    @BeforeTemplate
    ImmutableMultiset<T> before(ImmutableMultiset<T> multiset) {
      return ImmutableMultiset.copyOf(multiset);
    }

    @AfterTemplate
    ImmutableMultiset<T> after(ImmutableMultiset<T> multiset) {
      return multiset;
    }
  }
}
