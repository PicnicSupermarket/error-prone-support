package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigDecimal;
import org.assertj.core.api.AbstractBigDecimalAssert;
import org.assertj.core.api.BigDecimalAssert;

/**
 * Refaster templates related to AssertJ assertions over {@link BigDecimal}s.
 *
 * <p>Note that, contrary to collections of Refaster templates for other {@link
 * org.assertj.core.api.NumberAssert} subtypes, these templates do not rewrite to/from {@link
 * BigDecimalAssert#isEqualTo(Object)} and {@link BigDecimalAssert#isNotEqualTo(Object)}. This is
 * because {@link BigDecimal#equals(Object)} considers not only the numeric value of compared
 * instances, but also their scale. As a result various seemingly straightforward transformations
 * would actually subtly change the assertion's semantics.
 */
final class AssertJBigDecimalTemplates {
  private AssertJBigDecimalTemplates() {}

  static final class AbstractBigDecimalAssertIsEqualByComparingTo {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> bigDecimalAssert, BigDecimal n) {
      return Refaster.anyOf(
          bigDecimalAssert.isCloseTo(n, offset(BigDecimal.ZERO)),
          bigDecimalAssert.isCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> bigDecimalAssert, BigDecimal n) {
      return bigDecimalAssert.isEqualByComparingTo(n);
    }
  }

  static final class AbstractBigDecimalAssertIsNotEqualByComparingTo {
    @BeforeTemplate
    AbstractBigDecimalAssert<?> before(AbstractBigDecimalAssert<?> bigDecimalAssert, BigDecimal n) {
      return Refaster.anyOf(
          bigDecimalAssert.isNotCloseTo(n, offset(BigDecimal.ZERO)),
          bigDecimalAssert.isNotCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractBigDecimalAssert<?> after(AbstractBigDecimalAssert<?> bigDecimalAssert, BigDecimal n) {
      return bigDecimalAssert.isNotEqualByComparingTo(n);
    }
  }
}
