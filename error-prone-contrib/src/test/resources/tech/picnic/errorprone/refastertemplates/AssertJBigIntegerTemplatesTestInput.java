package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.assertj.core.data.Percentage.withPercentage;

import com.google.common.collect.ImmutableSet;
import java.math.BigInteger;
import org.assertj.core.api.AbstractBigIntegerAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJBigIntegerTemplates.AbstractBigIntegerAssertIsEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJBigIntegerTemplates.AbstractBigIntegerAssertIsNotEqualTo;
import tech.picnic.errorprone.refastertemplates.AssertJBigIntegerTemplates.AbstractBigIntegerAssertIsNotZero;
import tech.picnic.errorprone.refastertemplates.AssertJBigIntegerTemplates.AbstractBigIntegerAssertIsOne;
import tech.picnic.errorprone.refastertemplates.AssertJBigIntegerTemplates.AbstractBigIntegerAssertIsZero;

@TemplateCollection(AssertJBigIntegerTemplates.class)
final class AssertJBigIntegerTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(offset(0), withPercentage(0));
  }

  @Template(AbstractBigIntegerAssertIsEqualTo.class)
  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsEqualTo() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isCloseTo(BigInteger.ONE, offset(BigInteger.ZERO)),
        assertThat(BigInteger.ZERO).isCloseTo(BigInteger.ONE, withPercentage(0)));
  }

  @Template(AbstractBigIntegerAssertIsNotEqualTo.class)
  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isNotCloseTo(BigInteger.ONE, offset(BigInteger.ZERO)),
        assertThat(BigInteger.ZERO).isNotCloseTo(BigInteger.ONE, withPercentage(0)));
  }

  @Template(AbstractBigIntegerAssertIsZero.class)
  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsZero() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isZero(),
        assertThat(BigInteger.ZERO).isEqualTo(0L),
        assertThat(BigInteger.ZERO).isEqualTo(BigInteger.ZERO));
  }

  @Template(AbstractBigIntegerAssertIsNotZero.class)
  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsNotZero() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isNotZero(),
        assertThat(BigInteger.ZERO).isNotEqualTo(0L),
        assertThat(BigInteger.ZERO).isNotEqualTo(BigInteger.ZERO));
  }

  @Template(AbstractBigIntegerAssertIsOne.class)
  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsOne() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isOne(),
        assertThat(BigInteger.ZERO).isEqualTo(1L),
        assertThat(BigInteger.ZERO).isEqualTo(BigInteger.ONE));
  }
}
