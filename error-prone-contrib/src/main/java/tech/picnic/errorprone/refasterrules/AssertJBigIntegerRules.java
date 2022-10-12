package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigInteger;
import org.assertj.core.api.AbstractBigIntegerAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

// XXX: If we add a rule that drops unnecessary `L` suffixes from literal longs, then the `0L`/`1L`
// cases below can go.
@OnlineDocumentation
final class AssertJBigIntegerRules {
  private AssertJBigIntegerRules() {}

  static final class AbstractBigIntegerAssertIsEqualTo {
    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(AbstractBigIntegerAssert<?> bigIntegerAssert, BigInteger n) {
      return Refaster.anyOf(
          bigIntegerAssert.isCloseTo(n, offset(BigInteger.ZERO)),
          bigIntegerAssert.isCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigIntegerAssert<?> after(AbstractBigIntegerAssert<?> bigIntegerAssert, BigInteger n) {
      return bigIntegerAssert.isEqualTo(n);
    }
  }

  static final class AbstractBigIntegerAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(AbstractBigIntegerAssert<?> bigIntegerAssert, BigInteger n) {
      return Refaster.anyOf(
          bigIntegerAssert.isNotCloseTo(n, offset(BigInteger.ZERO)),
          bigIntegerAssert.isNotCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigIntegerAssert<?> after(AbstractBigIntegerAssert<?> bigIntegerAssert, BigInteger n) {
      return bigIntegerAssert.isNotEqualTo(n);
    }
  }

  static final class AbstractBigIntegerAssertIsZero {
    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(AbstractBigIntegerAssert<?> bigIntegerAssert) {
      return Refaster.anyOf(
          bigIntegerAssert.isZero(),
          bigIntegerAssert.isEqualTo(0L),
          bigIntegerAssert.isEqualTo(BigInteger.ZERO));
    }

    @AfterTemplate
    AbstractBigIntegerAssert<?> after(AbstractBigIntegerAssert<?> bigIntegerAssert) {
      return bigIntegerAssert.isEqualTo(0);
    }
  }

  static final class AbstractBigIntegerAssertIsNotZero {
    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(AbstractBigIntegerAssert<?> bigIntegerAssert) {
      return Refaster.anyOf(
          bigIntegerAssert.isNotZero(),
          bigIntegerAssert.isNotEqualTo(0L),
          bigIntegerAssert.isNotEqualTo(BigInteger.ZERO));
    }

    @AfterTemplate
    AbstractBigIntegerAssert<?> after(AbstractBigIntegerAssert<?> bigIntegerAssert) {
      return bigIntegerAssert.isNotEqualTo(0);
    }
  }

  static final class AbstractBigIntegerAssertIsOne {
    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(AbstractBigIntegerAssert<?> bigIntegerAssert) {
      return Refaster.anyOf(
          bigIntegerAssert.isOne(),
          bigIntegerAssert.isEqualTo(1L),
          bigIntegerAssert.isEqualTo(BigInteger.ONE));
    }

    @AfterTemplate
    AbstractBigIntegerAssert<?> after(AbstractBigIntegerAssert<?> bigIntegerAssert) {
      return bigIntegerAssert.isEqualTo(1);
    }
  }
}
