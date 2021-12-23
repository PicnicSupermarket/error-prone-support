package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.PrimitiveTemplates.GreaterThan;
import tech.picnic.errorprone.refastertemplates.PrimitiveTemplates.GreaterThanOrEqualTo;
import tech.picnic.errorprone.refastertemplates.PrimitiveTemplates.LessThan;
import tech.picnic.errorprone.refastertemplates.PrimitiveTemplates.LessThanOrEqualTo;
import tech.picnic.errorprone.refastertemplates.PrimitiveTemplates.LongToIntExact;

@TemplateCollection(PrimitiveTemplates.class)
final class PrimitiveTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Ints.class);
  }

  @Template(LessThan.class)
  ImmutableSet<Boolean> testLessThan() {
    return ImmutableSet.of(
        (byte) 3 < (byte) 4, (short) 3 < (short) 4, 3 < 4, 3L < 4L, 3F < 4F, 3.0 < 4.0);
  }

  @Template(LessThanOrEqualTo.class)
  ImmutableSet<Boolean> testLessThanOrEqualTo() {
    return ImmutableSet.of(
        (byte) 3 <= (byte) 4, (short) 3 <= (short) 4, 3 <= 4, 3L <= 4L, 3F <= 4F, 3.0 <= 4.0);
  }

  @Template(GreaterThan.class)
  ImmutableSet<Boolean> testGreaterThan() {
    return ImmutableSet.of(
        (byte) 3 > (byte) 4, (short) 3 > (short) 4, 3 > 4, 3L > 4L, 3F > 4F, 3.0 > 4.0);
  }

  @Template(GreaterThanOrEqualTo.class)
  ImmutableSet<Boolean> testGreaterThanOrEqualTo() {
    return ImmutableSet.of(
        (byte) 3 >= (byte) 4, (short) 3 >= (short) 4, 3 >= 4, 3L >= 4L, 3F >= 4F, 3.0 >= 4.0);
  }

  @Template(LongToIntExact.class)
  int testLongToIntExact() {
    return Math.toIntExact(Long.MAX_VALUE);
  }
}
