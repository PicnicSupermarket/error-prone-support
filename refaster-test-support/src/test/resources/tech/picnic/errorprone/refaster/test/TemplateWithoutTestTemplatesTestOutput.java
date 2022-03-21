package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster templates from `TemplateWithoutTestTemplates`. */
final class TemplateWithoutTestTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    return "foo".isEmpty();
  }
}
/* Did not encounter a test in `TemplateWithoutTestTemplatesTestInput` for the following template(s):
- AnotherTemplateWithoutTest
- TemplateWithoutTest
*/
