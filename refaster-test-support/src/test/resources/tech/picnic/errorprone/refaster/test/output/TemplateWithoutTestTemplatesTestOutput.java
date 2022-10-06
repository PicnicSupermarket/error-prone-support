package tech.picnic.errorprone.refaster.test.output;

/** Code to test the Refaster templates from {@link TemplateWithoutTestTemplates}. */
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
