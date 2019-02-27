package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/**
 * Don't use the ternary operator to compare two booleans.
 *
 * @see EqualBooleans
 */
final class UnequalBooleans {
  @BeforeTemplate
  boolean before(boolean b1, boolean b2) {
    return b1 ? !b2 : b2;
  }

  @AfterTemplate
  boolean after(boolean b1, boolean b2) {
    return b1 != b2;
  }
}
