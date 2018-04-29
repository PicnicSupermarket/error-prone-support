package tech.picnic.errorprone.refastertemplates.bigdecimals;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigDecimal;

/** Prefer using the constant {@link BigDecimal#TEN} when possible. */
final class BigDecimalTen {
  @BeforeTemplate
  BigDecimal before1() {
    return BigDecimal.valueOf(10);
  }

  @BeforeTemplate
  BigDecimal before2() {
    return new BigDecimal("10");
  }

  @AfterTemplate
  BigDecimal after() {
    return BigDecimal.TEN;
  }
}
