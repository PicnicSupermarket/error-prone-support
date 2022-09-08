package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.NumberAssert;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class AssertJNumberTemplatesTest implements RefasterTemplateTestCase {
  @SuppressWarnings("SimplifyBooleanExpression")
  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsEqualTo() {
    return ImmutableSet.of(
        assertThat(true).isEqualTo(false),
        assertThat(true).isEqualTo(false),
        assertThat((byte) 1).isEqualTo((byte) 2),
        assertThat((byte) 1).isEqualTo((byte) 2),
        assertThat((char) 1).isEqualTo((char) 2),
        assertThat((char) 1).isEqualTo((char) 2),
        assertThat((short) 1).isEqualTo((short) 2),
        assertThat((short) 1).isEqualTo((short) 2),
        assertThat(1).isEqualTo(2),
        assertThat(1).isEqualTo(2),
        assertThat(1L).isEqualTo(2L),
        assertThat(1L).isEqualTo(2L),
        assertThat(1F).isEqualTo(2F),
        assertThat(1F).isEqualTo(2F),
        assertThat(1.0).isEqualTo(2.0),
        assertThat(1.0).isEqualTo(2.0));
  }

  @SuppressWarnings("SimplifyBooleanExpression")
  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(true).isNotEqualTo(false),
        assertThat(true).isNotEqualTo(false),
        assertThat((byte) 1).isNotEqualTo((byte) 2),
        assertThat((byte) 1).isNotEqualTo((byte) 2),
        assertThat((char) 1).isNotEqualTo((char) 2),
        assertThat((char) 1).isNotEqualTo((char) 2),
        assertThat((short) 1).isNotEqualTo((short) 2),
        assertThat((short) 1).isNotEqualTo((short) 2),
        assertThat(1).isNotEqualTo(2),
        assertThat(1).isNotEqualTo(2),
        assertThat(1L).isNotEqualTo(2L),
        assertThat(1L).isNotEqualTo(2L),
        assertThat(1F).isNotEqualTo(2F),
        assertThat(1F).isNotEqualTo(2F),
        assertThat(1.0).isNotEqualTo(2.0),
        assertThat(1.0).isNotEqualTo(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsLessThan() {
    return ImmutableSet.of(
        assertThat((byte) 1).isLessThan((byte) 2),
        assertThat((byte) 1).isLessThan((byte) 2),
        assertThat((char) 1).isLessThan((char) 2),
        assertThat((char) 1).isLessThan((char) 2),
        assertThat((short) 1).isLessThan((short) 2),
        assertThat((short) 1).isLessThan((short) 2),
        assertThat(1).isLessThan(2),
        assertThat(1).isLessThan(2),
        assertThat(1L).isLessThan(2L),
        assertThat(1L).isLessThan(2L),
        assertThat(1F).isLessThan(2F),
        assertThat(1F).isLessThan(2F),
        assertThat(1.0).isLessThan(2.0),
        assertThat(1.0).isLessThan(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsLessThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 1).isLessThanOrEqualTo((byte) 2),
        assertThat((byte) 1).isLessThanOrEqualTo((byte) 2),
        assertThat((char) 1).isLessThanOrEqualTo((char) 2),
        assertThat((char) 1).isLessThanOrEqualTo((char) 2),
        assertThat((short) 1).isLessThanOrEqualTo((short) 2),
        assertThat((short) 1).isLessThanOrEqualTo((short) 2),
        assertThat(1).isLessThanOrEqualTo(2),
        assertThat(1).isLessThanOrEqualTo(2),
        assertThat(1L).isLessThanOrEqualTo(2L),
        assertThat(1L).isLessThanOrEqualTo(2L),
        assertThat(1F).isLessThanOrEqualTo(2F),
        assertThat(1F).isLessThanOrEqualTo(2F),
        assertThat(1.0).isLessThanOrEqualTo(2.0),
        assertThat(1.0).isLessThanOrEqualTo(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsGreaterThan() {
    return ImmutableSet.of(
        assertThat((byte) 1).isGreaterThan((byte) 2),
        assertThat((byte) 1).isGreaterThan((byte) 2),
        assertThat((char) 1).isGreaterThan((char) 2),
        assertThat((char) 1).isGreaterThan((char) 2),
        assertThat((short) 1).isGreaterThan((short) 2),
        assertThat((short) 1).isGreaterThan((short) 2),
        assertThat(1).isGreaterThan(2),
        assertThat(1).isGreaterThan(2),
        assertThat(1L).isGreaterThan(2L),
        assertThat(1L).isGreaterThan(2L),
        assertThat(1F).isGreaterThan(2F),
        assertThat(1F).isGreaterThan(2F),
        assertThat(1.0).isGreaterThan(2.0),
        assertThat(1.0).isGreaterThan(2.0));
  }

  ImmutableSet<AbstractAssert<?, ?>> testAssertThatIsGreaterThanOrEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 1).isGreaterThanOrEqualTo((byte) 2),
        assertThat((byte) 1).isGreaterThanOrEqualTo((byte) 2),
        assertThat((char) 1).isGreaterThanOrEqualTo((char) 2),
        assertThat((char) 1).isGreaterThanOrEqualTo((char) 2),
        assertThat((short) 1).isGreaterThanOrEqualTo((short) 2),
        assertThat((short) 1).isGreaterThanOrEqualTo((short) 2),
        assertThat(1).isGreaterThanOrEqualTo(2),
        assertThat(1).isGreaterThanOrEqualTo(2),
        assertThat(1L).isGreaterThanOrEqualTo(2L),
        assertThat(1L).isGreaterThanOrEqualTo(2L),
        assertThat(1F).isGreaterThanOrEqualTo(2F),
        assertThat(1F).isGreaterThanOrEqualTo(2F),
        assertThat(1.0).isGreaterThanOrEqualTo(2.0),
        assertThat(1.0).isGreaterThanOrEqualTo(2.0));
  }

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
