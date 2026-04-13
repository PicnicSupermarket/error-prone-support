package tech.picnic.errorprone.refasterrules;

import com.google.common.primitives.Chars;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.UnsignedBytes;
import com.google.common.primitives.UnsignedInts;
import com.google.common.primitives.UnsignedLongs;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.Arrays;
import java.util.Comparator;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with primitives. */
@OnlineDocumentation
final class PrimitiveRules {
  private PrimitiveRules() {}

  /** Prefer {@code a < b} over less explicit alternatives. */
  static final class LessThan {
    @BeforeTemplate
    @SuppressWarnings("java:S1940" /* This violation will be rewritten. */)
    boolean before(double a, double b) {
      return !(a >= b);
    }

    @AfterTemplate
    boolean after(double a, double b) {
      return a < b;
    }
  }

  /** Prefer {@code a <= b} over less explicit alternatives. */
  static final class LessThanOrEqualTo {
    @BeforeTemplate
    @SuppressWarnings("java:S1940" /* This violation will be rewritten. */)
    boolean before(double a, double b) {
      return !(a > b);
    }

    @AfterTemplate
    boolean after(double a, double b) {
      return a <= b;
    }
  }

  /** Prefer {@code a > b} over less explicit alternatives. */
  static final class GreaterThan {
    @BeforeTemplate
    @SuppressWarnings("java:S1940" /* This violation will be rewritten. */)
    boolean before(double a, double b) {
      return !(a <= b);
    }

    @AfterTemplate
    boolean after(double a, double b) {
      return a > b;
    }
  }

  /** Prefer {@code a >= b} over less explicit alternatives. */
  static final class GreaterThanOrEqualTo {
    @BeforeTemplate
    @SuppressWarnings("java:S1940" /* This violation will be rewritten. */)
    boolean before(double a, double b) {
      return !(a < b);
    }

    @AfterTemplate
    boolean after(double a, double b) {
      return a >= b;
    }
  }

  /** Prefer {@link Math#clamp(long, int, int)} over non-JDK or more verbose alternatives. */
  static final class MathClampInt {
    // XXX: The `Math.min`/`Math.max` patterns do not throw an `IllegalArgumentException` if `min >
    // max`, while the `Math.clamp` pattern does. This is considered an acceptable behavioral
    // change.
    @BeforeTemplate
    @SuppressWarnings("java:S6885" /* This violation will be rewritten. */)
    int before(int value, int min, int max) {
      return Refaster.anyOf(
          Math.min(max, Math.max(value, min)),
          Math.min(Math.max(value, min), max),
          Math.max(min, Math.min(value, max)),
          Math.max(Math.min(value, max), min),
          Ints.constrainToRange(value, min, max));
    }

    @AfterTemplate
    int after(int value, int min, int max) {
      return Math.clamp(value, min, max);
    }
  }

  /** Prefer {@link Math#clamp(long, long, long)} over non-JDK or more verbose alternatives. */
  static final class MathClampLong {
    // XXX: The `Math.min`/`Math.max` patterns do not throw an `IllegalArgumentException` if `min >
    // max`, while the `Math.clamp` pattern does. This is considered an acceptable behavioral
    // change.
    @BeforeTemplate
    @SuppressWarnings("java:S6885" /* This violation will be rewritten. */)
    long before(long value, long min, long max) {
      return Refaster.anyOf(
          Math.min(max, Math.max(value, min)),
          Math.min(Math.max(value, min), max),
          Math.max(min, Math.min(value, max)),
          Math.max(Math.min(value, max), min),
          Longs.constrainToRange(value, min, max));
    }

    @AfterTemplate
    long after(long value, long min, long max) {
      return Math.clamp(value, min, max);
    }
  }

