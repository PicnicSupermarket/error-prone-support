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
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert, int expected) {
      return Refaster.anyOf(
          intAssert.isCloseTo(expected, offset(0)),
          intAssert.isCloseTo(expected, withPercentage(0)));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert, int expected) {
      return intAssert.isEqualTo(expected);
    }
  }

  /** Prefer {@link AbstractIntegerAssert#isNotEqualTo(int)} over more contrived alternatives. */
  static final class AbstractIntegerAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert, int other) {
      return Refaster.anyOf(
          intAssert.isNotCloseTo(other, offset(0)),
          intAssert.isNotCloseTo(other, withPercentage(0)));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert, int other) {
      return intAssert.isNotEqualTo(other);
    }
  }

  /** Prefer {@link AbstractIntegerAssert#isEqualTo(int)} over less explicit alternatives. */
  static final class AbstractIntegerAssertIsEqualToZero {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isZero();
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isEqualTo(0);
    }
  }

  /** Prefer {@link AbstractIntegerAssert#isNotEqualTo(int)} over less explicit alternatives. */
  static final class AbstractIntegerAssertIsNotEqualToZero {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNotZero();
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isNotEqualTo(0);
    }
  }

  /** Prefer {@link AbstractIntegerAssert#isEqualTo(int)} over less explicit alternatives. */
  static final class AbstractIntegerAssertIsEqualToOne {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isOne();
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert) {
      return intAssert.isEqualTo(1);
    }
  }
}
