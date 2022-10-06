package tech.picnic.errorprone.refaster.test.output;

/** Code to test the Refaster templates from {@link MisnamedTestClassTemplates}. */
/* ERROR: Class should be named `MisnamedTestClassTemplatesTest`. */
final class IncorrectNameTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    return "foo".isEmpty();
  }
}

// This is a comment to appease Checkstyle.
/* ERROR: Unexpected token. */
;
