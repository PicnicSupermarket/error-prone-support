package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractDoubleAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJDoubleTemplates.AbstractDoubleAssertIsCloseToWithOffset;
import tech.picnic.errorprone.refastertemplates.AssertJDoubleTemplates.AbstractDoubleAssertIsEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJDoubleTemplates.AbstractDoubleAssertIsNotEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJDoubleTemplates.AbstractDoubleAssertIsNotZero;
import tech.picnic.errorprone.refastertemplates.AssertJDoubleTemplates.AbstractDoubleAssertIsOne;
import tech.picnic.errorprone.refastertemplates.AssertJDoubleTemplates.AbstractDoubleAssertIsZero;

@TemplateCollection(AssertJDoubleTemplates.class)
final class AssertJDoubleTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(withPercentage(0));
  }

  @Template(AbstractDoubleAssertIsCloseToWithOffset.class)
  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsCloseToWithOffset() {
    return ImmutableSet.of(
        assertThat(0.0).isCloseTo(1, offset(0.0)),
        assertThat(0.0).isCloseTo(Double.valueOf(1), offset(0.0)));
  }

  @Template(AbstractDoubleAssertIsEqualTo.class)
  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(0.0).isEqualTo(1),
        assertThat(0.0).isEqualTo(1),
        assertThat(0.0).isEqualTo(1),
        assertThat(0.0).isEqualTo(1),
        assertThat(0.0).isEqualTo(1));
  }

  @Template(AbstractDoubleAssertIsNotEqualTo.class)
  ImmutableSet<AbstractDoubleAssert<?>> testAbstractDoubleAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(0.0).isNotEqualTo(1),
        assertThat(0.0).isNotEqualTo(1),
        assertThat(0.0).isNotEqualTo(1),
        assertThat(0.0).isNotEqualTo(1),
        assertThat(0.0).isNotEqualTo(1));
  }

  @Template(AbstractDoubleAssertIsZero.class)
  AbstractDoubleAssert<?> testAbstractDoubleAssertIsZero() {
    return assertThat(0.0).isEqualTo(0);
  }

  @Template(AbstractDoubleAssertIsNotZero.class)
  AbstractDoubleAssert<?> testAbstractDoubleAssertIsNotZero() {
    return assertThat(0.0).isNotEqualTo(0);
  }

  @Template(AbstractDoubleAssertIsOne.class)
  AbstractDoubleAssert<?> testAbstractDoubleAssertIsOne() {
    return assertThat(0.0).isEqualTo(1);
  }
}
