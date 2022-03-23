package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster templates from `PartialTemplateMatchTemplates`. */
final class TemplateWithoutTestTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    boolean a = "foo" == "";
    "bar".equals("");
    return "baz".equals("");
  }

  private void foo() {
  }
}
