package tech.picnic.errorprone.refasterrules;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class PreconditionsRulesTest implements RefasterRuleCollectionTestCase {
  void testCheckArgumentEmpty() {
    if ("".isBlank()) {
      throw new IllegalArgumentException();
    }
  }

  void testCheckArgumentMessage() {
    if ("".isBlank()) {
      throw new IllegalArgumentException("The string is empty.");
    }
  }

  void testCheckStateEmpty() {
    if ("".isBlank()) {
      throw new IllegalStateException();
    }
  }

  void testCheckStateMessage() {
    if ("".isBlank()) {
      throw new IllegalStateException("The string is empty.");
    }
  }
}
