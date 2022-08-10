package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class BigDecimalTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<BigDecimal> testBigDecimalZero() {
    return ImmutableSet.of(BigDecimal.valueOf(0), BigDecimal.valueOf(0L), new BigDecimal("0"));
  }

  ImmutableSet<BigDecimal> testBigDecimalOne() {
    return ImmutableSet.of(BigDecimal.valueOf(1), BigDecimal.valueOf(1L), new BigDecimal("1"));
  }

  ImmutableSet<BigDecimal> testBigDecimalTen() {
    return ImmutableSet.of(BigDecimal.valueOf(10), BigDecimal.valueOf(10L), new BigDecimal("10"));
  }

  ImmutableSet<BigDecimal> testBigDecimalFactoryMethod() {
    return ImmutableSet.of(new BigDecimal(0), new BigDecimal(0L));
  }
}
