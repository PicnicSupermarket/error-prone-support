package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.math.RoundingMode;
import java.util.EnumSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableEnumSetRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(EnumSet.class);
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetIterable() {
    return ImmutableSet.of(
        ImmutableSet.copyOf(EnumSet.range(RoundingMode.UP, RoundingMode.UNNECESSARY)),
        ImmutableSet.copyOf(EnumSet.allOf(RoundingMode.class)));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetIterable1() {
    return ImmutableSet.of(
        ImmutableSet.copyOf(RoundingMode.values()),
        ImmutableSet.copyOf(RoundingMode.class.getEnumConstants()));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetOneElement() {
    return ImmutableSet.of(
        ImmutableSet.of(RoundingMode.UP), ImmutableSet.copyOf(EnumSet.of(RoundingMode.UP)));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetTwoElements() {
    return ImmutableSet.of(
        ImmutableSet.of(RoundingMode.UP, RoundingMode.DOWN),
        ImmutableSet.copyOf(EnumSet.of(RoundingMode.UP, RoundingMode.DOWN)));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetThreeElements() {
    return ImmutableSet.of(
        ImmutableSet.of(RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING),
        ImmutableSet.copyOf(EnumSet.of(RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING)));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetFourElements() {
    return ImmutableSet.of(
        ImmutableSet.of(
            RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING, RoundingMode.FLOOR),
        ImmutableSet.copyOf(
            EnumSet.of(
                RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING, RoundingMode.FLOOR)));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetFiveElements() {
    return ImmutableSet.of(
        ImmutableSet.of(
            RoundingMode.UP,
            RoundingMode.DOWN,
            RoundingMode.CEILING,
            RoundingMode.FLOOR,
            RoundingMode.UNNECESSARY),
        ImmutableSet.copyOf(
            EnumSet.of(
                RoundingMode.UP,
                RoundingMode.DOWN,
                RoundingMode.CEILING,
                RoundingMode.FLOOR,
                RoundingMode.UNNECESSARY)));
  }

  ImmutableSet<RoundingMode> testImmutableEnumSetSixElements() {
    return ImmutableSet.of(
        RoundingMode.UP,
        RoundingMode.DOWN,
        RoundingMode.CEILING,
        RoundingMode.FLOOR,
        RoundingMode.UNNECESSARY,
        RoundingMode.HALF_EVEN);
  }

  ImmutableSet<RoundingMode> testImmutableEnumSetVarArgs() {
    return ImmutableSet.copyOf(
        EnumSet.of(
            RoundingMode.UP,
            RoundingMode.DOWN,
            RoundingMode.CEILING,
            RoundingMode.FLOOR,
            RoundingMode.UNNECESSARY,
            RoundingMode.HALF_EVEN));
  }
}
