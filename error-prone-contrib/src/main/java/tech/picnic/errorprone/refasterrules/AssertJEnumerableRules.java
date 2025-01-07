package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.Iterables;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Collection;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.AbstractIterableSizeAssert;
import org.assertj.core.api.EnumerableAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJEnumerableRules {
  private AssertJEnumerableRules() {}

  static final class EnumerableAssertIsEmpty<E> {
    @BeforeTemplate
    void before(EnumerableAssert<?, E> enumAssert) {
      Refaster.anyOf(
          enumAssert.hasSize(0),
          enumAssert.hasSizeLessThanOrEqualTo(0),
          enumAssert.hasSizeLessThan(1));
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

  static final class EnumerableAssertIsNotEmpty<E> {
    @BeforeTemplate
    EnumerableAssert<?, E> before(EnumerableAssert<?, E> enumAssert) {
      return Refaster.anyOf(
          enumAssert.hasSizeGreaterThan(0), enumAssert.hasSizeGreaterThanOrEqualTo(1));
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIterableAssert<?, ?, E, ?> enumAssert) {
      return Refaster.anyOf(enumAssert.size().isNotEqualTo(0), enumAssert.size().isPositive());
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert) {
      return enumAssert.isNotEmpty();
    }
  }

  static final class EnumerableAssertHasSize<E> {
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isEqualTo(size);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int size) {
      return enumAssert.hasSize(size);
    }
  }

  static final class EnumerableAssertHasSizeLessThan<E> {
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isLessThan(size);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int size) {
      return enumAssert.hasSizeLessThan(size);
    }
  }

  static final class EnumerableAssertHasSizeLessThanOrEqualTo<E> {
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isLessThanOrEqualTo(size);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int size) {
      return enumAssert.hasSizeLessThanOrEqualTo(size);
    }
  }

  static final class EnumerableAssertHasSizeGreaterThan<E> {
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isGreaterThan(size);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int size) {
      return enumAssert.hasSizeGreaterThan(size);
    }
  }

  static final class EnumerableAssertHasSizeGreaterThanOrEqualTo<E> {
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int size) {
      return enumAssert.size().isGreaterThanOrEqualTo(size);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int size) {
      return enumAssert.hasSizeGreaterThanOrEqualTo(size);
    }
  }

  static final class EnumerableAssertHasSizeBetween<E> {
    @BeforeTemplate
    AbstractIterableSizeAssert<?, ?, E, ?> before(
        AbstractIterableAssert<?, ?, E, ?> enumAssert, int lower, int upper) {
      return enumAssert.size().isBetween(lower, upper);
    }

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert, int lower, int upper) {
      return enumAssert.hasSizeBetween(lower, upper);
    }
  }

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

    @AfterTemplate
    EnumerableAssert<?, S> after(EnumerableAssert<?, S> enumAssert, Iterable<E> iterable) {
      return enumAssert.hasSameSizeAs(iterable);
    }
  }
}
