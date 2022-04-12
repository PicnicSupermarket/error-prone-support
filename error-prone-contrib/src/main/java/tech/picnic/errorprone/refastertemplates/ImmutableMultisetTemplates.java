package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
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
    @BeforeTemplate
    ImmutableMultiset<T> before(T[] iterable) {
      return Refaster.anyOf(
          ImmutableMultiset.<T>builder().add(iterable).build(),
          Arrays.stream(iterable).collect(toImmutableMultiset()));
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

  /** Prefer {@link ImmutableMultiset#toImmutableMultiset()} over less idiomatic alternatives. */
  static final class StreamToImmutableMultiset<T> {
    @BeforeTemplate
    ImmutableMultiset<T> before(Stream<T> stream) {
      return Refaster.anyOf(
          ImmutableMultiset.copyOf(stream.iterator()),
          stream.collect(collectingAndThen(toList(), ImmutableMultiset::copyOf)));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ImmutableMultiset<T> after(Stream<T> stream) {
      return stream.collect(toImmutableMultiset());
    }
  }
}
