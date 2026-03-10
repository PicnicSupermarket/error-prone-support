package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractByteAssert;
import org.assertj.core.api.AbstractCharacterAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.api.AbstractFloatAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractShortAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to AssertJ assertions over primitive values. */
@OnlineDocumentation
final class AssertJPrimitiveRules {
  private AssertJPrimitiveRules() {}

  /** Prefer {@code isEqualTo} over less idiomatic alternatives. */
  static final class AssertThatIsEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(boolean actual, boolean expected) {
      return assertThat(actual).isSameAs(expected);
    }

    @BeforeTemplate
    AbstractByteAssert<?> before(byte actual, byte expected) {
      return assertThat(actual).isSameAs(expected);
    }

    @BeforeTemplate
    AbstractCharacterAssert<?> before(char actual, char expected) {
      return assertThat(actual).isSameAs(expected);
    }

    @BeforeTemplate
    AbstractShortAssert<?> before(short actual, short expected) {
      return assertThat(actual).isSameAs(expected);
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(int actual, int expected) {
      return assertThat(actual).isSameAs(expected);
    }

    @BeforeTemplate
    AbstractLongAssert<?> before(long actual, long expected) {
      return assertThat(actual).isSameAs(expected);
    }

    @BeforeTemplate
    AbstractFloatAssert<?> before(float actual, float expected) {
      return assertThat(actual).isSameAs(expected);
    }

    @BeforeTemplate
    AbstractDoubleAssert<?> before(double actual, double expected) {
      return assertThat(actual).isSameAs(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractBooleanAssert<?> after(boolean actual, boolean expected) {
      return assertThat(actual).isEqualTo(expected);
    }
  }

  /** Prefer {@code isNotEqualTo} over less idiomatic alternatives. */
  static final class AssertThatIsNotEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(boolean actual, boolean expected) {
      return assertThat(actual).isNotSameAs(expected);
    }

    @BeforeTemplate
    AbstractByteAssert<?> before(byte actual, byte expected) {
      return assertThat(actual).isNotSameAs(expected);
    }

    @BeforeTemplate
    AbstractCharacterAssert<?> before(char actual, char expected) {
      return assertThat(actual).isNotSameAs(expected);
    }

    @BeforeTemplate
    AbstractShortAssert<?> before(short actual, short expected) {
      return assertThat(actual).isNotSameAs(expected);
    }

    @BeforeTemplate
    AbstractIntegerAssert<?> before(int actual, int expected) {
      return assertThat(actual).isNotSameAs(expected);
    }

    @BeforeTemplate
    AbstractLongAssert<?> before(long actual, long expected) {
      return assertThat(actual).isNotSameAs(expected);
    }

    @BeforeTemplate
    AbstractFloatAssert<?> before(float actual, float expected) {
      return assertThat(actual).isNotSameAs(expected);
    }

    @BeforeTemplate
    AbstractDoubleAssert<?> before(double actual, double expected) {
      return assertThat(actual).isNotSameAs(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractBooleanAssert<?> after(boolean actual, boolean expected) {
      return assertThat(actual).isNotEqualTo(expected);
    }
  }

  /** Prefer {@code isLessThan} over less idiomatic alternatives. */
  static final class AssertThatIsLessThan {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(double actual, double expected) {
      return Refaster.anyOf(
          assertThat(actual < expected).isTrue(), assertThat(actual >= expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractDoubleAssert<?> after(double actual, double expected) {
      return assertThat(actual).isLessThan(expected);
    }
  }

  /** Prefer {@code isLessThanOrEqualTo} over less idiomatic alternatives. */
  static final class AssertThatIsLessThanOrEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(double actual, double expected) {
      return Refaster.anyOf(
          assertThat(actual <= expected).isTrue(), assertThat(actual > expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractDoubleAssert<?> after(double actual, double expected) {
      return assertThat(actual).isLessThanOrEqualTo(expected);
    }
  }

  /** Prefer {@code isGreaterThan} over less idiomatic alternatives. */
  static final class AssertThatIsGreaterThan {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(double actual, double expected) {
      return Refaster.anyOf(
          assertThat(actual > expected).isTrue(), assertThat(actual <= expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractDoubleAssert<?> after(double actual, double expected) {
      return assertThat(actual).isGreaterThan(expected);
    }
  }

  /** Prefer {@code isGreaterThanOrEqualTo} over less idiomatic alternatives. */
  static final class AssertThatIsGreaterThanOrEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(double actual, double expected) {
      return Refaster.anyOf(
          assertThat(actual >= expected).isTrue(), assertThat(actual < expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractDoubleAssert<?> after(double actual, double expected) {
      return assertThat(actual).isGreaterThanOrEqualTo(expected);
    }
  }
}
