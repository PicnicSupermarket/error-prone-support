package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with immutable {@link Enum} collections. */
@OnlineDocumentation
final class ImmutableEnumSetRules {
  private ImmutableEnumSetRules() {}

  /**
   * Prefer {@link Sets#immutableEnumSet(Iterable)} for enum collections to take advantage of
   * internally used {@link EnumSet}.
   */
  static final class ImmutableEnumSetIterable<T extends Enum<T>> {
    @BeforeTemplate
    ImmutableSet<T> before(Collection<T> elements) {
      return ImmutableSet.copyOf(elements);
    }

    @AfterTemplate
    ImmutableSet<T> after(Collection<T> elements) {
      return Sets.immutableEnumSet(elements);
    }
  }

  /**
   * Prefer {@link Sets#immutableEnumSet(Iterable)} for enum collections to take advantage of
   * internally used {@link EnumSet}.
   */
  static final class ImmutableEnumSetIterable1<T extends Enum<T>> {
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
   * internally used {@link EnumSet}.
   */
  static final class ImmutableEnumSetOneElement<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("ImmutableEnumSetIterable" /* This is a more specific template. */)
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
   * internally used {@link EnumSet}.
   */
  static final class ImmutableEnumSetTwoElements<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("ImmutableEnumSetIterable" /* This is a more specific template. */)
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
   * internally used {@link EnumSet}.
   */
  static final class ImmutableEnumSetThreeElements<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("ImmutableEnumSetIterable" /* This is a more specific template. */)
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
   * internally used {@link EnumSet}.
   */
  static final class ImmutableEnumSetFourElements<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("ImmutableEnumSetIterable" /* This is a more specific template. */)
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
   * internally used {@link EnumSet}.
   */
  static final class ImmutableEnumSetFiveElements<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("ImmutableEnumSetIterable" /* This is a more specific template. */)
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
   * internally used {@link EnumSet}.
   */
  static final class ImmutableEnumSetSixElements<T extends Enum<T>> {
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
   * internally used {@link EnumSet}.
   */
  static final class ImmutableEnumSetVarArgs<T extends Enum<T>> {
    @BeforeTemplate
    @SuppressWarnings("ImmutableEnumSetIterable" /* This is a more specific template. */)
    ImmutableSet<T> before(T e1, @Repeated T elements) {
      return ImmutableSet.copyOf(EnumSet.of(e1, Refaster.asVarargs(elements)));
    }

    @AfterTemplate
    ImmutableSet<T> after(T e1, @Repeated T elements) {
      return Sets.immutableEnumSet(e1, Refaster.asVarargs(elements));
    }
  }
}
