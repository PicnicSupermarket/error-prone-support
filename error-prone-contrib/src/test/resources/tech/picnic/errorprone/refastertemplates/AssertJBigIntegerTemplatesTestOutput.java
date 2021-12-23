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
        assertThat(BigInteger.ZERO).isEqualTo(BigInteger.ONE),
        assertThat(BigInteger.ZERO).isEqualTo(BigInteger.ONE));
  }

  @Template(AbstractBigIntegerAssertIsNotEqualTo.class)
  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsNotEqualTo() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isNotEqualTo(BigInteger.ONE),
        assertThat(BigInteger.ZERO).isNotEqualTo(BigInteger.ONE));
  }

  @Template(AbstractBigIntegerAssertIsZero.class)
  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsZero() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isEqualTo(0),
        assertThat(BigInteger.ZERO).isEqualTo(0),
        assertThat(BigInteger.ZERO).isEqualTo(0));
  }

  @Template(AbstractBigIntegerAssertIsNotZero.class)
  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsNotZero() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isNotEqualTo(0),
        assertThat(BigInteger.ZERO).isNotEqualTo(0),
        assertThat(BigInteger.ZERO).isNotEqualTo(0));
  }

  @Template(AbstractBigIntegerAssertIsOne.class)
  ImmutableSet<AbstractBigIntegerAssert<?>> testAbstractBigIntegerAssertIsOne() {
    return ImmutableSet.of(
        assertThat(BigInteger.ZERO).isEqualTo(1),
        assertThat(BigInteger.ZERO).isEqualTo(1),
        assertThat(BigInteger.ZERO).isEqualTo(1));
  }
}
