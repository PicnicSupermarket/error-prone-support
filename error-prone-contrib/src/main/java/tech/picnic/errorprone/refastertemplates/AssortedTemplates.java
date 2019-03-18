package tech.picnic.errorprone.refastertemplates;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
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
