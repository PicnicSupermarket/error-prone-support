package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster templates from {@link MethodWithoutPrefixTemplates}. */
final class MethodWithoutPrefixTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    return "foo".equals("");
  }

  private void foo() {}

  public String bar() {
    return "";
  }
}
