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
    void before(ThrowingCallable shouldRaiseThrowable, Class<T> type) {
      Refaster.anyOf(
          assertThatThrownBy(shouldRaiseThrowable).asInstanceOf(throwable(type)),
          assertThatThrownBy(shouldRaiseThrowable).asInstanceOf(type(type)));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    void after(ThrowingCallable shouldRaiseThrowable, Class<T> type) {
      assertThatThrownBy(shouldRaiseThrowable).isInstanceOf(type);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClass {
    @BeforeTemplate
    ThrowableAssertAlternative<IllegalArgumentException> before(
        ThrowingCallable shouldRaiseThrowable) {
      return assertThatIllegalArgumentException().isThrownBy(shouldRaiseThrowable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(ThrowingCallable shouldRaiseThrowable) {
      return assertThatThrownBy(shouldRaiseThrowable).isInstanceOf(IllegalArgumentException.class);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalArgumentException> before(
        ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatIllegalArgumentException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final
  class AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<?> before(ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatIllegalArgumentException()
          .isThrownBy(shouldRaiseThrowable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ?> after(ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatThrownBy(shouldRaiseThrowable)
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
  static final class AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessageVarargs {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalArgumentException> before(
        ThrowingCallable shouldRaiseThrowable, String message, @Repeated Object parameters) {
      return assertThatIllegalArgumentException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String message, @Repeated Object parameters) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage(message, parameters);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final
  class AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalArgumentException> before(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatIllegalArgumentException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessageStartingWith(description);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageStartingWith(description);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final
  class AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalArgumentException> before(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatIllegalArgumentException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessageContaining(description);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining(description);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final
  class AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClassHasMessageNotContainingAny {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIllegalArgumentExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalArgumentException> before(
        ThrowingCallable shouldRaiseThrowable, @Repeated CharSequence values) {
      return assertThatIllegalArgumentException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessageNotContainingAny(values);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, @Repeated CharSequence values) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageNotContainingAny(values);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfIllegalStateExceptionClass {
    @BeforeTemplate
    ThrowableAssertAlternative<IllegalStateException> before(
        ThrowingCallable shouldRaiseThrowable) {
      return assertThatIllegalStateException().isThrownBy(shouldRaiseThrowable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(ThrowingCallable shouldRaiseThrowable) {
      return assertThatThrownBy(shouldRaiseThrowable).isInstanceOf(IllegalStateException.class);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIllegalStateExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalStateException> before(
        ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatIllegalStateException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfIllegalStateExceptionClassRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIllegalStateExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<?> before(ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatIllegalStateException()
          .isThrownBy(shouldRaiseThrowable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ?> after(ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatThrownBy(shouldRaiseThrowable)
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
  static final class AssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessageVarargs {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIllegalStateExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalStateException> before(
        ThrowingCallable shouldRaiseThrowable, String message, @Repeated Object parameters) {
      return assertThatIllegalStateException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String message, @Repeated Object parameters) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessage(message, parameters);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final
  class AssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIllegalStateExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalStateException> before(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatIllegalStateException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessageStartingWith(description);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageStartingWith(description);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIllegalStateExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalStateException> before(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatIllegalStateException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessageContaining(description);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining(description);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final
  class AssertThatThrownByIsInstanceOfIllegalStateExceptionClassHasMessageNotContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIllegalStateExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IllegalStateException> before(
        ThrowingCallable shouldRaiseThrowable, String content) {
      return assertThatIllegalStateException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessageNotContaining(content);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String content) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageNotContaining(content);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfNullPointerExceptionClass {
    @BeforeTemplate
    ThrowableAssertAlternative<NullPointerException> before(ThrowingCallable shouldRaiseThrowable) {
      return assertThatNullPointerException().isThrownBy(shouldRaiseThrowable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(ThrowingCallable shouldRaiseThrowable) {
      return assertThatThrownBy(shouldRaiseThrowable).isInstanceOf(NullPointerException.class);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfNullPointerExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<NullPointerException> before(
        ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatNullPointerException().isThrownBy(shouldRaiseThrowable).withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(NullPointerException.class)
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfNullPointerExceptionClassRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfNullPointerExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<?> before(ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatNullPointerException()
          .isThrownBy(shouldRaiseThrowable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ?> after(ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatThrownBy(shouldRaiseThrowable)
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
  static final class AssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessageVarargs {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfNullPointerExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<NullPointerException> before(
        ThrowingCallable shouldRaiseThrowable, String message, @Repeated Object parameters) {
      return assertThatNullPointerException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String message, @Repeated Object parameters) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(NullPointerException.class)
          .hasMessage(message, parameters);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfNullPointerExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<NullPointerException> before(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatNullPointerException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessageStartingWith(description);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(NullPointerException.class)
          .hasMessageStartingWith(description);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfNullPointerExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<NullPointerException> before(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatNullPointerException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessageContaining(description);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining(description);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final
  class AssertThatThrownByIsInstanceOfNullPointerExceptionClassHasMessageNotContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfNullPointerExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<NullPointerException> before(
        ThrowingCallable shouldRaiseThrowable, String content) {
      return assertThatNullPointerException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessageNotContaining(content);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String content) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(NullPointerException.class)
          .hasMessageNotContaining(content);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfIOExceptionClass {
    @BeforeTemplate
    ThrowableAssertAlternative<IOException> before(ThrowingCallable shouldRaiseThrowable) {
      return assertThatIOException().isThrownBy(shouldRaiseThrowable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(ThrowingCallable shouldRaiseThrowable) {
      return assertThatThrownBy(shouldRaiseThrowable).isInstanceOf(IOException.class);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfIOExceptionClassHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIOExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IOException> before(
        ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatIOException().isThrownBy(shouldRaiseThrowable).withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IOException.class)
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfIOExceptionClassRootCauseHasMessage {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIOExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<?> before(ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatIOException()
          .isThrownBy(shouldRaiseThrowable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ?> after(ThrowingCallable shouldRaiseThrowable, String message) {
      return assertThatThrownBy(shouldRaiseThrowable)
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
  static final class AssertThatThrownByIsInstanceOfIOExceptionClassHasMessageVarargs {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIOExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IOException> before(
        ThrowingCallable shouldRaiseThrowable, String message, @Repeated Object parameters) {
      return assertThatIOException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String message, @Repeated Object parameters) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IOException.class)
          .hasMessage(message, parameters);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfIOExceptionClassHasMessageStartingWith {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIOExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IOException> before(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatIOException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessageStartingWith(description);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IOException.class)
          .hasMessageStartingWith(description);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfIOExceptionClassHasMessageContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIOExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IOException> before(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatIOException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessageContaining(description);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String description) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IOException.class)
          .hasMessageContaining(description);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfIOExceptionClassHasMessageNotContaining {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByIsInstanceOfIOExceptionClass" /* This is a more specific template. */)
    ThrowableAssertAlternative<IOException> before(
        ThrowingCallable shouldRaiseThrowable, String content) {
      return assertThatIOException()
          .isThrownBy(shouldRaiseThrowable)
          .withMessageNotContaining(content);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, String content) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(IOException.class)
          .hasMessageNotContaining(content);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByAsInstanceOfThrowable<T extends Throwable> {
    @BeforeTemplate
    ThrowableAssertAlternative<T> before(ThrowingCallable shouldRaiseThrowable, Class<T> type) {
      return assertThatExceptionOfType(type).isThrownBy(shouldRaiseThrowable);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, T> after(ThrowingCallable shouldRaiseThrowable, Class<T> type) {
      return assertThatThrownBy(shouldRaiseThrowable).asInstanceOf(throwable(type));
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfHasMessage<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    ThrowableAssertAlternative<T> before(
        ThrowingCallable shouldRaiseThrowable, Class<T> type, String message) {
      return assertThatExceptionOfType(type).isThrownBy(shouldRaiseThrowable).withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, Class<T> type, String message) {
      return assertThatThrownBy(shouldRaiseThrowable).isInstanceOf(type).hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfRootCauseHasMessage<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    ThrowableAssertAlternative<?> before(
        ThrowingCallable shouldRaiseThrowable, Class<T> type, String message) {
      return assertThatExceptionOfType(type)
          .isThrownBy(shouldRaiseThrowable)
          .havingRootCause()
          .withMessage(message);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ?> after(
        ThrowingCallable shouldRaiseThrowable, Class<T> type, String message) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(type)
          .rootCause()
          .hasMessage(message);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfHasMessageVarargs<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    ThrowableAssertAlternative<T> before(
        ThrowingCallable shouldRaiseThrowable,
        Class<T> type,
        String message,
        @Repeated Object parameters) {
      return assertThatExceptionOfType(type)
          .isThrownBy(shouldRaiseThrowable)
          .withMessage(message, parameters);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable,
        Class<T> type,
        String message,
        @Repeated Object parameters) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(type)
          .hasMessage(message, parameters);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfHasMessageStartingWith<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    ThrowableAssertAlternative<T> before(
        ThrowingCallable shouldRaiseThrowable, Class<T> type, String description) {
      return assertThatExceptionOfType(type)
          .isThrownBy(shouldRaiseThrowable)
          .withMessageStartingWith(description);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, Class<T> type, String description) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(type)
          .hasMessageStartingWith(description);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfHasMessageContaining<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    ThrowableAssertAlternative<T> before(
        ThrowingCallable shouldRaiseThrowable, Class<T> type, String description) {
      return assertThatExceptionOfType(type)
          .isThrownBy(shouldRaiseThrowable)
          .withMessageContaining(description);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, Class<T> type, String description) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(type)
          .hasMessageContaining(description);
    }
  }

  /**
   * Prefer {@link org.assertj.core.api.Assertions#assertThatThrownBy} over less idiomatic
   * alternatives.
   */
  @PossibleSourceIncompatibility
  static final class AssertThatThrownByIsInstanceOfHasMessageNotContaining<T extends Throwable> {
    @BeforeTemplate
    @SuppressWarnings(
        "AssertThatThrownByAsInstanceOfThrowable" /* This is a more specific template. */)
    ThrowableAssertAlternative<T> before(
        ThrowingCallable shouldRaiseThrowable, Class<T> type, String content) {
      return assertThatExceptionOfType(type)
          .isThrownBy(shouldRaiseThrowable)
          .withMessageNotContaining(content);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    AbstractThrowableAssert<?, ? extends Throwable> after(
        ThrowingCallable shouldRaiseThrowable, Class<T> type, String content) {
      return assertThatThrownBy(shouldRaiseThrowable)
          .isInstanceOf(type)
          .hasMessageNotContaining(content);
    }
  }

  /**
   * Prefer {@link AbstractThrowableAssert#hasMessage(String, Object...)} over less efficient
   * alternatives.
   */
  // XXX: Drop this rule in favour of a generic Error Prone check that flags `String.format(...)`
  // arguments to a wide range of format methods.
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

  /**
   * Prefer {@link AbstractThrowableAssert#withFailMessage(String, Object...)} over less efficient
   * alternatives.
   */
  // XXX: Drop this rule in favour of a generic Error Prone check that flags `String.format(...)`
  // arguments to a wide range of format methods.
  static final class AbstractThrowableAssertWithFailMessage {
    @BeforeTemplate
    AbstractThrowableAssert<?, ? extends Throwable> before(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String newErrorMessage,
        @Repeated Object args) {
      return abstractThrowableAssert.withFailMessage(newErrorMessage.formatted(args));
    }

    @AfterTemplate
    AbstractThrowableAssert<?, ? extends Throwable> after(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String newErrorMessage,
        @Repeated Object args) {
      return abstractThrowableAssert.withFailMessage(newErrorMessage, args);
    }
  }

  /** Prefer {@code throwableAssert.cause().isSameAs(expected)} over deprecated alternatives. */
  // XXX: This rule changes the `Throwable` against which subsequent assertions are made.
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
