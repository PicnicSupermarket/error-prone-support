package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster rules from {@link MissingTestAndWrongTestRules}. */
final class MissingTestAndWrongTestRulesTest implements RefasterRuleCollectionTestCase {
  boolean testWrongName() {
    "foo".equals("");
    "bar".equals("");
    return "baz".equals("");
  }
}
