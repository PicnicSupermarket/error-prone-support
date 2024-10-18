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
        !((byte) 3 >= (byte) 4),
        !((char) 3 >= (char) 4),
        !((short) 3 >= (short) 4),
        !(3 >= 4),
        !(3L >= 4L),
        !(3F >= 4F),
        !(3.0 >= 4.0));
  }

  ImmutableSet<Boolean> testLessThanOrEqualTo() {
    return ImmutableSet.of(
        !((byte) 3 > (byte) 4),
        !((char) 3 > (char) 4),
        !((short) 3 > (short) 4),
        !(3 > 4),
        !(3L > 4L),
        !(3F > 4F),
        !(3.0 > 4.0));
  }

  ImmutableSet<Boolean> testGreaterThan() {
    return ImmutableSet.of(
        !((byte) 3 <= (byte) 4),
        !((char) 3 <= (char) 4),
        !((short) 3 <= (short) 4),
        !(3 <= 4),
        !(3L <= 4L),
        !(3F <= 4F),
        !(3.0 <= 4.0));
  }

  ImmutableSet<Boolean> testGreaterThanOrEqualTo() {
    return ImmutableSet.of(
        !((byte) 3 < (byte) 4),
        !((char) 3 < (char) 4),
        !((short) 3 < (short) 4),
        !(3 < 4),
        !(3L < 4L),
        !(3F < 4F),
        !(3.0 < 4.0));
  }

  int testLongToIntExact() {
    return Ints.checkedCast(Long.MAX_VALUE);
  }

  int testBooleanHashCode() {
    return Booleans.hashCode(true);
  }

  int testByteHashCode() {
    return Bytes.hashCode((byte) 1);
  }

  int testCharacterHashCode() {
    return Chars.hashCode('a');
  }

  int testShortHashCode() {
    return Shorts.hashCode((short) 1);
  }

  int testIntegerHashCode() {
    return Ints.hashCode(1);
  }

  int testLongHashCode() {
    return Longs.hashCode(1);
  }

  int testFloatHashCode() {
    return Floats.hashCode(1);
  }

  int testDoubleHashCode() {
    return Doubles.hashCode(1);
  }

  int testCharacterBytes() {
    return Chars.BYTES;
  }

  int testShortBytes() {
    return Shorts.BYTES;
  }

  int testIntegerBytes() {
    return Ints.BYTES;
  }

  int testLongBytes() {
    return Longs.BYTES;
  }

  int testFloatBytes() {
    return Floats.BYTES;
  }

  int testDoubleBytes() {
    return Doubles.BYTES;
  }

  boolean testFloatIsFinite() {
    return Floats.isFinite(1);
  }

  boolean testDoubleIsFinite() {
    return Doubles.isFinite(1);
  }

  ImmutableSet<Boolean> testIntegerSignumIsPositive() {
    return ImmutableSet.of(
        Integer.signum(1) > 0,
        Integer.signum(2) >= 1,
        Integer.signum(3) <= 0,
        Integer.signum(4) < 1);
  }

  ImmutableSet<Boolean> testIntegerSignumIsNegative() {
    return ImmutableSet.of(
        Integer.signum(1) < 0,
        Integer.signum(2) <= -1,
        Integer.signum(3) >= 0,
        Integer.signum(4) > -1);
  }

  ImmutableSet<Boolean> testLongSignumIsPositive() {
    return ImmutableSet.of(
        Long.signum(1L) > 0, Long.signum(2L) >= 1, Long.signum(3L) <= 0, Long.signum(4L) < 1);
  }

  ImmutableSet<Boolean> testLongSignumIsNegative() {
    return ImmutableSet.of(
        Long.signum(1L) < 0, Long.signum(2L) <= -1, Long.signum(3L) >= 0, Long.signum(4L) > -1);
  }

  int testIntegerCompareUnsigned() {
    return UnsignedInts.compare(1, 2);
  }

  long testLongCompareUnsigned() {
    return UnsignedLongs.compare(1, 2);
  }

  int testIntegerDivideUnsigned() {
    return UnsignedInts.divide(1, 2);
  }

  long testLongDivideUnsigned() {
    return UnsignedLongs.divide(1, 2);
  }

  int testIntegerRemainderUnsigned() {
    return UnsignedInts.remainder(1, 2);
  }

  long testLongRemainderUnsigned() {
    return UnsignedLongs.remainder(1, 2);
  }

  ImmutableSet<Integer> testIntegerParseUnsignedInt() {
    return ImmutableSet.of(UnsignedInts.parseUnsignedInt("1"), Integer.parseUnsignedInt("2", 10));
  }

  ImmutableSet<Long> testLongParseUnsignedLong() {
    return ImmutableSet.of(UnsignedLongs.parseUnsignedLong("1"), Long.parseUnsignedLong("2", 10));
  }

  int testIntegerParseUnsignedIntWithRadix() {
    return UnsignedInts.parseUnsignedInt("1", 2);
  }

  long testLongParseUnsignedLongWithRadix() {
    return UnsignedLongs.parseUnsignedLong("1", 2);
  }

  ImmutableSet<String> testIntegerToUnsignedString() {
    return ImmutableSet.of(UnsignedInts.toString(1), Integer.toUnsignedString(2, 10));
  }

  ImmutableSet<String> testLongToUnsignedString() {
    return ImmutableSet.of(UnsignedLongs.toString(1), Long.toUnsignedString(2, 10));
  }

  String testIntegerToUnsignedStringWithRadix() {
    return UnsignedInts.toString(1, 2);
  }

  String testLongToUnsignedStringWithRadix() {
    return UnsignedLongs.toString(1, 2);
  }
}
