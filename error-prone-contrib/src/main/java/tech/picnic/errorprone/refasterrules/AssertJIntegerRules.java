package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.assertj.core.api.AbstractIntegerAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to AssertJ assertions over {@code int}s. */
@OnlineDocumentation
final class AssertJIntegerRules {
  private AssertJIntegerRules() {}

  /** Prefer {@link AbstractIntegerAssert#isEqualTo(int)} over more contrived alternatives. */
  static final class AbstractIntegerAssertIsEqualTo {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> integerAssert, int expected) {
      return Refaster.anyOf(
          integerAssert.isCloseTo(expected, offset(0)),
          integerAssert.isCloseTo(expected, withPercentage(0)));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> integerAssert, int expected) {
      return integerAssert.isEqualTo(expected);
    }
  }

  /** Prefer {@link AbstractIntegerAssert#isNotEqualTo(int)} over more contrived alternatives. */
  static final class AbstractIntegerAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> integerAssert, int other) {
      return Refaster.anyOf(
          integerAssert.isNotCloseTo(other, offset(0)),
          integerAssert.isNotCloseTo(other, withPercentage(0)));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> integerAssert, int other) {
      return integerAssert.isNotEqualTo(other);
    }
  }

  /** Prefer {@link AbstractIntegerAssert#isEqualTo(int)} over less explicit alternatives. */
  static final class AbstractIntegerAssertIsEqualToZero {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> integerAssert) {
      return integerAssert.isZero();
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> integerAssert) {
      return integerAssert.isEqualTo(0);
    }
  }

  /** Prefer {@link AbstractIntegerAssert#isNotEqualTo(int)} over less explicit alternatives. */
  static final class AbstractIntegerAssertIsNotEqualToZero {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> integerAssert) {
      return integerAssert.isNotZero();
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> integerAssert) {
      return integerAssert.isNotEqualTo(0);
    }
  }

  /** Prefer {@link AbstractIntegerAssert#isEqualTo(int)} over less explicit alternatives. */
  static final class AbstractIntegerAssertIsEqualToOne {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> integerAssert) {
      return integerAssert.isOne();
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> integerAssert) {
      return integerAssert.isEqualTo(1);
    }
  }
}
