package tech.picnic.errorprone.refaster.test;

final class TemplateWithoutTestTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    return "foo".isEmpty();
  }
}
/* Did not encounter test in /tech.picnic.errorprone.refaster.test.TemplateWithoutTestTemplatesTestInput.java for the following template(s):
- AnotherTemplateWithoutTest
- TemplateWithoutTest
*/
