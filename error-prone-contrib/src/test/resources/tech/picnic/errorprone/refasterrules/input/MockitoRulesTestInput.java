package tech.picnic.errorprone.refasterrules.input;

import static org.mockito.Mockito.mock;
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
    return times(0);
  }

  Object testVerifyOnce() {
    return verify(mock(Object.class), times(1));
  }

  Object testInvocationOnMockGetArguments() {
    return ((InvocationOnMock) null).getArguments()[0];
  }

  ImmutableSet<Number> testInvocationOnMockGetArgumentsWithTypeParameter() {
    return ImmutableSet.of(
        ((InvocationOnMock) null).getArgument(0, Integer.class),
        (Double) ((InvocationOnMock) null).getArgument(1));
  }
}
