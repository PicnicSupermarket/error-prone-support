package tech.picnic.errorprone.refasterrules;

import com.google.common.collect.ImmutableSet;
import java.security.SecureRandom;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import tech.picnic.errorprone.refaster.test.RefasterRuleCollectionTestCase;

final class RandomGeneratorRulesTest implements RefasterRuleCollectionTestCase {
  ImmutableSet<Double> testRandomGeneratorNextDouble() {
    return ImmutableSet.of(
        new Random().nextDouble(1),
        new SplittableRandom().nextDouble(2L),
        new SecureRandom().nextDouble(3.0));
  }

  ImmutableSet<Integer> testRandomGeneratorNextInt() {
    return ImmutableSet.of(new Random().nextInt(1), new SplittableRandom().nextInt(2));
  }

  ImmutableSet<Long> testRandomGeneratorNextLong() {
    return ImmutableSet.of(
        new Random().nextLong(1L),
        new SplittableRandom().nextLong(2L),
        new SecureRandom().nextLong(3L),
        ThreadLocalRandom.current().nextLong(4L));
  }
}
