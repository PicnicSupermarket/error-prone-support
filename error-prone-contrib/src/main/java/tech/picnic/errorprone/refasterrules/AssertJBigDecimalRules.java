package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigDecimal;
import org.assertj.core.api.AbstractBigDecimalAssert;
import org.assertj.core.api.BigDecimalAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/**
 * Refaster rules related to AssertJ assertions over {@link BigDecimal}s.
 *
 * <p>Note that, contrary to collections of Refaster rules for other {@link
 * org.assertj.core.api.NumberAssert} subtypes, these rules do not rewrite to/from {@link
 * BigDecimalAssert#isEqualTo(Object)} and {@link BigDecimalAssert#isNotEqualTo(Object)}. This is
 * because {@link BigDecimal#equals(Object)} considers not only the numeric value of compared
 * instances, but also their scale. As a result various seemingly straightforward transformations
 * would actually subtly change the assertion's semantics.
 */
@OnlineDocumentation
final class AssertJBigDecimalRules {
  private AssertJBigDecimalRules() {}

  /**
   * Prefer {@link AbstractBigDecimalAssert#isEqualByComparingTo(BigDecimal)} over more contrived
   * alternatives.
   */
  static final class AbstractBigDecimalAssertIsEqualByComparingTo {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(
        AbstractBigDecimalAssert<?> decimalAssert, BigDecimal other) {
      return Refaster.anyOf(
          decimalAssert.isCloseTo(other, offset(BigDecimal.ZERO)),
          decimalAssert.isCloseTo(other, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> decimalAssert, BigDecimal other) {
      return decimalAssert.isEqualByComparingTo(other);
    }
  }

  /**
   * Prefer {@link AbstractBigDecimalAssert#isNotEqualByComparingTo(BigDecimal)} over more contrived
   * alternatives.
   */
  static final class AbstractBigDecimalAssertIsNotEqualByComparingTo {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(
        AbstractBigDecimalAssert<?> decimalAssert, BigDecimal other) {
      return Refaster.anyOf(
          decimalAssert.isNotCloseTo(other, offset(BigDecimal.ZERO)),
          decimalAssert.isNotCloseTo(other, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> decimalAssert, BigDecimal other) {
      return decimalAssert.isNotEqualByComparingTo(other);
    }
  }
}
