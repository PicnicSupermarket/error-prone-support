package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.assertj.core.api.NumberAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJNumberTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsPositive() {
    return ImmutableSet.of(
        assertThat((byte) 0).isPositive(),
        assertThat((byte) 0).isPositive(),
        assertThat((short) 0).isPositive(),
        assertThat((short) 0).isPositive(),
        assertThat(0).isPositive(),
        assertThat(0).isPositive(),
        assertThat(0L).isPositive(),
        assertThat(0L).isPositive(),
        assertThat(0.0F).isPositive(),
        assertThat(0.0).isPositive(),
        assertThat(BigInteger.ZERO).isPositive(),
        assertThat(BigInteger.ZERO).isPositive(),
        assertThat(BigDecimal.ZERO).isPositive());
  }

  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNotPositive() {
    return ImmutableSet.of(
        assertThat((byte) 0).isNotPositive(),
        assertThat((byte) 0).isNotPositive(),
        assertThat((short) 0).isNotPositive(),
        assertThat((short) 0).isNotPositive(),
        assertThat(0).isNotPositive(),
        assertThat(0).isNotPositive(),
        assertThat(0L).isNotPositive(),
        assertThat(0L).isNotPositive(),
        assertThat(0.0F).isNotPositive(),
        assertThat(0.0).isNotPositive(),
        assertThat(BigInteger.ZERO).isNotPositive(),
        assertThat(BigInteger.ZERO).isNotPositive(),
        assertThat(BigDecimal.ZERO).isNotPositive());
  }

  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNegative() {
    return ImmutableSet.of(
        assertThat((byte) 0).isNegative(),
        assertThat((byte) 0).isNegative(),
        assertThat((short) 0).isNegative(),
        assertThat((short) 0).isNegative(),
        assertThat(0).isNegative(),
        assertThat(0).isNegative(),
        assertThat(0L).isNegative(),
        assertThat(0L).isNegative(),
        assertThat(0.0F).isNegative(),
        assertThat(0.0).isNegative(),
        assertThat(BigInteger.ZERO).isNegative(),
        assertThat(BigInteger.ZERO).isNegative(),
        assertThat(BigDecimal.ZERO).isNegative());
  }

  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNotNegative() {
    return ImmutableSet.of(
        assertThat((byte) 0).isNotNegative(),
        assertThat((byte) 0).isNotNegative(),
        assertThat((short) 0).isNotNegative(),
        assertThat((short) 0).isNotNegative(),
        assertThat(0).isNotNegative(),
        assertThat(0).isNotNegative(),
        assertThat(0L).isNotNegative(),
        assertThat(0L).isNotNegative(),
        assertThat(0.0F).isNotNegative(),
        assertThat(0.0).isNotNegative(),
        assertThat(BigInteger.ZERO).isNotNegative(),
        assertThat(BigInteger.ZERO).isNotNegative(),
        assertThat(BigDecimal.ZERO).isNotNegative());
  }

  ImmutableSet<NumberAssert<?, ?>> testAssertThatIsOdd() {
    return ImmutableSet.of(
        assertThat((byte) 1).isOdd(),
        assertThat(Byte.valueOf((byte) 1)).isOdd(),
        assertThat((char) 1 % 2).isEqualTo(1),
        assertThat(Character.valueOf((char) 1) % 2).isEqualTo(1),
        assertThat((short) 1).isOdd(),
        assertThat(Short.valueOf((short) 1)).isOdd(),
        assertThat(1).isOdd(),
        assertThat(Integer.valueOf(1)).isOdd(),
        assertThat(1L).isOdd(),
        assertThat(Long.valueOf(1)).isOdd());
  }

  ImmutableSet<NumberAssert<?, ?>> testAssertThatIsEven() {
    return ImmutableSet.of(
        assertThat((byte) 1).isEven(),
        assertThat(Byte.valueOf((byte) 1)).isEven(),
        assertThat((char) 1 % 2).isEqualTo(0),
        assertThat(Character.valueOf((char) 1) % 2).isEqualTo(0),
        assertThat((short) 1).isEven(),
        assertThat(Short.valueOf((short) 1)).isEven(),
        assertThat(1).isEven(),
        assertThat(Integer.valueOf(1)).isEven(),
        assertThat(1L).isEven(),
        assertThat(Long.valueOf(1)).isEven());
  }
}
