package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class BigDecimalRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<BigDecimal> testBigDecimalZero() {
    return ImmutableSet.of(BigDecimal.valueOf(0), BigDecimal.valueOf(0L), new BigDecimal("0"));
  }

  ImmutableSet<BigDecimal> testBigDecimalOne() {
    return ImmutableSet.of(BigDecimal.valueOf(1), BigDecimal.valueOf(1L), new BigDecimal("1"));
  }

  ImmutableSet<BigDecimal> testBigDecimalTen() {
    return ImmutableSet.of(BigDecimal.valueOf(10), BigDecimal.valueOf(10L), new BigDecimal("10"));
  }

  ImmutableSet<BigDecimal> testBigDecimalValueOf() {
    return ImmutableSet.of(new BigDecimal(2), new BigDecimal(2L), new BigDecimal(2.0));
  }

  ImmutableSet<Boolean> testBigDecimalSignumIsZero() {
    return ImmutableSet.of(
        BigDecimal.valueOf(1).compareTo(BigDecimal.ZERO) == 0,
        BigDecimal.ZERO.compareTo(BigDecimal.valueOf(2)) == 0,
        BigDecimal.valueOf(3).compareTo(BigDecimal.ZERO) != 0,
        BigDecimal.ZERO.compareTo(BigDecimal.valueOf(4)) != 0);
  }

  ImmutableSet<Boolean> testBigDecimalSignumIsPositive() {
    return ImmutableSet.of(
        BigDecimal.valueOf(1).compareTo(BigDecimal.ZERO) > 0,
        BigDecimal.ZERO.compareTo(BigDecimal.valueOf(2)) < 0,
        BigDecimal.valueOf(3).signum() > 0,
        BigDecimal.valueOf(4).signum() >= 1,
        BigDecimal.valueOf(5).compareTo(BigDecimal.ZERO) <= 0,
        BigDecimal.ZERO.compareTo(BigDecimal.valueOf(6)) >= 0,
        BigDecimal.valueOf(7).signum() <= 0,
        BigDecimal.valueOf(8).signum() < 1);
  }

  ImmutableSet<Boolean> testBigDecimalSignumIsNegative() {
    return ImmutableSet.of(
        BigDecimal.valueOf(1).compareTo(BigDecimal.ZERO) < 0,
        BigDecimal.ZERO.compareTo(BigDecimal.valueOf(2)) > 0,
        BigDecimal.valueOf(3).signum() < 0,
        BigDecimal.valueOf(4).signum() <= -1,
        BigDecimal.valueOf(5).compareTo(BigDecimal.ZERO) >= 0,
        BigDecimal.ZERO.compareTo(BigDecimal.valueOf(6)) <= 0,
        BigDecimal.valueOf(7).signum() >= 0,
        BigDecimal.valueOf(8).signum() > -1);
  }
}
