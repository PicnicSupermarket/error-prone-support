package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster rules from {@link MissingTestAndWrongTestRules}. */
final class MissingTestAndWrongTestRulesTest implements RefasterRuleCollectionTestCase {
  /*
   *  ERROR: The following matches unexpectedly occurred in method `testWrongName`:
   *  - Rule `StringIsEmpty` matches on line 6, while it should match in a method named `testStringIsEmpty`.
   *  - Rule `StringIsEmpty` matches on line 7, while it should match in a method named `testStringIsEmpty`.
   *  - Rule `StringIsEmpty` matches on line 8, while it should match in a method named `testStringIsEmpty`.
   */
  boolean testWrongName() {
    "foo".isEmpty();
    "bar".isEmpty();
    return "baz".isEmpty();
  }
}
/*
 *  ERROR: Did not encounter a test in `MissingTestAndWrongTestRulesTestInput.java` for the following rule(s):
 *  - RuleWithoutTest
 */
