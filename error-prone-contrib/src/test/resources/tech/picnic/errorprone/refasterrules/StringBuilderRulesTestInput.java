package tech.picnic.errorprone.refasterrules;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class StringBuilderRulesTest implements RefasterRuleCollectionTestCase {
  StringBuilder testStringBuilderRepeat() {
    return new StringBuilder().append("foo".repeat(3));
  }
}
