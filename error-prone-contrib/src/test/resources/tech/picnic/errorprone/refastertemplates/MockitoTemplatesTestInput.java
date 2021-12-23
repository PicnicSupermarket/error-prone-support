package tech.picnic.errorprone.refastertemplates;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableSet;
import org.mockito.verification.VerificationMode;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.MockitoTemplates.Never;
import tech.picnic.errorprone.refastertemplates.MockitoTemplates.VerifyOnce;

@TemplateCollection(MockitoTemplates.class)
final class MockitoTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(times(1));
  }

  @Template(Never.class)
  VerificationMode testNever() {
    return times(0);
  }

  @Template(VerifyOnce.class)
  Object testVerifyOnce() {
    return verify(mock(Object.class), times(1));
  }
}
