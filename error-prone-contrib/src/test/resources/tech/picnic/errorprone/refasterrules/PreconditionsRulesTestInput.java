package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class PreconditionsRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  @SuppressWarnings("RequireNonNullExpression")
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(checkNotNull(null));
  }

  void testCheckArgumentNot() {
    if ("foo".isEmpty()) {
      throw new IllegalArgumentException();
    }
  }

  void testCheckArgumentNotWithString() {
    if ("foo".isEmpty()) {
      throw new IllegalArgumentException("The string is empty");
    }
  }

  void testCheckElementIndex() {
    if (1 < 0 || 1 >= 2) {
      throw new IndexOutOfBoundsException("My index");
    }
  }

  String testRequireNonNullExpression() {
    return checkNotNull("foo");
  }

  void testRequireNonNullBlock() {
    if ("foo" == null) {
      throw new NullPointerException();
    }
  }

  String testRequireNonNullWithStringExpression() {
    return checkNotNull("foo", "The string is null");
  }

  void testRequireNonNullWithStringBlock() {
    if ("foo" == null) {
      throw new NullPointerException("The string is null");
    }
  }

  void testCheckPositionIndex() {
    if (1 < 0 || 1 > 2) {
      throw new IndexOutOfBoundsException();
    }
  }

  void testCheckPositionIndexWithString() {
    if (1 < 0 || 1 > 2) {
      throw new IndexOutOfBoundsException("My position");
    }
  }

  void testCheckStateNot() {
    if ("foo".isEmpty()) {
      throw new IllegalStateException();
    }
  }

  void testCheckStateNotWithString() {
    if ("foo".isEmpty()) {
      throw new IllegalStateException("The string is empty");
    }
  }
}
