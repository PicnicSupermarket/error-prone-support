package tech.picnic.errorprone.refasterrules;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
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
    @SuppressWarnings("java:S2111" /* This violation will be rewritten. */)
    BigDecimal before(double value) {
      return new BigDecimal(value);
    }

    @AfterTemplate
    BigDecimal after(double value) {
      return BigDecimal.valueOf(value);
    }
  }

  /** Prefer using {@link BigDecimal#signum()} over more contrived alternatives. */
  static final class BigDecimalSignumIsZero {
    @BeforeTemplate
    boolean before(BigDecimal value) {
      return Refaster.anyOf(
          value.compareTo(BigDecimal.ZERO) == 0, BigDecimal.ZERO.compareTo(value) == 0);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(BigDecimal value) {
      return value.signum() == 0;
    }
  }

  /**
   * Prefer a {@link BigDecimal#signum()} comparison to 0 over more contrived or less clear
   * alternatives.
   */
  static final class BigDecimalSignumIsPositive {
    @BeforeTemplate
    boolean before(BigDecimal value) {
      return Refaster.anyOf(
          value.compareTo(BigDecimal.ZERO) > 0,
          BigDecimal.ZERO.compareTo(value) < 0,
          value.signum() == 1,
          value.signum() >= 1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(BigDecimal value) {
      return value.signum() > 0;
    }
  }

  /**
   * Prefer a {@link BigDecimal#signum()} comparison to 0 over more contrived or less clear
   * alternatives.
   */
  static final class BigDecimalSignumIsNegative {
    @BeforeTemplate
    boolean before(BigDecimal value) {
      return Refaster.anyOf(
          value.compareTo(BigDecimal.ZERO) < 0,
          BigDecimal.ZERO.compareTo(value) > 0,
          value.signum() == -1,
          value.signum() <= -1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(BigDecimal value) {
      return value.signum() < 0;
    }
  }
}
