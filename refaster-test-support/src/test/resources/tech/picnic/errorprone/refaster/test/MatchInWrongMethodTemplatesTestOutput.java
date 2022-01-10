package tech.picnic.errorprone.refaster.test;

final class MatchInWrongMethodTemplatesTest implements RefasterTemplateTestCase {
  boolean testWrongName() {
    "foo".isEmpty();
    "bar".isEmpty();
    return "baz".isEmpty();
  }
}
/* The following matches occurred in method `testWrongName` (position: [131,233]):
- Template `StringIsEmpty` matched on position: [161..177)
- Template `StringIsEmpty` matched on position: [183..199)
- Template `StringIsEmpty` matched on position: [212..228)
*/
