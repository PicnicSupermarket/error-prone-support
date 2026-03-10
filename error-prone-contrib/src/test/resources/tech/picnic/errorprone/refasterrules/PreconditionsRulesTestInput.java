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
    if (!"bar".isEmpty()) {
      throw new IllegalArgumentException();
    }
  }

  void testCheckArgumentNotWithString() {
    if ("foo".isEmpty()) {
      throw new IllegalArgumentException("bar");
    }
    if (!"baz".isEmpty()) {
      throw new IllegalArgumentException("qux");
    }
  }

  void testCheckElementIndex() {
    if (1 < 0 || 1 >= 2) {
      throw new IndexOutOfBoundsException("foo");
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
    return checkNotNull("foo", "bar");
  }

  void testRequireNonNullWithStringBlock() {
    if ("foo" == null) {
      throw new NullPointerException("bar");
    }
  }

  void testCheckPositionIndex() {
    if (1 < 0 || 1 > 2) {
      throw new IndexOutOfBoundsException();
    }
  }

  void testCheckPositionIndexWithString() {
    if (1 < 0 || 1 > 2) {
      throw new IndexOutOfBoundsException("foo");
    }
  }

  void testCheckStateNot() {
    if ("foo".isEmpty()) {
      throw new IllegalStateException();
    }
  }

  void testCheckStateNotWithString() {
    if ("foo".isEmpty()) {
      throw new IllegalStateException("bar");
    }
  }
}
