package tech.picnic.errorprone.refaster.test.output;

/** Code to test the Refaster rules from {@link RuleWithoutTestRules}. */
final class RuleWithoutTestRulesTest implements RefasterRuleCollectionTestCase {
  boolean testStringIsEmpty() {
    return "foo".isEmpty();
  }
}
/*
 *  ERROR: Did not encounter a test in `RuleWithoutTestRulesTestInput.java` for the following rule(s):
 *  - AnotherRuleWithoutTest
 *  - RuleWithoutTest
 */
