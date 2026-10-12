package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster rule collection to validate the reporting of missing test methods. */
final class RuleWithoutTestRules {
  private RuleWithoutTestRules() {}

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

  static final class RuleWithoutTest {
    @BeforeTemplate
    boolean before(String str) {
      return str.equals("foo");
    }
  }

  static final class AnotherRuleWithoutTest {
    @BeforeTemplate
    boolean before(String str) {
      return str.equals("bar");
    }
  }
}
