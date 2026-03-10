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
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert, int n) {
      return Refaster.anyOf(
          intAssert.isCloseTo(n, offset(0)), intAssert.isCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert, int n) {
      return intAssert.isEqualTo(n);
    }
  }

  /** Prefer {@link AbstractIntegerAssert#isNotEqualTo(int)} over more contrived alternatives. */
  static final class AbstractIntegerAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(AbstractIntegerAssert<?> intAssert, int n) {
      return Refaster.anyOf(
          intAssert.isNotCloseTo(n, offset(0)), intAssert.isNotCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractIntegerAssert<?> after(AbstractIntegerAssert<?> intAssert, int n) {
      return intAssert.isNotEqualTo(n);
    }
  }

  /** Prefer {@link AbstractIntegerAssert#isEqualTo(int)} over less explicit alternatives. */
  static final class AbstractIntegerAssertIsZero {
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
  static final class AbstractIntegerAssertIsNotZero {
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
  static final class AbstractIntegerAssertIsOne {
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
