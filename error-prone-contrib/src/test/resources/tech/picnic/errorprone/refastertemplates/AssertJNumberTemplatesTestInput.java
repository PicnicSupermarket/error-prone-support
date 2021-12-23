package tech.picnic.errorprone.refastertemplates;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.assertj.core.api.NumberAssert;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.AssertJNumberTemplates.NumberAssertIsNegative;
import tech.picnic.errorprone.refastertemplates.AssertJNumberTemplates.NumberAssertIsNotNegative;
import tech.picnic.errorprone.refastertemplates.AssertJNumberTemplates.NumberAssertIsNotPositive;
import tech.picnic.errorprone.refastertemplates.AssertJNumberTemplates.NumberAssertIsPositive;

@TemplateCollection(AssertJNumberTemplates.class)
final class AssertJNumberTemplatesTest implements RefasterTemplateTestCase {
  @Template(NumberAssertIsPositive.class)
  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsPositive() {
    return ImmutableSet.of(
        assertThat((byte) 0).isGreaterThan((byte) 0),
        assertThat((byte) 0).isGreaterThanOrEqualTo((byte) 1),
        assertThat((short) 0).isGreaterThan((short) 0),
        assertThat((short) 0).isGreaterThanOrEqualTo((short) 1),
        assertThat(0).isGreaterThan(0),
        assertThat(0).isGreaterThanOrEqualTo(1),
        assertThat(0L).isGreaterThan(0),
        assertThat(0L).isGreaterThanOrEqualTo(1),
        assertThat(0.0F).isGreaterThan(0),
        assertThat(0.0).isGreaterThan(0),
        assertThat(BigInteger.ZERO).isGreaterThan(BigInteger.ZERO),
        assertThat(BigInteger.ZERO).isGreaterThanOrEqualTo(BigInteger.valueOf(1)),
        assertThat(BigDecimal.ZERO).isGreaterThan(BigDecimal.ZERO));
  }

  @Template(NumberAssertIsNotPositive.class)
  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNotPositive() {
    return ImmutableSet.of(
        assertThat((byte) 0).isLessThanOrEqualTo((byte) 0),
        assertThat((byte) 0).isLessThan((byte) 1),
        assertThat((short) 0).isLessThanOrEqualTo((short) 0),
        assertThat((short) 0).isLessThan((short) 1),
        assertThat(0).isLessThanOrEqualTo(0),
        assertThat(0).isLessThan(1),
        assertThat(0L).isLessThanOrEqualTo(0),
        assertThat(0L).isLessThan(1),
        assertThat(0.0F).isLessThanOrEqualTo(0),
        assertThat(0.0).isLessThanOrEqualTo(0),
        assertThat(BigInteger.ZERO).isLessThanOrEqualTo(BigInteger.ZERO),
        assertThat(BigInteger.ZERO).isLessThan(BigInteger.valueOf(1)),
        assertThat(BigDecimal.ZERO).isLessThanOrEqualTo(BigDecimal.ZERO));
  }

  @Template(NumberAssertIsNegative.class)
  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNegative() {
    return ImmutableSet.of(
        assertThat((byte) 0).isLessThan((byte) 0),
        assertThat((byte) 0).isLessThanOrEqualTo((byte) -1),
        assertThat((short) 0).isLessThan((short) 0),
        assertThat((short) 0).isLessThanOrEqualTo((short) -1),
        assertThat(0).isLessThan(0),
        assertThat(0).isLessThanOrEqualTo(-1),
        assertThat(0L).isLessThan(0),
        assertThat(0L).isLessThanOrEqualTo(-1),
        assertThat(0.0F).isLessThan(0),
        assertThat(0.0).isLessThan(0),
        assertThat(BigInteger.ZERO).isLessThan(BigInteger.ZERO),
        assertThat(BigInteger.ZERO).isLessThanOrEqualTo(BigInteger.valueOf(-1)),
        assertThat(BigDecimal.ZERO).isLessThan(BigDecimal.ZERO));
  }

  @Template(NumberAssertIsNotNegative.class)
  ImmutableSet<NumberAssert<?, ?>> testNumberAssertIsNotNegative() {
    return ImmutableSet.of(
        assertThat((byte) 0).isGreaterThanOrEqualTo((byte) 0),
        assertThat((byte) 0).isGreaterThan((byte) -1),
        assertThat((short) 0).isGreaterThanOrEqualTo((short) 0),
        assertThat((short) 0).isGreaterThan((short) -1),
        assertThat(0).isGreaterThanOrEqualTo(0),
        assertThat(0).isGreaterThan(-1),
        assertThat(0L).isGreaterThanOrEqualTo(0),
        assertThat(0L).isGreaterThan(-1),
        assertThat(0.0F).isGreaterThanOrEqualTo(0),
        assertThat(0.0).isGreaterThanOrEqualTo(0),
        assertThat(BigInteger.ZERO).isGreaterThanOrEqualTo(BigInteger.ZERO),
        assertThat(BigInteger.ZERO).isGreaterThan(BigInteger.valueOf(-1)),
        assertThat(BigDecimal.ZERO).isGreaterThanOrEqualTo(BigDecimal.ZERO));
  }
}
