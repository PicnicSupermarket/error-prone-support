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
        assertThat((byte) 1).isPositive(),
        assertThat((byte) 2).isPositive(),
        assertThat((short) 1).isPositive(),
        assertThat((short) 2).isPositive(),
        assertThat(1).isPositive(),
        assertThat(2).isPositive(),
        assertThat(1L).isPositive(),
        assertThat(2L).isPositive(),
        assertThat(1.0f).isPositive(),
        assertThat(1.0).isPositive(),
        assertThat(BigInteger.ONE).isPositive(),
        assertThat(BigInteger.TWO).isPositive(),
        assertThat(BigDecimal.ONE).isPositive());
  }

  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNotPositive() {
    return ImmutableSet.of(
        assertThat((byte) 1).isNotPositive(),
        assertThat((byte) 2).isNotPositive(),
        assertThat((short) 1).isNotPositive(),
        assertThat((short) 2).isNotPositive(),
        assertThat(1).isNotPositive(),
        assertThat(2).isNotPositive(),
        assertThat(1L).isNotPositive(),
        assertThat(2L).isNotPositive(),
        assertThat(1.0f).isNotPositive(),
        assertThat(1.0).isNotPositive(),
        assertThat(BigInteger.ONE).isNotPositive(),
        assertThat(BigInteger.TWO).isNotPositive(),
        assertThat(BigDecimal.ONE).isNotPositive());
  }

  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNegative() {
    return ImmutableSet.of(
        assertThat((byte) -1).isNegative(),
        assertThat((byte) -2).isNegative(),
        assertThat((short) -1).isNegative(),
        assertThat((short) -2).isNegative(),
        assertThat(-1).isNegative(),
        assertThat(-2).isNegative(),
        assertThat(-1L).isNegative(),
        assertThat(-2L).isNegative(),
        assertThat(-1.0f).isNegative(),
        assertThat(-1.0).isNegative(),
        assertThat(BigInteger.valueOf(-1)).isNegative(),
        assertThat(BigInteger.valueOf(-2)).isNegative(),
        assertThat(BigDecimal.valueOf(-1)).isNegative());
  }

  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNotNegative() {
    return ImmutableSet.of(
        assertThat((byte) 1).isNotNegative(),
        assertThat((byte) 2).isNotNegative(),
        assertThat((short) 1).isNotNegative(),
        assertThat((short) 2).isNotNegative(),
        assertThat(1).isNotNegative(),
        assertThat(2).isNotNegative(),
        assertThat(1L).isNotNegative(),
        assertThat(2L).isNotNegative(),
        assertThat(1.0f).isNotNegative(),
        assertThat(1.0).isNotNegative(),
        assertThat(BigInteger.ONE).isNotNegative(),
        assertThat(BigInteger.TWO).isNotNegative(),
        assertThat(BigDecimal.ONE).isNotNegative());
  }

  ImmutableSet<NumberAssert<?, ?>> testAssertThatIsOdd() {
    return ImmutableSet.of(
        assertThat((char) 1 % 2).isEqualTo(1), assertThat(1).isOdd(), assertThat(1L).isOdd());
  }

  ImmutableSet<NumberAssert<?, ?>> testAssertThatIsEven() {
    return ImmutableSet.of(
        assertThat((char) 1 % 2).isEqualTo(0), assertThat(1).isEven(), assertThat(1L).isEven());
  }
}
