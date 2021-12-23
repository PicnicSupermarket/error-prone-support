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
    return ImmutableSet.of(BigDecimal.valueOf(0), BigDecimal.valueOf(0L), new BigDecimal("0"));
  }

  @Template(BigDecimalOne.class)
  ImmutableSet<BigDecimal> testBigDecimalOne() {
    return ImmutableSet.of(BigDecimal.valueOf(1), BigDecimal.valueOf(1L), new BigDecimal("1"));
  }

  @Template(BigDecimalTen.class)
  ImmutableSet<BigDecimal> testBigDecimalTen() {
    return ImmutableSet.of(BigDecimal.valueOf(10), BigDecimal.valueOf(10L), new BigDecimal("10"));
  }

  @Template(BigDecimalFactoryMethod.class)
  ImmutableSet<BigDecimal> testBigDecimalFactoryMethod() {
    return ImmutableSet.of(new BigDecimal(0), new BigDecimal(0L));
  }
}
