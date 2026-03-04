package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Optional;
import java.util.function.Predicate;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractOptionalAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.OptionalAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/** Refaster rules related to AssertJ assertions over {@link Optional}s. */
@OnlineDocumentation
final class AssertJOptionalRules {
  private AssertJOptionalRules() {}

  /** Prefer {@code assertThat(optional).get()} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatOptional<T> {
    @BeforeTemplate
    ObjectAssert<T> before(Optional<T> optional) {
      return assertThat(optional.orElseThrow());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, T> after(Optional<T> optional) {
      return assertThat(optional).get();
    }
  }

  /** Prefer {@link AbstractOptionalAssert#isPresent()} over more contrived alternatives. */
  static final class AbstractOptionalAssertIsPresent<T> {
    @BeforeTemplate
    AbstractOptionalAssert<? extends AbstractOptionalAssert<?, T>, T> before(
        AbstractOptionalAssert<?, T> optionalAssert) {
      return Refaster.anyOf(
          optionalAssert.isNotEmpty(), optionalAssert.isNotEqualTo(Optional.empty()));
    }

    @AfterTemplate
    AbstractOptionalAssert<?, T> after(AbstractOptionalAssert<?, T> optionalAssert) {
      return optionalAssert.isPresent();
    }
  }

  /** Prefer {@code assertThat(optional).isPresent()} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatOptionalIsPresent<T> {
    @BeforeTemplate
    AbstractBooleanAssert<? extends AbstractBooleanAssert<?>> before(Optional<T> optional) {
      return Refaster.anyOf(
          assertThat(optional.isPresent()).isTrue(), assertThat(optional.isEmpty()).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    OptionalAssert<T> after(Optional<T> optional) {
      return assertThat(optional).isPresent();
    }
  }

  /** Prefer {@link AbstractOptionalAssert#isEmpty()} over more contrived alternatives. */
  static final class AbstractOptionalAssertIsEmpty<T> {
    @BeforeTemplate
    AbstractOptionalAssert<? extends AbstractOptionalAssert<?, T>, T> before(
        AbstractOptionalAssert<?, T> optionalAssert) {
      return Refaster.anyOf(
          optionalAssert.isNotPresent(), optionalAssert.isEqualTo(Optional.empty()));
    }

    @AfterTemplate
    AbstractOptionalAssert<?, T> after(AbstractOptionalAssert<?, T> optionalAssert) {
      return optionalAssert.isEmpty();
    }
  }

  /** Prefer {@code assertThat(optional).isEmpty()} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatOptionalIsEmpty<T> {
    @BeforeTemplate
    AbstractBooleanAssert<? extends AbstractBooleanAssert<?>> before(Optional<T> optional) {
      return Refaster.anyOf(
          assertThat(optional.isEmpty()).isTrue(), assertThat(optional.isPresent()).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    OptionalAssert<T> after(Optional<T> optional) {
      return assertThat(optional).isEmpty();
    }
  }

  /** Prefer {@link AbstractOptionalAssert#hasValue(Object)} over more contrived alternatives. */
  static final class AbstractOptionalAssertHasValue<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(AbstractOptionalAssert<?, T> optionalAssert, T value) {
      return Refaster.anyOf(
          optionalAssert.get().isEqualTo(value),
          optionalAssert.isEqualTo(Optional.of(value)),
          optionalAssert.contains(value),
          optionalAssert.isPresent().hasValue(value));
    }

    @AfterTemplate
    AbstractOptionalAssert<?, T> after(AbstractOptionalAssert<?, T> optionalAssert, T value) {
      return optionalAssert.hasValue(value);
    }
  }

  /**
   * Prefer {@link AbstractOptionalAssert#containsSame(Object)} over more contrived alternatives.
   */
  static final class AbstractOptionalAssertContainsSame<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(AbstractOptionalAssert<?, T> optionalAssert, T value) {
      return Refaster.anyOf(
          optionalAssert.get().isSameAs(value), optionalAssert.isPresent().isSameAs(value));
    }

    @AfterTemplate
    AbstractOptionalAssert<?, T> after(AbstractOptionalAssert<?, T> optionalAssert, T value) {
      return optionalAssert.containsSame(value);
    }
  }

  /**
   * Prefer {@code assertThat(optional).get().matches(predicate)} over more contrived alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatOptionalHasValueMatching<S, T extends S> {
    @BeforeTemplate
    OptionalAssert<T> before(Optional<T> optional, Predicate<S> predicate) {
      return assertThat(optional.filter(predicate)).isPresent();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, T> after(Optional<T> optional, Predicate<S> predicate) {
      return assertThat(optional).get().matches(predicate);
    }
  }
}
