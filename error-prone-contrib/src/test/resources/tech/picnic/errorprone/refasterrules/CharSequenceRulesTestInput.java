package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class CharSequenceRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Boolean> testCharSequenceIsEmpty() {
    return ImmutableSet.of(
        "foo".length() == 0,
        "bar".length() <= 0,
        "baz".length() < 1,
        "qux".length() != 0,
        "quux".length() > 0,
        "corge".length() >= 1);
  }
}
