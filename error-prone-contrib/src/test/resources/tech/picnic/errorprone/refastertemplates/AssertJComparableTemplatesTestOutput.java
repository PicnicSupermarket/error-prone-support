import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.assertj.core.api.AbstractComparableAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJComparableTempletesTest implements RefasterTemplateTestCase {
  ImmutableSet<AbstractComparableAssert<?, ?>>
      testAbstractComparableAssertActualIsLessThanExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isLessThan(BigDecimal.ONE),
        assertThat(BigInteger.ZERO).isLessThan(BigInteger.ONE));
  }

  ImmutableSet<AbstractComparableAssert<?, ?>>
      testAbstractComparableAssertActualIsLessThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isLessThanOrEqualTo(BigDecimal.ONE),
        assertThat(BigInteger.ZERO).isLessThanOrEqualTo(BigInteger.ONE));
  }

  ImmutableSet<AbstractComparableAssert<?, ?>>
      testAbstractComparableAssertActualIsGreaterThanExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isGreaterThan(BigDecimal.ONE),
        assertThat(BigInteger.ZERO).isGreaterThan(BigInteger.ONE));
  }

  ImmutableSet<AbstractComparableAssert<?, ?>>
      testAbstractComparableAssertActualIsGreaterThanOrEqualToExpected() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isGreaterThanOrEqualTo(BigDecimal.ONE),
        assertThat(BigInteger.ZERO).isGreaterThanOrEqualTo(BigInteger.ONE));
  }
}
