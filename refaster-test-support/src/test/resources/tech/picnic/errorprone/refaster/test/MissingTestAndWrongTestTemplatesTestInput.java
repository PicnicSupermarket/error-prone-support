package tech.picnic.errorprone.refaster.test;

final class MissingTestAndWrongTestTemplatesTest implements RefasterTemplateTestCase {
  boolean testWrongName() {
    "foo".equals("");
    "bar".equals("");
    return "baz".equals("");
  }
}
