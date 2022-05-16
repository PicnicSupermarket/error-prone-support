package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import org.assertj.core.api.ThrowableAssertAlternative;

final class AssertJThrowableTemplates {

  private AssertJThrowableTemplates() {}

  static final class AssertWithMessageStringFormatTemplate {

    @BeforeTemplate
    ThrowableAssertAlternative<?> before(
        ThrowableAssertAlternative<?> throwsAssert, String message, @Repeated Object args) {
      return throwsAssert.withMessage(String.format(message, args));
    }

    @AfterTemplate
    ThrowableAssertAlternative<?> after(
        ThrowableAssertAlternative<?> throwsAssert, String message, @Repeated Object args) {
      return throwsAssert.withMessage(message, args);
    }
  }
}
