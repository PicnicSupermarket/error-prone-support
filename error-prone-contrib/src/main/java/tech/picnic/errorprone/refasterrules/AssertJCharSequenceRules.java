package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractCharSequenceAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/** Refaster rules related to AssertJ assertions over {@link CharSequence}s. */
@OnlineDocumentation
final class AssertJCharSequenceRules {
  private AssertJCharSequenceRules() {}

  /** Prefer {@link AbstractCharSequenceAssert#isEmpty()} over more contrived alternatives. */
  static final class AssertThatIsEmpty {
    @BeforeTemplate
    void before(CharSequence actual) {
      Refaster.anyOf(
          assertThat(actual.isEmpty()).isTrue(),
          assertThat(actual.length()).isEqualTo(0L),
          assertThat(actual.length()).isNotPositive());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(CharSequence actual) {
      assertThat(actual).isEmpty();
    }
  }

  /** Prefer {@link AbstractCharSequenceAssert#isNotEmpty()} over more contrived alternatives. */
  static final class AssertThatIsNotEmpty {
    @BeforeTemplate
    AbstractAssert<?, ?> before(CharSequence actual) {
      return Refaster.anyOf(
          assertThat(actual.isEmpty()).isFalse(),
          assertThat(actual.length()).isNotEqualTo(0),
          assertThat(actual.length()).isPositive());
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractCharSequenceAssert<?, ? extends CharSequence> after(CharSequence actual) {
      return assertThat(actual).isNotEmpty();
    }
  }

  /** Prefer {@link AbstractCharSequenceAssert#hasSize(int)} over more contrived alternatives. */
  @PossibleSourceIncompatibility
  static final class AssertThatHasSize {
    @BeforeTemplate
    AbstractIntegerAssert<?> before(CharSequence actual, int expected) {
      return assertThat(actual.length()).isEqualTo(expected);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractCharSequenceAssert<?, ? extends CharSequence> after(CharSequence actual, int expected) {
      return assertThat(actual).hasSize(expected);
    }
  }
}
