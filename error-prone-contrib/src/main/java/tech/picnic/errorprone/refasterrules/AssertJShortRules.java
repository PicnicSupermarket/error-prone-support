package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.assertj.core.api.AbstractShortAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to AssertJ assertions over {@code short}s. */
@OnlineDocumentation
final class AssertJShortRules {
  private AssertJShortRules() {}

  /** Prefer {@link AbstractShortAssert#isEqualTo(short)} over less explicit alternatives. */
  static final class AbstractShortAssertIsEqualTo {
    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> shortAssert, short expected) {
      return Refaster.anyOf(
          shortAssert.isCloseTo(expected, offset((short) 0)),
          shortAssert.isCloseTo(expected, withPercentage(0)));
    }

    @AfterTemplate
    AbstractShortAssert<?> after(AbstractShortAssert<?> shortAssert, short expected) {
      return shortAssert.isEqualTo(expected);
    }
  }

  /** Prefer {@link AbstractShortAssert#isNotEqualTo(short)} over less explicit alternatives. */
  static final class AbstractShortAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> shortAssert, short other) {
      return Refaster.anyOf(
          shortAssert.isNotCloseTo(other, offset((short) 0)),
          shortAssert.isNotCloseTo(other, withPercentage(0)));
    }

    @AfterTemplate
    AbstractShortAssert<?> after(AbstractShortAssert<?> shortAssert, short other) {
      return shortAssert.isNotEqualTo(other);
    }
  }

  /** Prefer {@link AbstractShortAssert#isEqualTo(short)} over less explicit alternatives. */
  static final class AbstractShortAssertIsEqualToZero {
    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> shortAssert) {
      return shortAssert.isZero();
    }

    @AfterTemplate
    AbstractShortAssert<?> after(AbstractShortAssert<?> shortAssert) {
      return shortAssert.isEqualTo((short) 0);
    }
  }

  /** Prefer {@link AbstractShortAssert#isNotEqualTo(short)} over less explicit alternatives. */
  static final class AbstractShortAssertIsNotEqualToZero {
    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> shortAssert) {
      return shortAssert.isNotZero();
    }

    @AfterTemplate
    AbstractShortAssert<?> after(AbstractShortAssert<?> shortAssert) {
      return shortAssert.isNotEqualTo((short) 0);
    }
  }

  /** Prefer {@link AbstractShortAssert#isEqualTo(short)} over less explicit alternatives. */
  static final class AbstractShortAssertIsEqualToOne {
    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> shortAssert) {
      return shortAssert.isOne();
    }

    @AfterTemplate
    AbstractShortAssert<?> after(AbstractShortAssert<?> shortAssert) {
      return shortAssert.isEqualTo((short) 1);
    }
  }
}
