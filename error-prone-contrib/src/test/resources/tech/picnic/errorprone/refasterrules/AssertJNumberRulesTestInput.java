package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.assertj.core.api.NumberAssert;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class AssertJNumberRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsPositive() {
    return ImmutableSet.of(
        assertThat((byte) 1).isGreaterThan((byte) 0),
        assertThat((byte) 2).isGreaterThanOrEqualTo((byte) 1),
        assertThat((short) 1).isGreaterThan((short) 0),
        assertThat((short) 2).isGreaterThanOrEqualTo((short) 1),
        assertThat(1).isGreaterThan(0),
        assertThat(2).isGreaterThanOrEqualTo(1),
        assertThat(1L).isGreaterThan(0),
        assertThat(2L).isGreaterThanOrEqualTo(1),
        assertThat(1.0f).isGreaterThan(0),
        assertThat(1.0).isGreaterThan(0),
        assertThat(BigInteger.ONE).isGreaterThan(BigInteger.ZERO),
        assertThat(BigInteger.TWO).isGreaterThanOrEqualTo(BigInteger.valueOf(1)),
        assertThat(BigDecimal.ONE).isGreaterThan(BigDecimal.ZERO));
  }

  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNotPositive() {
    return ImmutableSet.of(
        assertThat((byte) 1).isLessThanOrEqualTo((byte) 0),
        assertThat((byte) 2).isLessThan((byte) 1),
        assertThat((short) 1).isLessThanOrEqualTo((short) 0),
        assertThat((short) 2).isLessThan((short) 1),
        assertThat(1).isLessThanOrEqualTo(0),
        assertThat(2).isLessThan(1),
        assertThat(1L).isLessThanOrEqualTo(0),
        assertThat(2L).isLessThan(1),
        assertThat(1.0f).isLessThanOrEqualTo(0),
        assertThat(1.0).isLessThanOrEqualTo(0),
        assertThat(BigInteger.ONE).isLessThanOrEqualTo(BigInteger.ZERO),
        assertThat(BigInteger.TWO).isLessThan(BigInteger.valueOf(1)),
        assertThat(BigDecimal.ONE).isLessThanOrEqualTo(BigDecimal.ZERO));
  }

  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNegative() {
    return ImmutableSet.of(
        assertThat((byte) -1).isLessThan((byte) 0),
        assertThat((byte) -2).isLessThanOrEqualTo((byte) -1),
        assertThat((short) -1).isLessThan((short) 0),
        assertThat((short) -2).isLessThanOrEqualTo((short) -1),
        assertThat(-1).isLessThan(0),
        assertThat(-2).isLessThanOrEqualTo(-1),
        assertThat(-1L).isLessThan(0),
        assertThat(-2L).isLessThanOrEqualTo(-1),
        assertThat(-1.0f).isLessThan(0),
        assertThat(-1.0).isLessThan(0),
        assertThat(BigInteger.valueOf(-1)).isLessThan(BigInteger.ZERO),
        assertThat(BigInteger.valueOf(-2)).isLessThanOrEqualTo(BigInteger.valueOf(-1)),
        assertThat(BigDecimal.valueOf(-1)).isLessThan(BigDecimal.ZERO));
  }

  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNotNegative() {
    return ImmutableSet.of(
        assertThat((byte) 1).isGreaterThanOrEqualTo((byte) 0),
        assertThat((byte) 2).isGreaterThan((byte) -1),
        assertThat((short) 1).isGreaterThanOrEqualTo((short) 0),
        assertThat((short) 2).isGreaterThan((short) -1),
        assertThat(1).isGreaterThanOrEqualTo(0),
        assertThat(2).isGreaterThan(-1),
        assertThat(1L).isGreaterThanOrEqualTo(0),
        assertThat(2L).isGreaterThan(-1),
        assertThat(1.0f).isGreaterThanOrEqualTo(0),
        assertThat(1.0).isGreaterThanOrEqualTo(0),
        assertThat(BigInteger.ONE).isGreaterThanOrEqualTo(BigInteger.ZERO),
        assertThat(BigInteger.TWO).isGreaterThan(BigInteger.valueOf(-1)),
        assertThat(BigDecimal.ONE).isGreaterThanOrEqualTo(BigDecimal.ZERO));
  }

  ImmutableSet<NumberAssert<?, ?>> testAssertThatIsOdd() {
    return ImmutableSet.of(
        assertThat((char) 1 % 2).isEqualTo(1),
        assertThat(1 % 2).isEqualTo(1),
        assertThat(1L % 2).isEqualTo(1));
  }

  ImmutableSet<NumberAssert<?, ?>> testAssertThatIsEven() {
    return ImmutableSet.of(
        assertThat((char) 1 % 2).isEqualTo(0),
        assertThat(1 % 2).isEqualTo(0),
        assertThat(1L % 2).isEqualTo(0));
  }
}
