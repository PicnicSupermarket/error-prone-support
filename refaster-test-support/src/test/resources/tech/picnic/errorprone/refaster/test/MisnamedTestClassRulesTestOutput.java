package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster rules from {@link MisnamedTestClassRules}. */
/* ERROR: Class should be named `MisnamedTestClassRulesTest`. */
final class IncorrectNameRulesTest implements RefasterRuleCollectionTestCase {
  boolean testStringIsEmpty() {
    return "foo".isEmpty();
  }
}

// This is a comment to appease Checkstyle.
/* ERROR: Unexpected token. */
;
