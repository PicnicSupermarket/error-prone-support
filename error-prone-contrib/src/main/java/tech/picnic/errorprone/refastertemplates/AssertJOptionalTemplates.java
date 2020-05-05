package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Optional;
import java.util.function.Predicate;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractOptionalAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.OptionalAssert;

final class AssertJOptionalTemplates {
  private AssertJOptionalTemplates() {}

  static final class AssertThatOptional<T> {
    @BeforeTemplate
    @SuppressWarnings("NullAway")
    ObjectAssert<T> before(Optional<T> optional) {
      return assertThat(optional.get());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, T> after(Optional<T> optional) {
      return assertThat(optional).get();
    }
  }

  static final class AbstractOptionalAssertIsPresent<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(AbstractOptionalAssert<?, T> optionalAssert) {
      return Refaster.anyOf(
          optionalAssert.isNotEmpty(), optionalAssert.isNotEqualTo(Optional.empty()));
    }

    @AfterTemplate
    AbstractOptionalAssert<?, T> after(AbstractOptionalAssert<?, T> optionalAssert) {
      return optionalAssert.isPresent();
    }
  }

  static final class AssertThatOptionalIsPresent<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Optional<T> optional) {
      return Refaster.anyOf(
          assertThat(optional.isPresent()).isTrue(), assertThat(optional.isEmpty()).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalAssert<T> after(Optional<T> optional) {
      return assertThat(optional).isPresent();
    }
  }

  static final class AbstractOptionalAssertIsEmpty<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(AbstractOptionalAssert<?, T> optionalAssert) {
      return Refaster.anyOf(
          optionalAssert.isNotPresent(), optionalAssert.isEqualTo(Optional.empty()));
    }

    @AfterTemplate
    AbstractOptionalAssert<?, T> after(AbstractOptionalAssert<?, T> optionalAssert) {
      return optionalAssert.isEmpty();
    }
  }

  static final class AssertThatOptionalIsEmpty<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(Optional<T> optional) {
      return Refaster.anyOf(
          assertThat(optional.isEmpty()).isTrue(), assertThat(optional.isPresent()).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    OptionalAssert<T> after(Optional<T> optional) {
      return assertThat(optional).isEmpty();
    }
  }

  static final class AbstractOptionalAssertHasValue<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(AbstractOptionalAssert<?, T> optionalAssert, T value) {
      return Refaster.anyOf(
          optionalAssert.get().isEqualTo(value),
          optionalAssert.isEqualTo(Optional.of(value)),
          optionalAssert.contains(value));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractOptionalAssert<?, T> after(AbstractOptionalAssert<?, T> optionalAssert, T value) {
      return optionalAssert.hasValue(value);
    }
  }

  static final class AssertThatOptionalHasValueMatching<T> {
    @BeforeTemplate
    AbstractOptionalAssert<?, T> before(Optional<T> optional, Predicate<? super T> predicate) {
      return assertThat(optional.filter(predicate)).isPresent();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, T> after(Optional<T> optional, Predicate<? super T> predicate) {
      return assertThat(optional).get().matches(predicate);
    }
  }
}
