package tech.picnic.errorprone.refastertemplates.bigdecimals;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigDecimal;

/** Prefer using the constant {@link BigDecimal#ONE} when possible. */
final class BigDecimalOne {
  @BeforeTemplate
  BigDecimal before1() {
    return BigDecimal.valueOf(1);
  }

  @BeforeTemplate
  BigDecimal before2() {
    return new BigDecimal("1");
  }

  @AfterTemplate
  BigDecimal after() {
    return BigDecimal.ONE;
  }
}
