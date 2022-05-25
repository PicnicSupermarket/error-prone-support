package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import java.math.BigInteger;
import org.assertj.core.api.AbstractBigIntegerAssert;

final class AssertJBigIntegerTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isCloseTo(BigInteger.ONE, offset(BigInteger.ZERO)),
        assertThat(BigInteger.ZERO).isCloseTo(BigInteger.ONE, withPercentage(0)));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isNotCloseTo(BigInteger.ONE, offset(BigInteger.ZERO)),
        assertThat(BigInteger.ZERO).isNotCloseTo(BigInteger.ONE, withPercentage(0)));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsZero() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isZero(),
        assertThat(BigInteger.ZERO).isEqualTo(0L),
        assertThat(BigInteger.ZERO).isEqualTo(BigInteger.ZERO));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsNotZero() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isNotZero(),
        assertThat(BigInteger.ZERO).isNotEqualTo(0L),
        assertThat(BigInteger.ZERO).isNotEqualTo(BigInteger.ZERO));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsOne() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isOne(),
        assertThat(BigInteger.ZERO).isEqualTo(1L),
        assertThat(BigInteger.ZERO).isEqualTo(BigInteger.ONE));
  }
}
