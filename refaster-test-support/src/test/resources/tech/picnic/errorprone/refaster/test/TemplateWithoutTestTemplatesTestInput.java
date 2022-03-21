package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster templates from `TemplateWithoutTestTemplates`. */
final class TemplateWithoutTestTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    return "foo".equals("");
  }
}
