package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableSet;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterTemplateTestCase;

final class EqualityTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Objects.class);
  }

  ImmutableSet<Boolean> testPrimitiveOrReferenceEquality() {
    return ImmutableSet.of(
        RoundingMode.UP.equals(RoundingMode.DOWN),
        Objects.equals(RoundingMode.UP, RoundingMode.DOWN),
        !RoundingMode.UP.equals(RoundingMode.DOWN),
        !Objects.equals(RoundingMode.UP, RoundingMode.DOWN));
  }

  boolean testEqualsPredicate() {
    // XXX: When boxing is involved this rule seems to break. Example:
    // Stream.of(1).anyMatch(e -> Integer.MIN_VALUE.equals(e));
    return Stream.of("foo").anyMatch(s -> "bar".equals(s));
  }

  boolean testDoubleNegation() {
    return !!Boolean.TRUE;
  }

  ImmutableSet<Boolean> testNegation() {
    return ImmutableSet.of(
        Boolean.TRUE ? !Boolean.FALSE : Boolean.FALSE,
        !(Boolean.TRUE == Boolean.FALSE),
        !((byte) 3 == (byte) 4),
        !((short) 3 == (short) 4),
        !(3 == 4),
        !(3L == 4L),
        !(3F == 4F),
        !(3.0 == 4.0),
        !(BoundType.OPEN == BoundType.CLOSED));
  }

  ImmutableSet<Boolean> testIndirectDoubleNegation() {
    return ImmutableSet.of(
        Boolean.TRUE ? Boolean.FALSE : !Boolean.FALSE,
        !(Boolean.TRUE != Boolean.FALSE),
        !((byte) 3 != (byte) 4),
        !((short) 3 != (short) 4),
        !(3 != 4),
        !(3L != 4L),
        !(3F != 4F),
        !(3.0 != 4.0),
        !(BoundType.OPEN != BoundType.CLOSED));
  }
}
