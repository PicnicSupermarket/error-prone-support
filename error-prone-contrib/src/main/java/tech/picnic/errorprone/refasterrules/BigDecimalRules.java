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

  /** Prefer {@link BigDecimal#ZERO} over less efficient alternatives. */
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

  /** Prefer {@link BigDecimal#ONE} over less efficient alternatives. */
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

  /** Prefer {@link BigDecimal#TWO} over less efficient alternatives. */
  static final class BigDecimalTwo {
    @BeforeTemplate
    BigDecimal before() {
      return Refaster.anyOf(BigDecimal.valueOf(2), new BigDecimal("2"));
    }

    @AfterTemplate
    BigDecimal after() {
      return BigDecimal.TWO;
    }
  }

  /** Prefer {@link BigDecimal#TEN} over less efficient alternatives. */
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

  /**
   * Prefer {@link BigDecimal#valueOf(double)} over the associated constructor.
   *
   * <p><strong>Warning:</strong> this rewrite changes the {@link BigDecimal} value created, as
   * {@link BigDecimal#valueOf(double)} uses the double's canonical string representation, while
   * {@link BigDecimal#BigDecimal(double)} uses the exact binary floating-point value.
   */
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

  /** Prefer a {@link BigDecimal#signum()} comparison to 0 over less explicit alternatives. */
  static final class BigDecimalSignumEqualToZero {
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
   * Prefer a {@link BigDecimal#signum()} comparison to 1 over less efficient or less explicit
   * alternatives.
   */
  static final class BigDecimalSignumEqualToOne {
    @BeforeTemplate
    boolean before(BigDecimal value) {
      return Refaster.anyOf(
          value.compareTo(BigDecimal.ZERO) > 0,
          BigDecimal.ZERO.compareTo(value) < 0,
          value.signum() > 0,
          value.signum() >= 1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(BigDecimal value) {
      return value.signum() == 1;
    }
  }

  /**
   * Prefer a {@link BigDecimal#signum()} comparison to -1 over less efficient or less explicit
   * alternatives.
   */
  static final class BigDecimalSignumEqualToNegativeOne {
    @BeforeTemplate
    boolean before(BigDecimal value) {
      return Refaster.anyOf(
          value.compareTo(BigDecimal.ZERO) < 0,
          BigDecimal.ZERO.compareTo(value) > 0,
          value.signum() < 0,
          value.signum() <= -1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(BigDecimal value) {
      return value.signum() == -1;
    }
  }
}
