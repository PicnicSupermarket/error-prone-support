package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigDecimal;

/** Refaster rules related to expressions dealing with {@link BigDecimal}s. */
final class BigDecimalRules {
  private BigDecimalRules() {}

  /** Prefer using the constant {@link BigDecimal#ZERO} when possible. */
  static final class BigDecimalZero {
    @BeforeTemplate
    BigDecimal before() {
      return Refaster.anyOf(BigDecimal.valueOf(0), new BigDecimal("0"));
    }

    @AfterTemplate
    BigDecimal after() {
      return BigDecimal.ZERO;
    }
  }

  /** Prefer using the constant {@link BigDecimal#ONE} when possible. */
  static final class BigDecimalOne {
    @BeforeTemplate
    BigDecimal before() {
      return Refaster.anyOf(BigDecimal.valueOf(1), new BigDecimal("1"));
    }

    @AfterTemplate
    BigDecimal after() {
      return BigDecimal.ONE;
    }
  }

  /** Prefer using the constant {@link BigDecimal#TEN} when possible. */
  static final class BigDecimalTen {
    @BeforeTemplate
    BigDecimal before() {
      return Refaster.anyOf(BigDecimal.valueOf(10), new BigDecimal("10"));
    }

    @AfterTemplate
    BigDecimal after() {
      return BigDecimal.TEN;
    }
  }

  /** Prefer {@link BigDecimal#valueOf(long)} over the associated constructor. */
  // XXX: Ideally we'd also rewrite `BigDecimal.valueOf("<some-integer-value>")`, but it doesn't
  // appear that's currently possible with Error Prone.
  static final class BigDecimalFactoryMethod {
    @BeforeTemplate
    BigDecimal before(long value) {
      return new BigDecimal(value);
    }

    @AfterTemplate
    BigDecimal after(long value) {
      return BigDecimal.valueOf(value);
    }
  }
}
