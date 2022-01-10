package tech.picnic.errorprone.refaster.test;

final class TemplateWithoutTestTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    return "foo".equals("");
  }
}
