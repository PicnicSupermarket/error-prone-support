package tech.picnic.errorprone.refasterrules;

import static com.google.errorprone.refaster.ImportPolicy.STATIC_IMPORT_ALWAYS;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.verification.VerificationMode;
import tech.picnic.errorprone.refaster.annotation.OnlineDocumentation;

/** Refaster rules related to Mockito expressions and statements. */
@OnlineDocumentation
final class MockitoRules {
  private MockitoRules() {}

  /** Prefer {@link Mockito#never()} over less explicit alternatives. */
  static final class Never {
    @BeforeTemplate
    VerificationMode before() {
      return times(0);
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    VerificationMode after() {
      return never();
    }
  }

  /** Prefer {@link Mockito#verify(Object)} over more verbose alternatives. */
  static final class Verify<T> {
    @BeforeTemplate
    T before(T mock) {
      return verify(mock, times(1));
    }

    @AfterTemplate
    @UseImportPolicy(STATIC_IMPORT_ALWAYS)
    T after(T mock) {
      return verify(mock);
    }
  }

  /** Prefer {@link InvocationOnMock#getArgument(int)} over more verbose alternatives. */
  static final class InvocationOnMockGetArgument {
    @BeforeTemplate
    Object before(InvocationOnMock mock, int i) {
      return mock.getArguments()[i];
    }

    @AfterTemplate
    Object after(InvocationOnMock mock, int i) {
      return mock.getArgument(i);
    }
  }

  /** Prefer {@code invocation.<T>getArgument(int)} over less explicit alternatives. */
  static final class InvocationOnMockGetArgumentObject<T> {
    @BeforeTemplate
    @SuppressWarnings("unchecked" /* Cast is presumed safe in matched context. */)
    T before(InvocationOnMock mock, int i) {
      return Refaster.anyOf(mock.getArgument(i, Refaster.<T>clazz()), (T) mock.getArgument(i));
    }

    @AfterTemplate
    T after(InvocationOnMock mock, int i) {
      return mock.<T>getArgument(i);
    }
  }
}
