package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster templates from `MissingTestAndWrongTestTemplates`. */
final class MissingTestAndWrongTestTemplatesTest implements RefasterTemplateTestCase {
  boolean testWrongName() {
    "foo".equals("");
    "bar".equals("");
    return "baz".equals("");
  }
}
