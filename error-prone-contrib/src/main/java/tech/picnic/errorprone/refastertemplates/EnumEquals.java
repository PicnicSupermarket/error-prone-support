package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Objects;

/**
 * Enums can be compared by reference. It is safe to do so even in the face of refactorings, because
 * if the type is ever converted to a non-enum, then Error-Prone will complain about any remaining
 * reference-based equality checks.
 */
// XXX: This Refaster rule is defined in terms of an inequality because of
// https://github.com/google/error-prone/issues/559
final class EnumEquals<T extends Enum<T>> {
  @BeforeTemplate
  @SuppressWarnings("boxing")
  boolean before(T a, T b) {
    return !Refaster.anyOf(a.equals(b), Objects.equals(a, b));
  }

  @AlsoNegation
  @AfterTemplate
  boolean after(T a, T b) {
    return a != b;
  }
}
