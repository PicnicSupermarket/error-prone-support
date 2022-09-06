package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import org.assertj.core.api.AbstractAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJComparableTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<AbstractAssert<?, ?>> testAbstractComparableAssertActualIsLessThanExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE) < 0).isTrue(),
        assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE) >= 0).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>>
      testAbstractComparableAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE) <= 0).isTrue(),
        assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE) > 0).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>> testAbstractComparableAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE) > 0).isTrue(),
        assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE) <= 0).isFalse());
  }

  ImmutableSet<AbstractAssert<?, ?>>
      testAbstractComparableAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE) >= 0).isTrue(),
        assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE) < 0).isFalse());
  }
}
