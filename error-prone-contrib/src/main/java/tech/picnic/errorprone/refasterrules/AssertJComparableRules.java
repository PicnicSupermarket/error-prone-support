package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.AbstractIntegerAssert;

final class AssertJComparableRules {
  private AssertJComparableRules() {}

  static final class AssertThatIsEqualByComparingTo<T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T expected) {
      return assertThat(actual.compareTo(expected)).isEqualTo(0);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractComparableAssert<?, ?> after(T actual, T expected) {
      return assertThat(actual).isEqualByComparingTo(expected);
    }
  }

  static final class AssertThatIsNotEqualByComparingTo<T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T expected) {
      return assertThat(actual.compareTo(expected)).isNotEqualTo(0);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractComparableAssert<?, ?> after(T actual, T expected) {
      return assertThat(actual).isNotEqualByComparingTo(expected);
    }
  }

  static final class AssertThatIsLessThan<T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T expected) {
      return assertThat(actual.compareTo(expected)).isNegative();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractComparableAssert<?, ?> after(T actual, T expected) {
      return assertThat(actual).isLessThan(expected);
    }
  }

  static final class AssertThatIsLessThanOrEqualTo<T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T expected) {
      return assertThat(actual.compareTo(expected)).isNotPositive();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractComparableAssert<?, ?> after(T actual, T expected) {
      return assertThat(actual).isLessThanOrEqualTo(expected);
    }
  }

  static final class AssertThatIsGreaterThan<T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T expected) {
      return assertThat(actual.compareTo(expected)).isPositive();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractComparableAssert<?, ?> after(T actual, T expected) {
      return assertThat(actual).isGreaterThan(expected);
    }
  }

  static final class AssertThatIsGreaterThanOrEqualTo<T extends Comparable<? super T>> {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(T actual, T expected) {
      return assertThat(actual.compareTo(expected)).isNotNegative();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractComparableAssert<?, ?> after(T actual, T expected) {
      return assertThat(actual).isGreaterThanOrEqualTo(expected);
    }
  }
}
