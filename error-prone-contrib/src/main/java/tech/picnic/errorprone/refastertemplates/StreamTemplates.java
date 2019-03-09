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
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link Stream}s. */
final class StreamTemplates {
  private StreamTemplates() {}

  /** Prefer {@link Stream#ofNullable(Object)} over more contrived alternatives. */
  static final class StreamOfNullable<T> {
    @BeforeTemplate
    Stream<T> before(T object) {
      return Refaster.anyOf(
          Stream.of(object).filter(Objects::nonNull), Optional.ofNullable(object).stream());
    }

    @AfterTemplate
    Stream<T> after(T object) {
      return Stream.ofNullable(object);
    }
  }

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

  /** Avoid unnecessary nesting of {@link Stream#filter(Predicate)} operations. */
  // XXX: There are int, long and double variants to this rule. Worth the hassle?
  abstract static class FilterOuterStreamAfterFlatMap<T, S> {
    @Placeholder
    abstract Stream<S> toStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Stream<S> before(Stream<T> stream, Predicate<? super S> predicate) {
      return stream.flatMap(v -> toStreamFunction(v).filter(predicate));
    }

    @AfterTemplate
    Stream<S> after(Stream<T> stream, Predicate<? super S> predicate) {
      return stream.flatMap(v -> toStreamFunction(v)).filter(predicate);
    }
  }

  /** Avoid unnecessary nesting of {@link Stream#map(Function)} operations. */
  // XXX: There are int, long and double variants to this rule. Worth the hassle?
  abstract static class MapOuterStreamAfterFlatMap<T, S, R> {
    @Placeholder
    abstract Stream<S> toStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Stream<R> before(Stream<T> stream, Function<? super S, ? extends R> function) {
      return stream.flatMap(v -> toStreamFunction(v).map(function));
    }

    @AfterTemplate
    Stream<R> after(Stream<T> stream, Function<? super S, ? extends R> function) {
      return stream.flatMap(v -> toStreamFunction(v)).map(function);
    }
  }

  /** Avoid unnecessary nesting of {@link Stream#flatMap(Function)} operations. */
  // XXX: There are int, long and double variants to this rule. Worth the hassle?
  abstract static class FlatMapOuterStreamAfterFlatMap<T, S, R> {
    @Placeholder
    abstract Stream<S> toStreamFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Stream<R> before(
        Stream<T> stream, Function<? super S, ? extends Stream<? extends R>> function) {
      return stream.flatMap(v -> toStreamFunction(v).flatMap(function));
    }

    @AfterTemplate
    Stream<R> after(Stream<T> stream, Function<? super S, ? extends Stream<? extends R>> function) {
      return stream.flatMap(v -> toStreamFunction(v)).flatMap(function);
    }
  }

  /** Prefer {@link ImmutableList#toImmutableList()} over the more verbose alternative. */
  // XXX: Once the code base has been sufficiently cleaned up, we might want to also rewrite
  // `Collectors.toList(`), with the caveat that it allows mutation (though this cannot be relied
  // upon) as well as nulls. Another option is to explicitly rewrite those variants to
  // `Collectors.toSet(ArrayList::new)`.
  static final class StreamToImmutableList<T> {
    @BeforeTemplate
    ImmutableList<T> before(Stream<T> stream) {
      return Refaster.anyOf(
          ImmutableList.copyOf(stream.iterator()), ImmutableList.copyOf(stream::iterator));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableList<T> after(Stream<T> stream) {
      return stream.collect(toImmutableList());
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
          ImmutableSet.copyOf(stream::iterator),
          stream.distinct().collect(toImmutableSet()));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableSet<T> after(Stream<T> stream) {
      return stream.collect(toImmutableSet());
    }
  }

  /**
   * Prefer {@link ImmutableSortedSet#toImmutableSortedSet(java.util.Comparator)} over less
   * idiomatic alternatives.
   */
  static final class StreamToImmutableSortedSet<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedSet<T> before(Stream<T> stream) {
      return Refaster.anyOf(
          ImmutableSortedSet.copyOf(stream.iterator()),
          ImmutableSortedSet.copyOf(stream::iterator));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableSortedSet<T> after(Stream<T> stream) {
      return stream.collect(toImmutableSortedSet(naturalOrder()));
    }
  }

  /** Prefer {@link ImmutableMultiset#toImmutableMultiset()} over less idiomatic alternatives. */
  static final class StreamToImmutableMultiset<T> {
    @BeforeTemplate
    ImmutableMultiset<T> before(Stream<T> stream) {
      return Refaster.anyOf(
          ImmutableMultiset.copyOf(stream.iterator()), ImmutableMultiset.copyOf(stream::iterator));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableMultiset<T> after(Stream<T> stream) {
      return stream.collect(toImmutableMultiset());
    }
  }

  /**
   * Prefer {@link ImmutableSortedMultiset#toImmutableSortedMultiset(java.util.Comparator)} over
   * less idiomatic alternatives.
   */
  static final class StreamToImmutableSortedMultiset<T extends Comparable<? super T>> {
    @BeforeTemplate
    ImmutableSortedMultiset<T> before(Stream<T> stream) {
      return Refaster.anyOf(
          ImmutableSortedMultiset.copyOf(stream.iterator()),
          ImmutableSortedMultiset.copyOf(stream::iterator));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    ImmutableSortedMultiset<T> after(Stream<T> stream) {
      return stream.collect(toImmutableSortedMultiset(naturalOrder()));
    }
  }
}
