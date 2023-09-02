package tech.picnic.errorprone.refasterrules;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableSet;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.verification.VerificationMode;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class MockitoRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(times(1));
  }

  VerificationMode testNever() {
    return never();
  }

  Object testVerifyOnce() {
    return verify(mock(Object.class));
  }

  Object testInvocationOnMockGetArguments() {
    return ((InvocationOnMock) null).getArgument(0);
  }

  ImmutableSet<Number> testInvocationOnMockGetArgumentsWithTypeParameter() {
    return ImmutableSet.of(
        ((InvocationOnMock) null).<Integer>getArgument(0),
        ((InvocationOnMock) null).<Double>getArgument(1));
  }
}
