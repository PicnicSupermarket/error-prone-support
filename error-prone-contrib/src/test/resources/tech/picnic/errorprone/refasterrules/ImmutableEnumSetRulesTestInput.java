package tech.picnic.errorprone.refasterrules;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.stream.Stream;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class ImmutableEnumSetRulesTest implements RefasterRuleCollectionTestCase {
  @Override
  public ImmutableSet<Object> elidedTypesAndStaticImports() {
    return ImmutableSet.of(EnumSet.class, toImmutableSet());
  }

  ImmutableSet<ImmutableSet<RoundingMode>> testSetsImmutableEnumSetIterable() {
    return ImmutableSet.of(
        ImmutableSet.copyOf(Iterables.cycle(RoundingMode.UP)),
        ImmutableSet.copyOf(EnumSet.allOf(RoundingMode.class)));
  }

  ImmutableSet<RoundingMode> testSetsImmutableEnumSetArraysAsList() {
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

  ImmutableSet<RoundingMode> testSetsImmutableEnumSetVarArgs() {
    return ImmutableSet.copyOf(
        EnumSet.of(
            RoundingMode.UP,
            RoundingMode.DOWN,
            RoundingMode.CEILING,
            RoundingMode.FLOOR,
            RoundingMode.UNNECESSARY,
            RoundingMode.HALF_EVEN));
  }

  ImmutableSet<BoundType> testStreamToImmutableEnumSet() {
    return Stream.of(BoundType.OPEN).collect(toImmutableSet());
  }
}
