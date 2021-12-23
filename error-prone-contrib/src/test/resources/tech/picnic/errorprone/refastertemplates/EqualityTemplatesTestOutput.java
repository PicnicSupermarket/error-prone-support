package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableSet;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.stream.Stream;
import tech.picnic.errorprone.annotations.Template;
import tech.picnic.errorprone.annotations.TemplateCollection;
import tech.picnic.errorprone.refastertemplates.EqualityTemplates.DoubleNegation;
import tech.picnic.errorprone.refastertemplates.EqualityTemplates.EqualsPredicate;
import tech.picnic.errorprone.refastertemplates.EqualityTemplates.IndirectDoubleNegation;
import tech.picnic.errorprone.refastertemplates.EqualityTemplates.Negation;
import tech.picnic.errorprone.refastertemplates.EqualityTemplates.PrimitiveOrReferenceEquality;

@TemplateCollection(EqualityTemplates.class)
final class EqualityTemplatesTest implements RefasterTemplateTestCase {
  @Override
  public ImmutableSet<?> elidedTypesAndStaticImports() {
    return ImmutableSet.of(Objects.class);
  }

  @Template(PrimitiveOrReferenceEquality.class)
  ImmutableSet<Boolean> testPrimitiveOrReferenceEquality() {
    return ImmutableSet.of(
        RoundingMode.UP == RoundingMode.DOWN,
        RoundingMode.UP == RoundingMode.DOWN,
        RoundingMode.UP != RoundingMode.DOWN,
        RoundingMode.UP != RoundingMode.DOWN);
  }

  @Template(EqualsPredicate.class)
  boolean testEqualsPredicate() {
    // XXX: When boxing is involved this rule seems to break. Example:
    // Stream.of(1).anyMatch(e -> Integer.MIN_VALUE.equals(e));
    return Stream.of("foo").anyMatch("bar"::equals);
  }

  @Template(DoubleNegation.class)
  boolean testDoubleNegation() {
    return true;
  }

  @Template(Negation.class)
  ImmutableSet<Boolean> testNegation() {
    return ImmutableSet.of(
        true != false,
        true != false,
        (byte) 3 != (byte) 4,
        (short) 3 != (short) 4,
        3 != 4,
        3L != 4L,
        3F != 4F,
        3.0 != 4.0,
        BoundType.OPEN != BoundType.CLOSED);
  }

  @Template(IndirectDoubleNegation.class)
  ImmutableSet<Boolean> testIndirectDoubleNegation() {
    return ImmutableSet.of(
        true == false,
        true == false,
        (byte) 3 == (byte) 4,
        (short) 3 == (short) 4,
        3 == 4,
        3L == 4L,
        3F == 4F,
        3.0 == 4.0,
        BoundType.OPEN == BoundType.CLOSED);
  }
}
