package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster templates from `PartialTemplateMatchTemplates`. */
final class TemplateWithoutTestTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    boolean a = "foo".equals("");
    "bar".isEmpty();
    return "baz".isEmpty();
  }
  /* The following matches unexpectedly occurred in method `testStringIsEmpty`:
  - Template `StringEquals` matches on line 6, while it should match in a method named `testStringEquals`.
  */

  private void foo() {
  }
}

