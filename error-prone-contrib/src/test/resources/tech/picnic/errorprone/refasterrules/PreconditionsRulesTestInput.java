package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class PreconditionsRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
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

  void testCheckElementIndexWithMessage() {
    if (1 < 0 || 1 >= 2) {
      throw new IndexOutOfBoundsException("My index");
    }
  }

  void testRequireNonNull() {
    if ("foo" == null) {
      throw new NullPointerException();
    }
    if (null == "foo") {
      throw new NullPointerException();
    }
    checkNotNull("foo");
    checkArgument("foo" != null);
    checkArgument(null != "foo");
  }

  void testRequireNonNullWithMessage() {
    if ("foo" == null) {
      throw new NullPointerException("The string is null");
    }
    if (null == "foo") {
      throw new NullPointerException("The string is null");
    }
    checkNotNull("foo", "The string is null");
    checkArgument("foo" != null, "The string is null");
    checkArgument(null != "foo", "The string is null");
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
}
