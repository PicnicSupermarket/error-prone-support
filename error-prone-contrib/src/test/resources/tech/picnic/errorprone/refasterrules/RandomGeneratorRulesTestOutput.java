package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.util.Random;
import java.util.SplittableRandom;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class RandomGeneratorRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Integer> testRandomGeneratorNextInt() {
    return ImmutableSet.of(new Random().nextInt(1), new SplittableRandom().nextInt(2));
  }

  ImmutableSet<Long> testRandomGeneratorNextLong() {
    return ImmutableSet.of(new Random().nextLong(1), new SplittableRandom().nextLong(2));
  }
}
