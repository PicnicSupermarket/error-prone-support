package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.Iterables;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Collection;
import org.assertj.core.api.EnumerableAssert;

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

    @AfterTemplate
    EnumerableAssert<?, E> after(EnumerableAssert<?, E> enumAssert) {
      return enumAssert.isNotEmpty();
    }
  }

  static final class EnumerableAssertHasSameSizeAs<S, T> {
    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumAssert, Iterable<T> iterable) {
      return enumAssert.hasSize(Iterables.size(iterable));
    }

    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumAssert, Collection<T> iterable) {
      return enumAssert.hasSize(iterable.size());
    }

    @BeforeTemplate
    EnumerableAssert<?, S> before(EnumerableAssert<?, S> enumAssert, T[] iterable) {
      return enumAssert.hasSize(iterable.length);
    }

    @AfterTemplate
    EnumerableAssert<?, S> after(EnumerableAssert<?, S> enumAssert, Iterable<T> iterable) {
      return enumAssert.hasSameSizeAs(iterable);
    }
  }
}
