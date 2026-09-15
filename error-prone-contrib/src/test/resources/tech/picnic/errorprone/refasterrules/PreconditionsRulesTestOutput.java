package tech.picnic.errorprone.refasterrules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class PreconditionsRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  @SuppressWarnings("RequireNonNullExpression")
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(checkNotNull(null), Optional.class);
  }

  void testCheckArgumentNot() {
    checkArgument(!"foo".isEmpty());
    checkArgument("bar".isEmpty());
  }

  void testCheckArgumentNotWithString() {
    checkArgument(!"foo".isEmpty(), "bar");
    checkArgument("baz".isEmpty(), "qux");
  }

  void testCheckElementIndex() {
    checkElementIndex(1, 2, "foo");
  }

  ImmutableSet<String> testRequireNonNullExpression() {
    return ImmutableSet.of(requireNonNull("foo"), requireNonNull("bar"));
  }

  void testRequireNonNullBlock() {
    requireNonNull("foo");
  }

  String testRequireNonNullWithStringExpression() {
    return requireNonNull("foo", "bar");
  }

  void testRequireNonNullWithStringBlock() {
    requireNonNull("foo", "bar");
  }

  void testCheckPositionIndex() {
    checkPositionIndex(1, 2);
  }

  void testCheckPositionIndexWithString() {
    checkPositionIndex(1, 2, "foo");
  }

  void testCheckStateNot() {
    checkState(!"foo".isEmpty());
  }

  void testCheckStateNotWithString() {
    checkState(!"foo".isEmpty(), "bar");
  }
}
