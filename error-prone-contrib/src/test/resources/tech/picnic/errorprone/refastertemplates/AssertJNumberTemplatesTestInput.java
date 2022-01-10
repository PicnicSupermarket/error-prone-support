package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.assertj.core.api.NumberAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJNumberTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsPositive() {
    return ImmutableSet.of(
        assertThat((byte) 0).isGreaterThan((byte) 0),
        assertThat((byte) 0).isGreaterThanOrEqualTo((byte) 1),
        assertThat((short) 0).isGreaterThan((short) 0),
        assertThat((short) 0).isGreaterThanOrEqualTo((short) 1),
        assertThat(0).isGreaterThan(0),
        assertThat(0).isGreaterThanOrEqualTo(1),
        assertThat(0L).isGreaterThan(0),
        assertThat(0L).isGreaterThanOrEqualTo(1),
        assertThat(0.0F).isGreaterThan(0),
        assertThat(0.0).isGreaterThan(0),
        assertThat(BigInteger.ZERO).isGreaterThan(BigInteger.ZERO),
        assertThat(BigInteger.ZERO).isGreaterThanOrEqualTo(BigInteger.valueOf(1)),
        assertThat(BigDecimal.ZERO).isGreaterThan(BigDecimal.ZERO));
  }

  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNotPositive() {
    return ImmutableSet.of(
        assertThat((byte) 0).isLessThanOrEqualTo((byte) 0),
        assertThat((byte) 0).isLessThan((byte) 1),
        assertThat((short) 0).isLessThanOrEqualTo((short) 0),
        assertThat((short) 0).isLessThan((short) 1),
        assertThat(0).isLessThanOrEqualTo(0),
        assertThat(0).isLessThan(1),
        assertThat(0L).isLessThanOrEqualTo(0),
        assertThat(0L).isLessThan(1),
        assertThat(0.0F).isLessThanOrEqualTo(0),
        assertThat(0.0).isLessThanOrEqualTo(0),
        assertThat(BigInteger.ZERO).isLessThanOrEqualTo(BigInteger.ZERO),
        assertThat(BigInteger.ZERO).isLessThan(BigInteger.valueOf(1)),
        assertThat(BigDecimal.ZERO).isLessThanOrEqualTo(BigDecimal.ZERO));
  }

  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNegative() {
    return ImmutableSet.of(
        assertThat((byte) 0).isLessThan((byte) 0),
        assertThat((byte) 0).isLessThanOrEqualTo((byte) -1),
        assertThat((short) 0).isLessThan((short) 0),
        assertThat((short) 0).isLessThanOrEqualTo((short) -1),
        assertThat(0).isLessThan(0),
        assertThat(0).isLessThanOrEqualTo(-1),
        assertThat(0L).isLessThan(0),
        assertThat(0L).isLessThanOrEqualTo(-1),
        assertThat(0.0F).isLessThan(0),
        assertThat(0.0).isLessThan(0),
        assertThat(BigInteger.ZERO).isLessThan(BigInteger.ZERO),
        assertThat(BigInteger.ZERO).isLessThanOrEqualTo(BigInteger.valueOf(-1)),
        assertThat(BigDecimal.ZERO).isLessThan(BigDecimal.ZERO));
  }

  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNotNegative() {
    return ImmutableSet.of(
        assertThat((byte) 0).isGreaterThanOrEqualTo((byte) 0),
        assertThat((byte) 0).isGreaterThan((byte) -1),
        assertThat((short) 0).isGreaterThanOrEqualTo((short) 0),
        assertThat((short) 0).isGreaterThan((short) -1),
        assertThat(0).isGreaterThanOrEqualTo(0),
        assertThat(0).isGreaterThan(-1),
        assertThat(0L).isGreaterThanOrEqualTo(0),
        assertThat(0L).isGreaterThan(-1),
        assertThat(0.0F).isGreaterThanOrEqualTo(0),
        assertThat(0.0).isGreaterThanOrEqualTo(0),
        assertThat(BigInteger.ZERO).isGreaterThanOrEqualTo(BigInteger.ZERO),
        assertThat(BigInteger.ZERO).isGreaterThan(BigInteger.valueOf(-1)),
        assertThat(BigDecimal.ZERO).isGreaterThanOrEqualTo(BigDecimal.ZERO));
  }

  ImmutableSet<NumberAssert<?, ?>> testAssertThatIsOdd() {
    return ImmutableSet.of(
        assertThat((byte) 1 % 2).isEqualTo(1),
        assertThat(Byte.valueOf((byte) 1) % 2).isEqualTo(1),
        assertThat(1 % 2).isEqualTo(1),
        assertThat(Integer.valueOf(1) % 2).isEqualTo(1),
        assertThat(1L % 2).isEqualTo(1),
        assertThat(Long.valueOf(1) % 2).isEqualTo(1),
        assertThat((short) 1 % 2).isEqualTo(1),
        assertThat(Short.valueOf((short) 1) % 2).isEqualTo(1));
  }

  ImmutableSet<NumberAssert<?, ?>> testAssertThatIsEven() {
    return ImmutableSet.of(
        assertThat((byte) 1 % 2).isEqualTo(0),
        assertThat(Byte.valueOf((byte) 1) % 2).isEqualTo(0),
        assertThat(1 % 2).isEqualTo(0),
        assertThat(Integer.valueOf(1) % 2).isEqualTo(0),
        assertThat(1L % 2).isEqualTo(0),
        assertThat(Long.valueOf(1) % 2).isEqualTo(0),
        assertThat((short) 1 % 2).isEqualTo(0),
        assertThat(Short.valueOf((short) 1) % 2).isEqualTo(0));
  }
}
