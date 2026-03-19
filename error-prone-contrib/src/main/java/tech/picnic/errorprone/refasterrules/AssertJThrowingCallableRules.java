package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.io.IOException;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.assertj.core.api.ThrowableAssertAlternative;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;
import tech.picnic.errorprone.refaster.annotation.PossibleSourceIncompatibility;

/**
 * Refaster rules related to AssertJ assertions over expressions that may throw a {@link Throwable}
 * subtype.
 *
 * <p>For reasons of consistency we prefer {@link
 * org.assertj.core.api.Assertions#assertThatThrownBy} over static methods for specific exception
 * types. Note that only the most common assertion expressions are rewritten here; covering all
 * cases would require the implementation of an Error Prone check instead.
 */
@OnlineDocumentation
final class AssertJThrowingCallableRules {
  private AssertJThrowingCallableRules() {}

  /**
   * Prefer {@link org.assertj.core.api.AbstractAssert#isInstanceOf} over more contrived
   * alternatives.
   */
  static final class AssertThatThrownByIsInstanceOf<T extends Throwable> {
    @BeforeTemplate
    void before(ThrowingCallable throwingCallable, Class<T> exceptionType) {
      Refaster.anyOf(
          assertThatThrownBy(throwingCallable).asInstanceOf(throwable(exceptionType)),
          assertThatThrownBy(throwingCallable).asInstanceOf(type(exceptionType)));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable throwingCallable, Class<T> exceptionType) {
      assertThatThrownBy(throwingCallable).isInstanceOf(exceptionType);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalArgumentException {
    @BeforeTemplate
    ThrowableAssertAlternative<IllegalArgumentException> before(ThrowingCallable throwingCallable) {
      return assertThatIllegalArgumentException().isThrownBy(throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(ThrowingCallable throwingCallable) {
      return assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalArgumentExceptionHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalArgumentException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalArgumentException().isThrownBy(throwingCallable).withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalArgumentExceptionRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* This is a more specific template. */)
    ThrowableAssertAlternative<?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalArgumentException()
          .isThrownBy(throwingCallable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalArgumentException.class)
          .rootCause()
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalArgumentExceptionHasMessageParameters {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalArgumentException> before(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatIllegalArgumentException()
          .isThrownBy(throwingCallable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage(message, parameters);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalArgumentExceptionHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalArgumentException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalArgumentException()
          .isThrownBy(throwingCallable)
          .withMessageStartingWith(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalArgumentExceptionHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalArgumentException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalArgumentException()
          .isThrownBy(throwingCallable)
          .withMessageContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalArgumentExceptionHasMessageNotContainingAny {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalArgumentException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalArgumentException> before(
        ThrowingCallable throwingCallable, @Repeated CharSequence values) {
      return assertThatIllegalArgumentException()
          .isThrownBy(throwingCallable)
          .withMessageNotContainingAny(values);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, @Repeated CharSequence values) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageNotContainingAny(values);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalStateException {
    @BeforeTemplate
    ThrowableAssertAlternative<IllegalStateException> before(ThrowingCallable throwingCallable) {
      return assertThatIllegalStateException().isThrownBy(throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(ThrowingCallable throwingCallable) {
      return assertThatThrownBy(throwingCallable).isInstanceOf(IllegalStateException.class);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalStateExceptionHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalStateException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalStateException().isThrownBy(throwingCallable).withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalStateExceptionRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* This is a more specific template. */)
    ThrowableAssertAlternative<?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalStateException()
          .isThrownBy(throwingCallable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalStateException.class)
          .rootCause()
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalStateExceptionHasMessageParameters {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalStateException> before(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatIllegalStateException()
          .isThrownBy(throwingCallable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessage(message, parameters);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalStateExceptionHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalStateException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalStateException()
          .isThrownBy(throwingCallable)
          .withMessageStartingWith(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageStartingWith(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalStateExceptionHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalStateException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalStateException()
          .isThrownBy(throwingCallable)
          .withMessageContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIllegalStateExceptionHasMessageNotContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIllegalStateException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalStateException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatIllegalStateException()
          .isThrownBy(throwingCallable)
          .withMessageNotContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageNotContaining(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByNullPointerException {
    @BeforeTemplate
    ThrowableAssertAlternative<NullPointerException> before(ThrowingCallable throwingCallable) {
      return assertThatNullPointerException().isThrownBy(throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(ThrowingCallable throwingCallable) {
      return assertThatThrownBy(throwingCallable).isInstanceOf(NullPointerException.class);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByNullPointerExceptionHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByNullPointerException" /* This is a more specific template. */)
    ThrowableAssertAlternative<NullPointerException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatNullPointerException().isThrownBy(throwingCallable).withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(NullPointerException.class)
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByNullPointerExceptionRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByNullPointerException" /* This is a more specific template. */)
    ThrowableAssertAlternative<?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatNullPointerException()
          .isThrownBy(throwingCallable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(NullPointerException.class)
          .rootCause()
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByNullPointerExceptionHasMessageParameters {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByNullPointerException" /* This is a more specific template. */)
    ThrowableAssertAlternative<NullPointerException> before(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatNullPointerException()
          .isThrownBy(throwingCallable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(NullPointerException.class)
          .hasMessage(message, parameters);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByNullPointerExceptionHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByNullPointerException" /* This is a more specific template. */)
    ThrowableAssertAlternative<NullPointerException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatNullPointerException()
          .isThrownBy(throwingCallable)
          .withMessageStartingWith(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(NullPointerException.class)
          .hasMessageStartingWith(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByNullPointerExceptionHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByNullPointerException" /* This is a more specific template. */)
    ThrowableAssertAlternative<NullPointerException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatNullPointerException()
          .isThrownBy(throwingCallable)
          .withMessageContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByNullPointerExceptionHasMessageNotContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByNullPointerException" /* This is a more specific template. */)
    ThrowableAssertAlternative<NullPointerException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatNullPointerException()
          .isThrownBy(throwingCallable)
          .withMessageNotContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(NullPointerException.class)
          .hasMessageNotContaining(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIOException {
    @BeforeTemplate
    ThrowableAssertAlternative<IOException> before(ThrowingCallable throwingCallable) {
      return assertThatIOException().isThrownBy(throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(ThrowingCallable throwingCallable) {
      return assertThatThrownBy(throwingCallable).isInstanceOf(IOException.class);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIOExceptionHasMessage {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByIOException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IOException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatIOException().isThrownBy(throwingCallable).withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IOException.class)
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIOExceptionRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByIOException" /* This is a more specific template. */)
    ThrowableAssertAlternative<?> before(ThrowingCallable throwingCallable, String message) {
      return assertThatIOException()
          .isThrownBy(throwingCallable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ?> after(ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IOException.class)
          .rootCause()
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIOExceptionHasMessageParameters {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByIOException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IOException> before(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatIOException().isThrownBy(throwingCallable).withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message, @Repeated Object parameters) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IOException.class)
          .hasMessage(message, parameters);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIOExceptionHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByIOException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IOException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatIOException().isThrownBy(throwingCallable).withMessageStartingWith(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IOException.class)
          .hasMessageStartingWith(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIOExceptionHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByIOException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IOException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatIOException().isThrownBy(throwingCallable).withMessageContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IOException.class)
          .hasMessageContaining(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIOExceptionHasMessageNotContaining {
    @BeforeTemplate
    @SuppressWarnings("AssertThatThrownByIOException" /* This is a more specific template. */)
    ThrowableAssertAlternative<IOException> before(
        ThrowingCallable throwingCallable, String message) {
      return assertThatIOException().isThrownBy(throwingCallable).withMessageNotContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(IOException.class)
          .hasMessageNotContaining(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByAsInstanceOfThrowable<T extends Throwable> {
    @BeforeTemplate
    ThrowableAssertAlternative<T> before(
        ThrowingCallable throwingCallable, Class<T> exceptionType) {
      return assertThatExceptionOfType(exceptionType).isThrownBy(throwingCallable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, T> after(ThrowingCallable throwingCallable, Class<T> exceptionType) {
      return assertThatThrownBy(throwingCallable).asInstanceOf(throwable(exceptionType));
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByHasMessage<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    ThrowableAssertAlternative<T> before(
        ThrowingCallable throwingCallable, Class<T> exceptionType, String message) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, Class<T> exceptionType, String message) {
      return assertThatThrownBy(throwingCallable).isInstanceOf(exceptionType).hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByRootCauseHasMessage<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    ThrowableAssertAlternative<?> before(
        ThrowingCallable throwingCallable, Class<T> exceptionType, String message) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ?> after(
        ThrowingCallable throwingCallable, Class<T> exceptionType, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(exceptionType)
          .rootCause()
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByHasMessageParameters<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    ThrowableAssertAlternative<T> before(
        ThrowingCallable throwingCallable,
        Class<T> exceptionType,
        String message,
        @Repeated Object parameters) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable,
        Class<T> exceptionType,
        String message,
        @Repeated Object parameters) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(exceptionType)
          .hasMessage(message, parameters);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByHasMessageStartingWith<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    ThrowableAssertAlternative<T> before(
        ThrowingCallable throwingCallable, Class<T> exceptionType, String message) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .withMessageStartingWith(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, Class<T> exceptionType, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(exceptionType)
          .hasMessageStartingWith(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByHasMessageContaining<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    ThrowableAssertAlternative<T> before(
        ThrowingCallable throwingCallable, Class<T> exceptionType, String message) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .withMessageContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, Class<T> exceptionType, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(exceptionType)
          .hasMessageContaining(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByHasMessageNotContaining<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    ThrowableAssertAlternative<T> before(
        ThrowingCallable throwingCallable, Class<T> exceptionType, String message) {
      return assertThatExceptionOfType(exceptionType)
          .isThrownBy(throwingCallable)
          .withMessageNotContaining(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable throwingCallable, Class<T> exceptionType, String message) {
      return assertThatThrownBy(throwingCallable)
          .isInstanceOf(exceptionType)
          .hasMessageNotContaining(message);
    }
  }

  // XXX: Drop this rule in favour of a generic Error Prone check that flags `String.format(...)`
  // arguments to a wide range of format methods.
  /**
   * Prefer {@link AbstractThrowableAssert#hasMessage(String, Object...)} over less efficient
   * alternatives.
   */
  static final class AbstractThrowableAssertHasMessage {
    @BeforeTemplate
    AbstractThrowableAssert<?, ? extends Throwable> before(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String message,
        @Repeated Object parameters) {
      return abstractThrowableAssert.hasMessage(message.formatted(parameters));
    }

    @AfterTemplate
    AbstractThrowableAssert<?, ? extends Throwable> after(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String message,
        @Repeated Object parameters) {
      return abstractThrowableAssert.hasMessage(message, parameters);
    }
  }

  // XXX: Drop this rule in favour of a generic Error Prone check that flags `String.format(...)`
  // arguments to a wide range of format methods.
  /**
   * Prefer {@link AbstractThrowableAssert#withFailMessage(String, Object...)} over less efficient
   * alternatives.
   */
  static final class AbstractThrowableAssertWithFailMessage {
    @BeforeTemplate
    AbstractThrowableAssert<?, ? extends Throwable> before(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String message,
        @Repeated Object parameters) {
      return abstractThrowableAssert.withFailMessage(message.formatted(parameters));
    }

    @AfterTemplate
    AbstractThrowableAssert<?, ? extends Throwable> after(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String message,
        @Repeated Object parameters) {
      return abstractThrowableAssert.withFailMessage(message, parameters);
    }
  }

  // XXX: This rule changes the `Throwable` against which subsequent assertions are made.
  /** Prefer {@code throwableAssert.cause().isSameAs(expected)} over deprecated alternatives. */
  static final class AbstractThrowableAssertCauseIsSameAs {
    @BeforeTemplate
    @SuppressWarnings("deprecation" /* This deprecated API usage will be rewritten. */)
    AbstractThrowableAssert<?, ? extends Throwable> before(
        AbstractThrowableAssert<?, ? extends Throwable> throwableAssert, Throwable expected) {
      return throwableAssert.hasCauseReference(expected);
    }

    @AfterTemplate
    AbstractThrowableAssert<?, ?> after(
        AbstractThrowableAssert<?, ? extends Throwable> throwableAssert, Throwable expected) {
      return throwableAssert.cause().isSameAs(expected);
    }
  }
}
