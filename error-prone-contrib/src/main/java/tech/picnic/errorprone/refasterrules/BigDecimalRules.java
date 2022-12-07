package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigDecimal;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with {@link BigDecimal}s. */
@OnlineDocumentation
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

  /** Prefer {@link BigDecimal#valueOf(double)} over the associated constructor. */
  // XXX: Ideally we also rewrite `new BigDecimal("<some-integer-value>")` in cases where the
  // specified number can be represented as an `int` or `long`, but that requires a custom
  // `BugChecker`.
  static final class BigDecimalValueOf {
    @BeforeTemplate
    BigDecimal before(double value) {
      return new BigDecimal(value);
    }

    @AfterTemplate
    BigDecimal after(double value) {
      return BigDecimal.valueOf(value);
    }
  }
}
