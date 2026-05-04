package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static java.util.Objects.checkIndex;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Iterator;
import java.util.Objects;
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

  /** Prefer {@link Objects#checkIndex(int, int)} over non-JDK alternatives. */
  static final class CheckIndexExpression {
    @BeforeTemplate
    int before(int index, int length) {
      return checkElementIndex(index, length);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    int after(int index, int length) {
      return checkIndex(index, length);
    }
  }

  /**
   * Prefer {@link Objects#checkIndex(int, int)} over less explicit alternatives.
   *
   * <p>If a custom error message is desired, consider using Guava's {@link
   * com.google.common.base.Preconditions#checkElementIndex(int, int, String)}.
   */
  static final class CheckIndexBlock {
    @BeforeTemplate
    void before(int index, int length) {
      if (index < 0 || index >= length) {
        throw new IndexOutOfBoundsException();
      }
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(int index, int length) {
      checkIndex(index, length);
    }
  }

  /** Prefer {@link Iterators#getNext(Iterator, Object)} over more contrived alternatives. */
  static final class IteratorsGetNext<T> {
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

  /** Prefer {@code firstTest || secondTest} over more contrived alternatives. */
  // XXX: This rule captures only the simplest case. `@AlsoNegation` doesn't help. Consider
  // contributing a Refaster patch, which handles the negation in the `@BeforeTemplate` more
  // intelligently.
  static final class Or {
    @BeforeTemplate
    @SuppressWarnings("java:S2589" /* This violation will be rewritten. */)
    boolean before(boolean b1, boolean b2) {
      return b1 || (!b1 && b2);
    }

    @AfterTemplate
    boolean after(boolean b1, boolean b2) {
      return b1 || b2;
    }
  }

  /**
   * Prefer {@link Stream#generate(java.util.function.Supplier)} over more contrived alternatives.
   */
  static final class StreamGenerate<T> {
    @BeforeTemplate
    Stream<T> before(T object) {
      return Streams.stream(Iterables.cycle(object));
    }

    @AfterTemplate
    Stream<T> after(T object) {
      return Stream.generate(() -> object);
    }
  }

  /** Prefer {@link Iterables#isEmpty(Iterable)} over more contrived alternatives. */
  static final class IterablesIsEmpty<T> {
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
  static final class SplitterSplitToStream {
    @BeforeTemplate
    Stream<String> before(Splitter splitter, CharSequence sequence) {
      return Refaster.anyOf(
          Streams.stream(splitter.split(sequence)), splitter.splitToList(sequence).stream());
    }

    @AfterTemplate
    Stream<String> after(Splitter splitter, CharSequence sequence) {
      return splitter.splitToStream(sequence);
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
