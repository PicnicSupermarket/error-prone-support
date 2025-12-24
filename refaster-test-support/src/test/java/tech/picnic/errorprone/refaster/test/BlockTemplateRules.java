package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster rule collection with BlockTemplate examples. */
final class BlockTemplateRules {
  private BlockTemplateRules() {}

  static final class ThrowIllegalArgumentException {
    @BeforeTemplate
    void before(boolean condition) {
      if (condition) {
        throw new IllegalArgumentException();
      }
    }

    @AfterTemplate
    void after(boolean condition) {
      if (!condition) {
        throw new IllegalArgumentException();
      }
    }
  }
}
