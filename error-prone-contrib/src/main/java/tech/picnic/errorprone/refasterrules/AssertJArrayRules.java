package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.NotMatches;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.ObjectArrayAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;
import tech.picnic.errorprone.refaster.matchers.IsMultidimensionalArray;

/** Refaster rules related to AssertJ assertions over arrays. */
// XXX: The `T[]` parameters do not match primitive type arrays. Consider covering those using
// additional `@BeforeTemplate` methods.
@OnlineDocumentation
final class AssertJArrayRules {
  private AssertJArrayRules() {}

  /** Prefer {@link ObjectArrayAssert#hasSize(int)} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasSize<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(
        @NotMatches(IsMultidimensionalArray.class) T[] actual, int expected) {
      return assertThat(actual.length).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectArrayAssert<T> after(T[] actual, int expected) {
      return assertThat(actual).hasSize(expected);
    }
  }

  /** Prefer {@link ObjectArrayAssert#hasSizeLessThan(int)} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasSizeLessThan<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(
        @NotMatches(IsMultidimensionalArray.class) T[] actual, int boundary) {
      return assertThat(actual.length).isLessThan(boundary);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectArrayAssert<T> after(T[] actual, int boundary) {
      return assertThat(actual).hasSizeLessThan(boundary);
    }
  }

  /**
   * Prefer {@link ObjectArrayAssert#hasSizeLessThanOrEqualTo(int)} over less explicit alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatHasSizeLessThanOrEqualTo<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(
        @NotMatches(IsMultidimensionalArray.class) T[] actual, int boundary) {
      return assertThat(actual.length).isLessThanOrEqualTo(boundary);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectArrayAssert<T> after(T[] actual, int boundary) {
      return assertThat(actual).hasSizeLessThanOrEqualTo(boundary);
    }
  }

  /** Prefer {@link ObjectArrayAssert#hasSizeGreaterThan(int)} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasSizeGreaterThan<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(
        @NotMatches(IsMultidimensionalArray.class) T[] actual, int boundary) {
      return assertThat(actual.length).isGreaterThan(boundary);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectArrayAssert<T> after(T[] actual, int boundary) {
      return assertThat(actual).hasSizeGreaterThan(boundary);
    }
  }

  /**
   * Prefer {@link ObjectArrayAssert#hasSizeGreaterThanOrEqualTo(int)} over less explicit
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatHasSizeGreaterThanOrEqualTo<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(
        @NotMatches(IsMultidimensionalArray.class) T[] actual, int boundary) {
      return assertThat(actual.length).isGreaterThanOrEqualTo(boundary);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectArrayAssert<T> after(T[] actual, int boundary) {
      return assertThat(actual).hasSizeGreaterThanOrEqualTo(boundary);
    }
  }

  /** Prefer {@link ObjectArrayAssert#hasSizeBetween(int, int)} over less explicit alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasSizeBetween<T> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(
        @NotMatches(IsMultidimensionalArray.class) T[] actual,
        int lowerBoundary,
        int higherBoundary) {
      return assertThat(actual.length).isBetween(lowerBoundary, higherBoundary);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    ObjectArrayAssert<T> after(T[] actual, int lowerBoundary, int higherBoundary) {
      return assertThat(actual).hasSizeBetween(lowerBoundary, higherBoundary);
    }
  }
}
