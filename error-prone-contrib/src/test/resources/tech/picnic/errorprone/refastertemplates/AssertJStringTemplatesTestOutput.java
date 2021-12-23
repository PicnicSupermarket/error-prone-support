package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJStringTemplates.AbstractStringAssertStringIsEmpty;
import tech.picnic.errorprone.refastertemplates.AssertJStringTemplates.AbstractStringAssertStringIsNotEmpty;
import tech.picnic.errorprone.refastertemplates.AssertJStringTemplates.AssertThatStringIsEmpty;
import tech.picnic.errorprone.refastertemplates.AssertJStringTemplates.AssertThatStringIsNotEmpty;

@TemplateCollection(AssertJStringTemplates.class)
final class AssertJStringTemplatesTest implements RefasterTemplateTestCase {
  @Template(AbstractStringAssertStringIsEmpty.class)
  void testAbstractStringAssertStringIsEmpty() {
    assertThat("foo").isEmpty();
  }

  @Template(AssertThatStringIsEmpty.class)
  void testAssertThatStringIsEmpty() {
    assertThat("foo").isEmpty();
  }

  @Template(AbstractStringAssertStringIsNotEmpty.class)
  AbstractStringAssert<?> testAbstractStringAssertStringIsNotEmpty() {
    return assertThat("foo").isNotEmpty();
  }

  @Template(AssertThatStringIsNotEmpty.class)
  AbstractAssert<?, ?> testAssertThatStringIsNotEmpty() {
    return assertThat("foo").isNotEmpty();
  }
}
