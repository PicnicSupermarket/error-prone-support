package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class CharSequenceRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Boolean> testCharSequenceIsEmpty() {
    return ImmutableSet.of(
        ((CharSequence) "foo").isEmpty(),
        ((CharSequence) "bar").isEmpty(),
        ((CharSequence) "baz").isEmpty(),
        !((CharSequence) "foo").isEmpty(),
        !((CharSequence) "bar").isEmpty(),
        !((CharSequence) "baz").isEmpty());
  }
}
