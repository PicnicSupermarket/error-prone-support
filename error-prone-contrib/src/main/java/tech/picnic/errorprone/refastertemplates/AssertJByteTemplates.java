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
import org.assertj.core.api.AbstractByteAssert;

final class AssertJByteTemplates {
  private AssertJByteTemplates() {}

  static final class AbstractByteAssertIsEqualTo {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> byteAssert, byte n) {
      return Refaster.anyOf(
          byteAssert.isCloseTo(n, offset((byte) 0)), byteAssert.isCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractByteAssert<?> after(AbstractByteAssert<?> byteAssert, byte n) {
      return byteAssert.isEqualTo(n);
    }
  }

  static final class AbstractByteAssertActualIsEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(byte actual, byte expected) {
      return Refaster.anyOf(
          assertThat(actual == expected).isTrue(), assertThat(actual != expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractByteAssert<?> after(byte actual, byte expected) {
      return assertThat(actual).isEqualTo(expected);
    }
  }

  static final class AbstractByteAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> byteAssert, byte n) {
      return Refaster.anyOf(
          byteAssert.isNotCloseTo(n, offset((byte) 0)),
          byteAssert.isNotCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractByteAssert<?> after(AbstractByteAssert<?> byteAssert, byte n) {
      return byteAssert.isNotEqualTo(n);
    }
  }

  static final class AbstractByteAssertActualIsNotEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(byte actual, byte expected) {
      return Refaster.anyOf(
          assertThat(actual != expected).isTrue(), assertThat(actual == expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractByteAssert<?> after(byte actual, byte expected) {
      return assertThat(actual).isNotEqualTo(expected);
    }
  }

  static final class AbstractByteAssertIsZero {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> byteAssert) {
      return byteAssert.isZero();
    }

    @AfterTemplate
    AbstractByteAssert<?> after(AbstractByteAssert<?> byteAssert) {
      return byteAssert.isEqualTo((byte) 0);
    }
  }

  static final class AbstractByteAssertIsNotZero {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> byteAssert) {
      return byteAssert.isNotZero();
    }

    @AfterTemplate
    AbstractByteAssert<?> after(AbstractByteAssert<?> byteAssert) {
      return byteAssert.isNotEqualTo((byte) 0);
    }
  }

  static final class AbstractByteAssertIsOne {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> byteAssert) {
      return byteAssert.isOne();
    }

    @AfterTemplate
    AbstractByteAssert<?> after(AbstractByteAssert<?> byteAssert) {
      return byteAssert.isEqualTo((byte) 1);
    }
  }

  static final class AbstractByteAssertActualIsLessThanExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(byte actual, byte expected) {
      return Refaster.anyOf(
          assertThat(actual < expected).isTrue(), assertThat(actual >= expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractByteAssert<?> after(byte actual, byte expected) {
      return assertThat(actual).isLessThan(expected);
    }
  }

  static final class AbstractByteAssertActualIsLessThanOrEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(byte actual, byte expected) {
      return Refaster.anyOf(
          assertThat(actual <= expected).isTrue(), assertThat(actual > expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractByteAssert<?> after(byte actual, byte expected) {
      return assertThat(actual).isLessThanOrEqualTo(expected);
    }
  }

  static final class AbstractByteAssertActualIsGreaterThanExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(byte actual, byte expected) {
      return Refaster.anyOf(
          assertThat(actual > expected).isTrue(), assertThat(actual <= expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractByteAssert<?> after(byte actual, byte expected) {
      return assertThat(actual).isGreaterThan(expected);
    }
  }

  static final class AbstractByteAssertActualIsGreaterThanOrEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(byte actual, byte expected) {
      return Refaster.anyOf(
          assertThat(actual >= expected).isTrue(), assertThat(actual < expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractByteAssert<?> after(byte actual, byte expected) {
      return assertThat(actual).isGreaterThanOrEqualTo(expected);
    }
  }
}
