package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractIntegerAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJIntegerTemplates.AbstractIntegerAssertIsEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJIntegerTemplates.AbstractIntegerAssertIsNotEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJIntegerTemplates.AbstractIntegerAssertIsNotZero;
import tech.picnic.errorprone.refastertemplates.AssertJIntegerTemplates.AbstractIntegerAssertIsOne;
import tech.picnic.errorprone.refastertemplates.AssertJIntegerTemplates.AbstractIntegerAssertIsZero;

@TemplateCollection(AssertJIntegerTemplates.class)
final class AssertJIntegerTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  @Template(AbstractIntegerAssertIsEqualTo.class)
  ImmutableSet<AbstractIntegerAssert<?>> testAbstractIntegerAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(0).isEqualTo(1),
        assertThat(0).isEqualTo(1),
        assertThat(0).isEqualTo(1),
        assertThat(0).isEqualTo(1),
        assertThat(0).isEqualTo(1));
  }

  @Template(AbstractIntegerAssertIsNotEqualTo.class)
  ImmutableSet<AbstractIntegerAssert<?>> testAbstractIntegerAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(0).isNotEqualTo(1),
        assertThat(0).isNotEqualTo(1),
        assertThat(0).isNotEqualTo(1),
        assertThat(0).isNotEqualTo(1),
        assertThat(0).isNotEqualTo(1));
  }

  @Template(AbstractIntegerAssertIsZero.class)
  AbstractIntegerAssert<?> testAbstractIntegerAssertIsZero() {
    return assertThat(0).isEqualTo(0);
  }

  @Template(AbstractIntegerAssertIsNotZero.class)
  AbstractIntegerAssert<?> testAbstractIntegerAssertIsNotZero() {
    return assertThat(0).isNotEqualTo(0);
  }

  @Template(AbstractIntegerAssertIsOne.class)
  AbstractIntegerAssert<?> testAbstractIntegerAssertIsOne() {
    return assertThat(0).isEqualTo(1);
  }
}
