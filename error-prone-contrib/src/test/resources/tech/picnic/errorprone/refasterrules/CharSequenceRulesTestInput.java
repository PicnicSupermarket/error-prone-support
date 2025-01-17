package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class CharSequenceRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Boolean> testCharSequenceIsEmpty() {
    return ImmutableSet.of(
        new StringBuilder("foo").length() == 0,
        new StringBuilder("bar").length() <= 0,
        new StringBuilder("baz").length() < 1,
        new StringBuilder("qux").length() != 0,
        new StringBuilder("quux").length() > 0,
        new StringBuilder("corge").length() >= 1);
  }
}
