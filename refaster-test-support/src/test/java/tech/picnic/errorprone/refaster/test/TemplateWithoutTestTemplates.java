package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster rule collection to validate the reporting of missing test methods. */
final class TemplateWithoutTestTemplates {
  private TemplateWithoutTestTemplates() {}

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

  static final class TemplateWithoutTest {
    @BeforeTemplate
    boolean before(String string) {
      return string.equals("foo");
    }
  }

  static final class AnotherTemplateWithoutTest {
    @BeforeTemplate
    boolean before(String string) {
      return string.equals("bar");
    }
  }
}
