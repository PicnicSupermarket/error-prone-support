package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster templates from `MissingTestAndWrongTestTemplates`. */
final class MissingTestAndWrongTestTemplatesTest implements RefasterTemplateTestCase {
  /*
   *  ERROR: The following matches unexpectedly occurred in method `testWrongName`:
   *  - Template `StringIsEmpty` matches on line 6, while it should match in a method named `testStringIsEmpty`.
   *  - Template `StringIsEmpty` matches on line 7, while it should match in a method named `testStringIsEmpty`.
   *  - Template `StringIsEmpty` matches on line 8, while it should match in a method named `testStringIsEmpty`.
   */
  boolean testWrongName() {
    "foo".isEmpty();
    "bar".isEmpty();
    return "baz".isEmpty();
  }
}
/*
 *  ERROR: Did not encounter a test in `MissingTestAndWrongTestTemplatesTestInput.java` for the following template(s):
 *  - TemplateWithoutTest
 */
