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

  ImmutableSet<BigDecimal> testBigDecimalFactoryMethod() {
    return ImmutableSet.of(BigDecimal.valueOf(0), BigDecimal.valueOf(0L));
  }
}
