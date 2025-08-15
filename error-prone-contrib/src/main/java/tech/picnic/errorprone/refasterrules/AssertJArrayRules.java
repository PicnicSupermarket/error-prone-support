package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.NotMatches;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractObjectArrayAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.matchers.IsMultidimensionalArray;

/** Refaster rules related to AssertJ assertions over arrays. */
// XXX: The `T[]` parameters do not match primitive type arrays. Consider covering those using
// additional `@BeforeTemplate` methods.
@OnlineDocumentation
final class AssertJArrayRules {
  private AssertJArrayRules() {}

  static final class AssertThatHasSize<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(
        @NotMatches(IsMultidimensionalArray.class) T[] array, int size) {
      return assertThat(array.length).isEqualTo(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectArrayAssert<?, T> after(T[] array, int size) {
      return assertThat(array).hasSize(size);
    }
  }

  static final class AssertThatHasSizeLessThan<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(
        @NotMatches(IsMultidimensionalArray.class) T[] array, int size) {
      return assertThat(array.length).isLessThan(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectArrayAssert<?, T> after(T[] array, int size) {
      return assertThat(array).hasSizeLessThan(size);
    }
  }

  static final class AssertThatHasSizeLessThanOrEqualTo<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(
        @NotMatches(IsMultidimensionalArray.class) T[] array, int size) {
      return assertThat(array.length).isLessThanOrEqualTo(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectArrayAssert<?, T> after(T[] array, int size) {
      return assertThat(array).hasSizeLessThanOrEqualTo(size);
    }
  }

  static final class AssertThatHasSizeGreaterThan<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(
        @NotMatches(IsMultidimensionalArray.class) T[] array, int size) {
      return assertThat(array.length).isGreaterThan(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectArrayAssert<?, T> after(T[] array, int size) {
      return assertThat(array).hasSizeGreaterThan(size);
    }
  }

  static final class AssertThatHasSizeGreaterThanOrEqualTo<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(
        @NotMatches(IsMultidimensionalArray.class) T[] array, int size) {
      return assertThat(array.length).isGreaterThanOrEqualTo(size);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectArrayAssert<?, T> after(T[] array, int size) {
      return assertThat(array).hasSizeGreaterThanOrEqualTo(size);
    }
  }

  static final class AssertThatHasSizeBetween<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(
        @NotMatches(IsMultidimensionalArray.class) T[] array, int lowerBound, int upperBound) {
      return assertThat(array.length).isBetween(lowerBound, upperBound);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectArrayAssert<?, T> after(T[] array, int lowerBound, int upperBound) {
      return assertThat(array).hasSizeBetween(lowerBound, upperBound);
    }
  }
}
