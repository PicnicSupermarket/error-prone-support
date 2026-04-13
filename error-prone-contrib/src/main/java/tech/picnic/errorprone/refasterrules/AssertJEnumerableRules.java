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
    void before(EnumerableAssert<?, E> enumAssert, @Matches(IsEmpty.class) Iterable<?> other) {
      Refaster.anyOf(
          enumAssert.hasSize(0),
          enumAssert.hasSizeLessThanOrEqualTo(0),
          enumAssert.hasSizeLessThan(1),
          enumAssert.hasSameSizeAs(other));
    }

    @BeforeTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    void before(
        ObjectEnumerableAssert<?, E> enumAssert,
        @Matches(IsEmpty.class) Iterable<? extends E> other) {
      Refaster.anyOf(
          enumAssert.containsExactlyElementsOf(other),
          enumAssert.containsExactlyInAnyOrderElementsOf(other),
          enumAssert.hasSameElementsAs(other),
          enumAssert.isSubsetOf(other),
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

  /** Prefer {@link EnumerableAssert#isEmpty()} over more contrived alternatives. */
  // XXX: This rule assumes that the rewritten assertion aims to compare the contents of two
  // iterables, rather than other semantics (such as `Set` vs. `List`).
  static final class AssertIsEmpty<
      E, A extends Assert<?, ? extends Iterable<? extends E>> & EnumerableAssert<?, E>> {
    @BeforeTemplate
    void before(A enumAssert, @Matches(IsEmpty.class) Iterable<?> expected) {
      enumAssert.isEqualTo(expected);
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
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int expected) {
      return enumAssert.size().isEqualTo(expected).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int expected) {
      return enumAssert.size().isEqualTo(expected);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int expected) {
      return enumAssert.hasSize(expected);
    }
  }

  /** Prefer {@link EnumerableAssert#hasSizeLessThan(int)} over more verbose alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeLessThan<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int boundary) {
      return enumAssert.size().isLessThan(boundary).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int boundary) {
      return enumAssert.size().isLessThan(boundary);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int boundary) {
      return enumAssert.hasSizeLessThan(boundary);
    }
  }

  /**
   * Prefer {@link EnumerableAssert#hasSizeLessThanOrEqualTo(int)} over more verbose alternatives.
   */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeLessThanOrEqualTo<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int boundary) {
      return enumAssert.size().isLessThanOrEqualTo(boundary).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int boundary) {
      return enumAssert.size().isLessThanOrEqualTo(boundary);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int boundary) {
      return enumAssert.hasSizeLessThanOrEqualTo(boundary);
    }
  }

  /** Prefer {@link EnumerableAssert#hasSizeGreaterThan(int)} over more verbose alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeGreaterThan<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int boundary) {
      return enumAssert.size().isGreaterThan(boundary).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int boundary) {
      return enumAssert.size().isGreaterThan(boundary);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int boundary) {
      return enumAssert.hasSizeGreaterThan(boundary);
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
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int boundary) {
      return enumAssert.size().isGreaterThanOrEqualTo(boundary).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int boundary) {
      return enumAssert.size().isGreaterThanOrEqualTo(boundary);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int boundary) {
      return enumAssert.hasSizeGreaterThanOrEqualTo(boundary);
    }
  }

  /** Prefer {@link EnumerableAssert#hasSizeBetween(int, int)} over more verbose alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeBetween<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int lowerBoundary, int higherBoundary) {
      return enumAssert.size().isBetween(lowerBoundary, higherBoundary).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int lowerBoundary, int higherBoundary) {
      return enumAssert.size().isBetween(lowerBoundary, higherBoundary);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(
        EnumerableAssert<?, E> enumAssert, int lowerBoundary, int higherBoundary) {
      return enumAssert.hasSizeBetween(lowerBoundary, higherBoundary);
    }
  }

  /** Prefer {@link EnumerableAssert#hasSameSizeAs(Iterable)} over more verbose alternatives. */
  static final class EnumerableAssertHasSameSizeAs<S, E> {
    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumAssert, Iterable<E> other) {
      return enumAssert.hasSize(Iterables.size(other));
    }

    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumAssert, Collection<E> other) {
      return enumAssert.hasSize(other.size());
    }

    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumAssert, E[] other) {
      return enumAssert.hasSize(other.length);
    }

    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumAssert, CharSequence other) {
      return enumAssert.hasSize(other.length());
    }

    @AfterTemplate
    EnumerableAssert<?, S> after(EnumerableAssert<?, S> enumAssert, Iterable<E> other) {
      return enumAssert.hasSameSizeAs(other);
    }
  }
}
