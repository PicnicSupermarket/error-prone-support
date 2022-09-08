package tech.picnic.errorprone.refastertemplates;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractBooleanAssert;
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
          doubleAssert.isCloseTo(n, offset(0.0)), doubleAssert.isCloseTo(n, withPercentage(0.0)));
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert, double n) {
      return doubleAssert.isEqualTo(n);
    }
  }

  static final class AssertThatIsEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(boolean actual, boolean expected) {
      return Refaster.anyOf(
          assertThat(actual == expected).isTrue(), assertThat(actual != expected).isFalse());
    }

    @BeforeTemplate
    AbstractBooleanAssert<?> before(double actual, double expected) {
      return Refaster.anyOf(
          assertThat(actual == expected).isTrue(), assertThat(actual != expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractBooleanAssert<?> after(boolean actual, boolean expected) {
      return assertThat(actual).isEqualTo(expected);
    }
  }

  static final class AbstractDoubleAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> doubleAssert, double n) {
      return Refaster.anyOf(
          doubleAssert.isNotCloseTo(n, offset(0.0)),
          doubleAssert.isNotCloseTo(n, withPercentage(0.0)));
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert, double n) {
      return doubleAssert.isNotEqualTo(n);
    }
  }

  static final class AssertThatIsNotEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(boolean actual, boolean expected) {
      return Refaster.anyOf(
          assertThat(actual != expected).isTrue(), assertThat(actual == expected).isFalse());
    }

    @BeforeTemplate
    AbstractBooleanAssert<?> before(double actual, double expected) {
      return Refaster.anyOf(
          assertThat(actual != expected).isTrue(), assertThat(actual == expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractBooleanAssert<?> after(boolean actual, boolean expected) {
      return assertThat(actual).isNotEqualTo(expected);
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

  static final class AssertThatIsLessThan {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(double actual, double expected) {
      return Refaster.anyOf(
          assertThat(actual < expected).isTrue(), assertThat(actual >= expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractDoubleAssert<?> after(double actual, double expected) {
      return assertThat(actual).isLessThan(expected);
    }
  }

  static final class AssertThatIsLessThanOrEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(double actual, double expected) {
      return Refaster.anyOf(
          assertThat(actual <= expected).isTrue(), assertThat(actual > expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractDoubleAssert<?> after(double actual, double expected) {
      return assertThat(actual).isLessThanOrEqualTo(expected);
    }
  }

  static final class AssertThatIsGreaterThan {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(double actual, double expected) {
      return Refaster.anyOf(
          assertThat(actual > expected).isTrue(), assertThat(actual <= expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractDoubleAssert<?> after(double actual, double expected) {
      return assertThat(actual).isGreaterThan(expected);
    }
  }

  static final class AssertThatIsGreaterThanOrEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(double actual, double expected) {
      return Refaster.anyOf(
          assertThat(actual >= expected).isTrue(), assertThat(actual < expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractDoubleAssert<?> after(double actual, double expected) {
      return assertThat(actual).isGreaterThanOrEqualTo(expected);
    }
  }
}
