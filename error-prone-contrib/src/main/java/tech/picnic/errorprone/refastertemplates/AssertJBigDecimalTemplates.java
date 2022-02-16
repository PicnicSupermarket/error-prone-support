package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.assertj.core.api.AbstractBigDecimalAssert;

// XXX: If we add a rule which drops unnecessary `L` suffixes from literal longs, then the `0L`/`1L`
// cases below can go.
final class AssertJBigDecimalTemplates {
  private AssertJBigDecimalTemplates() {}

  static final class AbstractBigDecimalAssertIsEqualTo {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> bigDecimalAssert, BigDecimal n) {
      return Refaster.anyOf(
          bigDecimalAssert.isCloseTo(n, offset(BigDecimal.ZERO)),
          bigDecimalAssert.isCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> bigDecimalAssert, BigDecimal n) {
      return bigDecimalAssert.isEqualTo(n);
    }
  }

  static final class AbstractBigDecimalAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> bigDecimalAssert, BigDecimal n) {
      return Refaster.anyOf(
          bigDecimalAssert.isNotCloseTo(n, offset(BigDecimal.ZERO)),
          bigDecimalAssert.isNotCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> bigDecimalAssert, BigDecimal n) {
      return bigDecimalAssert.isNotEqualTo(n);
    }
  }

  static final class AbstractBigDecimalAssertIsZero {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> bigDecimalAssert) {
      return Refaster.anyOf(
          bigDecimalAssert.isZero(),
          bigDecimalAssert.isEqualTo(0L),
          bigDecimalAssert.isEqualTo(BigDecimal.ZERO));
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> bigDecimalAssert) {
      return bigDecimalAssert.isEqualTo(0);
    }
  }

  static final class AbstractBigDecimalAssertIsNotZero {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> bigDecimalAssert) {
      return Refaster.anyOf(
          bigDecimalAssert.isNotZero(),
          bigDecimalAssert.isNotEqualTo(0L),
          bigDecimalAssert.isNotEqualTo(BigDecimal.ZERO));
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> bigDecimalAssert) {
      return bigDecimalAssert.isNotEqualTo(0);
    }
  }

  static final class AbstractBigDecimalAssertIsOne {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> bigDecimalAssert) {
      return Refaster.anyOf(
          bigDecimalAssert.isOne(),
          bigDecimalAssert.isEqualTo(1L),
          bigDecimalAssert.isEqualTo(BigDecimal.ONE));
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> bigDecimalAssert) {
      return bigDecimalAssert.isEqualTo(1);
    }
  }

  static final class AbstractBigDecimalAssertIsEqualToBigDecimal {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(
        AbstractBigDecimalAssert<?> abstractBigDecimalAssert, BigInteger bigDecimal) {
      return abstractBigDecimalAssert.isEqualTo(new BigDecimal(bigDecimal));
    }

    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(
        AbstractBigDecimalAssert<?> abstractBigDecimalAssert, char[] bigDecimal) {
      return abstractBigDecimalAssert.isEqualTo(new BigDecimal(bigDecimal));
    }

    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(
        AbstractBigDecimalAssert<?> abstractBigDecimalAssert, double bigDecimal) {
      return abstractBigDecimalAssert.isEqualTo(new BigDecimal(bigDecimal));
    }

    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(
        AbstractBigDecimalAssert<?> abstractBigDecimalAssert, int bigDecimal) {
      return abstractBigDecimalAssert.isEqualTo(new BigDecimal(bigDecimal));
    }

    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(
        AbstractBigDecimalAssert<?> abstractBigDecimalAssert, long bigDecimal) {
      return abstractBigDecimalAssert.isEqualTo(new BigDecimal(bigDecimal));
    }

    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(
        AbstractBigDecimalAssert<?> abstractBigDecimalAssert, String bigDecimal) {
      return abstractBigDecimalAssert.isEqualTo(new BigDecimal(bigDecimal));
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(
        AbstractBigDecimalAssert<?> abstractBigDecimalAssert, Object bigDecimal) {
      return abstractBigDecimalAssert.isEqualTo(bigDecimal);
    }
  }
}
