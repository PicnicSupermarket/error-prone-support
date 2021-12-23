package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.AbstractLongAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJLongTemplates.AbstractLongAssertIsEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJLongTemplates.AbstractLongAssertIsNotEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJLongTemplates.AbstractLongAssertIsNotZero;
import tech.picnic.errorprone.refastertemplates.AssertJLongTemplates.AbstractLongAssertIsOne;
import tech.picnic.errorprone.refastertemplates.AssertJLongTemplates.AbstractLongAssertIsZero;

@TemplateCollection(AssertJLongTemplates.class)
final class AssertJLongTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  @Template(AbstractLongAssertIsEqualTo.class)
  ImmutableSet<AbstractLongAssert<?>> testAbstractLongAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(0L).isCloseTo(1, offset(0L)),
        assertThat(0L).isCloseTo(Long.valueOf(1), offset(0L)),
        assertThat(0L).isCloseTo(1, withPercentage(0)),
        assertThat(0L).isCloseTo(Long.valueOf(1), withPercentage(0)),
        assertThat(0L).isEqualTo(Long.valueOf(1)));
  }

  @Template(AbstractLongAssertIsNotEqualTo.class)
  ImmutableSet<AbstractLongAssert<?>> testAbstractLongAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(0L).isNotCloseTo(1, offset(0L)),
        assertThat(0L).isNotCloseTo(Long.valueOf(1), offset(0L)),
        assertThat(0L).isNotCloseTo(1, withPercentage(0)),
        assertThat(0L).isNotCloseTo(Long.valueOf(1), withPercentage(0)),
        assertThat(0L).isNotEqualTo(Long.valueOf(1)));
  }

  @Template(AbstractLongAssertIsZero.class)
  AbstractLongAssert<?> testAbstractLongAssertIsZero() {
    return assertThat(0L).isZero();
  }

  @Template(AbstractLongAssertIsNotZero.class)
  AbstractLongAssert<?> testAbstractLongAssertIsNotZero() {
    return assertThat(0L).isNotZero();
  }

  @Template(AbstractLongAssertIsOne.class)
  AbstractLongAssert<?> testAbstractLongAssertIsOne() {
    return assertThat(0L).isOne();
  }
}
