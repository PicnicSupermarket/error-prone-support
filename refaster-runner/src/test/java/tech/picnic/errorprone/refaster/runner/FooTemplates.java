package tech.picnic.errorprone.refaster.runner;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** An example template collection used to test {@link CodeTransformers}. */
final class FooTemplates {
  private FooTemplates() {}

  /** Simple template for testing purposes. */
  static final class SimpleTemplate {
    @BeforeTemplate
    boolean before(String string) {
      return string.length() == 1;
    }

    @AfterTemplate
    boolean after(String string) {
      return string.length() > 0;
    }
  }
}
