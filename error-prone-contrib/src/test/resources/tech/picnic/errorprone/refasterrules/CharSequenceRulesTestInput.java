package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class CharSequenceRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Boolean> testCharSequenceIsEmpty() {
    return ImmutableSet.of(
        ((CharSequence) "foo").length() == 0,
        ((CharSequence) "bar").length() <= 0,
        ((CharSequence) "baz").length() < 1,
        ((CharSequence) "foo").length() != 0,
        ((CharSequence) "bar").length() > 0,
        ((CharSequence) "baz").length() >= 1);
  }
}
