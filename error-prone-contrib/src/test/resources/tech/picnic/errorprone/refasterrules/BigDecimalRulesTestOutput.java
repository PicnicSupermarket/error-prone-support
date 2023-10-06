package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class BigDecimalRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<BigDecimal> testBigDecimalZero() {
    return ImmutableSet.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
  }

  ImmutableSet<BigDecimal> testBigDecimalOne() {
    return ImmutableSet.of(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
  }

  ImmutableSet<BigDecimal> testBigDecimalTen() {
    return ImmutableSet.of(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
  }

  ImmutableSet<BigDecimal> testBigDecimalValueOf() {
    return ImmutableSet.of(BigDecimal.valueOf(2), BigDecimal.valueOf(2L), BigDecimal.valueOf(2.0));
  }

  ImmutableSet<Boolean> testBigDecimalCompareToZero() {
    return ImmutableSet.of(BigDecimal.ONE.signum() == 0, BigDecimal.ONE.signum() == 0);
  }

  ImmutableSet<Boolean> testBigDecimalCompareToNonZero() {
    return ImmutableSet.of(BigDecimal.ONE.signum() != 0, BigDecimal.ONE.signum() != 0);
  }

  ImmutableSet<Boolean> testBigDecimalCompareToZeroPositive() {
    return ImmutableSet.of(BigDecimal.ONE.signum() == 1, BigDecimal.ONE.signum() == 1);
  }

  ImmutableSet<Boolean> testBigDecimalCompareToZeroPositiveInclusive() {
    return ImmutableSet.of(BigDecimal.ONE.signum() >= 0, BigDecimal.ONE.signum() >= 0);
  }

  ImmutableSet<Boolean> testBigDecimalCompareToZeroNegative() {
    return ImmutableSet.of(BigDecimal.ONE.signum() == -1, BigDecimal.ONE.signum() == -1);
  }

  ImmutableSet<Boolean> testBigDecimalCompareToZeroNegativeInclusive() {
    return ImmutableSet.of(BigDecimal.ONE.signum() <= 0, BigDecimal.ONE.signum() <= 0);
  }

  ImmutableSet<Boolean> testBigDecimalPositiveSignum() {
    return ImmutableSet.of(
        BigDecimal.ZERO.signum() == 1,
        BigDecimal.ZERO.signum() == 1,
        BigDecimal.ZERO.signum() == 1);
  }

  ImmutableSet<Boolean> testBigDecimalNegativeSignum() {
    return ImmutableSet.of(
        BigDecimal.ZERO.signum() == -1,
        BigDecimal.ZERO.signum() == -1,
        BigDecimal.ZERO.signum() == -1);
  }
}
