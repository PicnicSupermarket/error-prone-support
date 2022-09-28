package tech.picnic.errorprone.refasterrules.input;

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

  void testCheckElementIndexWithMessage() {
    if (1 < 0 || 1 >= 2) {
      throw new IndexOutOfBoundsException("My index");
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

  void testCheckPositionIndex() {
    if (1 < 0 || 1 > 2) {
      throw new IndexOutOfBoundsException();
    }
  }

  void testCheckPositionIndexWithMessage() {
    if (1 < 0 || 1 > 2) {
      throw new IndexOutOfBoundsException("My position");
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
