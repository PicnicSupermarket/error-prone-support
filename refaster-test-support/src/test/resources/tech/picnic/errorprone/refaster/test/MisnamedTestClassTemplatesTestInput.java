package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster rules from {@link MisnamedTestClassTemplates}. */
final class IncorrectNameTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    return "foo".equals("");
  }
}

// This is a comment to appease Checkstyle.
;
