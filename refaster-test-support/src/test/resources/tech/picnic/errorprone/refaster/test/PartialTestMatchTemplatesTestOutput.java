package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster templates from {@link PartialTestMatchTemplates}. */
final class PartialTestMatchTemplatesTest implements RefasterTemplateTestCase {
  /*
   *  ERROR: The following matches unexpectedly occurred in method `testStringIsEmpty`:
   *  - Template `StringEquals` matches on line 6, while it should match in a method named `testStringEquals`.
   */
  boolean testStringIsEmpty() {
    boolean b = "foo".equals("");
    return "bar".isEmpty();
  }
}
