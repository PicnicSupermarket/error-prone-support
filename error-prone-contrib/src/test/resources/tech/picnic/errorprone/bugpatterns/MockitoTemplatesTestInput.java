package tech.picnic.errorprone.bugpatterns;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableSet;

final class MockitoTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(times(1));
  }

  Object testVerifyOnce() {
    return verify(mock(Object.class), times(1));
  }
}
