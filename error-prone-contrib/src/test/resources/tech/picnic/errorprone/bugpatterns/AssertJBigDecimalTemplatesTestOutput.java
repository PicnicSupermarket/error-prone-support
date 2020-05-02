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

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsZero() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isEqualTo(0),
        assertThat(BigDecimal.ZERO).isEqualTo(0),
        assertThat(BigDecimal.ZERO).isEqualTo(0));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsNotZero() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isNotEqualTo(0),
        assertThat(BigDecimal.ZERO).isNotEqualTo(0),
        assertThat(BigDecimal.ZERO).isNotEqualTo(0));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsOne() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isEqualTo(1),
        assertThat(BigDecimal.ZERO).isEqualTo(1),
        assertThat(BigDecimal.ZERO).isEqualTo(1));
  }
}
