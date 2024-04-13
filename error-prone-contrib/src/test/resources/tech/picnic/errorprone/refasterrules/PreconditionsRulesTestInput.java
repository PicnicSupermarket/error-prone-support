package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class PreconditionsRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  @SuppressWarnings("RequireNonNull")
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(checkNotNull(null));
  }

  void testCheckArgument() {
    if ("foo".isEmpty()) {
      throw new IllegalArgumentException();
    }
  }

  void testCheckArgumentWithMessage() {
    if ("foo".isEmpty()) {
      throw new IllegalArgumentException("The string is empty");
    }
  }

  void testCheckArgumentWithMessageAndArguments() {
    checkArgument("foo".isEmpty(), String.format("The %s is empty", 1));
    checkArgument("bar".isEmpty(), "The %s is %s".formatted(2.0, false));
  }

  void testCheckElementIndexWithMessage() {
    if (1 < 0 || 1 >= 2) {
      throw new IndexOutOfBoundsException("My index");
    }
  }

  String testRequireNonNull() {
    return checkNotNull("foo");
  }

  void testRequireNonNullStatement() {
    if ("foo" == null) {
      throw new NullPointerException();
    }
  }

  String testRequireNonNullWithMessage() {
    return checkNotNull("foo", "The string is null");
  }

  void testRequireNonNullWithMessageStatement() {
    if ("foo" == null) {
      throw new NullPointerException("The string is null");
    }
  }

  void testCheckPositionIndex() {
    if (1 < 0 || 1 > 2) {
      throw new IndexOutOfBoundsException();
    }
  }

  void testCheckPositionIndexWithMessage() {
    if (1 < 0 || 1 > 2) {
      throw new IndexOutOfBoundsException("My position");
    }
  }

  void testCheckState() {
    if ("foo".isEmpty()) {
      throw new IllegalStateException();
    }
  }

  void testCheckStateWithMessage() {
    if ("foo".isEmpty()) {
      throw new IllegalStateException("The string is empty");
    }
  }

  void testCheckStateWithMessageAndArguments() {
    checkState("foo".isEmpty(), String.format("The %s is empty", 1));
    checkState("bar".isEmpty(), "The %s is %s".formatted(2.0, false));
  }
}
