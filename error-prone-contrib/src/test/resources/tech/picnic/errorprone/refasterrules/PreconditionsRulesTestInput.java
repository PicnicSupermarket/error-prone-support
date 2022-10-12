package tech.picnic.errorprone.refasterrules;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class PreconditionsRulesTest implements RefasterRuleCollectionTestCase {
  void testCheckArgument() {
    if ("foo".isEmpty()) {
      throw new IllegalArgumentException();
    }
  }

  void testCheckArgumentWithMessage() {
    if ("foo".isEmpty()) {
      throw new IllegalArgumentException("The string is empty");
    }
  }

  void testCheckNotNull() {
    if ("foo" == null) {
      throw new NullPointerException();
    }
  }

  void testCheckNotNullWithMessage() {
    if ("foo" == null) {
      throw new NullPointerException("The string is null");
    }
  }

  void testCheckState() {
    if ("foo".isEmpty()) {
      throw new IllegalStateException();
    }
  }

  void testCheckStateWithMessage() {
    if ("foo".isEmpty()) {
      throw new IllegalStateException("The string is empty");
    }
  }
}
