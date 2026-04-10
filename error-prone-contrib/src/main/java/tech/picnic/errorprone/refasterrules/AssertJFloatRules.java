package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.assertj.core.api.AbstractFloatAssert;
import org.assertj.core.data.Offset;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to AssertJ assertions over {@code float}s. */
@OnlineDocumentation
final class AssertJFloatRules {
  private AssertJFloatRules() {}

  /**
   * Prefer {@link AbstractFloatAssert#isCloseTo(float, Offset)} over less explicit alternatives.
   */
  static final class AbstractFloatAssertIsCloseTo {
    @BeforeTemplate
    AbstractFloatAssert<?> before(
        AbstractFloatAssert<?> floatAssert, float expected, Offset<Float> offset) {
      return floatAssert.isEqualTo(expected, offset);
    }

    @BeforeTemplate
    AbstractFloatAssert<?> before(
        AbstractFloatAssert<?> floatAssert, Float expected, Offset<Float> offset) {
      return floatAssert.isEqualTo(expected, offset);
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(
        AbstractFloatAssert<?> floatAssert, float expected, Offset<Float> offset) {
      return floatAssert.isCloseTo(expected, offset);
    }
  }

  /** Prefer {@link AbstractFloatAssert#isEqualTo(float)} over more contrived alternatives. */
  static final class AbstractFloatAssertIsEqualTo {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert, float expected) {
      return Refaster.anyOf(
          floatAssert.isCloseTo(expected, offset(0f)),
          floatAssert.isCloseTo(expected, withPercentage(0)));
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert, float expected) {
      return floatAssert.isEqualTo(expected);
    }
  }

  /** Prefer {@link AbstractFloatAssert#isNotEqualTo(float)} over more contrived alternatives. */
  static final class AbstractFloatAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert, float other) {
      return Refaster.anyOf(
          floatAssert.isNotCloseTo(other, offset(0f)),
          floatAssert.isNotCloseTo(other, withPercentage(0)));
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert, float other) {
      return floatAssert.isNotEqualTo(other);
    }
  }

  /** Prefer {@code isEqualTo(0)} over more contrived alternatives. */
  static final class AbstractFloatAssertIsEqualToZero {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isZero();
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isEqualTo(0);
    }
  }

  /** Prefer {@code isNotEqualTo(0)} over more contrived alternatives. */
  static final class AbstractFloatAssertIsNotEqualToZero {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isNotZero();
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isNotEqualTo(0);
    }
  }

  /** Prefer {@code isEqualTo(1)} over more contrived alternatives. */
  static final class AbstractFloatAssertIsEqualToOne {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isOne();
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isEqualTo(1);
    }
  }
}
