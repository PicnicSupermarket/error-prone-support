package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;

final class BigDecimalTemplatesTest implements RefasterTemplateTestCase {
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
