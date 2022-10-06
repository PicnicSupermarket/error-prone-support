package tech.picnic.errorprone.refaster.test.input;

/** Code to test the Refaster templates from {@link MisnamedTestClassTemplates}. */
final class IncorrectNameTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    return "foo".equals("");
  }
}

// This is a comment to appease Checkstyle.
;
