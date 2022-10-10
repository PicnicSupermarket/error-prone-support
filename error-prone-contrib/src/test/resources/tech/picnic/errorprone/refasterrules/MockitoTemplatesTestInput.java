package tech.picnic.errorprone.refasterrules;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableSet;
import org.mockito.verification.VerificationMode;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class MockitoTemplatesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(times(1));
  }

  VerificationMode testNever() {
    return times(0);
  }

  Object testVerifyOnce() {
    return verify(mock(Object.class), times(1));
  }
}
