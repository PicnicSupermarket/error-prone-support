package tech.picnic.errorprone.refasterrules;

import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableSet;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class EqualityRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Objects.class, Optional.class, isEqual(null), not(null));
  }

  ImmutableSet<Boolean> testPrimitiveOrReferenceEquality() {
    return ImmutableSet.of(
        RoundingMode.UP.equals(RoundingMode.DOWN),
        Objects.equals(RoundingMode.UP, RoundingMode.DOWN),
        RoundingMode.UP.ordinal() == RoundingMode.DOWN.ordinal(),
        !RoundingMode.UP.equals(RoundingMode.DOWN),
        !Objects.equals(RoundingMode.UP, RoundingMode.DOWN),
        RoundingMode.UP.ordinal() != RoundingMode.DOWN.ordinal());
  }

  boolean testEqualsPredicate() {
    // XXX: When boxing is involved this rule seems to break. Example:
    // Stream.of(1).anyMatch(e -> Integer.MIN_VALUE.equals(e));
    return Stream.of("foo").anyMatch(s -> "bar".equals(s));
  }

  ImmutableSet<Boolean> testEnumsReferenceEqualityLambda() {

    return ImmutableSet.of(
        Stream.of(RoundingMode.UP).anyMatch(isEqual(RoundingMode.DOWN)),
        Stream.of(RoundingMode.UP).anyMatch(RoundingMode.DOWN::equals));
  }

  boolean testDoubleNegation() {
    return !!Boolean.TRUE;
  }

  @SuppressWarnings("SimplifyBooleanExpression")
  ImmutableSet<Boolean> testNegation() {
    return ImmutableSet.of(
        true ? !false : false,
        !(true == false),
        !((byte) 3 == (byte) 4),
        !((char) 3 == (char) 4),
        !((short) 3 == (short) 4),
        !(3 == 4),
        !(3L == 4L),
        !(3F == 4F),
        !(3.0 == 4.0),
        !(BoundType.OPEN == BoundType.CLOSED));
  }

  @SuppressWarnings("SimplifyBooleanExpression")
  ImmutableSet<Boolean> testIndirectDoubleNegation() {
    return ImmutableSet.of(
        true ? false : !false,
        !(true != false),
        !((byte) 3 != (byte) 4),
        !((char) 3 != (char) 4),
        !((short) 3 != (short) 4),
        !(3 != 4),
        !(3L != 4L),
        !(3F != 4F),
        !(3.0 != 4.0),
        !(BoundType.OPEN != BoundType.CLOSED));
  }

  Predicate<String> testPredicateLambda() {
    return not(v -> v.isEmpty());
  }

  ImmutableSet<Boolean> testEquals() {
    return ImmutableSet.of(
        Optional.of("foo").equals(Optional.of("bar")),
        Optional.of("baz").equals(Optional.ofNullable("qux")),
        Optional.ofNullable("quux").equals(Optional.of("quuz")));
  }

  boolean testObjectsEquals() {
    return Optional.ofNullable("foo").equals(Optional.ofNullable("bar"));
  }
}
