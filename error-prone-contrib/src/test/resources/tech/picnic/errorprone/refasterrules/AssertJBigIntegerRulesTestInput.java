package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import java.math.BigInteger;
import org.assertj.core.api.AbstractBigIntegerAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJBigIntegerRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isCloseTo(BigInteger.ONE, offset(BigInteger.ZERO)),
        assertThat(BigInteger.ONE).isCloseTo(BigInteger.TWO, withPercentage(0)));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isNotCloseTo(BigInteger.ONE, offset(BigInteger.ZERO)),
        assertThat(BigInteger.ONE).isNotCloseTo(BigInteger.TWO, withPercentage(0)));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsZero() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isZero(),
        assertThat(BigInteger.ONE).isEqualTo(0L),
        assertThat(BigInteger.TWO).isEqualTo(BigInteger.ZERO));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsNotZero() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isNotZero(),
        assertThat(BigInteger.ONE).isNotEqualTo(0L),
        assertThat(BigInteger.TWO).isNotEqualTo(BigInteger.ZERO));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsOne() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isOne(),
        assertThat(BigInteger.ONE).isEqualTo(1L),
        assertThat(BigInteger.TWO).isEqualTo(BigInteger.ONE));
  }
}
