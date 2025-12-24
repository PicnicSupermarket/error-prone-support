package tech.picnic.errorprone.refaster.test;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/**
 * Refaster rules that use various operators to exercise the {@code
 * RefasterRuleSelector.TemplateIdentifierExtractor} code paths.
 */
final class OperatorRefasterRules {
  private OperatorRefasterRules() {}

  static final class LessThanOperator {
    @BeforeTemplate
    boolean before(int a, int b) {
      return a < b;
    }

    @AfterTemplate
    boolean after(int a, int b) {
      return b > a;
    }
  }

  static final class ConditionalAndOperator {
    @BeforeTemplate
    boolean before(boolean a, boolean b) {
      return a && b;
    }

    @AfterTemplate
    boolean after(boolean a, boolean b) {
      return !(!a || !b);
    }
  }

  static final class PlusOperator {
    @BeforeTemplate
    int before(int a, int b) {
      return a + b;
    }

    @AfterTemplate
    int after(int a, int b) {
      return b + a;
    }
  }

  static final class LogicalComplementOperator {
    @BeforeTemplate
    boolean before(boolean a) {
      return !a;
    }

    @AfterTemplate
    boolean after(boolean a) {
      return a == false;
    }
  }
}
