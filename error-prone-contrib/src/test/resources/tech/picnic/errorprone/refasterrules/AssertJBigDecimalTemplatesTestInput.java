package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import org.assertj.core.api.AbstractBigDecimalAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJBigDecimalTemplatesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsEqualByComparingTo() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isCloseTo(BigDecimal.ONE, offset(BigDecimal.ZERO)),
        assertThat(BigDecimal.ZERO).isCloseTo(BigDecimal.ONE, withPercentage(0)));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsNotEqualByComparingTo() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isNotCloseTo(BigDecimal.ONE, offset(BigDecimal.ZERO)),
        assertThat(BigDecimal.ZERO).isNotCloseTo(BigDecimal.ONE, withPercentage(0)));
  }
}
