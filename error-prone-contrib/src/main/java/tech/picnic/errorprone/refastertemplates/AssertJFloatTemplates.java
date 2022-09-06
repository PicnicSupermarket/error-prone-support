package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractFloatAssert;
import org.assertj.core.data.Offset;

final class AssertJFloatTemplates {
  private AssertJFloatTemplates() {}

  static final class AbstractFloatAssertIsCloseToWithOffset {
    @BeforeTemplate
    AbstractFloatAssert<?> before(
        AbstractFloatAssert<?> floatAssert, float n, Offset<Float> offset) {
      return floatAssert.isEqualTo(n, offset);
    }

    @BeforeTemplate
    AbstractFloatAssert<?> before(
        AbstractFloatAssert<?> floatAssert, Float n, Offset<Float> offset) {
      return floatAssert.isEqualTo(n, offset);
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(
        AbstractFloatAssert<?> floatAssert, float n, Offset<Float> offset) {
      return floatAssert.isCloseTo(n, offset);
    }
  }

  static final class AbstractFloatAssertIsEqualTo {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert, float n) {
      return Refaster.anyOf(
          floatAssert.isCloseTo(n, offset(0F)), floatAssert.isCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert, float n) {
      return floatAssert.isEqualTo(n);
    }
  }

  static final class AbstractFloatAssertActualIsEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(float actual, float expected) {
      return Refaster.anyOf(
          assertThat(actual == expected).isTrue(), assertThat(actual != expected).isFalse());
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(float actual, float expected) {
      return assertThat(actual).isEqualTo(expected);
    }
  }

  static final class AbstractFloatAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert, float n) {
      return Refaster.anyOf(
          floatAssert.isNotCloseTo(n, offset(0F)), floatAssert.isNotCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert, float n) {
      return floatAssert.isNotEqualTo(n);
    }
  }

  static final class AbstractFloatAssertActualIsNotEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(float actual, float expected) {
      return Refaster.anyOf(
          assertThat(actual != expected).isTrue(), assertThat(actual == expected).isFalse());
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(float actual, float expected) {
      return assertThat(actual).isNotEqualTo(expected);
    }
  }

  static final class AbstractFloatAssertIsZero {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isZero();
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isEqualTo(0);
    }
  }

  static final class AbstractFloatAssertIsNotZero {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isNotZero();
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isNotEqualTo(0);
    }
  }

  static final class AbstractFloatAssertIsOne {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isOne();
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isEqualTo(1);
    }
  }

  static final class AbstractFloatAssertActualIsLessThanExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(float actual, float expected) {
      return Refaster.anyOf(
          assertThat(actual < expected).isTrue(), assertThat(actual >= expected).isFalse());
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(float actual, float expected) {
      return assertThat(actual).isLessThan(expected);
    }
  }

  static final class AbstractFloatAssertActualIsLessThanOrEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(float actual, float expected) {
      return Refaster.anyOf(
          assertThat(actual <= expected).isTrue(), assertThat(actual > expected).isFalse());
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(float actual, float expected) {
      return assertThat(actual).isLessThanOrEqualTo(expected);
    }
  }

  static final class AbstractFloatAssertActualIsGreaterThanExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(float actual, float expected) {
      return Refaster.anyOf(
          assertThat(actual > expected).isTrue(), assertThat(actual <= expected).isFalse());
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(float actual, float expected) {
      return assertThat(actual).isGreaterThan(expected);
    }
  }

  static final class AbstractFloatAssertActualIsGreaterThanOrEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(float actual, float expected) {
      return Refaster.anyOf(
          assertThat(actual >= expected).isTrue(), assertThat(actual < expected).isFalse());
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(float actual, float expected) {
      return assertThat(actual).isGreaterThanOrEqualTo(expected);
    }
  }
}
