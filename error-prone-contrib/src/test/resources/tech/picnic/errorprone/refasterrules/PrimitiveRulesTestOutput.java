package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Booleans;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Chars;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class PrimitiveRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(
        Booleans.class,
        Bytes.class,
        Chars.class,
        Doubles.class,
        Floats.class,
        Ints.class,
        Longs.class,
        Shorts.class);
  }

  ImmutableSet<Boolean> testLessThan() {
    return ImmutableSet.of(
        (byte) 3 < (byte) 4,
        (char) 3 < (char) 4,
        (short) 3 < (short) 4,
        3 < 4,
        3L < 4L,
        3F < 4F,
        3.0 < 4.0);
  }

  ImmutableSet<Boolean> testLessThanOrEqualTo() {
    return ImmutableSet.of(
        (byte) 3 <= (byte) 4,
        (char) 3 <= (char) 4,
        (short) 3 <= (short) 4,
        3 <= 4,
        3L <= 4L,
        3F <= 4F,
        3.0 <= 4.0);
  }

  ImmutableSet<Boolean> testGreaterThan() {
    return ImmutableSet.of(
        (byte) 3 > (byte) 4,
        (char) 3 > (char) 4,
        (short) 3 > (short) 4,
        3 > 4,
        3L > 4L,
        3F > 4F,
        3.0 > 4.0);
  }

  ImmutableSet<Boolean> testGreaterThanOrEqualTo() {
    return ImmutableSet.of(
        (byte) 3 >= (byte) 4,
        (char) 3 >= (char) 4,
        (short) 3 >= (short) 4,
        3 >= 4,
        3L >= 4L,
        3F >= 4F,
        3.0 >= 4.0);
  }

  int testLongToIntExact() {
    return Math.toIntExact(Long.MAX_VALUE);
  }

  int testBooleanHashCode() {
    return Boolean.hashCode(true);
  }

  int testByteHashCode() {
    return Byte.hashCode((byte) 1);
  }

  int testCharacterHashCode() {
    return Character.hashCode('a');
  }

  int testShortHashCode() {
    return Short.hashCode((short) 1);
  }

  int testIntegerHashCode() {
    return Integer.hashCode(1);
  }

  int testLongHashCode() {
    return Long.hashCode(1);
  }

  int testFloatHashCode() {
    return Float.hashCode(1);
  }

  int testDoubleHashCode() {
    return Double.hashCode(1);
  }

  int testBooleanCompare() {
    return Boolean.compare(false, true);
  }

  int testCharacterCompare() {
    return Character.compare('a', 'b');
  }

  int testShortCompare() {
    return Short.compare((short) 1, (short) 2);
  }

  int testIntegerCompare() {
    return Integer.compare(1, 2);
  }

  int testLongCompare() {
    return Long.compare(1, 2);
  }

  int testFloatCompare() {
    return Float.compare(1, 2);
  }

  int testDoubleCompare() {
    return Double.compare(1, 2);
  }

  int testCharacterBytes() {
    return Character.BYTES;
  }

  int testShortBytes() {
    return Short.BYTES;
  }

  int testIntegerBytes() {
    return Integer.BYTES;
  }

  int testLongBytes() {
    return Long.BYTES;
  }

  int testFloatBytes() {
    return Float.BYTES;
  }

  int testDoubleBytes() {
    return Double.BYTES;
  }

  boolean testFloatIsFinite() {
    return Float.isFinite(1);
  }

  boolean testDoubleIsFinite() {
    return Double.isFinite(1);
  }

  ImmutableSet<Boolean> testIntegerSignumIsPositive() {
    return ImmutableSet.of(
        Integer.signum(1) == 1,
        Integer.signum(2) == 1,
        Integer.signum(3) != 1,
        Integer.signum(4) != 1);
  }

  ImmutableSet<Boolean> testIntegerSignumIsNegative() {
    return ImmutableSet.of(
        Integer.signum(1) == -1,
        Integer.signum(2) == -1,
        Integer.signum(3) != -1,
        Integer.signum(4) != -1);
  }

  ImmutableSet<Boolean> testLongSignumIsPositive() {
    return ImmutableSet.of(
        Long.signum(1L) == 1, Long.signum(2L) == 1, Long.signum(3L) != 1, Long.signum(4L) != 1);
  }

  ImmutableSet<Boolean> testLongSignumIsNegative() {
    return ImmutableSet.of(
        Long.signum(1L) == -1, Long.signum(2L) == -1, Long.signum(3L) != -1, Long.signum(4L) != -1);
  }
}
