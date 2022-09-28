package tech.picnic.errorprone.refaster.test.output;

/** Code to test the Refaster rules from {@link MatchInWrongMethodRules}. */
final class MatchInWrongMethodRulesTest implements RefasterRuleCollectionTestCase {
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
