package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class CharSequenceRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Boolean> testCharSequenceIsEmpty() {
    return ImmutableSet.of(
        "foo".isEmpty(),
        "bar".isEmpty(),
        "baz".isEmpty(),
        !"qux".isEmpty(),
        !"quux".isEmpty(),
        !"corge".isEmpty());
  }
}
