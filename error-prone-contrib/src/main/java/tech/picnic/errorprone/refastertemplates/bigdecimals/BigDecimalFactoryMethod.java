package tech.picnic.errorprone.refastertemplates.bigdecimals;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.math.BigDecimal;

/** Prefer {@link BigDecimal#valueOf(long)} over the associated constructor. */
// XXX: Ideally we'd also rewrite `BigDecimal.valueOf("<some-integer-value>")`, but it doesn't
// appear that's currently possible with Error Prone.
final class BigDecimalFactoryMethod {
  @BeforeTemplate
  BigDecimal before(long value) {
    return new BigDecimal(value);
  }

  @AfterTemplate
  BigDecimal after(long value) {
    return BigDecimal.valueOf(value);
  }
}
