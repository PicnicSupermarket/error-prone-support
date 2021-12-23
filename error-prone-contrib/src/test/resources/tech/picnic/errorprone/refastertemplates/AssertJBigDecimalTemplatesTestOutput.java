package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import org.assertj.core.api.AbstractBigDecimalAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJBigDecimalTemplates.AbstractBigDecimalAssertIsEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJBigDecimalTemplates.AbstractBigDecimalAssertIsNotEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJBigDecimalTemplates.AbstractBigDecimalAssertIsNotZero;
import tech.picnic.errorprone.refastertemplates.AssertJBigDecimalTemplates.AbstractBigDecimalAssertIsOne;
import tech.picnic.errorprone.refastertemplates.AssertJBigDecimalTemplates.AbstractBigDecimalAssertIsZero;

@TemplateCollection(AssertJBigDecimalTemplates.class)
final class AssertJBigDecimalTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  @Template(AbstractBigDecimalAssertIsEqualTo.class)
  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isEqualTo(BigDecimal.ONE),
        assertThat(BigDecimal.ZERO).isEqualTo(BigDecimal.ONE));
  }

  @Template(AbstractBigDecimalAssertIsNotEqualTo.class)
  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isNotEqualTo(BigDecimal.ONE),
        assertThat(BigDecimal.ZERO).isNotEqualTo(BigDecimal.ONE));
  }

  @Template(AbstractBigDecimalAssertIsZero.class)
  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsZero() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isEqualTo(0),
        assertThat(BigDecimal.ZERO).isEqualTo(0),
        assertThat(BigDecimal.ZERO).isEqualTo(0));
  }

  @Template(AbstractBigDecimalAssertIsNotZero.class)
  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsNotZero() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isNotEqualTo(0),
        assertThat(BigDecimal.ZERO).isNotEqualTo(0),
        assertThat(BigDecimal.ZERO).isNotEqualTo(0));
  }

  @Template(AbstractBigDecimalAssertIsOne.class)
  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsOne() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isEqualTo(1),
        assertThat(BigDecimal.ZERO).isEqualTo(1),
        assertThat(BigDecimal.ZERO).isEqualTo(1));
  }
}
