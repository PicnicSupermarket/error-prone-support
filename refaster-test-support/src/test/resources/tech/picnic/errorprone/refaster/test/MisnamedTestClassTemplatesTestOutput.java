package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster rules from {@link MisnamedTestClassTemplates}. */
/* ERROR: Class should be named `MisnamedTestClassTemplatesTest`. */
final class IncorrectNameTemplatesTest implements RefasterRuleCollectionTestCase {
  boolean testStringIsEmpty() {
    return "foo".isEmpty();
  }
}

// This is a comment to appease Checkstyle.
/* ERROR: Unexpected token. */
;
