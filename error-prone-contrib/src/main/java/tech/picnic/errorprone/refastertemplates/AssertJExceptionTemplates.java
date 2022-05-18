package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import org.assertj.core.api.AbstractThrowableAssert;

final class AssertJExceptionTemplates {
  private AssertJExceptionTemplates() {}

  static final class ThrowableAssertAlternativeHasMessageArgs {
    @BeforeTemplate
    AbstractThrowableAssert<?, ? extends Throwable> before(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String message,
        @Repeated Object parameters) {
      return abstractThrowableAssert.hasMessage(String.format(message, parameters));
    }

    @AfterTemplate
    AbstractThrowableAssert<?, ? extends Throwable> after(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String message,
        @Repeated Object parameters) {
      return abstractThrowableAssert.hasMessage(message, parameters);
    }
  }

  static final class ThrowableAssertAlternativeWithFailMessageArgs {
    @BeforeTemplate
    AbstractThrowableAssert<?, ? extends Throwable> before(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String message,
        @Repeated Object parameters) {
      return abstractThrowableAssert.withFailMessage(String.format(message, parameters));
    }

    @AfterTemplate
    AbstractThrowableAssert<?, ? extends Throwable> after(
        AbstractThrowableAssert<?, ? extends Throwable> abstractThrowableAssert,
        String message,
        @Repeated Object parameters) {
      return abstractThrowableAssert.withFailMessage(message, parameters);
    }
  }
}
