package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster template with a number as suffix to validate that it is reported correctly. */
final class MethodNameWithNumberTemplates {
  private MethodNameWithNumberTemplates() {}

  static final class StringIsEmpty2 {
    @BeforeTemplate
    boolean before(String string) {
      return string.equals("");
    }

    @AfterTemplate
    boolean after(String string) {
      return string.isEmpty();
    }
  }
}
