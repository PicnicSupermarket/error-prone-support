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
import com.google.common.primitives.UnsignedInts;
import com.google.common.primitives.UnsignedLongs;
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
        Shorts.class,
        UnsignedInts.class,
        UnsignedLongs.class);
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

  int testIntegerCompareUnsigned() {
    return Integer.compareUnsigned(1, 2);
  }

  long testLongCompareUnsigned() {
    return Long.compareUnsigned(1, 2);
  }

  int testIntegerDivideUnsigned() {
    return Integer.divideUnsigned(1, 2);
  }

  long testLongDivideUnsigned() {
    return Long.divideUnsigned(1, 2);
  }

  int testIntegerRemainderUnsigned() {
    return Integer.remainderUnsigned(1, 2);
  }

  long testLongRemainderUnsigned() {
    return Long.remainderUnsigned(1, 2);
  }

  ImmutableSet<Integer> testIntegerParseUnsignedInt() {
    return ImmutableSet.of(Integer.parseUnsignedInt("1"), Integer.parseUnsignedInt("2"));
  }

  ImmutableSet<Long> testLongParseUnsignedLong() {
    return ImmutableSet.of(Long.parseUnsignedLong("1"), Long.parseUnsignedLong("2"));
  }

  int testIntegerParseUnsignedIntWithRadix() {
    return Integer.parseUnsignedInt("1", 2);
  }

  long testLongParseUnsignedLongWithRadix() {
    return Long.parseUnsignedLong("1", 2);
  }

  ImmutableSet<String> testIntegerToUnsignedString() {
    return ImmutableSet.of(Integer.toUnsignedString(1), Integer.toUnsignedString(2));
  }

  ImmutableSet<String> testLongToUnsignedString() {
    return ImmutableSet.of(Long.toUnsignedString(1), Long.toUnsignedString(2));
  }

  String testIntegerToUnsignedStringWithRadix() {
    return Integer.toUnsignedString(1, 2);
  }

  String testLongToUnsignedStringWithRadix() {
    return Long.toUnsignedString(1, 2);
  }
}
