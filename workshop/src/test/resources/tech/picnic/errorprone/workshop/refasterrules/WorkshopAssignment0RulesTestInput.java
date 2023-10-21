package tech.picnic.errorprone.workshop.refasterrules;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class WorkshopAssignment0RulesTest implements RefasterRuleCollectionTestCase {
  boolean testExampleStringIsEmpty() {
    boolean b = "foo".length() == 0;
    return "bar".length() == 0;
  }
}