  /** Prefer {@link Math#clamp(float, float, float)} over non-JDK or more verbose alternatives. */
  static final class MathClampFloat {
    // XXX: The `Math.min`/`Math.max` patterns do not throw an `IllegalArgumentException` if `min >
    // max`, while the `Math.clamp` pattern does. This is considered an acceptable behavioral
    // change.
    @BeforeTemplate
    @SuppressWarnings("java:S6885" /* This violation will be rewritten. */)
    float before(float value, float min, float max) {
      return Refaster.anyOf(
          Math.min(max, Math.max(value, min)),
          Math.min(Math.max(value, min), max),
          Math.max(min, Math.min(value, max)),
          Math.max(Math.min(value, max), min),
          Floats.constrainToRange(value, min, max));
    }

    @AfterTemplate
    float after(float value, float min, float max) {
      return Math.clamp(value, min, max);
    }
  }

  /**
   * Prefer {@link Math#clamp(double, double, double)} over non-JDK or more verbose alternatives.
   */
  static final class MathClampDouble {
    // XXX: The `Math.min`/`Math.max` patterns do not throw an `IllegalArgumentException` if `min >
    // max`, while the `Math.clamp` pattern does. This is considered an acceptable behavioral
    // change.
    @BeforeTemplate
    @SuppressWarnings("java:S6885" /* This violation will be rewritten. */)
    double before(double value, double min, double max) {
      return Refaster.anyOf(
          Math.min(max, Math.max(value, min)),
          Math.min(Math.max(value, min), max),
          Math.max(min, Math.min(value, max)),
          Math.max(Math.min(value, max), min),
          Doubles.constrainToRange(value, min, max));
    }

    @AfterTemplate
    double after(double value, double min, double max) {
      return Math.clamp(value, min, max);
    }
  }

  /** Prefer {@link Math#toIntExact(long)} over non-JDK alternatives. */
  // XXX: This rule changes the exception possibly thrown from `IllegalArgumentException` to
  // `ArithmeticException`.
  static final class MathToIntExact {
    @BeforeTemplate
    int before(long value) {
      return Ints.checkedCast(value);
    }

    @AfterTemplate
    int after(long value) {
      return Math.toIntExact(value);
    }
  }

  /** Prefer {@link Character#BYTES} over non-JDK alternatives. */
  static final class CharacterBytes {
    @BeforeTemplate
    int before() {
      return Chars.BYTES;
    }

    @AfterTemplate
    int after() {
      return Character.BYTES;
    }
  }

  /** Prefer {@link Short#BYTES} over non-JDK alternatives. */
  static final class ShortBytes {
    @BeforeTemplate
    int before() {
      return Shorts.BYTES;
    }

    @AfterTemplate
    int after() {
      return Short.BYTES;
    }
  }

  /** Prefer {@link Integer#BYTES} over non-JDK alternatives. */
  static final class IntegerBytes {
    @BeforeTemplate
    int before() {
      return Ints.BYTES;
    }

    @AfterTemplate
    int after() {
      return Integer.BYTES;
    }
  }

  /** Prefer {@link Long#BYTES} over non-JDK alternatives. */
  static final class LongBytes {
    @BeforeTemplate
    int before() {
      return Longs.BYTES;
    }

    @AfterTemplate
    int after() {
      return Long.BYTES;
    }
  }

  /** Prefer {@link Float#BYTES} over non-JDK alternatives. */
  static final class FloatBytes {
    @BeforeTemplate
    int before() {
      return Floats.BYTES;
    }

    @AfterTemplate
    int after() {
      return Float.BYTES;
    }
  }

  /** Prefer {@link Double#BYTES} over non-JDK alternatives. */
  static final class DoubleBytes {
    @BeforeTemplate
    int before() {
      return Doubles.BYTES;
    }

    @AfterTemplate
    int after() {
      return Double.BYTES;
    }
  }

