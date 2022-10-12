package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class PreconditionsRulesTest implements RefasterRuleCollectionTestCase {
  void testCheckArgument() {
    checkArgument(!"".isEmpty());
  }

  void testCheckArgumentWithMessage() {
    checkArgument(!"".isEmpty(), "The string is empty");
  }

  void testCheckState() {
    checkState(!"".isEmpty());
  }

  void testCheckStateWithMessage() {
    checkState(!"".isEmpty(), "The string is empty");
  }
}
