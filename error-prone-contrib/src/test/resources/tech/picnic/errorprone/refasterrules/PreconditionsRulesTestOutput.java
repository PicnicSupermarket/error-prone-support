package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class PreconditionsRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  @SuppressWarnings("RequireNonNull")
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(checkNotNull(null));
  }

  void testCheckArgument() {
    checkArgument(!"foo".isEmpty());
  }

  void testCheckArgumentWithMessage() {
    checkArgument(!"foo".isEmpty(), "The string is empty");
  }

  void testCheckArgumentWithMessageAndArguments() {
    checkArgument("foo".isEmpty(), "The %s is empty", 1);
    checkArgument("bar".isEmpty(), "The %s is %s", 2.0, false);
  }

  void testCheckElementIndexWithMessage() {
    checkElementIndex(1, 2, "My index");
  }

  String testRequireNonNull() {
    return requireNonNull("foo");
  }

  void testRequireNonNullStatement() {
    requireNonNull("foo");
  }

  String testRequireNonNullWithMessage() {
    return requireNonNull("foo", "The string is null");
  }

  void testRequireNonNullWithMessageStatement() {
    requireNonNull("foo", "The string is null");
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

  void testCheckStateWithMessageAndArguments() {
    checkState("foo".isEmpty(), "The %s is empty", 1);
    checkState("bar".isEmpty(), "The %s is %s", 2.0, false);
  }
}
