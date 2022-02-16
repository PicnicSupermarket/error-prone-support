package tech.picnic.errorprone.bugpatterns;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.assertj.core.api.AbstractBigDecimalAssert;

final class AssertJBigDecimalTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isCloseTo(BigDecimal.ONE, offset(BigDecimal.ZERO)),
        assertThat(BigDecimal.ZERO).isCloseTo(BigDecimal.ONE, withPercentage(0)));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isNotCloseTo(BigDecimal.ONE, offset(BigDecimal.ZERO)),
        assertThat(BigDecimal.ZERO).isNotCloseTo(BigDecimal.ONE, withPercentage(0)));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsZero() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isZero(),
        assertThat(BigDecimal.ZERO).isEqualTo(0L),
        assertThat(BigDecimal.ZERO).isEqualTo(BigDecimal.ZERO));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsNotZero() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isNotZero(),
        assertThat(BigDecimal.ZERO).isNotEqualTo(0L),
        assertThat(BigDecimal.ZERO).isNotEqualTo(BigDecimal.ZERO));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsOne() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isOne(),
        assertThat(BigDecimal.ZERO).isEqualTo(1L),
        assertThat(BigDecimal.ZERO).isEqualTo(BigDecimal.ONE));
  }

  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsEqualToBigDecimal() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isEqualTo(new BigDecimal(BigInteger.ONE)),
        assertThat(BigDecimal.ZERO).isEqualTo(new BigDecimal(new char[] {'1'})),
        assertThat(BigDecimal.ZERO).isEqualTo(new BigDecimal(1D)),
        assertThat(BigDecimal.ZERO).isEqualTo(new BigDecimal(1)),
        assertThat(BigDecimal.ZERO).isEqualTo(new BigDecimal(1L)),
        assertThat(BigDecimal.ZERO).isEqualTo(new BigDecimal("1")));
  }
}
