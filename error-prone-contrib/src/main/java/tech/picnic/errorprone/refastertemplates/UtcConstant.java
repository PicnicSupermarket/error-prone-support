package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Use {@link ZoneOffset#UTC} when possible. */
final class UtcConstant {
  @BeforeTemplate
  ZoneId before() {
    return ZoneId.of("UTC");
  }

  @AfterTemplate
  ZoneOffset after() {
    return ZoneOffset.UTC;
  }
}
