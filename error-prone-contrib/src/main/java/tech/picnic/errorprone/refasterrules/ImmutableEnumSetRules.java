package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Sets.toImmutableEnumSet;
import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/**
 * Refaster rules related to expressions dealing with {@code
 * com.google.common.collect.ImmutableEnumSet}s.
 */
// XXX: Some of the rules defined here impact iteration order. That's a rather subtle change. Should
// we emit a comment warning about this fact? (This may produce a lot of noise. A bug checker could
// in some cases determine whether iteration order is important.)
// XXX: Consider replacing the `SetsImmutableEnumSet[N]` Refaster rules with a bug checker, such
// that call to `ImmutableSet#of(Object, Object, Object, Object, Object, Object, Object[])` with
// enum-typed values can also be rewritten.
@OnlineDocumentation
final class ImmutableEnumSetRules {
  private ImmutableEnumSetRules() {}

  /**
   * Prefer {@link Sets#immutableEnumSet(Iterable)} for enum collections to take advantage of the
   * internally used {@link EnumSet}.
   *
   * <p><strong>Warning:</strong> this rule is not completely behavior preserving: while the
   * original code produces a set that iterates over its elements in the same order as the input
   * {@link Iterable}, the replacement code iterates over the elements in enum definition order.
   */
  static final class SetsImmutableEnumSetIterable<T extends Enum<T>> {
    @BeforeTemplate
    ImmutableSet<T> before(Iterable<T> elements) {
      return ImmutableSet.copyOf(elements);
    }

    @BeforeTemplate
    ImmutableSet<T> before(Collection<T> elements) {
      return ImmutableSet.copyOf(elements);
    }

    @AfterTemplate
    ImmutableSet<T> after(Iterable<T> elements) {
      return Sets.immutableEnumSet(elements);
    }
  }

  /**
   * Prefer {@link Sets#immutableEnumSet(Iterable)} for enum collections to take advantage of the
   * internally used {@link EnumSet}.
   *
   * <p><strong>Warning:</strong> this rule is not completely behavior preserving: while the
   * original code produces a set that iterates over its elements in the same order as defined in
   * the array, the replacement code iterates over the elements in enum definition order.
   */
  static final class SetsImmutableEnumSetArraysAsList<T extends Enum<T>> {
    @BeforeTemplate
    ImmutableSet<T> before(T[] elements) {
      return ImmutableSet.copyOf(elements);
    }

    @AfterTemplate
    ImmutableSet<T> after(T[] elements) {
      return Sets.immutableEnumSet(Arrays.asList(elements));
    }
  }

  /**
   * Prefer {@link Sets#immutableEnumSet(Enum, Enum[])} for enum collections to take advantage of
   * the internally used {@link EnumSet}.
   */
  static final class SetsImmutableEnumSet1<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("SetsImmutableEnumSetIterable" /* This is a more specific template. */)
    ImmutableSet<T> before(T e1) {
      return Refaster.anyOf(ImmutableSet.of(e1), ImmutableSet.copyOf(EnumSet.of(e1)));
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    ImmutableSet<T> after(T e1) {
      return Sets.immutableEnumSet(e1);
    }
  }

  /**
   * Prefer {@link Sets#immutableEnumSet(Enum, Enum[])} for enum collections to take advantage of
   * the internally used {@link EnumSet}.
   *
   * <p><strong>Warning:</strong> this rule is not completely behavior preserving: while the {@link
   * ImmutableSet#of} expression produces a set that iterates over its elements in the listed order,
   * the replacement code iterates over the elements in enum definition order.
   */
  static final class SetsImmutableEnumSet2<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("SetsImmutableEnumSetIterable" /* This is a more specific template. */)
    ImmutableSet<T> before(T e1, T e2) {
      return Refaster.anyOf(ImmutableSet.of(e1, e2), ImmutableSet.copyOf(EnumSet.of(e1, e2)));
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    ImmutableSet<T> after(T e1, T e2) {
      return Sets.immutableEnumSet(e1, e2);
    }
  }

