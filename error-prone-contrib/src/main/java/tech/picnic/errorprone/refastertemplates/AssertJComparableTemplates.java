package tech.picnic.errorprone.refastertemplates;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.AbstractIntegerAssert;

final class AssertJComparableTemplates {
  private AssertJComparableTemplates() {}

  static final class AbstractComparableAssertActualIsLessThanExpected<
      T extends Comparable<? super T>> {
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

  static final class AbstractComparableAssertActualIsLessThanOrEqualToExpected<
      T extends Comparable<? super T>> {
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

  static final class AbstractComparableAssertActualIsGreaterThanExpected<
      T extends Comparable<? super T>> {
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

  static final class AbstractComparableAssertActualIsGreaterThanOrEqualToExpected<
      T extends Comparable<? super T>> {
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
