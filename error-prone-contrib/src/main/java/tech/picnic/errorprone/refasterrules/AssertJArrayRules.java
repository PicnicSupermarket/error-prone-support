package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractObjectArrayAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

@OnlineDocumentation
final class AssertJArrayRules {
  private AssertJArrayRules() {}

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
