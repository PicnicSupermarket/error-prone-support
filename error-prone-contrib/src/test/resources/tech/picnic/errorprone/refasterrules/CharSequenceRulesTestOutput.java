package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class CharSequenceRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Boolean> testCharSequenceIsEmpty() {
    return ImmutableSet.of(
        new StringBuilder("foo").isEmpty(),
        new StringBuilder("bar").isEmpty(),
        new StringBuilder("baz").isEmpty(),
        !new StringBuilder("qux").isEmpty(),
        !new StringBuilder("quux").isEmpty(),
        !new StringBuilder("corge").isEmpty());
  }
}
