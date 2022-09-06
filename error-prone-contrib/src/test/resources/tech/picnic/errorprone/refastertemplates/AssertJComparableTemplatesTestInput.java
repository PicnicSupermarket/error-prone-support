package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJComparableTemplatesTest implements RefasterTemplateTestCase {
  AbstractAssert<?, ?> testAbstractComparableAssertActualIsLessThanExpected() {
    return assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE)).isNegative();
  }

  AbstractAssert<?, ?> testAbstractComparableAssertActualIsLessThanOrEqualToExpected() {
    return assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE)).isNotPositive();
  }

  AbstractAssert<?, ?> testAbstractComparableAssertActualIsGreaterThanExpected() {
    return assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE)).isPositive();
  }

  AbstractAssert<?, ?> testAbstractComparableAssertActualIsGreaterThanOrEqualToExpected() {
    return assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE)).isNotNegative();
  }
}
