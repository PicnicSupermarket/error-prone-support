package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.assertj.core.api.AbstractByteAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to AssertJ assertions over {@code byte}s. */
@OnlineDocumentation
final class AssertJByteRules {
  private AssertJByteRules() {}

  /** Prefer {@link AbstractByteAssert#isEqualTo(byte)} over more contrived alternatives. */
  static final class AbstractByteAssertIsEqualTo {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> byteAssert, byte expected) {
      return Refaster.anyOf(
          byteAssert.isCloseTo(expected, offset((byte) 0)),
          byteAssert.isCloseTo(expected, withPercentage(0)));
    }

    @AfterTemplate
    AbstractByteAssert<?> after(AbstractByteAssert<?> byteAssert, byte expected) {
      return byteAssert.isEqualTo(expected);
    }
  }

  /** Prefer {@link AbstractByteAssert#isNotEqualTo(byte)} over more contrived alternatives. */
  static final class AbstractByteAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> byteAssert, byte other) {
      return Refaster.anyOf(
          byteAssert.isNotCloseTo(other, offset((byte) 0)),
          byteAssert.isNotCloseTo(other, withPercentage(0)));
    }

    @AfterTemplate
    AbstractByteAssert<?> after(AbstractByteAssert<?> byteAssert, byte other) {
      return byteAssert.isNotEqualTo(other);
    }
  }

  /** Prefer {@link AbstractByteAssert#isEqualTo(byte)} over less explicit alternatives. */
  static final class AbstractByteAssertIsEqualToZero {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> byteAssert) {
      return byteAssert.isZero();
    }

    @AfterTemplate
    AbstractByteAssert<?> after(AbstractByteAssert<?> byteAssert) {
      return byteAssert.isEqualTo((byte) 0);
    }
  }

  /** Prefer {@link AbstractByteAssert#isNotEqualTo(byte)} over less explicit alternatives. */
  static final class AbstractByteAssertIsNotEqualToZero {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> byteAssert) {
      return byteAssert.isNotZero();
    }

    @AfterTemplate
    AbstractByteAssert<?> after(AbstractByteAssert<?> byteAssert) {
      return byteAssert.isNotEqualTo((byte) 0);
    }
  }

  /** Prefer {@link AbstractByteAssert#isEqualTo(byte)} over less explicit alternatives. */
  static final class AbstractByteAssertIsEqualToOne {
    @BeforeTemplate
    AbstractByteAssert<?> before(AbstractByteAssert<?> byteAssert) {
      return byteAssert.isOne();
    }

    @AfterTemplate
    AbstractByteAssert<?> after(AbstractByteAssert<?> byteAssert) {
      return byteAssert.isEqualTo((byte) 1);
    }
  }
}
