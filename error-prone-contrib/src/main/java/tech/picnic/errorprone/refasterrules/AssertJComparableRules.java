package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/** Refaster rules related to AssertJ assertions over {@link Comparable}s. */
@OnlineDocumentation
final class AssertJComparableRules {
  private AssertJComparableRules() {}

  /**
   * Prefer {@link AbstractComparableAssert#isEqualByComparingTo(Comparable)} over more contrived
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatIsEqualByComparingTo<T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T other) {
      return assertThat(actual.compareTo(other)).isEqualTo(0);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractComparableAssert<?, T> after(T actual, T other) {
      return assertThat(actual).isEqualByComparingTo(other);
    }
  }

  /**
   * Prefer {@link AbstractComparableAssert#isNotEqualByComparingTo(Comparable)} over more contrived
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatIsNotEqualByComparingTo<T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T other) {
      return assertThat(actual.compareTo(other)).isNotEqualTo(0);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractComparableAssert<?, T> after(T actual, T other) {
      return assertThat(actual).isNotEqualByComparingTo(other);
    }
  }

  /**
   * Prefer {@link AbstractComparableAssert#isLessThan(Comparable)} over more contrived
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatIsLessThan<T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T other) {
      return assertThat(actual.compareTo(other)).isNegative();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractComparableAssert<?, T> after(T actual, T other) {
      return assertThat(actual).isLessThan(other);
    }
  }

  /**
   * Prefer {@link AbstractComparableAssert#isLessThanOrEqualTo(Comparable)} over more contrived
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatIsLessThanOrEqualTo<T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T other) {
      return assertThat(actual.compareTo(other)).isNotPositive();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractComparableAssert<?, T> after(T actual, T other) {
      return assertThat(actual).isLessThanOrEqualTo(other);
    }
  }

  /**
   * Prefer {@link AbstractComparableAssert#isGreaterThan(Comparable)} over more contrived
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatIsGreaterThan<T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T other) {
      return assertThat(actual.compareTo(other)).isPositive();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractComparableAssert<?, T> after(T actual, T other) {
      return assertThat(actual).isGreaterThan(other);
    }
  }

  /**
   * Prefer {@link AbstractComparableAssert#isGreaterThanOrEqualTo(Comparable)} over more contrived
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatIsGreaterThanOrEqualTo<T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T other) {
      return assertThat(actual.compareTo(other)).isNotNegative();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractComparableAssert<?, T> after(T actual, T other) {
      return assertThat(actual).isGreaterThanOrEqualTo(other);
    }
  }
}
