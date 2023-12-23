package tech.picnic.errorprone.refaster.test.input;

/** Code to test the Refaster rules from {@link MatchInWrongMethodRules}. */
final class MatchInWrongMethodRulesTest implements RefasterRuleCollectionTestCase {
  boolean testWrongName() {
    "foo".equals("");
    "bar".equals("");
    return "baz".equals("");
  }
}
