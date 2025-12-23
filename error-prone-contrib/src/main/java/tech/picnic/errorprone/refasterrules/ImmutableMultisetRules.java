package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Collections.singleton;

import com.google.common.collect.ImmutableMultiset;
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
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link ImmutableMultiset}s. */
@OnlineDocumentation
final class ImmutableMultisetRules {
  private ImmutableMultisetRules() {}

  /** Prefer {@link ImmutableMultiset#builder()} over the associated constructor. */
  // XXX: This rule may drop generic type information, leading to non-compilable code.
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
      return ImmutableMultiset.copyOf(stream.iterator());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ImmutableMultiset<T> after(Stream<T> stream) {
      return stream.collect(toImmutableMultiset());
    }
  }

  /**
   * Prefer {@link ImmutableMultiset.Builder#add(Object)} over {@link
   * ImmutableMultiset.Builder#addAll(Iterable)} when adding a single element.
   */
  static final class ImmutableMultisetBuilderAddOverAddAllSingleElement<T> {
    @BeforeTemplate
    ImmutableMultiset.Builder<T> before(ImmutableMultiset.Builder<T> builder, T element) {
      return Refaster.anyOf(
          builder.addAll(ImmutableMultiset.of(element)),
          builder.addAll(singleton(element)),
          builder.addAll(Set.of(element)));
    }

    @AfterTemplate
    ImmutableMultiset.Builder<T> after(ImmutableMultiset.Builder<T> builder, T element) {
      return builder.add(element);
    }
  }
}
