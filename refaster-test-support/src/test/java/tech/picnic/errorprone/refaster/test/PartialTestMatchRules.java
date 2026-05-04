package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/**
 * Refaster rule collection to validate that matches for one rule in a test method meant to cover
 * another rule are reported.
 */
final class PartialTestMatchRules {
  private PartialTestMatchRules() {}

  static final class StringEquals {
    @BeforeTemplate
    boolean before(String str) {
      return str.toCharArray().length == 0;
    }

    @AfterTemplate
    boolean after(String str) {
      return str.equals("");
    }
  }

  static final class StringIsEmpty {
    @BeforeTemplate
    boolean before(String str) {
      return str.equals("");
    }

    @AfterTemplate
    boolean after(String str) {
      return str.isEmpty();
    }
  }
}
