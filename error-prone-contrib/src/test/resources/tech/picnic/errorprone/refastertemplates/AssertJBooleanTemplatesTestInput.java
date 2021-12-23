package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractBooleanAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJBooleanTemplates.AbstractBooleanAssertIsEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJBooleanTemplates.AbstractBooleanAssertIsFalse;
import tech.picnic.errorprone.refastertemplates.AssertJBooleanTemplates.AbstractBooleanAssertIsNotEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJBooleanTemplates.AbstractBooleanAssertIsTrue;
import tech.picnic.errorprone.refastertemplates.AssertJBooleanTemplates.AssertThatBooleanIsFalse;
import tech.picnic.errorprone.refastertemplates.AssertJBooleanTemplates.AssertThatBooleanIsTrue;

@TemplateCollection(AssertJBooleanTemplates.class)
final class AssertJBooleanTemplatesTest implements RefasterTemplateTestCase {
  @Template(AbstractBooleanAssertIsEqualTo.class)
  AbstractBooleanAssert<?> testAbstractBooleanAssertIsEqualTo() {
    return assertThat(true).isNotEqualTo(!false);
  }

  @Template(AbstractBooleanAssertIsNotEqualTo.class)
  AbstractBooleanAssert<?> testAbstractBooleanAssertIsNotEqualTo() {
    return assertThat(true).isEqualTo(!false);
  }

  @Template(AbstractBooleanAssertIsTrue.class)
  ImmutableSet<AbstractBooleanAssert<?>> testAbstractBooleanAssertIsTrue() {
    return ImmutableSet.of(
        assertThat(true).isEqualTo(true),
        assertThat(true).isEqualTo(Boolean.TRUE),
        assertThat(true).isNotEqualTo(false),
        assertThat(true).isNotEqualTo(Boolean.FALSE));
  }

  @Template(AssertThatBooleanIsTrue.class)
  AbstractBooleanAssert<?> testAssertThatBooleanIsTrue() {
    return assertThat(!true).isFalse();
  }

  @Template(AbstractBooleanAssertIsFalse.class)
  ImmutableSet<AbstractBooleanAssert<?>> testAbstractBooleanAssertIsFalse() {
    return ImmutableSet.of(
        assertThat(true).isEqualTo(false),
        assertThat(true).isEqualTo(Boolean.FALSE),
        assertThat(true).isNotEqualTo(true),
        assertThat(true).isNotEqualTo(Boolean.TRUE));
  }

  @Template(AssertThatBooleanIsFalse.class)
  AbstractBooleanAssert<?> testAssertThatBooleanIsFalse() {
    return assertThat(!true).isTrue();
  }
}
