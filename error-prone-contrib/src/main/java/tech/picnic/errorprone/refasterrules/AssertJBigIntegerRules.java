package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigInteger;
import org.assertj.core.api.AbstractBigIntegerAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to AssertJ assertions over {@link BigInteger}s. */
// XXX: If we add a rule that drops unnecessary `L` suffixes from literal longs, then the `0L`/`1L`
// cases below can go.
@OnlineDocumentation
final class AssertJBigIntegerRules {
  private AssertJBigIntegerRules() {}

  /** Prefer {@code isEqualTo(n)} over more contrived alternatives. */
  static final class AbstractBigIntegerAssertIsEqualTo {
    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(
        AbstractBigIntegerAssert<?> bigIntegerAssert, BigInteger expected) {
      return Refaster.anyOf(
          bigIntegerAssert.isCloseTo(expected, offset(BigInteger.ZERO)),
          bigIntegerAssert.isCloseTo(expected, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigIntegerAssert<?> after(
        AbstractBigIntegerAssert<?> bigIntegerAssert, BigInteger expected) {
      return bigIntegerAssert.isEqualTo(expected);
    }
  }

  /** Prefer {@code isNotEqualTo(n)} over more contrived alternatives. */
  static final class AbstractBigIntegerAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(
        AbstractBigIntegerAssert<?> bigIntegerAssert, BigInteger other) {
      return Refaster.anyOf(
          bigIntegerAssert.isNotCloseTo(other, offset(BigInteger.ZERO)),
          bigIntegerAssert.isNotCloseTo(other, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigIntegerAssert<?> after(
        AbstractBigIntegerAssert<?> bigIntegerAssert, BigInteger other) {
      return bigIntegerAssert.isNotEqualTo(other);
    }
  }

  /** Prefer {@code isEqualTo(0)} over more contrived alternatives. */
  static final class AbstractBigIntegerAssertIsEqualToZero {
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

  /** Prefer {@code isNotEqualTo(0)} over more contrived alternatives. */
  static final class AbstractBigIntegerAssertIsNotEqualToZero {
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

  /** Prefer {@code isEqualTo(1)} over more contrived alternatives. */
  static final class AbstractBigIntegerAssertIsEqualToOne {
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
