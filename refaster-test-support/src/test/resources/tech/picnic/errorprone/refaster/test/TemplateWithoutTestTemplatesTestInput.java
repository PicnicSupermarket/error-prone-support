package tech.picnic.errorprone.refaster.test;

/** Code to test the Refaster rules from {@link TemplateWithoutTestTemplates}. */
final class TemplateWithoutTestTemplatesTest implements RefasterRuleCollectionTestCase {
  boolean testStringIsEmpty() {
    return "foo".equals("");
  }
}
