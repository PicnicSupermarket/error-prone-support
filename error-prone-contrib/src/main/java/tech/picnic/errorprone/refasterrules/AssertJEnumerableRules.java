package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.Iterables;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Matches;
import java.util.Collection;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.AbstractIterableSizeAssert;
import org.assertj.core.api.Assert;
import org.assertj.core.api.EnumerableAssert;
import org.assertj.core.api.ObjectEnumerableAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;
import tech.picnic.errorprone.refaster.matchers.IsEmpty;

/** Refaster rules related to AssertJ assertions over enumerable objects. */
@OnlineDocumentation
final class AssertJEnumerableRules {
  private AssertJEnumerableRules() {}

  /** Prefer {@link EnumerableAssert#isEmpty()} over more contrived alternatives. */
  static final class EnumerableAssertIsEmpty<E> {
    @BeforeTemplate
    void before(
        EnumerableAssert<?, E> enumAssert, @Matches(IsEmpty.class) Iterable<?> emptyIterable) {
      Refaster.anyOf(
          enumAssert.hasSize(0),
          enumAssert.hasSizeLessThanOrEqualTo(0),
          enumAssert.hasSizeLessThan(1),
          enumAssert.hasSameSizeAs(emptyIterable));
    }

    @BeforeTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    void before(
        ObjectEnumerableAssert<?, E> enumAssert,
        @Matches(IsEmpty.class) Iterable<? extends E> emptyIterable) {
      Refaster.anyOf(
          enumAssert.containsExactlyElementsOf(emptyIterable),
          enumAssert.containsExactlyInAnyOrderElementsOf(emptyIterable),
          enumAssert.hasSameElementsAs(emptyIterable),
          enumAssert.isSubsetOf(emptyIterable),
          enumAssert.containsExactly(),
          enumAssert.containsExactlyInAnyOrder(),
          enumAssert.containsOnly(),
          enumAssert.isSubsetOf());
    }

    @BeforeTemplate
    void before(AbstractIterableAssert<?, ?, E, ?> enumAssert) {
      enumAssert.size().isNotPositive();
    }

    @AfterTemplate
    void after(EnumerableAssert<?, E> enumAssert) {
      enumAssert.isEmpty();
    }
  }

  // XXX: This rule assumes that the rewritten assertion aims to compare the contents of two
  // iterables, rather than other semantics (such as `Set` vs. `List`).
  /** Prefer {@link EnumerableAssert#isEmpty()} over more contrived alternatives. */
  static final class AssertAndEnumerableAssertIsEmpty<
      E, A extends Assert<?, ? extends Iterable<? extends E>> & EnumerableAssert<?, E>> {
    @BeforeTemplate
    void before(A enumAssert, @Matches(IsEmpty.class) Iterable<?> emptyIterable) {
      enumAssert.isEqualTo(emptyIterable);
    }

    @AfterTemplate
    void after(A enumAssert) {
      enumAssert.isEmpty();
    }
  }

  /** Prefer {@link EnumerableAssert#isNotEmpty()} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertIsNotEmpty<E> {
    @BeforeTemplate
    EnumerableAssert<?, E> before(EnumerableAssert<?, E> enumAssert) {
      return Refaster.anyOf(
          enumAssert.hasSizeGreaterThan(0), enumAssert.hasSizeGreaterThanOrEqualTo(1));
    }

    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(AbstractIterableAssert<?, ?, E, ?> enumAssert) {
      return Refaster.anyOf(
          enumAssert.size().isNotEqualTo(0).returnToIterable(),
          enumAssert.size().isPositive().returnToIterable());
    }

    @BeforeTemplate
    AbstractIterableSizeAssert<
            ? extends
                AbstractIterableAssert<
                    ? extends AbstractIterableAssert<?, ?, E, ?>,
                    ? extends Iterable<? extends E>,
                    E,
                    ? extends AbstractAssert<? extends AbstractAssert<?, E>, E>>,
            ? extends Iterable<? extends E>,
            E,
            ? extends AbstractAssert<? extends AbstractAssert<?, E>, E>>
        before2(AbstractIterableAssert<?, ?, E, ?> enumAssert) {
      return Refaster.anyOf(enumAssert.size().isNotEqualTo(0), enumAssert.size().isPositive());
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert) {
      return enumAssert.isNotEmpty();
    }
  }

  /** Prefer {@link EnumerableAssert#hasSize(int)} over more verbose alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSize<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isEqualTo(size).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isEqualTo(size);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int size) {
      return enumAssert.hasSize(size);
    }
  }

  /** Prefer {@link EnumerableAssert#hasSizeLessThan(int)} over more verbose alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeLessThan<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isLessThan(size).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isLessThan(size);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int size) {
      return enumAssert.hasSizeLessThan(size);
    }
  }

  /**
   * Prefer {@link EnumerableAssert#hasSizeLessThanOrEqualTo(int)} over more verbose alternatives.
   */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeLessThanOrEqualTo<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isLessThanOrEqualTo(size).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isLessThanOrEqualTo(size);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int size) {
      return enumAssert.hasSizeLessThanOrEqualTo(size);
    }
  }

  /** Prefer {@link EnumerableAssert#hasSizeGreaterThan(int)} over more verbose alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeGreaterThan<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isGreaterThan(size).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isGreaterThan(size);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int size) {
      return enumAssert.hasSizeGreaterThan(size);
    }
  }

  /**
   * Prefer {@link EnumerableAssert#hasSizeGreaterThanOrEqualTo(int)} over more verbose
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeGreaterThanOrEqualTo<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isGreaterThanOrEqualTo(size).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isGreaterThanOrEqualTo(size);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int size) {
      return enumAssert.hasSizeGreaterThanOrEqualTo(size);
    }
  }

  /** Prefer {@link EnumerableAssert#hasSizeBetween(int, int)} over more verbose alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeBetween<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int lower, int upper) {
      return enumAssert.size().isBetween(lower, upper).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int lower, int upper) {
      return enumAssert.size().isBetween(lower, upper);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int lower, int upper) {
      return enumAssert.hasSizeBetween(lower, upper);
    }
  }

  /** Prefer {@link EnumerableAssert#hasSameSizeAs(Iterable)} over more verbose alternatives. */
  static final class EnumerableAssertHasSameSizeAs<S, E> {
    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumAssert, Iterable<E> iterable) {
      return enumAssert.hasSize(Iterables.size(iterable));
    }

    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumAssert, Collection<E> iterable) {
      return enumAssert.hasSize(iterable.size());
    }

    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumAssert, E[] iterable) {
      return enumAssert.hasSize(iterable.length);
    }

    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumAssert, CharSequence iterable) {
      return enumAssert.hasSize(iterable.length());
    }

    @AfterTemplate
    EnumerableAssert<?, S> after(EnumerableAssert<?, S> enumAssert, Iterable<E> iterable) {
      return enumAssert.hasSameSizeAs(iterable);
    }
  }
}
