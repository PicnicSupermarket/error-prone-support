package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.assertj.core.api.AbstractComparableAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJComparableTemplatesTest implements RefasterRuleCollectionTestCase {
  AbstractComparableAssert<?, ?> testAssertThatIsEqualByComparingTo() {
    return assertThat(BigDecimal.ZERO).isEqualByComparingTo(BigDecimal.ONE);
  }

  AbstractComparableAssert<?, ?> testAssertThatIsNotEqualByComparingTo() {
    return assertThat(BigDecimal.ZERO).isNotEqualByComparingTo(BigDecimal.ONE);
  }

  AbstractComparableAssert<?, ?> testAssertThatIsLessThan() {
    return assertThat(BigDecimal.ZERO).isLessThan(BigDecimal.ONE);
  }

  AbstractComparableAssert<?, ?> testAssertThatIsLessThanOrEqualTo() {
    return assertThat(BigDecimal.ZERO).isLessThanOrEqualTo(BigDecimal.ONE);
  }

  AbstractComparableAssert<?, ?> testAssertThatIsGreaterThan() {
    return assertThat(BigDecimal.ZERO).isGreaterThan(BigDecimal.ONE);
  }

  AbstractComparableAssert<?, ?> testAssertThatIsGreaterThanOrEqualTo() {
    return assertThat(BigDecimal.ZERO).isGreaterThanOrEqualTo(BigDecimal.ONE);
  }
}
