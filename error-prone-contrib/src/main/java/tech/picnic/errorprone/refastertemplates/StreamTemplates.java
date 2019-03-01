package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMultiset.toImmutableMultiset;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSortedMultiset.toImmutableSortedMultiset;
import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link Stream}s. */
final class StreamTemplates {
  private StreamTemplates() {}

  /** Don't unnecessarily call {@link Streams#concat(Stream...)}. */
  // XXX: There are int, long and double variants to this rule. Probably not worth the hassle.
  static final class ConcatOneStream<T> {
    @BeforeTemplate
    Stream<T> before(Stream<T> stream) {
      return Streams.concat(stream);
    }

    @AfterTemplate
    Stream<T> after(Stream<T> stream) {
      return stream;
    }
  }

  /** Prefer {@link Stream#concat(Stream, Stream)} over the Guava alternative. */
  // XXX: There are int, long and double variants to this rule. Worth the hassle?
  static final class ConcatTwoStreams<T> {
    @BeforeTemplate
    Stream<T> before(Stream<T> s1, Stream<T> s2) {
      return Streams.concat(s1, s2);
    }

    @AfterTemplate
    Stream<T> after(Stream<T> s1, Stream<T> s2) {
      return Stream.concat(s1, s2);
    }
  }

  /** Prefer {@link ImmutableList#toImmutableList()} over the more verbose alternative. */
  static final class StreamToImmutableList<T> {
    @BeforeTemplate
    ImmutableList<T> before(Stream<T> stream) {
      return ImmutableList.copyOf(stream.iterator());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableList<T> after(Stream<T> stream) {
      return stream.collect(toImmutableList());
    }
  }

  /** Prefer {@link ImmutableSet#toImmutableSet()} over the more verbose alternative. */
  static final class StreamToImmutableSet<T> {
    @BeforeTemplate
    ImmutableSet<T> before(Stream<T> stream) {
      return ImmutableSet.copyOf(stream.iterator());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableSet<T> after(Stream<T> stream) {
      return stream.collect(toImmutableSet());
    }
  }

  /**
   * Prefer {@link ImmutableSortedSet#toImmutableSortedSet(java.util.Comparator)} over the less
   * idiomatic alternative.
   */
  static final class StreamToImmutableSortedSet<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedSet<T> before(Stream<T> stream) {
      return ImmutableSortedSet.copyOf(stream.iterator());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableSortedSet<T> after(Stream<T> stream) {
      return stream.collect(toImmutableSortedSet(naturalOrder()));
    }
  }

  /** Prefer {@link ImmutableMultiset#toImmutableMultiset()} over the more verbose alternative. */
  static final class StreamToImmutableMultiset<T> {
    @BeforeTemplate
    ImmutableMultiset<T> before(Stream<T> stream) {
      return ImmutableMultiset.copyOf(stream.iterator());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableMultiset<T> after(Stream<T> stream) {
      return stream.collect(toImmutableMultiset());
    }
  }

  /**
   * Prefer {@link ImmutableSortedMultiset#toImmutableSortedMultiset(java.util.Comparator)} over the
   * less idiomatic alternative.
   */
  static final class StreamToImmutableSortedMultiset<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedMultiset<T> before(Stream<T> stream) {
      return ImmutableSortedMultiset.copyOf(stream.iterator());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableSortedMultiset<T> after(Stream<T> stream) {
      return stream.collect(toImmutableSortedMultiset(naturalOrder()));
    }
  }
}
