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

  ImmutableSet<RoundingMode> testSetsImmutableEnumSetIterable() {
    return Sets.immutableEnumSet(EnumSet.range(RoundingMode.UP, RoundingMode.UNNECESSARY));
  }

  ImmutableSet<RoundingMode> testSetsImmutableEnumSetIterableArray() {
    return Sets.immutableEnumSet(Arrays.asList(RoundingMode.values()));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testSetsImmutableEnumSet1() {
    return ImmutableSet.of(
        Sets.immutableEnumSet(RoundingMode.UP), Sets.immutableEnumSet(RoundingMode.UP));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testSetsImmutableEnumSet2() {
    return ImmutableSet.of(
        Sets.immutableEnumSet(RoundingMode.UP, RoundingMode.DOWN),
        Sets.immutableEnumSet(RoundingMode.UP, RoundingMode.DOWN));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testSetsImmutableEnumSet3() {
    return ImmutableSet.of(
        Sets.immutableEnumSet(RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING),
        Sets.immutableEnumSet(RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testSetsImmutableEnumSet4() {
    return ImmutableSet.of(
        Sets.immutableEnumSet(
            RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING, RoundingMode.FLOOR),
        Sets.immutableEnumSet(
            RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING, RoundingMode.FLOOR));
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testSetsImmutableEnumSet5() {
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

  ImmutableSet<RoundingMode> testSetsImmutableEnumSet6() {
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
