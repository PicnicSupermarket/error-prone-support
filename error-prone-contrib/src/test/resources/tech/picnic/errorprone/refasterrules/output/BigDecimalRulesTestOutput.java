package tech.picnic.errorprone.refasterrules.output;

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

  ImmutableSet<Boolean> testBigDecimalSignumIsZero() {
    return ImmutableSet.of(
        BigDecimal.valueOf(1).signum() == 0,
        BigDecimal.valueOf(2).signum() == 0,
        BigDecimal.valueOf(3).signum() != 0,
        BigDecimal.valueOf(4).signum() != 0);
  }

  ImmutableSet<Boolean> testBigDecimalSignumIsPositive() {
    return ImmutableSet.of(
        BigDecimal.valueOf(1).signum() == 1,
        BigDecimal.valueOf(2).signum() == 1,
        BigDecimal.valueOf(3).signum() == 1,
        BigDecimal.valueOf(4).signum() == 1,
        BigDecimal.valueOf(5).signum() != 1,
        BigDecimal.valueOf(6).signum() != 1,
        BigDecimal.valueOf(7).signum() != 1,
        BigDecimal.valueOf(8).signum() != 1);
  }

  ImmutableSet<Boolean> testBigDecimalSignumIsNegative() {
    return ImmutableSet.of(
        BigDecimal.valueOf(1).signum() == -1,
        BigDecimal.valueOf(2).signum() == -1,
        BigDecimal.valueOf(3).signum() == -1,
        BigDecimal.valueOf(4).signum() == -1,
        BigDecimal.valueOf(5).signum() != -1,
        BigDecimal.valueOf(6).signum() != -1,
        BigDecimal.valueOf(7).signum() != -1,
        BigDecimal.valueOf(8).signum() != -1);
  }
}
