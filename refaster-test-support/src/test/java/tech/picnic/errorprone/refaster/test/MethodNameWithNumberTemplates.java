package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/**
 * Refaster template collection containing a template with a numeric name suffix, used to validate
 * that it is reported correctly.
 */
// XXX: Rename and generalize to test more happy flow cases?
// XXX: If so, also test block templates with placeholders and templates that introduce static
// imports.
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
