package tech.picnic.errorprone.refasterrules.output;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableSet;
import org.mockito.verification.VerificationMode;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class MockitoRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(times(1));
  }

  VerificationMode testNever() {
    return never();
  }

  Object testVerifyOnce() {
    return verify(mock(Object.class));
  }
}
