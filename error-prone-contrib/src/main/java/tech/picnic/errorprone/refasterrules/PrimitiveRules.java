package tech.picnic.errorprone.refasterrules;

import com.google.common.primitives.Booleans;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Chars;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import com.google.errorprone.refaster.annotation.AfterTemplate;
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

  /** Prefer {@link Boolean#compare(boolean, boolean)} over the Guava alternative. */
  static final class BooleanCompare {
    @BeforeTemplate
    int before(boolean a, boolean b) {
      return Booleans.compare(a, b);
    }

    @AfterTemplate
    int after(boolean a, boolean b) {
      return Boolean.compare(a, b);
    }
  }

  /** Prefer {@link Character#compare(char, char)} over the Guava alternative. */
  static final class CharacterCompare {
    @BeforeTemplate
    int before(char a, char b) {
      return Chars.compare(a, b);
    }

    @AfterTemplate
    int after(char a, char b) {
      return Character.compare(a, b);
    }
  }

  /** Prefer {@link Short#compare(short, short)} over the Guava alternative. */
  static final class ShortCompare {
    @BeforeTemplate
    int before(short a, short b) {
      return Shorts.compare(a, b);
    }

    @AfterTemplate
    int after(short a, short b) {
      return Short.compare(a, b);
    }
  }

  /** Prefer {@link Integer#compare(int, int)} over the Guava alternative. */
  static final class IntegerCompare {
    @BeforeTemplate
    int before(int a, int b) {
      return Ints.compare(a, b);
    }

    @AfterTemplate
    int after(int a, int b) {
      return Integer.compare(a, b);
    }
  }

  /** Prefer {@link Long#compare(long, long)} over the Guava alternative. */
  static final class LongCompare {
    @BeforeTemplate
    int before(long a, long b) {
      return Longs.compare(a, b);
    }

    @AfterTemplate
    int after(long a, long b) {
      return Long.compare(a, b);
    }
  }

  /** Prefer {@link Float#compare(float, float)} over the Guava alternative. */
  static final class FloatCompare {
    @BeforeTemplate
    int before(float a, float b) {
      return Floats.compare(a, b);
    }

    @AfterTemplate
    int after(float a, float b) {
      return Float.compare(a, b);
    }
  }

  /** Prefer {@link Double#compare(double, double)} over the Guava alternative. */
  static final class DoubleCompare {
    @BeforeTemplate
    int before(double a, double b) {
      return Doubles.compare(a, b);
    }

    @AfterTemplate
    int after(double a, double b) {
      return Double.compare(a, b);
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
}
