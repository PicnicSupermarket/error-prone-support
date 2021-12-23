package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.assertj.core.api.AbstractBigDecimalAssert;
import org.assertj.core.api.AbstractBigIntegerAssert;
import org.assertj.core.api.AbstractByteAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.api.AbstractFloatAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractShortAssert;
import org.assertj.core.api.NumberAssert;

final class AssertJNumberTemplates {
  private AssertJNumberTemplates() {}

  static final class NumberAssertIsPositive {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> numberAssert) {
      return Refaster.anyOf(
          numberAssert.isGreaterThan((byte) 0), numberAssert.isGreaterThanOrEqualTo((byte) 1));
    }

    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> numberAssert) {
      return Refaster.anyOf(
          numberAssert.isGreaterThan((short) 0), numberAssert.isGreaterThanOrEqualTo((short) 1));
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> numberAssert) {
      return Refaster.anyOf(numberAssert.isGreaterThan(0), numberAssert.isGreaterThanOrEqualTo(1));
    }

    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> numberAssert) {
      return Refaster.anyOf(numberAssert.isGreaterThan(0), numberAssert.isGreaterThanOrEqualTo(1));
    }

    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> numberAssert) {
      return numberAssert.isGreaterThan(0);
    }

    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> numberAssert) {
      return numberAssert.isGreaterThan(0);
    }

    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(AbstractBigIntegerAssert<?> numberAssert) {
      return Refaster.anyOf(
          numberAssert.isGreaterThan(BigInteger.ZERO),
          numberAssert.isGreaterThanOrEqualTo(BigInteger.valueOf(1)));
    }

    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> numberAssert) {
      return numberAssert.isGreaterThan(BigDecimal.ZERO);
    }

    @AfterTemplate
    NumberAssert<?, ?> after(NumberAssert<?, ?> numberAssert) {
      return numberAssert.isPositive();
    }
  }

  static final class NumberAssertIsNotPositive {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> numberAssert) {
      return Refaster.anyOf(
          numberAssert.isLessThanOrEqualTo((byte) 0), numberAssert.isLessThan((byte) 1));
    }

    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> numberAssert) {
      return Refaster.anyOf(
          numberAssert.isLessThanOrEqualTo((short) 0), numberAssert.isLessThan((short) 1));
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> numberAssert) {
      return Refaster.anyOf(numberAssert.isLessThanOrEqualTo(0), numberAssert.isLessThan(1));
    }

    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> numberAssert) {
      return Refaster.anyOf(numberAssert.isLessThanOrEqualTo(0), numberAssert.isLessThan(1));
    }

    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> numberAssert) {
      return numberAssert.isLessThanOrEqualTo(0);
    }

    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> numberAssert) {
      return numberAssert.isLessThanOrEqualTo(0);
    }

    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(AbstractBigIntegerAssert<?> numberAssert) {
      return Refaster.anyOf(
          numberAssert.isLessThanOrEqualTo(BigInteger.ZERO),
          numberAssert.isLessThan(BigInteger.valueOf(1)));
    }

    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> numberAssert) {
      return numberAssert.isLessThanOrEqualTo(BigDecimal.ZERO);
    }

    @AfterTemplate
    NumberAssert<?, ?> after(NumberAssert<?, ?> numberAssert) {
      return numberAssert.isNotPositive();
    }
  }

  static final class NumberAssertIsNegative {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> numberAssert) {
      return Refaster.anyOf(
          numberAssert.isLessThan((byte) 0), numberAssert.isLessThanOrEqualTo((byte) -1));
    }

    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> numberAssert) {
      return Refaster.anyOf(
          numberAssert.isLessThan((short) 0), numberAssert.isLessThanOrEqualTo((short) -1));
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> numberAssert) {
      return Refaster.anyOf(numberAssert.isLessThan(0), numberAssert.isLessThanOrEqualTo(-1));
    }

    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> numberAssert) {
      return Refaster.anyOf(numberAssert.isLessThan(0), numberAssert.isLessThanOrEqualTo(-1));
    }

    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> numberAssert) {
      return numberAssert.isLessThan(0);
    }

    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> numberAssert) {
      return numberAssert.isLessThan(0);
    }

    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(AbstractBigIntegerAssert<?> numberAssert) {
      return Refaster.anyOf(
          numberAssert.isLessThan(BigInteger.ZERO),
          numberAssert.isLessThanOrEqualTo(BigInteger.valueOf(-1)));
    }

    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> numberAssert) {
      return numberAssert.isLessThan(BigDecimal.ZERO);
    }

    @AfterTemplate
    NumberAssert<?, ?> after(NumberAssert<?, ?> numberAssert) {
      return numberAssert.isNegative();
    }
  }

  static final class NumberAssertIsNotNegative {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> numberAssert) {
      return Refaster.anyOf(
          numberAssert.isGreaterThanOrEqualTo((byte) 0), numberAssert.isGreaterThan((byte) -1));
    }

    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> numberAssert) {
      return Refaster.anyOf(
          numberAssert.isGreaterThanOrEqualTo((short) 0), numberAssert.isGreaterThan((short) -1));
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> numberAssert) {
      return Refaster.anyOf(numberAssert.isGreaterThanOrEqualTo(0), numberAssert.isGreaterThan(-1));
    }

    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> numberAssert) {
      return Refaster.anyOf(numberAssert.isGreaterThanOrEqualTo(0), numberAssert.isGreaterThan(-1));
    }

    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> numberAssert) {
      return numberAssert.isGreaterThanOrEqualTo(0);
    }

    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> numberAssert) {
      return numberAssert.isGreaterThanOrEqualTo(0);
    }

    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(AbstractBigIntegerAssert<?> numberAssert) {
      return Refaster.anyOf(
          numberAssert.isGreaterThanOrEqualTo(BigInteger.ZERO),
          numberAssert.isGreaterThan(BigInteger.valueOf(-1)));
    }

    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> numberAssert) {
      return numberAssert.isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @AfterTemplate
    NumberAssert<?, ?> after(NumberAssert<?, ?> numberAssert) {
      return numberAssert.isNotNegative();
    }
  }
}
