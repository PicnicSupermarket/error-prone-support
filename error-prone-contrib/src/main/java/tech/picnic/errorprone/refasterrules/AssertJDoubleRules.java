package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.data.Offset;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/** Refaster rules related to AssertJ assertions over {@code double}s. */
@OnlineDocumentation
final class AssertJDoubleRules {
  private AssertJDoubleRules() {}

  /**
   * Prefer {@link AbstractDoubleAssert#isCloseTo(double, Offset)} over less explicit alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AbstractDoubleAssertIsCloseTo {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(
        AbstractDoubleAssert<?> doubleAssert, double expected, Offset<Double> offset) {
      return doubleAssert.isEqualTo(expected, offset);
    }

    @BeforeTemplate
    AbstractDoubleAssert<?> before(
        AbstractDoubleAssert<?> doubleAssert, Double expected, Offset<Double> offset) {
      return doubleAssert.isEqualTo(expected, offset);
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(
        AbstractDoubleAssert<?> doubleAssert, double expected, Offset<Double> offset) {
      return doubleAssert.isCloseTo(expected, offset);
    }
  }

  /** Prefer {@link AbstractDoubleAssert#isEqualTo(double)} over more contrived alternatives. */
  static final class AbstractDoubleAssertIsEqualTo {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> doubleAssert, double expected) {
      return Refaster.anyOf(
          doubleAssert.isCloseTo(expected, offset(0.0)),
          doubleAssert.isCloseTo(expected, withPercentage(0.0)));
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert, double expected) {
      return doubleAssert.isEqualTo(expected);
    }
  }

  /** Prefer {@link AbstractDoubleAssert#isNotEqualTo(double)} over more contrived alternatives. */
  static final class AbstractDoubleAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> doubleAssert, double other) {
      return Refaster.anyOf(
          doubleAssert.isNotCloseTo(other, offset(0.0)),
          doubleAssert.isNotCloseTo(other, withPercentage(0.0)));
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert, double other) {
      return doubleAssert.isNotEqualTo(other);
    }
  }

  /** Prefer {@link AbstractDoubleAssert#isEqualTo(double)} over less explicit alternatives. */
  static final class AbstractDoubleAssertIsEqualToZero {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> doubleAssert) {
      return doubleAssert.isZero();
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert) {
      return doubleAssert.isEqualTo(0);
    }
  }

  /** Prefer {@link AbstractDoubleAssert#isNotEqualTo(double)} over less explicit alternatives. */
  static final class AbstractDoubleAssertIsNotEqualToZero {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> doubleAssert) {
      return doubleAssert.isNotZero();
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert) {
      return doubleAssert.isNotEqualTo(0);
    }
  }

  /** Prefer {@link AbstractDoubleAssert#isEqualTo(double)} over less explicit alternatives. */
  static final class AbstractDoubleAssertIsEqualToOne {
    @BeforeTemplate
    AbstractDoubleAssert<?> before(AbstractDoubleAssert<?> doubleAssert) {
      return doubleAssert.isOne();
    }

    @AfterTemplate
    AbstractDoubleAssert<?> after(AbstractDoubleAssert<?> doubleAssert) {
      return doubleAssert.isEqualTo(1);
    }
  }
}
