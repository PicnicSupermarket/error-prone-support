package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableSet;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.stream.Stream;

final class EqualityTemplatesTest implements RefasterTemplateTestCase {
  ImmutableSet<Boolean> testPrimitiveOrReferenceEquality() {
    return ImmutableSet.of(
        true == false,
        (byte) 0 == (byte) 1,
        (short) 0 == (short) 1,
        0 == 1,
        0L == 1L,
        0F == 1F,
        0.0 == 1.0,
        Objects.equals(Boolean.TRUE, Boolean.FALSE),
        Objects.equals(Byte.valueOf((byte) 0), Byte.valueOf((byte) 1)),
        Objects.equals(Short.valueOf((short) 0), Short.valueOf((short) 1)),
        Objects.equals(Integer.valueOf(0), Integer.valueOf(1)),
        Objects.equals(Long.valueOf(0L), Long.valueOf(1L)),
        Objects.equals(Float.valueOf(0F), Float.valueOf(1F)),
        Objects.equals(Double.valueOf(0.0), Double.valueOf(1.0)),
        RoundingMode.UP == RoundingMode.DOWN,
        RoundingMode.UP == RoundingMode.DOWN,
        true != false,
        (byte) 0 != (byte) 1,
        (short) 0 != (short) 1,
        0 != 1,
        0L != 1L,
        0F != 1F,
        0.0 != 1.0,
        !Objects.equals(Boolean.TRUE, Boolean.FALSE),
        !Objects.equals(Byte.valueOf((byte) 0), Byte.valueOf((byte) 1)),
        !Objects.equals(Short.valueOf((short) 0), Short.valueOf((short) 1)),
        !Objects.equals(Integer.valueOf(0), Integer.valueOf(1)),
        !Objects.equals(Long.valueOf(0L), Long.valueOf(1L)),
        !Objects.equals(Float.valueOf(0F), Float.valueOf(1F)),
        !Objects.equals(Double.valueOf(0.0), Double.valueOf(1.0)),
        RoundingMode.UP != RoundingMode.DOWN,
        RoundingMode.UP != RoundingMode.DOWN);
  }

  boolean testEqualsPredicate() {
    // XXX: When boxing is involved this rule seems to break. Example:
    // Stream.of(1).anyMatch(e -> Integer.MIN_VALUE.equals(e));
    return Stream.of("foo").anyMatch("bar"::equals);
  }

  boolean testDoubleNegation() {
    return true;
  }

  ImmutableSet<Boolean> testNegation() {
    return ImmutableSet.of(
        true != false,
        true != false,
        (byte) 3 != (byte) 4,
        (short) 3 != (short) 4,
        3 != 4,
        3L != 4L,
        3F != 4F,
        3.0 != 4.0,
        BoundType.OPEN != BoundType.CLOSED);
  }

  ImmutableSet<Boolean> testIndirectDoubleNegation() {
    return ImmutableSet.of(
        true == false,
        true == false,
        (byte) 3 == (byte) 4,
        (short) 3 == (short) 4,
        3 == 4,
        3L == 4L,
        3F == 4F,
        3.0 == 4.0,
        BoundType.OPEN == BoundType.CLOSED);
  }
}
