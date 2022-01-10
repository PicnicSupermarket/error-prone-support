package tech.picnic.errorprone.refaster.test;

final class MatchInWrongMethodTemplatesTest implements RefasterTemplateTestCase {
  boolean testWrongName() {
    "foo".equals("");
    "bar".equals("");
    return "baz".equals("");
  }
}
