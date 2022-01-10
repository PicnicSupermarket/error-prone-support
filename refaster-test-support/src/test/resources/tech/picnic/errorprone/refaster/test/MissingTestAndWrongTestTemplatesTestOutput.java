package tech.picnic.errorprone.refaster.test;

final class MissingTestAndWrongTestTemplatesTest implements RefasterTemplateTestCase {
  boolean testWrongName() {
    "foo".isEmpty();
    "bar".isEmpty();
    return "baz".isEmpty();
  }
}
/* Did not encounter test in /tech.picnic.errorprone.refaster.test.MissingTestAndWrongTestTemplatesTestInput.java for the following template(s):
- TemplateWithoutTest
*/
/* The following matches occurred in method `testWrongName` (position: [136,238]):
- Template `StringIsEmpty` matched on position: [166..182)
- Template `StringIsEmpty` matched on position: [188..204)
- Template `StringIsEmpty` matched on position: [217..233)
*/
