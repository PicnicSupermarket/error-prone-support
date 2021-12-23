package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractFloatAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJFloatTemplates.AbstractFloatAssertIsCloseToWithOffset;
import tech.picnic.errorprone.refastertemplates.AssertJFloatTemplates.AbstractFloatAssertIsEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJFloatTemplates.AbstractFloatAssertIsNotEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJFloatTemplates.AbstractFloatAssertIsNotZero;
import tech.picnic.errorprone.refastertemplates.AssertJFloatTemplates.AbstractFloatAssertIsOne;
import tech.picnic.errorprone.refastertemplates.AssertJFloatTemplates.AbstractFloatAssertIsZero;

@TemplateCollection(AssertJFloatTemplates.class)
final class AssertJFloatTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(withPercentage(0));
  }

  @Template(AbstractFloatAssertIsCloseToWithOffset.class)
  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsCloseToWithOffset() {
    return ImmutableSet.of(
        assertThat(0F).isCloseTo(1, offset(0F)),
        assertThat(0F).isCloseTo(Float.valueOf(1), offset(0F)));
  }

  @Template(AbstractFloatAssertIsEqualTo.class)
  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(0F).isEqualTo(1),
        assertThat(0F).isEqualTo(1),
        assertThat(0F).isEqualTo(1),
        assertThat(0F).isEqualTo(1),
        assertThat(0F).isEqualTo(1));
  }

  @Template(AbstractFloatAssertIsNotEqualTo.class)
  ImmutableSet<AbstractFloatAssert<?>> testAbstractFloatAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(0F).isNotEqualTo(1),
        assertThat(0F).isNotEqualTo(1),
        assertThat(0F).isNotEqualTo(1),
        assertThat(0F).isNotEqualTo(1),
        assertThat(0F).isNotEqualTo(1));
  }

  @Template(AbstractFloatAssertIsZero.class)
  AbstractFloatAssert<?> testAbstractFloatAssertIsZero() {
    return assertThat(0F).isEqualTo(0);
  }

  @Template(AbstractFloatAssertIsNotZero.class)
  AbstractFloatAssert<?> testAbstractFloatAssertIsNotZero() {
    return assertThat(0F).isNotEqualTo(0);
  }

  @Template(AbstractFloatAssertIsOne.class)
  AbstractFloatAssert<?> testAbstractFloatAssertIsOne() {
    return assertThat(0F).isEqualTo(1);
  }
}
