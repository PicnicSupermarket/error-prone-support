package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.assertj.core.api.AbstractLongAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to AssertJ assertions over {@code long}s. */
@OnlineDocumentation
final class AssertJLongRules {
  private AssertJLongRules() {}

  /** Prefer {@link AbstractLongAssert#isEqualTo(long)} over more contrived alternatives. */
  static final class AbstractLongAssertIsEqualTo {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert, long expected) {
      return Refaster.anyOf(
          longAssert.isCloseTo(expected, offset(0L)),
          longAssert.isCloseTo(expected, withPercentage(0)));
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert, long expected) {
      return longAssert.isEqualTo(expected);
    }
  }

  /** Prefer {@link AbstractLongAssert#isNotEqualTo(long)} over more contrived alternatives. */
  static final class AbstractLongAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert, long other) {
      return Refaster.anyOf(
          longAssert.isNotCloseTo(other, offset(0L)),
          longAssert.isNotCloseTo(other, withPercentage(0)));
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert, long other) {
      return longAssert.isNotEqualTo(other);
    }
  }

  /** Prefer {@link AbstractLongAssert#isEqualTo(long)} over less explicit alternatives. */
  static final class AbstractLongAssertIsEqualToZero {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert) {
      return longAssert.isZero();
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert) {
      return longAssert.isEqualTo(0);
    }
  }

  /** Prefer {@link AbstractLongAssert#isNotEqualTo(long)} over less explicit alternatives. */
  static final class AbstractLongAssertIsNotEqualToZero {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert) {
      return longAssert.isNotZero();
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert) {
      return longAssert.isNotEqualTo(0);
    }
  }

  /** Prefer {@link AbstractLongAssert#isEqualTo(long)} over less explicit alternatives. */
  static final class AbstractLongAssertIsEqualToOne {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert) {
      return longAssert.isOne();
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert) {
      return longAssert.isEqualTo(1);
    }
  }
}
