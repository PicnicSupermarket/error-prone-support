package tech.picnic.errorprone.refasterrules.output;

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
        assertThat(BigInteger.ZERO).isEqualTo(BigInteger.ONE));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isNotEqualTo(BigInteger.ONE),
        assertThat(BigInteger.ZERO).isNotEqualTo(BigInteger.ONE));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsZero() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isEqualTo(0),
        assertThat(BigInteger.ZERO).isEqualTo(0),
        assertThat(BigInteger.ZERO).isEqualTo(0));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsNotZero() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isNotEqualTo(0),
        assertThat(BigInteger.ZERO).isNotEqualTo(0),
        assertThat(BigInteger.ZERO).isNotEqualTo(0));
  }

  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsOne() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isEqualTo(1),
        assertThat(BigInteger.ZERO).isEqualTo(1),
        assertThat(BigInteger.ZERO).isEqualTo(1));
  }
}
