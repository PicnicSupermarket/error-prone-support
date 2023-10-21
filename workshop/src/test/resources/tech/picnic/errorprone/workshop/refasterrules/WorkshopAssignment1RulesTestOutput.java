package tech.picnic.errorprone.workshop.refasterrules;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class WorkshopAssignment1RulesTest implements RefasterRuleCollectionTestCase {
  String testNewStringCharArray() {
    new String(new char[] {});
    new String(new char[] {'f', 'o', 'o'});
    return new String(new char[] {'b', 'a', 'r'});
  }
}
