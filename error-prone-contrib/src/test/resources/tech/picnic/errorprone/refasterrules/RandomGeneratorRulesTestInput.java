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
        new Random().nextDouble() * 1,
        2L * new SplittableRandom().nextDouble(),
        new SecureRandom().nextDouble() * 3.0);
  }

  double testRandomGeneratorNextDoubleWithOrigin() {
    return 1.0 + new Random().nextDouble(2.0);
  }

  ImmutableSet<Integer> testRandomGeneratorNextInt() {
    return ImmutableSet.of(
        (int) new Random().nextDouble(1), (int) Math.round(new SplittableRandom().nextDouble(2)));
  }

  int testRandomGeneratorNextIntWithOrigin() {
    return 1 + new Random().nextInt(2);
  }

  ImmutableSet<Long> testRandomGeneratorNextLong() {
    return ImmutableSet.of(
        (long) new Random().nextDouble((double) 1L),
        Math.round(new SplittableRandom().nextDouble((double) 2L)),
        (long) new SecureRandom().nextDouble(3L),
        Math.round(ThreadLocalRandom.current().nextDouble(4L)));
  }

  long testRandomGeneratorNextLongWithOrigin() {
    return 1L + new Random().nextLong(2L);
  }
}
