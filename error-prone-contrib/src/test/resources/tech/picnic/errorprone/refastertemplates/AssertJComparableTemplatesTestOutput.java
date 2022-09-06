import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJComparableTempletesTest implements RefasterTemplateTestCase {
  ImmutableSet<AbstractAssert<?, ?>> testAbstractComparableAssertActualIsLessThanExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isLessThan(BigDecimal.ONE),
        assertThat(BigDecimal.ZERO).isLessThan(BigDecimal.ONE));
  }

  ImmutableSet<AbstractAssert<?, ?>>
      testAbstractComparableAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isLessThanOrEqualTo(BigDecimal.ONE),
        assertThat(BigDecimal.ZERO).isLessThanOrEqualTo(BigDecimal.ONE));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractComparableAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isGreaterThan(BigDecimal.ONE),
        assertThat(BigDecimal.ZERO).isGreaterThan(BigDecimal.ONE));
  }

  ImmutableSet<AbstractAssert<?, ?>>
      testAbstractComparableAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isGreaterThanOrEqualTo(BigDecimal.ONE),
        assertThat(BigDecimal.ZERO).isGreaterThanOrEqualTo(BigDecimal.ONE));
  }
}
