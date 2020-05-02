package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.data.Offset;

final class AssertJDoubleTemplates {
  private AssertJDoubleTemplates() {}

  static final class AbstractDoubleAssertIsCloseToWithOffset {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(
        AbstractDoubleAssert<?> doubleAssert, double n, Offset<Double> offset) {
      return doubleAssert.isEqualTo(n, offset);
    }

    @BeforeTemplate
    AbstractDoubleAssert<?> before(
        AbstractDoubleAssert<?> doubleAssert, Double n, Offset<Double> offset) {
      return doubleAssert.isEqualTo(n, offset);
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(
        AbstractDoubleAssert<?> doubleAssert, double n, Offset<Double> offset) {
      return doubleAssert.isCloseTo(n, offset);
    }
  }

  static final class AbstractDoubleAssertIsEqualTo {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> doubleAssert, double n) {
      return Refaster.anyOf(
          doubleAssert.isCloseTo(n, offset(0.0)),
          doubleAssert.isCloseTo(Double.valueOf(n), offset(0.0)),
          doubleAssert.isCloseTo(n, withPercentage(0.0)),
          doubleAssert.isCloseTo(Double.valueOf(n), withPercentage(0.0)),
          doubleAssert.isEqualTo(Double.valueOf(n)));
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert, double n) {
      return doubleAssert.isEqualTo(n);
    }
  }

  static final class AbstractDoubleAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> doubleAssert, double n) {
      return Refaster.anyOf(
          doubleAssert.isNotCloseTo(n, offset(0.0)),
          doubleAssert.isNotCloseTo(Double.valueOf(n), offset(0.0)),
          doubleAssert.isNotCloseTo(n, withPercentage(0.0)),
          doubleAssert.isNotCloseTo(Double.valueOf(n), withPercentage(0.0)),
          doubleAssert.isNotEqualTo(Double.valueOf(n)));
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert, double n) {
      return doubleAssert.isNotEqualTo(n);
    }
  }

  static final class AbstractDoubleAssertIsZero {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> doubleAssert) {
      return doubleAssert.isZero();
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert) {
      return doubleAssert.isEqualTo(0);
    }
  }

  static final class AbstractDoubleAssertIsNotZero {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> doubleAssert) {
      return doubleAssert.isNotZero();
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert) {
      return doubleAssert.isNotEqualTo(0);
    }
  }

  static final class AbstractDoubleAssertIsOne {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> doubleAssert) {
      return doubleAssert.isOne();
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert) {
      return doubleAssert.isEqualTo(1);
    }
  }
}
