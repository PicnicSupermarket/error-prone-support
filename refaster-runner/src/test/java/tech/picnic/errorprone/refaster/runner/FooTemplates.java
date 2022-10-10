package tech.picnic.errorprone.refaster.runner;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** An example rule collection used to test {@link CodeTransformers}. */
final class FooTemplates {
  private FooTemplates() {}

  /** Simple rule for testing purposes. */
  static final class SimpleTemplate {
    @BeforeTemplate
    boolean before(String string) {
      return string.toCharArray().length == 0;
    }

    @AfterTemplate
    boolean after(String string) {
      return string.isEmpty();
    }
  }
}
