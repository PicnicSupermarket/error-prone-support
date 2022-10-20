package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;

import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class PreconditionsRulesTest implements RefasterRuleCollectionTestCase {
  void testCheckArgument() {
    checkArgument(!"foo".isEmpty());
  }

  void testCheckArgumentWithMessage() {
    checkArgument(!"foo".isEmpty(), "The string is empty");
  }

  void testCheckElementIndexWithMessage() {
    checkElementIndex(1, 2, "My index");
  }

  void testCheckNotNull() {
    checkNotNull("foo");
  }

  void testCheckNotNullWithMessage() {
    checkNotNull("foo", "The string is null");
  }

  void testCheckPositionIndex() {
    checkPositionIndex(1, 2);
  }

  void testCheckPositionIndexWithMessage() {
    checkPositionIndex(1, 2, "My position");
  }

  void testCheckState() {
    checkState(!"foo".isEmpty());
  }

  void testCheckStateWithMessage() {
    checkState(!"foo".isEmpty(), "The string is empty");
  }
}
