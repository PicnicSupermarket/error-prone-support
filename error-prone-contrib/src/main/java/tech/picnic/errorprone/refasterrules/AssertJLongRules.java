package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.assertj.core.api.AbstractLongAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJLongRules {
  private AssertJLongRules() {}

  static final class AbstractLongAssertIsEqualTo {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert, long n) {
      return Refaster.anyOf(
          longAssert.isCloseTo(n, offset(0L)), longAssert.isCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert, long n) {
      return longAssert.isEqualTo(n);
    }
  }

  static final class AbstractLongAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert, long n) {
      return Refaster.anyOf(
          longAssert.isNotCloseTo(n, offset(0L)), longAssert.isNotCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert, long n) {
      return longAssert.isNotEqualTo(n);
    }
  }

  static final class AbstractLongAssertIsZero {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert) {
      return longAssert.isZero();
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert) {
      return longAssert.isEqualTo(0);
    }
  }

  static final class AbstractLongAssertIsNotZero {
    @BeforeTemplate
    AbstractLongAssert<?> before(AbstractLongAssert<?> longAssert) {
      return longAssert.isNotZero();
    }

    @AfterTemplate
    AbstractLongAssert<?> after(AbstractLongAssert<?> longAssert) {
      return longAssert.isNotEqualTo(0);
    }
  }

  static final class AbstractLongAssertIsOne {
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
