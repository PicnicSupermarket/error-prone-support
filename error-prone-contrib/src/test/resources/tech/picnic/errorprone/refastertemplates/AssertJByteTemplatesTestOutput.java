package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractByteAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJByteTemplates.AbstractByteAssertIsEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJByteTemplates.AbstractByteAssertIsNotEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJByteTemplates.AbstractByteAssertIsNotZero;
import tech.picnic.errorprone.refastertemplates.AssertJByteTemplates.AbstractByteAssertIsOne;
import tech.picnic.errorprone.refastertemplates.AssertJByteTemplates.AbstractByteAssertIsZero;

@TemplateCollection(AssertJByteTemplates.class)
final class AssertJByteTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  @Template(AbstractByteAssertIsEqualTo.class)
  ImmutableSet<AbstractByteAssert<?>> testAbstractByteAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 0).isEqualTo((byte) 1),
        assertThat((byte) 0).isEqualTo((byte) 1),
        assertThat((byte) 0).isEqualTo((byte) 1),
        assertThat((byte) 0).isEqualTo((byte) 1),
        assertThat((byte) 0).isEqualTo((byte) 1));
  }

  @Template(AbstractByteAssertIsNotEqualTo.class)
  ImmutableSet<AbstractByteAssert<?>> testAbstractByteAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat((byte) 0).isNotEqualTo((byte) 1),
        assertThat((byte) 0).isNotEqualTo((byte) 1),
        assertThat((byte) 0).isNotEqualTo((byte) 1),
        assertThat((byte) 0).isNotEqualTo((byte) 1),
        assertThat((byte) 0).isNotEqualTo((byte) 1));
  }

  @Template(AbstractByteAssertIsZero.class)
  AbstractByteAssert<?> testAbstractByteAssertIsZero() {
    return assertThat((byte) 0).isEqualTo((byte) 0);
  }

  @Template(AbstractByteAssertIsNotZero.class)
  AbstractByteAssert<?> testAbstractByteAssertIsNotZero() {
    return assertThat((byte) 0).isNotEqualTo((byte) 0);
  }

  @Template(AbstractByteAssertIsOne.class)
  AbstractByteAssert<?> testAbstractByteAssertIsOne() {
    return assertThat((byte) 0).isEqualTo((byte) 1);
  }
}
