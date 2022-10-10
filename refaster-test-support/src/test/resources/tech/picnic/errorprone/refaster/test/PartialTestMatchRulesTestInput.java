package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster rules from {@link PartialTestMatchRules}. */
final class PartialTestMatchRulesTest implements RefasterRuleCollectionTestCase {
  boolean testStringIsEmpty() {
    boolean b = "foo".toCharArray().length == 0;
    return "bar".equals("");
  }
}
