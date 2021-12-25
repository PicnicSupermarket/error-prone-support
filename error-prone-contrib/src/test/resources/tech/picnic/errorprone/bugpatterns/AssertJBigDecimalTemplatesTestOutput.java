package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import org.assertj.core.api.AbstractBigDecimalAssert;

final class AssertJBigDecimalTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isEqualTo(BigDecimal.ONE),
        assertThat(BigDecimal.ZERO).isEqualTo(BigDecimal.ONE));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isNotEqualTo(BigDecimal.ONE),
        assertThat(BigDecimal.ZERO).isNotEqualTo(BigDecimal.ONE));
  }

  AbstractBigDecimalAssert<?> testAbstractBigDecimalAssertIsZero() {
    return assertThat(BigDecimal.ZERO).isZero();
  }

  AbstractBigDecimalAssert<?> testAbstractBigDecimalAssertIsNotZero() {
    return assertThat(BigDecimal.ZERO).isNotZero();
  }

  AbstractBigDecimalAssert<?> testAbstractBigDecimalAssertIsOne() {
    return assertThat(BigDecimal.ZERO).isOne();
  }
}
