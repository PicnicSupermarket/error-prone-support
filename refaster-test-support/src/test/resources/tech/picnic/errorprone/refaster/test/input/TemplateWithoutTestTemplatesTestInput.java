package tech.picnic.errorprone.refaster.test.input;

/** Code to test the Refaster templates from {@link TemplateWithoutTestTemplates}. */
final class TemplateWithoutTestTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    return "foo".equals("");
  }
}
