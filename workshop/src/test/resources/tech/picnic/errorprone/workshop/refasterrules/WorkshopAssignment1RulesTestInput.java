package tech.picnic.errorprone.workshop.refasterrules;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class WorkshopAssignment1RulesTest implements RefasterRuleCollectionTestCase {
  String testNewStringCharArray() {
    String.copyValueOf(new char[] {});
    String.copyValueOf(new char[] {'f', 'o', 'o'});
    return String.copyValueOf(new char[] {'b', 'a', 'r'});
  }
}
