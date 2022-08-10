package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster templates from {@link MatchInWrongMethodTemplates}. */
final class MatchInWrongMethodTemplatesTest implements RefasterTemplateTestCase {
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
