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
        EnumerableAssert<?, E> enumerableAssert, @Matches(IsEmpty.class) Iterable<?> emptyOther) {
      Refaster.anyOf(
          enumerableAssert.hasSize(0),
          enumerableAssert.hasSizeLessThanOrEqualTo(0),
          enumerableAssert.hasSizeLessThan(1),
          enumerableAssert.hasSameSizeAs(emptyOther));
    }

    @BeforeTemplate
    @SuppressWarnings("unchecked" /* Safe generic array type creation. */)
    void before(
        ObjectEnumerableAssert<?, E> enumerableAssert,
        @Matches(IsEmpty.class) Iterable<? extends E> emptyOther) {
      Refaster.anyOf(
          enumerableAssert.containsExactlyElementsOf(emptyOther),
          enumerableAssert.containsExactlyInAnyOrderElementsOf(emptyOther),
          enumerableAssert.hasSameElementsAs(emptyOther),
          enumerableAssert.isSubsetOf(emptyOther),
          enumerableAssert.containsExactly(),
          enumerableAssert.containsExactlyInAnyOrder(),
          enumerableAssert.containsOnly(),
          enumerableAssert.isSubsetOf());
    }

    @BeforeTemplate
    void before(AbstractIterableAssert<?, ?, E, ?> enumerableAssert) {
      enumerableAssert.size().isNotPositive();
    }

    @AfterTemplate
    void after(EnumerableAssert<?, E> enumerableAssert) {
      enumerableAssert.isEmpty();
    }
  }

  /** Prefer {@link EnumerableAssert#isEmpty()} over more contrived alternatives. */
  // XXX: This rule assumes that the rewritten assertion aims to compare the contents of two
  // iterables, rather than other semantics (such as `Set` vs. `List`).
  static final class AssertIsEmpty<
      E, A extends Assert<?, ? extends Iterable<? extends E>> & EnumerableAssert<?, E>> {
    @BeforeTemplate
    void before(A enumAssert, @Matches(IsEmpty.class) Iterable<?> emptyExpected) {
      enumAssert.isEqualTo(emptyExpected);
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
    EnumerableAssert<?, E> before(EnumerableAssert<?, E> enumerableAssert) {
      return Refaster.anyOf(
          enumerableAssert.hasSizeGreaterThan(0), enumerableAssert.hasSizeGreaterThanOrEqualTo(1));
    }

    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(AbstractIterableAssert<?, ?, E, ?> enumerableAssert) {
      return Refaster.anyOf(
          enumerableAssert.size().isNotEqualTo(0).returnToIterable(),
          enumerableAssert.size().isPositive().returnToIterable());
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
        before2(AbstractIterableAssert<?, ?, E, ?> enumerableAssert) {
      return Refaster.anyOf(
          enumerableAssert.size().isNotEqualTo(0), enumerableAssert.size().isPositive());
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumerableAssert) {
      return enumerableAssert.isNotEmpty();
    }
  }

  /** Prefer {@link EnumerableAssert#hasSize(int)} over more verbose alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSize<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumerableAssert, int expected) {
      return enumerableAssert.size().isEqualTo(expected).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumerableAssert, int expected) {
      return enumerableAssert.size().isEqualTo(expected);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumerableAssert, int expected) {
      return enumerableAssert.hasSize(expected);
    }
  }

  /** Prefer {@link EnumerableAssert#hasSizeLessThan(int)} over more verbose alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeLessThan<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumerableAssert, int boundary) {
      return enumerableAssert.size().isLessThan(boundary).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumerableAssert, int boundary) {
      return enumerableAssert.size().isLessThan(boundary);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumerableAssert, int boundary) {
      return enumerableAssert.hasSizeLessThan(boundary);
    }
  }

  /**
   * Prefer {@link EnumerableAssert#hasSizeLessThanOrEqualTo(int)} over more verbose alternatives.
   */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeLessThanOrEqualTo<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumerableAssert, int boundary) {
      return enumerableAssert.size().isLessThanOrEqualTo(boundary).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumerableAssert, int boundary) {
      return enumerableAssert.size().isLessThanOrEqualTo(boundary);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumerableAssert, int boundary) {
      return enumerableAssert.hasSizeLessThanOrEqualTo(boundary);
    }
  }

  /** Prefer {@link EnumerableAssert#hasSizeGreaterThan(int)} over more verbose alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeGreaterThan<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumerableAssert, int boundary) {
      return enumerableAssert.size().isGreaterThan(boundary).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumerableAssert, int boundary) {
      return enumerableAssert.size().isGreaterThan(boundary);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumerableAssert, int boundary) {
      return enumerableAssert.hasSizeGreaterThan(boundary);
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
        AbstractIterableAssert<?, ?, E, ?> enumerableAssert, int boundary) {
      return enumerableAssert.size().isGreaterThanOrEqualTo(boundary).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumerableAssert, int boundary) {
      return enumerableAssert.size().isGreaterThanOrEqualTo(boundary);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumerableAssert, int boundary) {
      return enumerableAssert.hasSizeGreaterThanOrEqualTo(boundary);
    }
  }

  /** Prefer {@link EnumerableAssert#hasSizeBetween(int, int)} over more verbose alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSizeBetween<E> {
    @BeforeTemplate
    AbstractIterableAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumerableAssert,
        int lowerBoundary,
        int higherBoundary) {
      return enumerableAssert.size().isBetween(lowerBoundary, higherBoundary).returnToIterable();
    }

    // XXX: If this template matches, then the expression's return type changes incompatibly.
    // Consider moving this template to a separate block (statement) rule.
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before2(
        AbstractIterableAssert<?, ?, E, ?> enumerableAssert,
        int lowerBoundary,
        int higherBoundary) {
      return enumerableAssert.size().isBetween(lowerBoundary, higherBoundary);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(
        EnumerableAssert<?, E> enumerableAssert, int lowerBoundary, int higherBoundary) {
      return enumerableAssert.hasSizeBetween(lowerBoundary, higherBoundary);
    }
  }

  /** Prefer {@link EnumerableAssert#hasSameSizeAs(Iterable)} over more verbose alternatives. */
  @PossibleSourceIncompatibility
  static final class EnumerableAssertHasSameSizeAs<S, E> {
    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumerableAssert, Iterable<E> other) {
      return enumerableAssert.hasSize(Iterables.size(other));
    }

    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumerableAssert, Collection<E> other) {
      return enumerableAssert.hasSize(other.size());
    }

    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumerableAssert, E[] other) {
      return enumerableAssert.hasSize(other.length);
    }

    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumerableAssert, CharSequence other) {
      return enumerableAssert.hasSize(other.length());
    }

    @AfterTemplate
    EnumerableAssert<?, S> after(EnumerableAssert<?, S> enumerableAssert, Iterable<E> other) {
      return enumerableAssert.hasSameSizeAs(other);
    }
  }
}
