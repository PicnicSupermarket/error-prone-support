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
        assertThat(BigDecimal.ZERO).isCloseTo(BigDecimal.ONE, offset(BigDecimal.ZERO)),
        assertThat(BigDecimal.ZERO).isCloseTo(BigDecimal.ONE, withPercentage(0)));
  }

  @Template(AbstractBigDecimalAssertIsNotEqualTo.class)
  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isNotCloseTo(BigDecimal.ONE, offset(BigDecimal.ZERO)),
        assertThat(BigDecimal.ZERO).isNotCloseTo(BigDecimal.ONE, withPercentage(0)));
  }

  @Template(AbstractBigDecimalAssertIsZero.class)
  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsZero() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isZero(),
        assertThat(BigDecimal.ZERO).isEqualTo(0L),
        assertThat(BigDecimal.ZERO).isEqualTo(BigDecimal.ZERO));
  }

  @Template(AbstractBigDecimalAssertIsNotZero.class)
  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsNotZero() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isNotZero(),
        assertThat(BigDecimal.ZERO).isNotEqualTo(0L),
        assertThat(BigDecimal.ZERO).isNotEqualTo(BigDecimal.ZERO));
  }

  @Template(AbstractBigDecimalAssertIsOne.class)
  ImmutableSet<AbstractBigDecimalAssert<?>> testAbstractBigDecimalAssertIsOne() {
    return ImmutableSet.of(
        assertThat(BigDecimal.ZERO).isOne(),
        assertThat(BigDecimal.ZERO).isEqualTo(1L),
        assertThat(BigDecimal.ZERO).isEqualTo(BigDecimal.ONE));
  }
}
