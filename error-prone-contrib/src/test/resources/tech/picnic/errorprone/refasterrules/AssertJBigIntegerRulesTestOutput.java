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
        assertThat(BigInteger.ZERO).isEqualTo(BigInteger.ONE),
        assertThat(BigInteger.ONE).isEqualTo(BigInteger.TWO));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isNotEqualTo(BigInteger.ONE),
        assertThat(BigInteger.ONE).isNotEqualTo(BigInteger.TWO));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsEqualToZero() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isEqualTo(0),
        assertThat(BigInteger.ONE).isEqualTo(0),
        assertThat(BigInteger.TWO).isEqualTo(0));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsNotEqualToZero() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isNotEqualTo(0),
        assertThat(BigInteger.ONE).isNotEqualTo(0),
        assertThat(BigInteger.TWO).isNotEqualTo(0));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsEqualToOne() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isEqualTo(1),
        assertThat(BigInteger.ONE).isEqualTo(1),
        assertThat(BigInteger.TWO).isEqualTo(1));
  }
}
