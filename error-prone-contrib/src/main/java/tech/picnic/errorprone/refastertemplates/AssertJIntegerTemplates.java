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
import org.assertj.core.api.AbstractIntegerAssert;

final class AssertJIntegerTemplates {
  private AssertJIntegerTemplates() {}

  static final class AbstractIntegerAssertIsEqualTo {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert, int n) {
      return Refaster.anyOf(
          intAssert.isCloseTo(n, offset(0)), intAssert.isCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert, int n) {
      return intAssert.isEqualTo(n);
    }
  }

  static final class AbstractIntegerAssertActualIsEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(int actual, int expected) {
      return Refaster.anyOf(
          assertThat(actual == expected).isTrue(), assertThat(actual != expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractIntegerAssert<?> after(int actual, int expected) {
      return assertThat(actual).isEqualTo(expected);
    }
  }

  static final class AbstractIntegerAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert, int n) {
      return Refaster.anyOf(
          intAssert.isNotCloseTo(n, offset(0)), intAssert.isNotCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert, int n) {
      return intAssert.isNotEqualTo(n);
    }
  }

  static final class AbstractIntegerAssertActualIsNotEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(int actual, int expected) {
      return Refaster.anyOf(
          assertThat(actual != expected).isTrue(), assertThat(actual == expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractIntegerAssert<?> after(int actual, int expected) {
      return assertThat(actual).isNotEqualTo(expected);
    }
  }

  static final class AbstractIntegerAssertIsZero {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isZero();
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isEqualTo(0);
    }
  }

  static final class AbstractIntegerAssertIsNotZero {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNotZero();
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNotEqualTo(0);
    }
  }

  static final class AbstractIntegerAssertIsOne {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isOne();
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isEqualTo(1);
    }
  }

  static final class AbstractIntegerAssertActualIsLessThanExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(int actual, int expected) {
      return Refaster.anyOf(
          assertThat(actual < expected).isTrue(), assertThat(actual >= expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractIntegerAssert<?> after(int actual, int expected) {
      return assertThat(actual).isLessThan(expected);
    }
  }

  static final class AbstractIntegerAssertActualIsLessThanOrEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(int actual, int expected) {
      return Refaster.anyOf(
          assertThat(actual <= expected).isTrue(), assertThat(actual > expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractIntegerAssert<?> after(int actual, int expected) {
      return assertThat(actual).isLessThanOrEqualTo(expected);
    }
  }

  static final class AbstractIntegerAssertActualIsGreaterThanExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(int actual, int expected) {
      return Refaster.anyOf(
          assertThat(actual > expected).isTrue(), assertThat(actual <= expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractIntegerAssert<?> after(int actual, int expected) {
      return assertThat(actual).isGreaterThan(expected);
    }
  }

  static final class AbstractIntegerAssertActualIsGreaterThanOrEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(int actual, int expected) {
      return Refaster.anyOf(
          assertThat(actual >= expected).isTrue(), assertThat(actual < expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractIntegerAssert<?> after(int actual, int expected) {
      return assertThat(actual).isGreaterThanOrEqualTo(expected);
    }
  }
}
