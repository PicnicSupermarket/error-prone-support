package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster template with a number as suffix to validate that it is reported correctly. */
final class PartialTemplateMatchTemplates {
  private PartialTemplateMatchTemplates() {}

  static final class StringIsEmpty {
    @BeforeTemplate
    boolean before(String string) {
      return string.equals("");
    }

    @AfterTemplate
    boolean after(String string) {
      return string.isEmpty();
    }
  }

  static final class StringEquals {
    @BeforeTemplate
    boolean before(String string1, String string2) {
      return string1 == string2;
    }

    @AfterTemplate
    boolean after(String string1, String string2) {
      return string1.equals(string2);
    }
  }
}
