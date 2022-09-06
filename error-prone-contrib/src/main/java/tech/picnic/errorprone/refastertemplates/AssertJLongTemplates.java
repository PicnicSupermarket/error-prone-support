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
import org.assertj.core.api.AbstractLongAssert;

final class AssertJLongTemplates {
  private AssertJLongTemplates() {}

  static final class AbstractLongAssertIsEqualTo {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert, long n) {
      return Refaster.anyOf(
          longAssert.isCloseTo(n, offset(0L)), longAssert.isCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert, long n) {
      return longAssert.isEqualTo(n);
    }
  }

  static final class AbstractLongAssertActualIsEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(long actual, long expected) {
      return Refaster.anyOf(
          assertThat(actual == expected).isTrue(), assertThat(actual != expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractLongAssert<?> after(long actual, long expected) {
      return assertThat(actual).isEqualTo(expected);
    }
  }

  static final class AbstractLongAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert, long n) {
      return Refaster.anyOf(
          longAssert.isNotCloseTo(n, offset(0L)), longAssert.isNotCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert, long n) {
      return longAssert.isNotEqualTo(n);
    }
  }

  static final class AbstractLongAssertActualIsNotEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(long actual, long expected) {
      return Refaster.anyOf(
          assertThat(actual != expected).isTrue(), assertThat(actual == expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractLongAssert<?> after(long actual, long expected) {
      return assertThat(actual).isNotEqualTo(expected);
    }
  }

  static final class AbstractLongAssertIsZero {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert) {
      return longAssert.isZero();
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert) {
      return longAssert.isEqualTo(0);
    }
  }

  static final class AbstractLongAssertIsNotZero {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert) {
      return longAssert.isNotZero();
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert) {
      return longAssert.isNotEqualTo(0);
    }
  }

  static final class AbstractLongAssertIsOne {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert) {
      return longAssert.isOne();
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert) {
      return longAssert.isEqualTo(1);
    }
  }

  static final class AbstractLongAssertActualIsLessThanExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(long actual, long expected) {
      return Refaster.anyOf(
          assertThat(actual < expected).isTrue(), assertThat(actual >= expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractLongAssert<?> after(long actual, long expected) {
      return assertThat(actual).isLessThan(expected);
    }
  }

  static final class AbstractLongAssertActualIsLessThanOrEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(long actual, long expected) {
      return Refaster.anyOf(
          assertThat(actual <= expected).isTrue(), assertThat(actual > expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractLongAssert<?> after(long actual, long expected) {
      return assertThat(actual).isLessThanOrEqualTo(expected);
    }
  }

  static final class AbstractLongAssertActualIsGreaterThanExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(long actual, long expected) {
      return Refaster.anyOf(
          assertThat(actual > expected).isTrue(), assertThat(actual <= expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractLongAssert<?> after(long actual, long expected) {
      return assertThat(actual).isGreaterThan(expected);
    }
  }

  static final class AbstractLongAssertActualIsGreaterThanOrEqualToExpected {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(long actual, long expected) {
      return Refaster.anyOf(
          assertThat(actual >= expected).isTrue(), assertThat(actual < expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractLongAssert<?> after(long actual, long expected) {
      return assertThat(actual).isGreaterThanOrEqualTo(expected);
    }
  }
}
