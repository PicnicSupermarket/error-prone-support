package tech.picnic.errorprone.refasterrules;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class StringBuilderRulesTest implements RefasterRuleCollectionTestCase {
  StringBuilder testStringBuilderRepeat() {
    return new StringBuilder().repeat("foo", 1);
  }
}
