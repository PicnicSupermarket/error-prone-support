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
        AbstractBigIntegerAssert<?> integerAssert, BigInteger expected) {
      return Refaster.anyOf(
          integerAssert.isCloseTo(expected, offset(BigInteger.ZERO)),
          integerAssert.isCloseTo(expected, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigIntegerAssert<?> after(
        AbstractBigIntegerAssert<?> integerAssert, BigInteger expected) {
      return integerAssert.isEqualTo(expected);
    }
  }

  /** Prefer {@code isNotEqualTo(n)} over more contrived alternatives. */
  static final class AbstractBigIntegerAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(
        AbstractBigIntegerAssert<?> integerAssert, BigInteger other) {
      return Refaster.anyOf(
          integerAssert.isNotCloseTo(other, offset(BigInteger.ZERO)),
          integerAssert.isNotCloseTo(other, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigIntegerAssert<?> after(AbstractBigIntegerAssert<?> integerAssert, BigInteger other) {
      return integerAssert.isNotEqualTo(other);
    }
  }

  /** Prefer {@code isEqualTo(0)} over more contrived alternatives. */
  static final class AbstractBigIntegerAssertIsEqualToZero {
    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(AbstractBigIntegerAssert<?> integerAssert) {
      return Refaster.anyOf(
          integerAssert.isZero(),
          integerAssert.isEqualTo(0L),
          integerAssert.isEqualTo(BigInteger.ZERO));
    }

    @AfterTemplate
    AbstractBigIntegerAssert<?> after(AbstractBigIntegerAssert<?> integerAssert) {
      return integerAssert.isEqualTo(0);
    }
  }

  /** Prefer {@code isNotEqualTo(0)} over more contrived alternatives. */
  static final class AbstractBigIntegerAssertIsNotEqualToZero {
    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(AbstractBigIntegerAssert<?> integerAssert) {
      return Refaster.anyOf(
          integerAssert.isNotZero(),
          integerAssert.isNotEqualTo(0L),
          integerAssert.isNotEqualTo(BigInteger.ZERO));
    }

    @AfterTemplate
    AbstractBigIntegerAssert<?> after(AbstractBigIntegerAssert<?> integerAssert) {
      return integerAssert.isNotEqualTo(0);
    }
  }

  /** Prefer {@code isEqualTo(1)} over more contrived alternatives. */
  static final class AbstractBigIntegerAssertIsEqualToOne {
    @BeforeTemplate
    AbstractBigIntegerAssert<?> before(AbstractBigIntegerAssert<?> integerAssert) {
      return Refaster.anyOf(
          integerAssert.isOne(),
          integerAssert.isEqualTo(1L),
          integerAssert.isEqualTo(BigInteger.ONE));
    }

    @AfterTemplate
    AbstractBigIntegerAssert<?> after(AbstractBigIntegerAssert<?> integerAssert) {
      return integerAssert.isEqualTo(1);
    }
  }
}
