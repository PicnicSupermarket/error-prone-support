package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.EnumSet;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableEnumSetRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(EnumSet.class);
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetIterable() {
    return ImmutableSet.of(
        Sets.immutableEnumSet(EnumSet.range(RoundingMode.UP, RoundingMode.UNNECESSARY)),
        Sets.immutableEnumSet(EnumSet.allOf(RoundingMode.class)));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetIterable1() {
    return ImmutableSet.of(
        Sets.immutableEnumSet(Arrays.asList(RoundingMode.values())),
        Sets.immutableEnumSet(Arrays.asList(RoundingMode.class.getEnumConstants())));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetOneElement() {
    return ImmutableSet.of(
        Sets.immutableEnumSet(RoundingMode.UP), Sets.immutableEnumSet(RoundingMode.UP));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetTwoElements() {
    return ImmutableSet.of(
        Sets.immutableEnumSet(RoundingMode.UP, RoundingMode.DOWN),
        Sets.immutableEnumSet(RoundingMode.UP, RoundingMode.DOWN));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetThreeElements() {
    return ImmutableSet.of(
        Sets.immutableEnumSet(RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING),
        Sets.immutableEnumSet(RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetFourElements() {
    return ImmutableSet.of(
        Sets.immutableEnumSet(
            RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING, RoundingMode.FLOOR),
        Sets.immutableEnumSet(
            RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING, RoundingMode.FLOOR));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testImmutableEnumSetFiveElements() {
    return ImmutableSet.of(
        Sets.immutableEnumSet(
            RoundingMode.UP,
            RoundingMode.DOWN,
            RoundingMode.CEILING,
            RoundingMode.FLOOR,
            RoundingMode.UNNECESSARY),
        Sets.immutableEnumSet(
            RoundingMode.UP,
            RoundingMode.DOWN,
            RoundingMode.CEILING,
            RoundingMode.FLOOR,
            RoundingMode.UNNECESSARY));
  }

  ImmutableSet<RoundingMode> testImmutableEnumSetSixElements() {
    return Sets.immutableEnumSet(
        RoundingMode.UP,
        RoundingMode.DOWN,
        RoundingMode.CEILING,
        RoundingMode.FLOOR,
        RoundingMode.UNNECESSARY,
        RoundingMode.HALF_EVEN);
  }

  ImmutableSet<RoundingMode> testImmutableEnumSetVarArgs() {
    return Sets.immutableEnumSet(
        RoundingMode.UP,
        RoundingMode.DOWN,
        RoundingMode.CEILING,
        RoundingMode.FLOOR,
        RoundingMode.UNNECESSARY,
        RoundingMode.HALF_EVEN);
  }
}
