package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.assertj.core.api.AbstractShortAssert;

final class AssertJShortTemplates {
  private AssertJShortTemplates() {}

  static final class AbstractShortAssertIsEqualTo {
    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> shortAssert, short n) {
      return Refaster.anyOf(
          shortAssert.isCloseTo(n, offset((short) 0)), shortAssert.isCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractShortAssert<?> after(AbstractShortAssert<?> shortAssert, short n) {
      return shortAssert.isEqualTo(n);
    }
  }

  static final class AbstractShortAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> shortAssert, short n) {
      return Refaster.anyOf(
          shortAssert.isNotCloseTo(n, offset((short) 0)),
          shortAssert.isNotCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractShortAssert<?> after(AbstractShortAssert<?> shortAssert, short n) {
      return shortAssert.isNotEqualTo(n);
    }
  }

  static final class AbstractShortAssertIsZero {
    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> shortAssert) {
      return shortAssert.isZero();
    }

    @AfterTemplate
    AbstractShortAssert<?> after(AbstractShortAssert<?> shortAssert) {
      return shortAssert.isEqualTo((short) 0);
    }
  }

  static final class AbstractShortAssertIsNotZero {
    @BeforeTemplate
    AbstractShortAssert<?> before(AbstractShortAssert<?> shortAssert) {
      return shortAssert.isNotZero();
    }

    @AfterTemplate
    AbstractShortAssert<?> after(AbstractShortAssert<?> shortAssert) {
      return shortAssert.isNotEqualTo((short) 0);
    }
  }

  static final class AbstractShortAssertIsOne {
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
