package tech.picnic.errorprone.refasterrules;

import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.assertj.core.api.AbstractFloatAssert;
import org.assertj.core.data.Offset;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJFloatRules {
  private AssertJFloatRules() {}

  static final class AbstractFloatAssertIsCloseToWithOffset {
    @BeforeTemplate
    AbstractFloatAssert<?> before(
        AbstractFloatAssert<?> floatAssert, float n, Offset<Float> offset) {
      return floatAssert.isEqualTo(n, offset);
    }

    @BeforeTemplate
    AbstractFloatAssert<?> before(
        AbstractFloatAssert<?> floatAssert, Float n, Offset<Float> offset) {
      return floatAssert.isEqualTo(n, offset);
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(
        AbstractFloatAssert<?> floatAssert, float n, Offset<Float> offset) {
      return floatAssert.isCloseTo(n, offset);
    }
  }

  static final class AbstractFloatAssertIsEqualTo {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert, float n) {
      return Refaster.anyOf(
          floatAssert.isCloseTo(n, offset(0F)), floatAssert.isCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert, float n) {
      return floatAssert.isEqualTo(n);
    }
  }

  static final class AbstractFloatAssertIsNotEqualTo {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert, float n) {
      return Refaster.anyOf(
          floatAssert.isNotCloseTo(n, offset(0F)), floatAssert.isNotCloseTo(n, withPercentage(0)));
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert, float n) {
      return floatAssert.isNotEqualTo(n);
    }
  }

  static final class AbstractFloatAssertIsZero {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isZero();
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isEqualTo(0);
    }
  }

  static final class AbstractFloatAssertIsNotZero {
    @BeforeTemplate
    AbstractFloatAssert<?> before(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isNotZero();
    }

    @AfterTemplate
    AbstractFloatAssert<?> after(AbstractFloatAssert<?> floatAssert) {
      return floatAssert.isNotEqualTo(0);
    }
  }

  static final class AbstractFloatAssertIsOne {
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
