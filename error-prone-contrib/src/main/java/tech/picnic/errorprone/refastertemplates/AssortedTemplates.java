package tech.picnic.errorprone.refastertemplates;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.toImmutableEnumSet;
import static java.util.Objects.checkIndex;

import com.google.common.base.Splitter;
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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
      return checkElementIndex(index, size);
    }

    @AfterTemplate
    int after(int index, int size) {
      return checkIndex(index, size);
    }
  }

  // XXX: We could add a rule for `new EnumMap(Map<K, ? extends V> m)`, but that constructor does
  // not allow an empty non-EnumMap to be provided.
  static final class CreateEnumMap<K extends Enum<K>, V> {
    @BeforeTemplate
    Map<K, V> before() {
      return new HashMap<>();
    }

    @AfterTemplate
    Map<K, V> after() {
      return new EnumMap<>(Refaster.<K>clazz());
    }
  }

  static final class MapGetOrNull<K, V, L> {
    @BeforeTemplate
    @Nullable
    V before(Map<K, V> map, L key) {
      return map.getOrDefault(key, null);
    }

    @AfterTemplate
    @Nullable
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
  // XXX: ^ Consider emitting a comment warning about this fact?
  static final class StreamToImmutableEnumSet<T extends Enum<T>> {
    @BeforeTemplate
    ImmutableSet<T> before(Stream<T> stream) {
      return stream.collect(toImmutableSet());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
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
    @Nullable
    T after(Iterator<T> iterator, T defaultValue) {
      return Iterators.getNext(iterator, defaultValue);
    }
  }

  /** Don't unnecessarily repeat boolean expressions. */
  // XXX: This template captures only the simplest case. `@AlsoNegation` doesn't help. Consider
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
      return Collections.disjoint(set1, set2);
    }
  }

  /**
   * Don't unnecessarily copy collections before passing them to {@link
   * Collections#disjoint(Collection, Collection)}.
   */
  // XXX: Other copy operations could be elided too, but these are most common after application of
  // the `DisjointSets` template defined above. If we ever introduce a generic "makes a copy"
  // stand-in, use it here.
  static final class DisjointCollections<T> {
    @BeforeTemplate
    boolean before(Collection<T> collection1, Collection<T> collection2) {
      return Refaster.anyOf(
          Collections.disjoint(new HashSet<>(collection1), collection2),
          Collections.disjoint(collection1, new HashSet<>(collection2)));
    }

    @AfterTemplate
    boolean after(Collection<T> collection1, Collection<T> collection2) {
      return Collections.disjoint(collection1, collection2);
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

  /** Don't unnecessarily use {@link Map#entrySet()}. */
  static final class MapKeyStream<K, V> {
    @BeforeTemplate
    Stream<K> before(Map<K, V> map) {
      return map.entrySet().stream().map(Map.Entry::getKey);
    }

    @AfterTemplate
    Stream<K> after(Map<K, V> map) {
      return map.keySet().stream();
    }
  }

  /** Don't unnecessarily use {@link Map#entrySet()}. */
  static final class MapValueStream<K, V> {
    @BeforeTemplate
    Stream<V> before(Map<K, V> map) {
      return map.entrySet().stream().map(Map.Entry::getValue);
    }

    @AfterTemplate
    Stream<V> after(Map<K, V> map) {
      return map.values().stream();
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
