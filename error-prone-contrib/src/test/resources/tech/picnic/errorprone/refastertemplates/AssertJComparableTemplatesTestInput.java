import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.assertj.core.api.AbstractBooleanAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJComparableTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<AbstractBooleanAssert<?>> testAbstractComparableAssertActualIsLessThanExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE) < 0).isTrue(),
        assertThat(BigInteger.ZERO.compareTo(BigInteger.ONE) >= 0).isFalse());
  }

  ImmutableSet<AbstractBooleanAssert<?>>
      testAbstractComparableAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE) <= 0).isTrue(),
        assertThat(BigInteger.ZERO.compareTo(BigInteger.ONE) > 0).isFalse());
  }

  ImmutableSet<AbstractBooleanAssert<?>> testAbstractComparableAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE) > 0).isTrue(),
        assertThat(BigInteger.ZERO.compareTo(BigInteger.ONE) <= 0).isFalse());
  }

  ImmutableSet<AbstractBooleanAssert<?>>
      testAbstractComparableAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO.compareTo(BigDecimal.ONE) >= 0).isTrue(),
        assertThat(BigInteger.ZERO.compareTo(BigInteger.ONE) < 0).isFalse());
  }
}
