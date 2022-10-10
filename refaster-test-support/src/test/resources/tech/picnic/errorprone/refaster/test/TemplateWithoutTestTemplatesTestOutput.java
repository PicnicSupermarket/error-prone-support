package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster rules from {@link TemplateWithoutTestTemplates}. */
final class TemplateWithoutTestTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    return "foo".isEmpty();
  }
}
/*
 *  ERROR: Did not encounter a test in `TemplateWithoutTestTemplatesTestInput.java` for the following template(s):
 *  - AnotherTemplateWithoutTest
 *  - TemplateWithoutTest
 */
