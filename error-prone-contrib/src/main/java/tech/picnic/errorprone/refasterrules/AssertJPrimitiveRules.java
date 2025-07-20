package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractObjectArrayAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJPrimitiveRules {
  private AssertJPrimitiveRules() {}

  static final class AssertThatIsEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(boolean actual, boolean expected) {
      return Refaster.anyOf(
          assertThat(actual == expected).isTrue(), assertThat(actual != expected).isFalse());
    }

    @BeforeTemplate
    @SuppressWarnings(
        "java:S1244" /* The (in)equality checks are fragile, but may be seen in the wild. */)
    AbstractBooleanAssert<?> before(double actual, double expected) {
      return Refaster.anyOf(
          assertThat(actual == expected).isTrue(), assertThat(actual != expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractBooleanAssert<?> after(boolean actual, boolean expected) {
      return assertThat(actual).isEqualTo(expected);
    }
  }

  static final class AssertThatIsNotEqualTo {
    @BeforeTemplate
    AbstractBooleanAssert<?> before(boolean actual, boolean expected) {
      return Refaster.anyOf(
          assertThat(actual != expected).isTrue(), assertThat(actual == expected).isFalse());
    }

    @BeforeTemplate
    @SuppressWarnings(
        "java:S1244" /* The (in)equality checks are fragile, but may be seen in the wild. */)
    AbstractBooleanAssert<?> before(double actual, double expected) {
      return Refaster.anyOf(
          assertThat(actual != expected).isTrue(), assertThat(actual == expected).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractBooleanAssert<?> after(boolean actual, boolean expected) {
      return assertThat(actual).isNotEqualTo(expected);
    }
  }

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

  static final class AssertThatArrayIsEmpty<T> {
    @BeforeTemplate
    void before(T[] array) {
      assertThat(array.length).isZero();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(T[] array) {
      assertThat(array).isEmpty();
    }
  }

  static final class AssertThatArrayHasSize<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T[] array, int size) {
      return assertThat(array.length).isEqualTo(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectArrayAssert<?, T> after(T[] array, int size) {
      return assertThat(array).hasSize(size);
    }
  }

  static final class AssertThatArrayHasSameSizeAs<T, U> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T[] array1, U[] array2) {
      return assertThat(array1.length).isEqualTo(array2.length);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectArrayAssert<?, T> after(T[] array1, U[] array2) {
      return assertThat(array1).hasSameSizeAs(array2);
    }
  }

  static final class AssertThatArrayHasSizeLessThanOrEqualTo<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T[] array, int size) {
      return assertThat(array.length).isLessThanOrEqualTo(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectArrayAssert<?, T> after(T[] array, int size) {
      return assertThat(array).hasSizeLessThanOrEqualTo(size);
    }
  }

  static final class AssertThatArrayHasSizeLessThan<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T[] array, int size) {
      return assertThat(array.length).isLessThan(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectArrayAssert<?, T> after(T[] array, int size) {
      return assertThat(array).hasSizeLessThan(size);
    }
  }

  static final class AssertThatArrayHasSizeGreaterThan<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T[] array, int size) {
      return assertThat(array.length).isGreaterThan(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectArrayAssert<?, T> after(T[] array, int size) {
      return assertThat(array).hasSizeGreaterThan(size);
    }
  }

  static final class AssertThatArrayHasSizeGreaterThanOrEqualTo<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T[] array, int size) {
      return assertThat(array.length).isGreaterThanOrEqualTo(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectArrayAssert<?, T> after(T[] array, int size) {
      return assertThat(array).hasSizeGreaterThanOrEqualTo(size);
    }
  }
}
