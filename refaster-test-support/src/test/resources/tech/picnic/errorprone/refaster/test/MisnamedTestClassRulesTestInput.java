package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster rules from {@link MisnamedTestClassRules}. */
final class IncorrectNameRulesTest implements RefasterRuleCollectionTestCase {
  boolean testStringIsEmpty() {
    return "foo".equals("");
  }
}

// This is a comment to appease Checkstyle.
;
