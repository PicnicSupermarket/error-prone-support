package tech.picnic.errorprone.workshop.refasterrules;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class WorkshopAssignment3RulesTest implements RefasterRuleCollectionTestCase {
  void testCheckArgumentWithoutMessage() {
    if (!"foo".isEmpty()) {
      throw new IllegalArgumentException();
    }
    if (!"bar".isEmpty()) {
      throw new IllegalArgumentException();
    }
  }

  void testCheckArgumentWithMessage() {
    if (!"foo".isEmpty()) {
      throw new IllegalArgumentException("The string is not empty");
    }
    if (!"bar".isEmpty()) {
      throw new IllegalArgumentException("The rule should be able rewrite all kinds of messages ;).");
    }
  }
}
