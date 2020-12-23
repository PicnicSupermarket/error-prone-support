package tech.picnic.errorprone.refastertemplates;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

/** Refaster templates related to Mockito expressions and statements. */
final class MockitoTemplates {
  private MockitoTemplates() {}

  /**
   * Prefer {@link Mockito#never()}} over explicitly specifying that the associated invocation must
   * happen precisely zero times.
   */
  static final class Never {
    @BeforeTemplate
    VerificationMode before() {
      return times(0);
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    VerificationMode after() {
      return never();
    }
  }

  /**
   * Prefer {@link Mockito#verify(Object)} over explicitly specifying that the associated invocation
   * must happen precisely once; this is the default behavior.
   */
  static final class VerifyOnce<T> {
    @BeforeTemplate
    T before(T mock) {
      return verify(mock, times(1));
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    T after(T mock) {
      return verify(mock);
    }
  }
}
