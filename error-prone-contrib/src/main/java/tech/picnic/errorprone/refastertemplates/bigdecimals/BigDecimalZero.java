package tech.picnic.errorprone.refastertemplates.bigdecimals;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigDecimal;

/** Prefer using the constant {@link BigDecimal#ZERO} when possible. */
final class BigDecimalZero {
  @BeforeTemplate
  BigDecimal before1() {
    return BigDecimal.valueOf(0);
  }

  @BeforeTemplate
  BigDecimal before2() {
    return new BigDecimal("0");
  }

  @AfterTemplate
  BigDecimal after() {
    return BigDecimal.ZERO;
  }
}
