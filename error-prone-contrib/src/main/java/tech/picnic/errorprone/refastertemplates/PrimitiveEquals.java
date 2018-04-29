package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.NoAutoboxing;
import java.util.Objects;

/** Avoid boxing when comparing primitive values. */
final class PrimitiveEquals {
  @NoAutoboxing
  @BeforeTemplate
  boolean before(long a, long b) {
    return Objects.equals(a, b);
  }

  @AlsoNegation
  @AfterTemplate
  boolean after(long a, long b) {
    return a == b;
  }
}
