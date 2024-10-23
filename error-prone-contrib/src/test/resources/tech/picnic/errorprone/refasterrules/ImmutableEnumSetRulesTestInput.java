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

  ImmutableSet<RoundingMode> testSetsImmutableEnumSetIterable() {
    return ImmutableSet.copyOf(EnumSet.range(RoundingMode.UP, RoundingMode.UNNECESSARY));
  }

  ImmutableSet<RoundingMode> testSetsImmutableEnumSetIterableArray() {
    return ImmutableSet.copyOf(RoundingMode.values());
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testSetsImmutableEnumSet1() {
    return ImmutableSet.of(
        ImmutableSet.of(RoundingMode.UP), ImmutableSet.copyOf(EnumSet.of(RoundingMode.UP)));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testSetsImmutableEnumSet2() {
    return ImmutableSet.of(
        ImmutableSet.of(RoundingMode.UP, RoundingMode.DOWN),
        ImmutableSet.copyOf(EnumSet.of(RoundingMode.UP, RoundingMode.DOWN)));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testSetsImmutableEnumSet3() {
    return ImmutableSet.of(
        ImmutableSet.of(RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING),
        ImmutableSet.copyOf(EnumSet.of(RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING)));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testSetsImmutableEnumSet4() {
    return ImmutableSet.of(
        ImmutableSet.of(
            RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING, RoundingMode.FLOOR),
        ImmutableSet.copyOf(
            EnumSet.of(
                RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING, RoundingMode.FLOOR)));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testSetsImmutableEnumSet5() {
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

  ImmutableSet<RoundingMode> testSetsImmutableEnumSet6() {
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
