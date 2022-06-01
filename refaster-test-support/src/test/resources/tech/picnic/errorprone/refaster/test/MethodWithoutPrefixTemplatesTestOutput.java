package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster templates from {@link MethodWithoutPrefixTemplates}. */
final class MethodWithoutPrefixTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    return "foo".isEmpty();
  }

  /* ERROR: Method names should start with `test`. */
  private void foo() {}

  /* ERROR: Method names should start with `test`. */
  public String bar() {
    return "";
  }
}
