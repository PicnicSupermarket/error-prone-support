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

/** Refaster rules related to AssertJ assertions over {@link Optional}s. */
@OnlineDocumentation
final class AssertJOptionalRules {
  private AssertJOptionalRules() {}

  /** Prefer {@code assertThat(optional).get()} over more contrived alternatives. */
  static final class AssertThatGet<T> {
    @BeforeTemplate
    ObjectAssert<T> before(Optional<T> actual) {
      return assertThat(actual.orElseThrow());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, T> after(Optional<T> actual) {
      return assertThat(actual).get();
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
  static final class AssertThatIsPresent<T> {
    @BeforeTemplate
    AbstractBooleanAssert<? extends AbstractBooleanAssert<?>> before(Optional<T> actual) {
      return Refaster.anyOf(
          assertThat(actual.isPresent()).isTrue(), assertThat(actual.isEmpty()).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    OptionalAssert<T> after(Optional<T> actual) {
      return assertThat(actual).isPresent();
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
  static final class AssertThatIsEmpty<T> {
    @BeforeTemplate
    AbstractBooleanAssert<? extends AbstractBooleanAssert<?>> before(Optional<T> actual) {
      return Refaster.anyOf(
          assertThat(actual.isEmpty()).isTrue(), assertThat(actual.isPresent()).isFalse());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    OptionalAssert<T> after(Optional<T> actual) {
      return assertThat(actual).isEmpty();
    }
  }

  /** Prefer {@link AbstractOptionalAssert#hasValue(Object)} over more contrived alternatives. */
  static final class AbstractOptionalAssertHasValue<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(AbstractOptionalAssert<?, T> optionalAssert, T expectedValue) {
      return Refaster.anyOf(
          optionalAssert.get().isEqualTo(expectedValue),
          optionalAssert.isEqualTo(Optional.of(expectedValue)),
          optionalAssert.contains(expectedValue),
          optionalAssert.isPresent().hasValue(expectedValue));
    }

    @AfterTemplate
    AbstractOptionalAssert<?, T> after(
        AbstractOptionalAssert<?, T> optionalAssert, T expectedValue) {
      return optionalAssert.hasValue(expectedValue);
    }
  }

  /**
   * Prefer {@link AbstractOptionalAssert#containsSame(Object)} over more contrived alternatives.
   */
  static final class AbstractOptionalAssertContainsSame<T> {
    @BeforeTemplate
    AbstractAssert<?, ?> before(AbstractOptionalAssert<?, T> optionalAssert, T expectedValue) {
      return Refaster.anyOf(
          optionalAssert.get().isSameAs(expectedValue),
          optionalAssert.isPresent().isSameAs(expectedValue));
    }

    @AfterTemplate
    AbstractOptionalAssert<?, T> after(
        AbstractOptionalAssert<?, T> optionalAssert, T expectedValue) {
      return optionalAssert.containsSame(expectedValue);
    }
  }

  /**
   * Prefer {@code assertThat(optional).get().matches(predicate)} over more contrived alternatives.
   */
  static final class AssertThatGetMatches<S, T extends S> {
    @BeforeTemplate
    OptionalAssert<T> before(Optional<T> actual, Predicate<S> predicate) {
      return assertThat(actual.filter(predicate)).isPresent();
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractObjectAssert<?, T> after(Optional<T> actual, Predicate<S> predicate) {
      return assertThat(actual).get().matches(predicate);
    }
  }
}