  /** Prefer {@code Integer.signum(i) > 0} over less idiomatic alternatives. */
  static final class IntegerSignumIsPositive {
    @BeforeTemplate
    boolean before(int i) {
      return Refaster.anyOf(Integer.signum(i) == 1, Integer.signum(i) >= 1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(int i) {
      return Integer.signum(i) > 0;
    }
  }

  /** Prefer {@code Integer.signum(i) < 0} over less idiomatic alternatives. */
  static final class IntegerSignumIsNegative {
    @BeforeTemplate
    boolean before(int i) {
      return Refaster.anyOf(Integer.signum(i) == -1, Integer.signum(i) <= -1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(int i) {
      return Integer.signum(i) < 0;
    }
  }

  /** Prefer {@code Long.signum(i) > 0} over less idiomatic alternatives. */
  static final class LongSignumIsPositive {
    @BeforeTemplate
    boolean before(long i) {
      return Refaster.anyOf(Long.signum(i) == 1, Long.signum(i) >= 1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(long i) {
      return Long.signum(i) > 0;
    }
  }

  /** Prefer {@code Long.signum(i) < 0} over less idiomatic alternatives. */
  static final class LongSignumIsNegative {
    @BeforeTemplate
    boolean before(long i) {
      return Refaster.anyOf(Long.signum(i) == -1, Long.signum(i) <= -1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(long i) {
      return Long.signum(i) < 0;
    }
  }

  /** Prefer {@link Integer#compareUnsigned(int, int)} over non-JDK alternatives. */
  static final class IntegerCompareUnsigned {
    @BeforeTemplate
    int before(int x, int y) {
      return UnsignedInts.compare(x, y);
    }

    @AfterTemplate
    int after(int x, int y) {
      return Integer.compareUnsigned(x, y);
    }
  }

  /** Prefer {@link Long#compareUnsigned(long, long)} over non-JDK alternatives. */
  static final class LongCompareUnsigned {
    @BeforeTemplate
    int before(long x, long y) {
      return UnsignedLongs.compare(x, y);
    }

    @AfterTemplate
    int after(long x, long y) {
      return Long.compareUnsigned(x, y);
    }
  }

  /** Prefer {@link Integer#divideUnsigned(int, int)} over non-JDK alternatives. */
  static final class IntegerDivideUnsigned {
    @BeforeTemplate
    int before(int dividend, int divisor) {
      return UnsignedInts.divide(dividend, divisor);
    }

    @AfterTemplate
    int after(int dividend, int divisor) {
      return Integer.divideUnsigned(dividend, divisor);
    }
  }

  /** Prefer {@link Long#divideUnsigned(long, long)} over non-JDK alternatives. */
  static final class LongDivideUnsigned {
    @BeforeTemplate
    long before(long dividend, long divisor) {
      return UnsignedLongs.divide(dividend, divisor);
    }

    @AfterTemplate
    long after(long dividend, long divisor) {
      return Long.divideUnsigned(dividend, divisor);
    }
  }

  /** Prefer {@link Integer#remainderUnsigned(int, int)} over non-JDK alternatives. */
  static final class IntegerRemainderUnsigned {
    @BeforeTemplate
    int before(int dividend, int divisor) {
      return UnsignedInts.remainder(dividend, divisor);
    }

    @AfterTemplate
    int after(int dividend, int divisor) {
      return Integer.remainderUnsigned(dividend, divisor);
    }
  }

  /** Prefer {@link Long#remainderUnsigned(long, long)} over non-JDK alternatives. */
  static final class LongRemainderUnsigned {
    @BeforeTemplate
    long before(long dividend, long divisor) {
      return UnsignedLongs.remainder(dividend, divisor);
    }

    @AfterTemplate
    long after(long dividend, long divisor) {
      return Long.remainderUnsigned(dividend, divisor);
    }
  }

  /** Prefer {@link Integer#parseUnsignedInt(String)} over non-JDK or more verbose alternatives. */
  static final class IntegerParseUnsignedInt {
    @BeforeTemplate
    int before(String s) {
      return Refaster.anyOf(UnsignedInts.parseUnsignedInt(s), Integer.parseUnsignedInt(s, 10));
    }

    @AfterTemplate
    int after(String s) {
      return Integer.parseUnsignedInt(s);
    }
  }

  /** Prefer {@link Long#parseUnsignedLong(String)} over non-JDK or more verbose alternatives. */
  static final class LongParseUnsignedLong {
    @BeforeTemplate
    long before(String s) {
      return Refaster.anyOf(UnsignedLongs.parseUnsignedLong(s), Long.parseUnsignedLong(s, 10));
    }

    @AfterTemplate
    long after(String s) {
      return Long.parseUnsignedLong(s);
    }
  }

  /** Prefer {@link Integer#parseUnsignedInt(String, int)} over non-JDK alternatives. */
  static final class IntegerParseUnsignedIntWithInt {
    @BeforeTemplate
    int before(String s, int radix) {
      return UnsignedInts.parseUnsignedInt(s, radix);
    }

    @AfterTemplate
    int after(String s, int radix) {
      return Integer.parseUnsignedInt(s, radix);
    }
  }

  /** Prefer {@link Long#parseUnsignedLong(String, int)} over non-JDK alternatives. */
  static final class LongParseUnsignedLongWithInt {
    @BeforeTemplate
    long before(String s, int radix) {
      return UnsignedLongs.parseUnsignedLong(s, radix);
    }

    @AfterTemplate
    long after(String s, int radix) {
      return Long.parseUnsignedLong(s, radix);
    }
  }

  /** Prefer {@link Integer#toUnsignedString(int)} over non-JDK or more verbose alternatives. */
  static final class IntegerToUnsignedString {
    @BeforeTemplate
    String before(int i) {
      return Refaster.anyOf(UnsignedInts.toString(i), Integer.toUnsignedString(i, 10));
    }

    @AfterTemplate
    String after(int i) {
      return Integer.toUnsignedString(i);
    }
  }

  /** Prefer {@link Long#toUnsignedString(long)} over non-JDK or more verbose alternatives. */
  static final class LongToUnsignedString {
    @BeforeTemplate
    String before(long i) {
      return Refaster.anyOf(UnsignedLongs.toString(i), Long.toUnsignedString(i, 10));
    }

    @AfterTemplate
    String after(long i) {
      return Long.toUnsignedString(i);
    }
  }

  /** Prefer {@link Integer#toUnsignedString(int, int)} over non-JDK alternatives. */
  static final class IntegerToUnsignedStringWithInt {
    @BeforeTemplate
    String before(int i, int radix) {
      return UnsignedInts.toString(i, radix);
    }

    @AfterTemplate
    String after(int i, int radix) {
      return Integer.toUnsignedString(i, radix);
    }
  }

  /** Prefer {@link Long#toUnsignedString(long, int)} over non-JDK alternatives. */
  static final class LongToUnsignedStringWithInt {
    @BeforeTemplate
    String before(long i, int radix) {
      return UnsignedLongs.toString(i, radix);
    }

    @AfterTemplate
    String after(long i, int radix) {
      return Long.toUnsignedString(i, radix);
    }
  }

  /** Prefer {@link Arrays#compareUnsigned(byte[], byte[])} over non-JDK alternatives. */
  // XXX: This rule will yield non-compilable code if the result of the replaced expression is
  // dereferenced. Investigate how to make this safe.
  static final class ArraysCompareUnsignedByte {
    @BeforeTemplate
    Comparator<byte[]> before() {
      return UnsignedBytes.lexicographicalComparator();
    }

    @AfterTemplate
    Comparator<byte[]> after() {
      return Arrays::compareUnsigned;
    }
  }

  /** Prefer {@link Arrays#compareUnsigned(int[], int[])} over non-JDK alternatives. */
  // XXX: This rule will yield non-compilable code if the result of the replaced expression is
  // dereferenced. Investigate how to make this safe.
  static final class ArraysCompareUnsignedInt {
    @BeforeTemplate
    Comparator<int[]> before() {
      return UnsignedInts.lexicographicalComparator();
    }

    @AfterTemplate
    Comparator<int[]> after() {
      return Arrays::compareUnsigned;
    }
  }

  /** Prefer {@link Arrays#compareUnsigned(long[], long[])} over non-JDK alternatives. */
  // XXX: This rule will yield non-compilable code if the result of the replaced expression is
  // dereferenced. Investigate how to make this safe.
  static final class ArraysCompareUnsignedLong {
    @BeforeTemplate
    Comparator<long[]> before() {
      return UnsignedLongs.lexicographicalComparator();
    }

    @AfterTemplate
    Comparator<long[]> after() {
      return Arrays::compareUnsigned;
    }
  }
}
