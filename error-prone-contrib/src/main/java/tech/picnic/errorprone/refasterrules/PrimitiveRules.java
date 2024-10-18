package tech.picnic.errorprone.refasterrules;

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
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to expressions dealing with primitives. */
@OnlineDocumentation
final class PrimitiveRules {
  private PrimitiveRules() {}

  /** Avoid contrived ways of expressing the "less than" relationship. */
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

  /** Avoid contrived ways of expressing the "less than or equal to" relationship. */
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

  /** Avoid contrived ways of expressing the "greater than" relationship. */
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

  /** Avoid contrived ways of expressing the "greater than or equal to" relationship. */
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

  /** Prefer {@link Math#toIntExact(long)} over the Guava alternative. */
  // XXX: This rule changes the exception possibly thrown from `IllegalArgumentException` to
  // `ArithmeticException`.
  static final class LongToIntExact {
    @BeforeTemplate
    int before(long l) {
      return Ints.checkedCast(l);
    }

    @AfterTemplate
    int after(long l) {
      return Math.toIntExact(l);
    }
  }

  /** Prefer {@link Boolean#hashCode(boolean)} over the Guava alternative. */
  static final class BooleanHashCode {
    @BeforeTemplate
    int before(boolean b) {
      return Booleans.hashCode(b);
    }

    @AfterTemplate
    int after(boolean b) {
      return Boolean.hashCode(b);
    }
  }

  /** Prefer {@link Byte#hashCode(byte)} over the Guava alternative. */
  static final class ByteHashCode {
    @BeforeTemplate
    int before(byte b) {
      return Bytes.hashCode(b);
    }

    @AfterTemplate
    int after(byte b) {
      return Byte.hashCode(b);
    }
  }

  /** Prefer {@link Character#hashCode(char)} over the Guava alternative. */
  static final class CharacterHashCode {
    @BeforeTemplate
    int before(char c) {
      return Chars.hashCode(c);
    }

    @AfterTemplate
    int after(char c) {
      return Character.hashCode(c);
    }
  }

  /** Prefer {@link Short#hashCode(short)} over the Guava alternative. */
  static final class ShortHashCode {
    @BeforeTemplate
    int before(short s) {
      return Shorts.hashCode(s);
    }

    @AfterTemplate
    int after(short s) {
      return Short.hashCode(s);
    }
  }

  /** Prefer {@link Integer#hashCode(int)} over the Guava alternative. */
  static final class IntegerHashCode {
    @BeforeTemplate
    int before(int i) {
      return Ints.hashCode(i);
    }

    @AfterTemplate
    int after(int i) {
      return Integer.hashCode(i);
    }
  }

  /** Prefer {@link Long#hashCode(long)} over the Guava alternative. */
  static final class LongHashCode {
    @BeforeTemplate
    int before(long l) {
      return Longs.hashCode(l);
    }

    @AfterTemplate
    int after(long l) {
      return Long.hashCode(l);
    }
  }

  /** Prefer {@link Float#hashCode(float)} over the Guava alternative. */
  static final class FloatHashCode {
    @BeforeTemplate
    int before(float f) {
      return Floats.hashCode(f);
    }

    @AfterTemplate
    int after(float f) {
      return Float.hashCode(f);
    }
  }

  /** Prefer {@link Double#hashCode(double)} over the Guava alternative. */
  static final class DoubleHashCode {
    @BeforeTemplate
    int before(double d) {
      return Doubles.hashCode(d);
    }

    @AfterTemplate
    int after(double d) {
      return Double.hashCode(d);
    }
  }

  /** Prefer {@link Character#BYTES} over the Guava alternative. */
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

  /** Prefer {@link Short#BYTES} over the Guava alternative. */
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

  /** Prefer {@link Integer#BYTES} over the Guava alternative. */
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

  /** Prefer {@link Long#BYTES} over the Guava alternative. */
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

  /** Prefer {@link Float#BYTES} over the Guava alternative. */
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

  /** Prefer {@link Double#BYTES} over the Guava alternative. */
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

  /** Prefer {@link Float#isFinite(float)} over the Guava alternative. */
  static final class FloatIsFinite {
    @BeforeTemplate
    boolean before(float f) {
      return Floats.isFinite(f);
    }

    @AfterTemplate
    boolean after(float f) {
      return Float.isFinite(f);
    }
  }

  /** Prefer {@link Double#isFinite(double)} over the Guava alternative. */
  static final class DoubleIsFinite {
    @BeforeTemplate
    boolean before(double d) {
      return Doubles.isFinite(d);
    }

    @AfterTemplate
    boolean after(double d) {
      return Double.isFinite(d);
    }
  }

