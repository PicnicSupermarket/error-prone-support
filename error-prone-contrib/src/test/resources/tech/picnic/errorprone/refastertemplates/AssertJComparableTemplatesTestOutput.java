package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJComparableTemplatesTest implements RefasterTemplateTestCase {
  AbstractAssert<?, ?> testAbstractComparableAssertActualIsLessThanExpected() {
    return assertThat(BigDecimal.ZERO).isLessThan(BigDecimal.ONE);
  }

  AbstractAssert<?, ?> testAbstractComparableAssertActualIsLessThanOrEqualToExpected() {
    return assertThat(BigDecimal.ZERO).isLessThanOrEqualTo(BigDecimal.ONE);
  }

  AbstractAssert<?, ?> testAbstractComparableAssertActualIsGreaterThanExpected() {
    return assertThat(BigDecimal.ZERO).isGreaterThan(BigDecimal.ONE);
  }

  AbstractAssert<?, ?> testAbstractComparableAssertActualIsGreaterThanOrEqualToExpected() {
    return assertThat(BigDecimal.ZERO).isGreaterThanOrEqualTo(BigDecimal.ONE);
  }
}
