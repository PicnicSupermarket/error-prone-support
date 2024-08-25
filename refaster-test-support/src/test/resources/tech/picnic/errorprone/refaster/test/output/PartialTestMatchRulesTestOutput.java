package tech.picnic.errorprone.refaster.test.output;

/** Code to test the Refaster rules from {@link PartialTestMatchRules}. */
final class PartialTestMatchRulesTest implements RefasterRuleCollectionTestCase {
  /*
   *  ERROR: The following matches unexpectedly occurred in method `testStringIsEmpty`:
   *  - Rule `StringEquals` matches on line 6, while it should match in a method named `testStringEquals`.
   */
  boolean testStringIsEmpty() {
    boolean b = "foo".equals("");
    return "bar".isEmpty();
  }
}
