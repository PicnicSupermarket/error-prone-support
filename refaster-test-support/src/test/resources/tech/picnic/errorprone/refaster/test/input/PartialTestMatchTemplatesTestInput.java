package tech.picnic.errorprone.refaster.test.input;

/** Code to test the Refaster templates from {@link PartialTestMatchTemplates}. */
final class PartialTestMatchTemplatesTest implements RefasterTemplateTestCase {
  boolean testStringIsEmpty() {
    boolean b = "foo".toCharArray().length == 0;
    return "bar".equals("");
  }
}
