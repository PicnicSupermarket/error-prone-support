package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import org.assertj.core.api.AbstractBigDecimalAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJBigDecimalRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsEqualByComparingTo() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isEqualByComparingTo(BigDecimal.ONE),
        assertThat(BigDecimal.ONE).isEqualByComparingTo(BigDecimal.TEN));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsNotEqualByComparingTo() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isNotEqualByComparingTo(BigDecimal.ONE),
        assertThat(BigDecimal.ONE).isNotEqualByComparingTo(BigDecimal.TEN));
  }
}
