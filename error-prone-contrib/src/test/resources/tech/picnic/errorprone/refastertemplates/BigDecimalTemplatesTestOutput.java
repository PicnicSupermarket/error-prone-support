package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.BigDecimalTemplates.BigDecimalFactoryMethod;
import tech.picnic.errorprone.refastertemplates.BigDecimalTemplates.BigDecimalOne;
import tech.picnic.errorprone.refastertemplates.BigDecimalTemplates.BigDecimalTen;
import tech.picnic.errorprone.refastertemplates.BigDecimalTemplates.BigDecimalZero;

@TemplateCollection(BigDecimalTemplates.class)
final class BigDecimalTemplatesTest implements RefasterTemplateTestCase {
  @Template(BigDecimalZero.class)
  ImmutableSet<BigDecimal> testBigDecimalZero() {
    return ImmutableSet.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
  }

  @Template(BigDecimalOne.class)
  ImmutableSet<BigDecimal> testBigDecimalOne() {
    return ImmutableSet.of(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
  }

  @Template(BigDecimalTen.class)
  ImmutableSet<BigDecimal> testBigDecimalTen() {
    return ImmutableSet.of(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
  }

  @Template(BigDecimalFactoryMethod.class)
  ImmutableSet<BigDecimal> testBigDecimalFactoryMethod() {
    return ImmutableSet.of(BigDecimal.valueOf(0), BigDecimal.valueOf(0L));
  }
}
