package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.util.Random;
import java.util.SplittableRandom;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class RandomGeneratorRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Integer> testRandomGeneratorNextInt() {
    return ImmutableSet.of(
        (int) (new Random().nextDouble() * 1),
        (int) Math.round(new SplittableRandom().nextDouble() * 2));
  }

  ImmutableSet<Long> testRandomGeneratorNextLong() {
    return ImmutableSet.of(
        (long) (new Random().nextDouble() * 1),
        Math.round(new SplittableRandom().nextDouble() * 2));
  }
}