  /**
   * Prefer {@link Sets#immutableEnumSet(Enum, Enum[])} for enum collections to take advantage of
   * the internally used {@link EnumSet}.
   *
   * <p><strong>Warning:</strong> this rule is not completely behavior preserving: while the {@link
   * ImmutableSet#of} expression produces a set that iterates over its elements in the listed order,
   * the replacement code iterates over the elements in enum definition order.
   */
  static final class SetsImmutableEnumSet3<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("SetsImmutableEnumSetIterable" /* This is a more specific template. */)
    ImmutableSet<T> before(T e1, T e2, T e3) {
      return Refaster.anyOf(
          ImmutableSet.of(e1, e2, e3), ImmutableSet.copyOf(EnumSet.of(e1, e2, e3)));
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    ImmutableSet<T> after(T e1, T e2, T e3) {
      return Sets.immutableEnumSet(e1, e2, e3);
    }
  }

  /**
   * Prefer {@link Sets#immutableEnumSet(Enum, Enum[])} for enum collections to take advantage of
   * the internally used {@link EnumSet}.
   *
   * <p><strong>Warning:</strong> this rule is not completely behavior preserving: while the {@link
   * ImmutableSet#of} expression produces a set that iterates over its elements in the listed order,
   * the replacement code iterates over the elements in enum definition order.
   */
  static final class SetsImmutableEnumSet4<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("SetsImmutableEnumSetIterable" /* This is a more specific template. */)
    ImmutableSet<T> before(T e1, T e2, T e3, T e4) {
      return Refaster.anyOf(
          ImmutableSet.of(e1, e2, e3, e4), ImmutableSet.copyOf(EnumSet.of(e1, e2, e3, e4)));
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    ImmutableSet<T> after(T e1, T e2, T e3, T e4) {
      return Sets.immutableEnumSet(e1, e2, e3, e4);
    }
  }

  /**
   * Prefer {@link Sets#immutableEnumSet(Enum, Enum[])} for enum collections to take advantage of
   * the internally used {@link EnumSet}.
   *
   * <p><strong>Warning:</strong> this rule is not completely behavior preserving: while the {@link
   * ImmutableSet#of} expression produces a set that iterates over its elements in the listed order,
   * the replacement code iterates over the elements in enum definition order.
   */
  static final class SetsImmutableEnumSet5<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("SetsImmutableEnumSetIterable" /* This is a more specific template. */)
    ImmutableSet<T> before(T e1, T e2, T e3, T e4, T e5) {
      return Refaster.anyOf(
          ImmutableSet.of(e1, e2, e3, e4, e5), ImmutableSet.copyOf(EnumSet.of(e1, e2, e3, e4, e5)));
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    ImmutableSet<T> after(T e1, T e2, T e3, T e4, T e5) {
      return Sets.immutableEnumSet(e1, e2, e3, e4, e5);
    }
  }

  /**
   * Prefer {@link Sets#immutableEnumSet(Enum, Enum[])} for enum collections to take advantage of
   * the internally used {@link EnumSet}.
   *
   * <p><strong>Warning:</strong> this rule is not completely behavior preserving: while the
   * original code produces a set that iterates over its elements in the listed order, the
   * replacement code iterates over the elements in enum definition order.
   */
  static final class SetsImmutableEnumSet6<T extends Enum<T>> {
    @BeforeTemplate
    ImmutableSet<T> before(T e1, T e2, T e3, T e4, T e5, T e6) {
      return ImmutableSet.of(e1, e2, e3, e4, e5, e6);
    }

    @AfterTemplate
    @SuppressWarnings("unchecked")
    ImmutableSet<T> after(T e1, T e2, T e3, T e4, T e5, T e6) {
      return Sets.immutableEnumSet(e1, e2, e3, e4, e5, e6);
    }
  }

  /**
   * Prefer {@link Sets#immutableEnumSet(Enum, Enum[])} for enum collections to take advantage of
   * the internally used {@link EnumSet}.
   */
  static final class SetsImmutableEnumSetVarArgs<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("SetsImmutableEnumSetIterable" /* This is a more specific template. */)
    ImmutableSet<T> before(T e1, @Repeated T elements) {
      return ImmutableSet.copyOf(EnumSet.of(e1, Refaster.asVarargs(elements)));
    }

    @AfterTemplate
    ImmutableSet<T> after(T e1, @Repeated T elements) {
      return Sets.immutableEnumSet(e1, Refaster.asVarargs(elements));
    }
  }

  /**
   * Use {@link Sets#toImmutableEnumSet()} when possible, as it is more efficient than {@link
   * ImmutableSet#toImmutableSet()} and produces a more compact object.
   *
   * <p><strong>Warning:</strong> this rule is not completely behavior preserving: while the
   * original code produces a set that iterates over its elements in encounter order, the
   * replacement code iterates over the elements in enum definition order.
   */
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
}
