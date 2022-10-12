package tech.picnic.errorprone.refasterrules;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class PreconditionsRulesTest implements RefasterRuleCollectionTestCase {
  void testCheckArgument() {
    if ("".isEmpty()) {
      throw new IllegalArgumentException();
    }
  }

  void testCheckArgumentWithMessage() {
    if ("".isEmpty()) {
      throw new IllegalArgumentException("The string is empty");
    }
  }

  void testCheckState() {
    if ("".isEmpty()) {
      throw new IllegalStateException();
    }
  }

  void testCheckStateWithMessage() {
    if ("".isEmpty()) {
      throw new IllegalStateException("The string is empty");
    }
  }
}
