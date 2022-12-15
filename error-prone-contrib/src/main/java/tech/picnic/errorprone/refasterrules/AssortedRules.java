package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.toImmutableEnumSet;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Collections.disjoint;
import static java.util.Objects.checkIndex;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/**
 * Assorted Refaster rules that do not (yet) belong in one of the other classes with more topical
 * Refaster rules.
 */
@OnlineDocumentation
final class AssortedRules {
  private AssortedRules() {}

  /** Prefer {@link Objects#checkIndex(int, int)} over the Guava alternative. */
  static final class CheckIndex {
    @BeforeTemplate
    int before(int index, int size) {
      return checkElementIndex(index, size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    int after(int index, int size) {
      return checkIndex(index, size);
    }
  }

  /**
   * Prefer {@link Objects#checkIndex(int, int)} over less descriptive or more verbose alternatives.
   *
   * <p>If a custom error message is desired, consider using Guava's {@link
   * com.google.common.base.Preconditions#checkElementIndex(int, int, String)}.
   */
  static final class CheckIndexConditional {
    @BeforeTemplate
    void before(int index, int size) {
      if (index < 0 || index >= size) {
        throw new IndexOutOfBoundsException();
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(int index, int size) {
      checkIndex(index, size);
    }
  }

  /**
   * Use {@link Sets#toImmutableEnumSet()} when possible, as it is more efficient than {@link
   * ImmutableSet#toImmutableSet()} and produces a more compact object.
   *
   * <p><strong>Warning:</strong> this rewrite rule is not completely behavior preserving: while the
   * original code produces a set that iterates over the elements in encounter order, the
   * replacement code iterates over the elements in enum definition order.
   */
  // XXX: ^ Consider emitting a comment warning about this fact?
  static final class StreamToImmutableEnumSet<T extends Enum<T>> {
    @BeforeTemplate
    ImmutableSet<T> before(Stream<T> stream) {
      return stream.collect(toImmutableSet());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ImmutableSet<T> after(Stream<T> stream) {
      return stream.collect(toImmutableEnumSet());
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

    @AfterTemplate
    @Nullable T after(Iterator<T> iterator, T defaultValue) {
      return Iterators.getNext(iterator, defaultValue);
    }
  }

  /** Don't unnecessarily repeat boolean expressions. */
  // XXX: This rule captures only the simplest case. `@AlsoNegation` doesn't help. Consider
  // contributing a Refaster patch, which handles the negation in the `@BeforeTemplate` more
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

  /**
   * Prefer {@link Collections#disjoint(Collection, Collection)} over more contrived alternatives.
   */
  static final class DisjointSets<T> {
    @BeforeTemplate
    boolean before(Set<T> set1, Set<T> set2) {
      return Sets.intersection(set1, set2).isEmpty();
    }

    @BeforeTemplate
    boolean before2(Set<T> set1, Set<T> set2) {
      return set1.stream().noneMatch(set2::contains);
    }

    @AfterTemplate
    boolean after(Set<T> set1, Set<T> set2) {
      return disjoint(set1, set2);
    }
  }

  /**
   * Don't unnecessarily copy collections before passing them to {@link
   * Collections#disjoint(Collection, Collection)}.
   */
  // XXX: Other copy operations could be elided too, but these are most common after application of
  // the `DisjointSets` rule defined above. If we ever introduce a generic "makes a copy" stand-in,
  // use it here.
  static final class DisjointCollections<T> {
    @BeforeTemplate
    boolean before(Collection<T> collection1, Collection<T> collection2) {
      return Refaster.anyOf(
          disjoint(ImmutableSet.copyOf(collection1), collection2),
          disjoint(new HashSet<>(collection1), collection2),
          disjoint(collection1, ImmutableSet.copyOf(collection2)),
          disjoint(collection1, new HashSet<>(collection2)));
    }

    @AfterTemplate
    boolean after(Collection<T> collection1, Collection<T> collection2) {
      return disjoint(collection1, collection2);
    }
  }

  /** Prefer {@link Iterables#isEmpty(Iterable)} over more contrived alternatives. */
  static final class IterableIsEmpty<T> {
    @BeforeTemplate
    boolean before(Iterable<T> iterable) {
      return !iterable.iterator().hasNext();
    }

    @AfterTemplate
    boolean after(Iterable<T> iterable) {
      return Iterables.isEmpty(iterable);
    }
  }

  /** Prefer {@link Splitter#splitToStream(CharSequence)} over less efficient alternatives. */
  static final class SplitToStream {
    @BeforeTemplate
    Stream<String> before(Splitter splitter, CharSequence charSequence) {
      return Refaster.anyOf(
          Streams.stream(splitter.split(charSequence)),
          splitter.splitToList(charSequence).stream());
    }

    @AfterTemplate
    Stream<String> after(Splitter splitter, CharSequence charSequence) {
      return splitter.splitToStream(charSequence);
    }
  }

  // /**
  //  * Don't unnecessarily pass a method reference to {@link Supplier#get()} or wrap this method
  //  * in a lambda expression.
  //  */
  // // XXX: This rule rewrites both expressions and statements (good), but does not ensure that the
  // // actually `anyStatement` accepts a `Supplier<T>`. For example, it will also match if the
  // // statement requires a `com.google.common.base.Supplier` rather than a
  // // `java.util.function.Supplier`. Investigate how we can improve Refaster matching support.
  // abstract static class SupplierAsSupplier<T> {
  //   @Placeholder
  //   abstract void anyStatement(Supplier<T> supplier);
  //
  //   @BeforeTemplate
  //   void before(Supplier<T> supplier) {
  //     anyStatement(() -> supplier.get());
  //   }
  //
  //   @AfterTemplate
  //   void after(Supplier<T> supplier) {
  //     anyStatement(supplier);
  //   }
  // }
}