  /** Prefer an {@link Integer#signum(int)} comparison to 1 over less clear alternatives. */
  static final class IntegerSignumIsPositive {
    @BeforeTemplate
    boolean before(int i) {
      return Refaster.anyOf(Integer.signum(i) > 0, Integer.signum(i) >= 1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(int i) {
      return Integer.signum(i) == 1;
    }
  }

  /** Prefer an {@link Integer#signum(int)} comparison to -1 over less clear alternatives. */
  static final class IntegerSignumIsNegative {
    @BeforeTemplate
    boolean before(int i) {
      return Refaster.anyOf(Integer.signum(i) < 0, Integer.signum(i) <= -1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(int i) {
      return Integer.signum(i) == -1;
    }
  }

  /** Prefer an {@link Long#signum(long)} comparison to 1 over less clear alternatives. */
  static final class LongSignumIsPositive {
    @BeforeTemplate
    boolean before(long l) {
      return Refaster.anyOf(Long.signum(l) > 0, Long.signum(l) >= 1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(long l) {
      return Long.signum(l) == 1;
    }
  }

  /** Prefer an {@link Long#signum(long)} comparison to -1 over less clear alternatives. */
  static final class LongSignumIsNegative {
    @BeforeTemplate
    boolean before(long l) {
      return Refaster.anyOf(Long.signum(l) < 0, Long.signum(l) <= -1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(long l) {
      return Long.signum(l) == -1;
    }
  }

  /** Prefer JDK's {@link Integer#compareUnsigned(int, int)} over third-party alternatives. */
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

  /** Prefer JDK's {@link Long#compareUnsigned(long, long)} over third-party alternatives. */
  static final class LongCompareUnsigned {
    @BeforeTemplate
    long before(long x, long y) {
      return UnsignedLongs.compare(x, y);
    }

    @AfterTemplate
    long after(long x, long y) {
      return Long.compareUnsigned(x, y);
    }
  }

  /** Prefer JDK's {@link Integer#divideUnsigned(int, int)} over third-party alternatives. */
  static final class IntegerDivideUnsigned {
    @BeforeTemplate
    int before(int x, int y) {
      return UnsignedInts.divide(x, y);
    }

    @AfterTemplate
    int after(int x, int y) {
      return Integer.divideUnsigned(x, y);
    }
  }

  /** Prefer JDK's {@link Long#divideUnsigned(long, long)} over third-party alternatives. */
  static final class LongDivideUnsigned {
    @BeforeTemplate
    long before(long x, long y) {
      return UnsignedLongs.divide(x, y);
    }

    @AfterTemplate
    long after(long x, long y) {
      return Long.divideUnsigned(x, y);
    }
  }

  /** Prefer JDK's {@link Integer#remainderUnsigned(int, int)} over third-party alternatives. */
  static final class IntegerRemainderUnsigned {
    @BeforeTemplate
    int before(int x, int y) {
      return UnsignedInts.remainder(x, y);
    }

    @AfterTemplate
    int after(int x, int y) {
      return Integer.remainderUnsigned(x, y);
    }
  }

  /** Prefer JDK's {@link Long#remainderUnsigned(long, long)} over third-party alternatives. */
  static final class LongRemainderUnsigned {
    @BeforeTemplate
    long before(long x, long y) {
      return UnsignedLongs.remainder(x, y);
    }

    @AfterTemplate
    long after(long x, long y) {
      return Long.remainderUnsigned(x, y);
    }
  }

  /**
   * Prefer JDK's {@link Integer#parseUnsignedInt(String)} over third-party or more verbose
   * alternatives.
   */
  static final class IntegerParseUnsignedInt {
    @BeforeTemplate
    int before(String string) {
      return Refaster.anyOf(
          UnsignedInts.parseUnsignedInt(string), Integer.parseUnsignedInt(string, 10));
    }

    @AfterTemplate
    int after(String string) {
      return Integer.parseUnsignedInt(string);
    }
  }

  /**
   * Prefer JDK's {@link Long#parseUnsignedLong(String)} over third-party or more verbose
   * alternatives.
   */
  static final class LongParseUnsignedLong {
    @BeforeTemplate
    long before(String string) {
      return Refaster.anyOf(
          UnsignedLongs.parseUnsignedLong(string), Long.parseUnsignedLong(string, 10));
    }

    @AfterTemplate
    long after(String string) {
      return Long.parseUnsignedLong(string);
    }
  }

  /** Prefer JDK's {@link Integer#parseUnsignedInt(String, int)} over third-party alternatives. */
  static final class IntegerParseUnsignedIntWithRadix {
    @BeforeTemplate
    int before(String string, int radix) {
      return UnsignedInts.parseUnsignedInt(string, radix);
    }

    @AfterTemplate
    int after(String string, int radix) {
      return Integer.parseUnsignedInt(string, radix);
    }
  }

  /** Prefer JDK's {@link Long#parseUnsignedLong(String, int)} over third-party alternatives. */
  static final class LongParseUnsignedLongWithRadix {
    @BeforeTemplate
    long before(String string, int radix) {
      return UnsignedLongs.parseUnsignedLong(string, radix);
    }

    @AfterTemplate
    long after(String string, int radix) {
      return Long.parseUnsignedLong(string, radix);
    }
  }

  /**
   * Prefer JDK's {@link Integer#toUnsignedString(int)} over third-party or more verbose
   * alternatives.
   */
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

  /**
   * Prefer JDK's {@link Long#toUnsignedString(long)} over third-party or more verbose alternatives.
   */
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

  /**
   * Prefer JDK's {@link Integer#toUnsignedString(int,int)} over third-party or more verbose
   * alternatives.
   */
  static final class IntegerToUnsignedStringWithRadix {
    @BeforeTemplate
    String before(int i, int radix) {
      return UnsignedInts.toString(i, radix);
    }

    @AfterTemplate
    String after(int i, int radix) {
      return Integer.toUnsignedString(i, radix);
    }
  }

  /**
   * Prefer JDK's {@link Long#toUnsignedString(long,int)} over third-party or more verbose
   * alternatives.
   */
  static final class LongToUnsignedStringWithRadix {
    @BeforeTemplate
    String before(long i, int radix) {
      return UnsignedLongs.toString(i, radix);
    }

    @AfterTemplate
    String after(long i, int radix) {
      return Long.toUnsignedString(i, radix);
    }
  }
}
