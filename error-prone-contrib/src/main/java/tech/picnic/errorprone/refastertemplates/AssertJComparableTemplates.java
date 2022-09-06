package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractComparableAssert;

final class AssertJComparableTemplates {
  private AssertJComparableTemplates() {}

  static final class AbstractComparableAssertActualIsLessThanExpected<
      T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(T actual, T expected) {
      return Refaster.anyOf(
          assertThat(actual.compareTo(expected) < 0).isTrue(),
          assertThat(actual.compareTo(expected) >= 0).isFalse());
    }

    @AfterTemplate
    AbstractComparableAssert<?, ?> after(T actual, T expected) {
      return assertThat(actual).isLessThan(expected);
    }
  }

  static final class AbstractComparableAssertActualIsLessThanOrEqualToExpected<
      T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(T actual, T expected) {
      return Refaster.anyOf(
          assertThat(actual.compareTo(expected) <= 0).isTrue(),
          assertThat(actual.compareTo(expected) > 0).isFalse());
    }

    @AfterTemplate
    AbstractComparableAssert<?, ?> after(T actual, T expected) {
      return assertThat(actual).isLessThanOrEqualTo(expected);
    }
  }

  static final class AbstractComparableAssertActualIsGreaterThanExpected<
      T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(T actual, T expected) {
      return Refaster.anyOf(
          assertThat(actual.compareTo(expected) > 0).isTrue(),
          assertThat(actual.compareTo(expected) <= 0).isFalse());
    }

    @AfterTemplate
    AbstractComparableAssert<?, ?> after(T actual, T expected) {
      return assertThat(actual).isGreaterThan(expected);
    }
  }

  static final class AbstractComparableAssertActualIsGreaterThanOrEqualToExpected<
      T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(T actual, T expected) {
      return Refaster.anyOf(
          assertThat(actual.compareTo(expected) >= 0).isTrue(),
          assertThat(actual.compareTo(expected) < 0).isFalse());
    }

    @AfterTemplate
    AbstractComparableAssert<?, ?> after(T actual, T expected) {
      return assertThat(actual).isGreaterThanOrEqualTo(expected);
    }
  }
}
