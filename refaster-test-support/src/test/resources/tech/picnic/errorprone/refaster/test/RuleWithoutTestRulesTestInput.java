package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster rules from {@link RuleWithoutTestRules}. */
final class RuleWithoutTestRulesTest implements RefasterRuleCollectionTestCase {
  boolean testStringIsEmpty() {
    return "foo".equals("");
  }
}
